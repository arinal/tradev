package org.lamedh.voltrad.core

import monocle.Focus
import io.circe.Codec

import cats.derived.*
import cats.kernel.Eq
import cats.Show

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
