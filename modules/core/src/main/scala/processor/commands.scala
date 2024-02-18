package org.lamedh.voltrad.core

import lib.core.*

import io.circe.Codec
import cats.Show
import cats.derived.*
import cats.kernel.Eq
import cats.kernel.Order

import java.time.Instant
import java.util.UUID

type CommandId = CommandId.Type
object CommandId extends IdNewtype

enum TradeAction derives Eq, Show:
  case Ask, Bid

// val emptyUUID = UUID.fromString("00000000-0000-0000-0000-000000000000")

enum TradeCommand derives Eq, Show, Codec.AsObject:
  val id: CommandId
  val cid: Cid
  val symbol: Symbol
  val timestamp: Timestamp

  case Create(
      id: CommandId,
      cid: Cid,
      symbol: Symbol,
      action: TradeAction,
      price: Price,
      quantity: Quantity,
      timestamp: Timestamp
  )

  case Update(
      id: CommandId,
      cid: Cid,
      symbol: Symbol,
      action: TradeAction,
      price: Price,
      quantity: Quantity,
      timestamp: Timestamp
  )

  // case Nop(
  //     id: CommandId = CommandId(emptyUUID),
  //     cid: Cid = Cid(emptyUUID),
  //     symbol: Symbol = Symbol.XEMPTY,
  //     timestamp: Timestamp = Timestamp(Instant.EPOCH)
  // )

enum SwitchCommand derives Eq, Show, Codec.AsObject:
  def id: CommandId
  def cid: Cid
  def createdAt: Timestamp

  case Start(
      id: CommandId,
      cid: Cid,
      createdAt: Timestamp
  )

  case Stop(
      id: CommandId,
      cid: Cid,
      createdAt: Timestamp
  )
