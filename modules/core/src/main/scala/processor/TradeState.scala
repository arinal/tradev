package org.lamedh.voltrad.core

import lib.core.NumNewtype
import cats.Show
import cats.derived.*
import cats.kernel.Eq
import cats.syntax.all.*
import monocle.Focus
import monocle.function.At
import monocle.function.Index

type Quantity = Quantity.Type
object Quantity extends NumNewtype[Int]

case class TradeState(
    status: TradeStatus,
    prices: Map[Symbol, Prices]
) derives Eq, Show:

  def modify(symbol: Symbol)(action: TradeAction, price: Price, quantity: Quantity): TradeState =
    val h = Prices.L.high.modify(p => if price > p then price else p)
    val l = Prices.L.low.modify(p => if price < p then price else p)
    action match
      case TradeAction.Ask =>
        val f = Prices.L.asks.modify(p => p.updated(price, quantity))
        val g = f.andThen(h).andThen(l)
        TradeState.L.prices.at(symbol).modify(_.orElse(Prices.empty.some).map(g))(this)
      case TradeAction.Bid =>
        val f = Prices.L.bids.modify(p => p.updated(price, quantity))
        val g = f.andThen(h).andThen(l)
        TradeState.L.prices.at(symbol).modify(_.orElse(Prices.empty.some).map(g))(this)

object TradeState:
  val empty = TradeState(TradeStatus.Off, Map.empty)

  object L:
    val status = Focus[TradeState](_.status)
    val prices = Focus[TradeState](_.prices)

final case class Prices(
    asks: Prices.Asks,
    bids: Prices.Bids,
    high: Price,
    low: Price
) derives Eq, Show

object Prices:
  type Asks = Map[AskPrice, Quantity]
  type Bids = Map[BidPrice, Quantity]

  val empty: Prices = Prices(Map.empty, Map.empty, Price(0.0), Price(0.0))

  object L:
    val asks = Focus[Prices](_.asks)
    val bids = Focus[Prices](_.bids)
    val high = Focus[Prices](_.high)
    val low  = Focus[Prices](_.low)
