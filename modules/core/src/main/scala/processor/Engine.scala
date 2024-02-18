package org.lamedh.voltrad.core

import lib.core.*
import lib.core.eda.*
import lib.core.eda.Consumer.Message

import cats.effect.kernel.Sync
import cats.syntax.all.*

object Engine:

  type Msg[Id] = Message[Id, TradeCommand] | Message[Id, SwitchCommand]

  def fsm[F[_]: Sync, Id](
      tradeProd: Producer[F, TradeEvent],
      switchProd: Producer[F, SwitchEvent],
      tradeAck: TradeAcker[F, Id],
      switchAck: SwitchAcker[F, Id]
  ): Fsm[F, TradeState, Msg[Id], Unit] =

    def sendEvent(id: Id, st: TradeState, cmd: TradeCommand | SwitchCommand): F[(TradeState, Unit)] =
      val (nst, f) = TradeFsm.fsm.run(st, cmd)
      (GenUUID[F].make[EventId], Time[F].now)
        .mapN(f)
        .flatMap {
          case ev: TradeEvent  => tradeProd.send(ev).flatTap(_ => tradeAck.ack(id))
          case ev: SwitchEvent => switchProd.send(ev).flatTap(_ => switchAck.ack(id))
        }.tupleLeft(nst)
        .handleErrorWith(e => Logger[F].error(s"Error processing command $cmd").tupleLeft(st))

    Fsm { case (st, msg) => sendEvent(msg.id, st, msg.payload) }
