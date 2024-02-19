package lib.infra.eda.pulsar

import lib.core.eda.Producer

import dev.profunktor.pulsar.Topic
import dev.profunktor.pulsar.Config.PulsarURL
import dev.profunktor.pulsar.Config as PulsarConfig
import dev.profunktor.pulsar.{ Producer as PulsarProducer, * }
import io.circe.Encoder
import io.circe.syntax.*

import cats.Applicative
import cats.Parallel
import cats.effect.kernel.Async
import cats.effect.kernel.Resource

import java.nio.charset.StandardCharsets.UTF_8

object Producer:

  case class Config(
      url: PulsarURL,
      topic: Topic.Name
  )

  def make[F[_]: Async: Parallel, A: Encoder](
      client: Pulsar.T,
      url: PulsarURL,
      topic: Topic.Name
  ): Resource[F, Producer[F, A]] =
    val encoder: A => Array[Byte] = a => a.asJson.noSpaces.getBytes(UTF_8)

    PulsarProducer.make[F, A](client, pulsarTopic(url, topic), encoder).map { p =>
      new:
        def send(a: A): F[Unit]                                  = p.send_(a)
        def send(a: A, properties: Map[String, String]): F[Unit] = p.send_(a, properties)
    }

  def make[F[_]: Async: Parallel, A: Encoder](
      client: Pulsar.T,
      config: Config
  ): Resource[F, Producer[F, A]] =
    make(client, config.url, config.topic)
