package lib.core

import cats.effect.kernel.Sync
import io.odin.{ Logger as OdinLogger, consoleLogger }
import io.odin.formatter.Formatter

trait Logger[F[_]]:
  def info(str: => String): F[Unit]
  def error(str: => String): F[Unit]
  def debug(str: => String): F[Unit]
  def warn(str: => String): F[Unit]
  
object Logger:

  def apply[F[_]: Logger]: Logger[F] = summon
  
  def from[F[_]](log: OdinLogger[F]): Logger[F] = new:
    def info(str: => String): F[Unit]  = log.info(str)
    def error(str: => String): F[Unit] = log.error(str)
    def debug(str: => String): F[Unit] = log.debug(str)
    def warn(str: => String): F[Unit]  = log.warn(str)
    
  given [F[_]: Sync]: Logger[F] =
    from[F] {
      consoleLogger[F](Formatter.colorful)
    }
