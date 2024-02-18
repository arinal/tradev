package org.lamedh.voltrad.core

import lib.core.*
import lib.core.eda.*
import lib.core.eda.Consumer.Message

import cats.effect.kernel.Sync
import cats.syntax.all.*

object Engine:

  def fsm[F[_]: Sync, Id](
      tradeProd: Producer[F, TradeEvent],
      switchProd: Producer[F, SwitchEvent],
      tradeAck: Consumer[F, Id, TradeCommand],
      switchAck: Consumer[F, Id, SwitchCommand]
  ): Fsm[F, TradeState, Message[Id, TradeCommand] | Message[Id, SwitchCommand], Unit] =

    def sendEvent(id: Id, st: TradeState, cmd: TradeCommand | SwitchCommand): F[(TradeState, Unit)] =
      val (nst, f) = TradeFsm.fsm.run(st, cmd)
      (GenUUID[F].make[EventId], Time[F].now)
        .mapN(f)
        .flatMap {
          case ev: TradeEvent  => tradeProd.send(ev).flatTap(_ => tradeAck.ack(id))
          case ev: SwitchEvent => switchProd.send(ev).flatTap(_ => switchAck.ack(id))
        }.tupleLeft(nst)

    Fsm { case (st, msg) => sendEvent(msg.id, st, msg.payload) }
