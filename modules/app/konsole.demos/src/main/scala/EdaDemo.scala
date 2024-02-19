package demos

import lib.core.eda.Consumer
import lib.core.eda.Producer

import fs2.Stream

import cats.effect.*
import cats.effect.std.Queue
import cats.syntax.all.*

import scala.concurrent.duration.*

object EdaDemo extends IOApp.Simple:

  def run =
    Queue.bounded[IO, Option[String]](500).flatMap { q =>
      val consumer = Consumer.local(q)
      val producer = Producer.local(q)

      val p1 = consumer.receive.evalTap(s => IO.println(s">>> got: $s"))

      val p2 = Stream.resource(producer).flatMap { p =>
        Stream.sleep[IO](100.millis).as("test").repeatN(3).evalMap(p.send)
      }

      p1.concurrently(p2).compile.drain
    }
