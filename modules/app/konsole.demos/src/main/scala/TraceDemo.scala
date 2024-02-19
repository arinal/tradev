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

// Domin model
case class Person(id: Int, name: String) derives Show, Codec.AsObject
case class UserCreated(id: UUID, person: Person) derives Codec.AsObject

//  ____
// / ___|  ___ _ ____   _____ _ __
// \___ \ / _ \ '__\ \ / / _ \ '__|
//  ___) |  __/ |   \ V /  __/ |
// |____/ \___|_|    \_/ \___|_|

final class UserServer[F[_]: Sync: MonadCancelThrow] extends Http4sDsl[F]:

  /** 
   * @param ep: EntryPoint[F] - the entry point to create new span
   * @param producer: Producer[F, UserCreated] - used to send messages to a message broker
   */
  def app(ep: EntryPoint[F], producer: Producer[F, UserCreated]) =

    // HIGHTLIGHT: use http headers to extract from upstream
    // try to continue the trace given upstream, fallback to create new root span if failed
    def spanRoot(req: org.http4s.Request[F]) =
      val map  = req.headers.headers.map { h => h.name -> h.value }.toMap
      val kern = Kernel(map) // kernel is natchez term for shared context accross services
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
              k  <- t2.kernel
              id <- GenUUID[F].make
              event = UserCreated(id, newUser)
              // HIGHTLIGHT: send message using additional headers
              // in another word: the event doesn't need to have context specific properties
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
      // honeycomb is just the name of specific provider for endpoint
      // another provider like DataDog can be used
      ep   <- honeycombEp[IO]("server-app")
      prod <- producer[IO]
      routes = new UserServer[IO].app(ep, prod)
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
