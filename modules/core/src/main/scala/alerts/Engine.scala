package org.lamedh.voltrad.core
package alerts

import lib.core.Fsm
import lib.core.eda.Consumer.Message
import lib.core.eda.Consumer
import cats.syntax.all.*
import cats.effect.kernel.Sync

object Engine:

  type In[Id] = Message[Id, TradeEvent | SwitchEvent | PriceUpdate]

  def fsm[F[_]: Sync, Id](
      switchAck: SwitchAcker[F, Id],
      tradeAck: TradeAcker[F, Id]
  ): Fsm[F, TradeState, In[Id], Unit] =
    Fsm {
      case (st, Message(id, _, _: SwitchEvent.Ignored)) =>
        switchAck.ack(id).tupleLeft(st)
      case (st, Message(id, _, _: TradeEvent.CommandRejected)) =>
        tradeAck.ack(id).tupleLeft(st)
      case _ => ???
    }
