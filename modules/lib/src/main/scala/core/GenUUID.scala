package lib.core

import monocle.Iso

import cats.effect.kernel.Sync
import cats.syntax.functor.*

import java.util.UUID

trait GenUUID[F[_]]:
  def make[A: IsUUID]: F[A]

object GenUUID:
  def apply[F[_]: GenUUID]: GenUUID[F] = summon

  given [F[_]: Sync]: GenUUID[F] with
    def make[A: IsUUID]: F[A] =
      Sync[F].delay(UUID.randomUUID()).map(IsUUID[A].iso.get)
 
trait IsUUID[A]:
  def iso: Iso[UUID, A]

object IsUUID:
  def apply[A: IsUUID]: IsUUID[A] = summon

  given IsUUID[UUID] with
    def iso: Iso[UUID, UUID] =
      Iso[UUID, UUID](identity)(identity)

