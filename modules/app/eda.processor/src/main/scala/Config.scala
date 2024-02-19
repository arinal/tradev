package org.lamedh.voltrad.app.eda.processor

import lib.infra.eda.pulsar.Consumer.{ Config as ConsumerConfig }
import lib.infra.eda.pulsar.Producer.{ Config as ProducerConfig }

import dev.profunktor.pulsar.Config.PulsarURL
import dev.profunktor.pulsar.Topic
import dev.profunktor.pulsar.Subscription

case class Config(
    tradeConsumer: ConsumerConfig,
    switchConsumer: ConsumerConfig,
    tradeProducer: ProducerConfig,
    switchProducer: ProducerConfig
)

object Config:

  val pulsarUrl = PulsarURL("pulsar://192.168.59.100:32198")

  val default = Config(
    tradeConsumer = ConsumerConfig(
      url          = pulsarUrl,
      topic        = Topic.Name("trade-commands"),
      subscription = Subscription.Name("trading"),
      subType      = Subscription.Type.Shared
    ),
    switchConsumer = ConsumerConfig(
      url          = pulsarUrl,
      topic        = Topic.Name("switch-commands"),
      subscription = Subscription.Name("trading"),
      subType      = Subscription.Type.Exclusive
    ),
    tradeProducer = ProducerConfig(
      url   = pulsarUrl,
      topic = Topic.Name("trade-event")
    ),
    switchProducer = ProducerConfig(
      url   = pulsarUrl,
      topic = Topic.Name("switch-event")
    )
  )
