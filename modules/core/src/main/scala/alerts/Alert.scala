package org.lamedh.voltrad.core
package alerts

import cats.Show
import cats.derived.*
import io.circe.Codec
import lib.core.IdNewtype
import lib.core.Cid
import lib.core.Timestamp
import cats.kernel.Eq

type AlertId = AlertId.Type
object AlertId extends IdNewtype

enum AlertType derives Eq, Show:
  case StrongBuy, StrongSell, Neutral, Buy, Sell
  
enum Alert derives Codec.AsObject, Show:
  def id: AlertId
  def cid: Cid
  def createdAt: Timestamp

  case TradeAlert(
      id: AlertId,
      cid: Cid,
      alertType: AlertType,
      symbol: Symbol,
      askPrice: AskPrice,
      bidPrice: BidPrice,
      high: Price,
      low: Price,
      createdAt: Timestamp
  )

  case TradeUpdate(
      id: AlertId,
      cid: Cid,
      status: TradeStatus,
      createdAt: Timestamp
  )

