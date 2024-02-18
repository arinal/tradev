package org.lamedh.voltrad.app.eda.processor

import org.lamedh.voltrad.core.*

import lib.core.Logger
import lib.core.eda.Producer
import lib.core.eda.Consumer.Message
import lib.infra.eda.pulsar

import dev.profunktor.pulsar.Config.PulsarURL
import io.circe.Codec
import io.circe.syntax.*

import fs2.Stream

import cats.effect.*
import cats.syntax.all.*
import cats.effect.kernel.Resource

import scala.concurrent.duration.*
import org.apache.pulsar.client.api.MessageId

object Main extends IOApp.Simple:

  import lib.core.ext.*

  def run: IO[Unit] =
    Stream
      .resource(resources)
      .flatMap { (trCons, swCons, fsm) =>
        trCons.receiveM.either(swCons.receiveM)
          .union
          .evalMapAccumulate(TradeState.empty)(fsm.run)
          .evalTap { case (state, _) => Logger[IO].info(s"State: $state") }
      }
      .compile
      .drain

  def resources =
    val conf = Config.default
    for
      _      <- Resource.eval(Logger[IO].info("Initializing the trade processor..."))
      client <- pulsar.client[IO](conf.tradeProducer.url)
      trProd <- pulsar.Producer.make[IO, TradeEvent](client, conf.tradeProducer)
      swProd <- pulsar.Producer.make[IO, SwitchEvent](client, conf.switchProducer)
      trCons <- pulsar.Consumer.make[IO, TradeCommand](client, conf.tradeConsumer)
      swCons <- pulsar.Consumer.make[IO, SwitchCommand](client, conf.switchConsumer)
      fsm = Engine.fsm[IO, MessageId](trProd, swProd, trCons, swCons)
    yield (trCons, swCons, fsm)
