package org.lamedh.voltrad.core

import lib.core.NumNewtype

import monocle.Focus
import io.circe.Codec

import cats.Show
import cats.derived.*
import cats.kernel.Eq
import cats.syntax.all.*

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
