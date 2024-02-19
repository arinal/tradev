package org.lamedh.voltrad.core

import lib.core.*

import io.circe.Codec

import cats.Show
import cats.derived.*
import cats.kernel.Eq

type EventId = EventId.Type
object EventId extends IdNewtype

enum TradeEvent derives Eq, Show, Codec.AsObject:
  val id: EventId
  val cid: Cid
  val command: TradeCommand
  val createdAt: Timestamp

  case CommandExecuted(
      id: EventId,
      cid: Cid,
      command: TradeCommand,
      createdAt: Timestamp
  )

  case CommandRejected(
      id: EventId,
      cid: Cid,
      command: TradeCommand,
      reason: String,
      createdAt: Timestamp
  )

enum SwitchEvent derives Eq, Show, Codec.AsObject:
  def id: EventId
  def cid: Cid
  def createdAt: Timestamp

  case Started(
      id: EventId,
      cid: Cid,
      createdAt: Timestamp
  )

  case Stopped(
      id: EventId,
      cid: Cid,
      createdAt: Timestamp
  )

  case Ignored(
      id: EventId,
      cid: Cid,
      createdAt: Timestamp
  )
