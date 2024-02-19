package org.lamedh.voltrad.core

import lib.core.NumNewtype
import lib.core.eda.Consumer

import cats.derived.*
import cats.kernel.Eq
import cats.Show

type SwitchAcker[F[_], Id] = Consumer[F, Id, SwitchCommand]
type TradeAcker[F[_], Id]  = Consumer[F, Id, TradeCommand]

enum TradeStatus derives Eq, Show:
  case On, Off

type Price = Price.Type
object Price extends NumNewtype[BigDecimal]

type AskPrice = Price
type BidPrice = Price
