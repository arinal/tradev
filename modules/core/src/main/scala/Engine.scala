package org.lamedh.voltrad.core

import lib.core.*
import lib.core.eda.*
import lib.core.eda.Consumer.Message
import cats.effect.kernel.Sync
import cats.syntax.all.*

object Engine:

  def fsm[F[_]: Sync, Id](
      tradeProd: Producer[F, TradeEvent],
      switchProd: Producer[F, SwitchEvent]
  ): Fsm[F, TradeState, Message[Id, TradeCommand] | Message[Id, SwitchCommand], Unit] =

    def sendEvent(st: TradeState, cmd: TradeCommand | SwitchCommand): F[(TradeState, Unit)] =
      val (nst, f) = TradeFsm.fsm.run(st, cmd)
      (GenUUID[F].make[EventId], Time[F].now).mapN(f).flatMap {
        case ev: TradeEvent  => tradeProd.send(ev)
        case ev: SwitchEvent => switchProd.send(ev)
      }.tupleLeft(nst)

    Fsm { case (st, msg) => sendEvent(st, msg.payload) }
