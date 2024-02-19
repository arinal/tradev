package lib.core

import cats.effect.kernel.Sync
import cats.syntax.functor.*

import java.time.Instant

trait Time[F[_]]:
  def now: F[Timestamp]

object Time:
  def apply[F[_]: Time]: Time[F] = summon

  given [F[_]: Sync]: Time[F] with
    def now: F[Timestamp] = Sync[F].delay(Instant.now()).map(t => Timestamp(t))
