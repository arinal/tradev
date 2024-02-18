package lib.core
package eda

import cats.effect.std.Queue
import cats.effect.kernel.Resource
import cats.Applicative

trait Producer[F[_], A]:
  def send(a: A): F[Unit]
  def send(a: A, properties: Map[String, String]): F[Unit]

object Producer:

  def local[F[_]: Applicative, A](queue: Queue[F, Option[A]]): Resource[F, Producer[F, A]] =
    Resource.make(
      Applicative[F].pure(
        new Producer[F, A]:
          def send(a: A): F[Unit]                                  = queue.offer(Some(a))
          def send(a: A, properties: Map[String, String]): F[Unit] = queue.offer(Some(a))
      )
    )(_ => queue.offer(None))
