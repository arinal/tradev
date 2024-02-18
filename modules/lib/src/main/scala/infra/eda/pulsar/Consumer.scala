package lib.infra.eda.pulsar

import lib.core.eda.Consumer
import lib.core.eda.Consumer.Message

import cats.Applicative
import cats.Parallel
import cats.effect.kernel.Async
import cats.effect.kernel.Resource
import cats.syntax.all.*
import dev.profunktor.pulsar.Config.PulsarURL
import dev.profunktor.pulsar.{ Consumer as PulsarConsumer, * }
import fs2.Stream
import io.circe.Decoder
import io.circe.Encoder
import io.circe.parser.decode
import io.circe.syntax.*

import java.nio.charset.StandardCharsets.UTF_8
import org.apache.pulsar.client.api.MessageId

object Consumer:

  case class Config(
      url: PulsarURL,
      topic: Topic.Name,
      subscription: Subscription.Name,
      subType: Subscription.Type
  )

  def make[F[_]: Async, A: Decoder](
      client: Pulsar.T,
      url: PulsarURL,
      topic: Topic.Name,
      subscriptionName: Subscription.Name,
      subType: Subscription.Type
  ): Resource[F, Consumer[F, MessageId, A]] =

    val decoder: Array[Byte] => F[A] = bs => Async[F].fromEither(decode[A](new String(bs, UTF_8)))

    val sub = Subscription.Builder
      .withName(subscriptionName)
      .withType(subType)
      .build

    PulsarConsumer.make[F, A](client, pulsarTopic(url, topic), sub, decoder).map { c =>
      new:
        def receive: Stream[F, A] = c.autoSubscribe
        def receiveM: Stream[F, Message[MessageId, A]] = c.subscribe.map { m =>
          Message(m.id, m.properties, m.payload)
        }

        def ack(id: MessageId): F[Unit]       = c.ack(id)
        def ack(ids: Set[MessageId]): F[Unit] = c.ack(ids)
        def nack(id: MessageId): F[Unit]      = c.nack(id)

    }

  def make[F[_]: Async, A: Decoder](
      client: Pulsar.T,
      config: Config
  ): Resource[F, Consumer[F, MessageId, A]] =
    make(client, config.url, config.topic, config.subscription, config.subType)
