package lib.core
package eda

import fs2.Stream
import cats.effect.std.Queue
import cats.effect.kernel.Sync
import cats.syntax.all.*

trait Consumer[F[_], Id, A]:
  def receive: Stream[F, A]
  def receiveM: Stream[F, Consumer.Message[Id, A]]
  def ack(id: Id): F[Unit]
  def ack(ids: Set[Id]): F[Unit]
  def nack(id: Id): F[Unit]

object Consumer:

  final case class Message[Id, A](id: Id, props: Map[String, String], payload: A)

  def local[F[_]: Sync, A](queue: Queue[F, Option[A]]): Consumer[F, String, A] =
    import GenUUID.*
    new Consumer[F, String, A]:

      def receive: Stream[F, A] = Stream.fromQueueNoneTerminated(queue)

      def receiveM: Stream[F, Message[String, A]] =
        receive.evalMap(a => GenUUID[F].make.map(uuid => Message(uuid.toString, Map.empty, a)))

      def ack(id: String): F[Unit]       = Sync[F].unit
      def ack(ids: Set[String]): F[Unit] = Sync[F].unit
      def nack(id: String): F[Unit]      = Sync[F].unit
