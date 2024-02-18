package org.lamedh.voltrad.core

import TradeCommand.*
import SwitchCommand.*
import TradeEvent.*
import SwitchEvent.*
import Status.*
import lib.core.Fsm
import lib.core.Timestamp

object TradeFsm:

  val fsm = Fsm.id[TradeState, TradeCommand | SwitchCommand, (EventId, Timestamp) => TradeEvent | SwitchEvent] {
    // Trading commands
    case (st @ TradeState(On, _), cmd @ Create(_, cid, symbol, action, price, quantity, _)) =>
      val nst = st.modify(symbol)(action, price, quantity)
      nst -> ((id, ts) => CommandExecuted(id, cid, cmd, ts))
    case (st @ TradeState(On, _), cmd @ Update(_, cid, symbol, action, price, quantity, _)) =>
      val nst = st.modify(symbol)(action, price, quantity)
      nst -> ((id, ts) => CommandExecuted(id, cid, cmd, ts))
    case (st @ TradeState(Off, _), cmd: TradeCommand) =>
      st -> ((id, ts) => CommandRejected(id, cmd.cid, cmd, "Trade is off", ts))

    // Switch commands
    case (st @ TradeState(Off, _), cmd @ Start(_, cid, _)) =>
      val nst = st.copy(status = On)
      nst -> ((id, ts) => Started(id, cid, ts))
    case (st @ TradeState(On, _), Stop(_, cid, _)) =>
      val nst = st.copy(status = Off)
      nst -> ((id, ts) => Stopped(id, cid, ts))
    case (st @ TradeState(On, _), Start(_, cid, _)) =>
      st -> ((id, ts) => Ignored(id, cid, ts))
    case (st @ TradeState(Off, _), Stop(_, cid, _)) =>
      st -> ((id, ts) => Ignored(id, cid, ts))
  }
