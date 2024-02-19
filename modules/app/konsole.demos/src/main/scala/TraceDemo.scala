package demo

import lib.core.eda.Producer
import lib.core.eda.Consumer
import lib.core.GenUUID.*
import lib.core.GenUUID

import org.http4s.dsl.Http4sDsl
import org.http4s.HttpRoutes
import natchez.Trace
import org.http4s.ember.server.EmberServerBuilder
import natchez.honeycomb.Honeycomb
import io.honeycomb.libhoney.DefaultDebugResponseObserver
import io.honeycomb.libhoney.ResponseObserver
import natchez.http4s.syntax.entrypoint.*
import natchez.EntryPoint
import natchez.Kernel
import org.typelevel.ci.*
import io.circe.Codec
import io.circe.syntax.*

import fs2.Stream

import cats.effect.IO
import cats.effect.IOApp
import cats.syntax.all.*
import cats.derived.*
import cats.Applicative
import cats.Monad
import cats.effect.kernel.Sync
import cats.effect.kernel.{ MonadCancelThrow, Resource }
import cats.Show

import java.util.UUID

//  ____  
// |  _ \  ___  _ __ ___   __ _(_)_ __
// | | | |/ _ \| '_ ` _ \ / _` | | '_ \
// | |_| | (_) | | | | | | (_| | | | | |
// |____/ \___/|_| |_| |_|\__,_|_|_| |_|

// The event that will be sent to message broker upon user creation.
// The event DOES NOT contain any tracing context specific properties.
// Event is part of domain model, while tracing context is infrasucture / application concerns.
// Being the lowest layer, domain model should never access the upper layer.
case class UserCreated(id: UUID, person: Person) derives Codec.AsObject

// You might notice that the domain has circe specific logic, which
// is not a domain concerns. Since putting circe directly in domain
// class using derives omits a lot of boilerplate codes, it's a good trade off.
// Another example not to be too strict with the principles.
case class Person(id: Int, name: String) derives Show, Codec.AsObject

//  ____
// / ___|  ___ _ ____   _____ _ __
// \___ \ / _ \ '__\ \ / / _ \ '__|
//  ___) |  __/ |   \ V /  __/ |
// |____/ \___|_|    \_/ \___|_|

final class UserServer[F[_]: Sync] extends Http4sDsl[F]:

  /**
   * Create htt4s routes with 2 endpoints, our span creation begins on every endpoint.
   * To understand the span structures better, see the link below.
   * @see https://typelevel.org/natchez/overview.html
   * @param ep: EntryPoint[F] - the entry point to create new span
   * @param producer: Producer - generic trait to send the events to message broker
   */
  def routes(ep: EntryPoint[F], producer: Producer[F, UserCreated]) =

    // Extract the http headers (since this is a HTTP server app)
    // to get the current tracing context propagated from upstream (if any).
    // The extracted context will then used to create a new span.
    // Upon failure (no propagated context found), fallback to create a new root span.
    // This function is called on every endpoint.
    def spanRoot(req: org.http4s.Request[F]) =
      val headers  = req.headers.headers.map { h => h.name -> h.value }.toMap
      // kernel is the natchez term for tracing context data used for propagation.
      // Below, the data is extracted from http headers.
      val kern = Kernel(headers) 
      ep.continueOrElseRoot("rootHttp", kern)

    HttpRoutes.of[F] {
      case req @ GET -> Root / "user" / IntVar(n) =>
        spanRoot(req).use { t1 =>
          t1.span("getUser").use { t2 =>
            val user = Person(n, "John Damn")
            t2.put("userId" -> n) *>
              Ok(user.asJson.show)
          }
        }
      case req @ POST -> Root / "user" / IntVar(n) =>
        spanRoot(req).use { t1 =>
          t1.span("createUser").use { t2 =>
            val newUser = Person(n, "John Damn")
            for
              _  <- t2.put("user" -> newUser.show)
              _  <- t2.log("Send userid to producer")
              // Get the current kernel to be propagated to broker.
              // Current kernel can be extracted anywhere in our code, as long as we have the span.
              k  <- t2.kernel
              id <- GenUUID[F].make
              event = UserCreated(id, newUser)
              // Sending with producer needs 2 parameters: the payload and additional headers.
              // Kafka and Pulsar provide an additional headers to be sent along with the payload.
              // This is one of the way to avoid putting the tracing context into the domain model.
              // See the link below for pulsar producer implementation:
              // https://github.com/arinal/tradev/blob/main/modules/lib/src/main/scala/infra/eda/pulsar/Producer.scala#L36
              _  <- producer.send(event, k.headers)
              ok <- Ok(s"$newUser is created")
            yield ok
          }
        }
    }.orNotFound

object ServerMain extends IOApp.Simple:

  def run: IO[Unit] = resources.use(_.useForever)

  val resources =
    for
      // honeycomb is just the name of specific provider for tracing.
      // another provider like DataDog could be used.
      ep   <- honeycombEp[IO]("server-app")
      prod <- producer[IO]
      routes = new UserServer[IO].routes(ep, prod)
      server = EmberServerBuilder.default[IO].withHttpApp(routes).build
    yield server
    
//   ____
//  / ___|___  _ __  ___ _   _ _ __ ___   ___ _ __
// | |   / _ \| '_ \/ __| | | | '_ ` _ \ / _ \ '__|
// | |__| (_) | | | \__ \ |_| | | | | | |  __/ |
//  \____\___/|_| |_|___/\__,_|_| |_| |_|\___|_|

object ConsumerMain extends IOApp.Simple:
  def run: IO[Unit] =
    Stream.resource(resources)
      .flatMap { (ep, consumer) =>
        // `consumer.receiveMsg` is a stream of `Message`.
        // It contains both the payload (the event) and the
        // `Map[String, String]` which in this case, contains the kernel.
        // See the structures of `Message`:
        // https://github.com/arinal/tradev/blob/main/modules/lib/src/main/scala/core/eda/Consumer.scala#L19
        // Also, this is how `.receiveMsg` in pulsar is impelemented:
        // https://github.com/arinal/tradev/blob/main/modules/lib/src/main/scala/infra/eda/pulsar/Consumer.scala#L51-L53
        consumer.receiveMsg.evalMap { msg =>
          val k = msg.props.toKernel
          ep.continueOrElseRoot("rootConsumer", k).use { t =>
            t.span("handleUserCreated").use { t2 =>
              for
                _ <- t2.put("user" -> msg.payload.person.show)
                _ <- t2.log("Process message")
              yield ()
            }
          }
        }
      }
      .compile
      .drain

  val resources =
    for
      ep       <- honeycombEp[IO]("consumer-app")
      consumer <- consumer[IO]
    yield (ep, consumer)

//  _   _ _   _ _
// | | | | |_(_) |___
// | | | | __| | / __|
// | |_| | |_| | \__ \
//  \___/ \__|_|_|___/

def honeycombEp[F[_]: Sync](app: String): Resource[F, EntryPoint[F]] =
  Honeycomb.entryPoint[F](app) { ob => Sync[F].delay(ob.build) }
def dataDogEp[F[_]: Sync]: Resource[F, EntryPoint[F]]                   = ???
def producer[F[_]: Sync]: Resource[F, Producer[F, UserCreated]]         = ???
def consumer[F[_]: Sync]: Resource[F, Consumer[F, String, UserCreated]] = ???

extension (k: Kernel)
  def headers: Map[String, String] = k.toHeaders.map {
    (k, v) => k.show -> v
  }

extension (kv: Map[String, String])
  def toKernel: Kernel = Kernel(kv.map { (k, v) => ci"$k" -> v })
