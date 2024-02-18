package org.lamedh.voltrad.core

import lib.core.NumNewtype
import cats.derived.*
import cats.kernel.Eq
import cats.Show

enum TradeStatus derives Eq, Show:
  case On, Off

type Price = Price.Type
object Price extends NumNewtype[BigDecimal]

type AskPrice = Price
type BidPrice = Price

