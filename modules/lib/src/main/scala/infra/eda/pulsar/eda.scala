package lib.infra.eda.pulsar

import dev.profunktor.pulsar.Config.PulsarURL
import dev.profunktor.pulsar.Topic
import dev.profunktor.pulsar.Config
import cats.effect.kernel.Async
import dev.profunktor.pulsar.Pulsar

def client[F[_]: Async](url: PulsarURL) = Pulsar.make(url, Pulsar.Settings())

private def pulsarTopic(url: PulsarURL, topic: Topic.Name) =
  val config =
    Config.Builder
      .withTenant("public")
      .withNameSpace("default")
      .withURL(url)
      .build
  Topic.Builder
    .withName(topic)
    .withType(Topic.Type.Persistent)
    .withConfig(config)
    .build
