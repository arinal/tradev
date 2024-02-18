package demos

import cats.effect.IOApp
import cats.effect.IO
import cats.effect.kernel.Ref
import cats.effect.std.Supervisor
import cats.effect.kernel.Resource

object CtxDemo extends IOApp.Simple:

  final case class Log(ref: Ref[IO, List[String]]):
    def log(msg: String): IO[Unit] = ref.update(_ :+ msg)
    def get: IO[List[String]]      = ref.get

  final case class Ctx(id: Int, sp: Supervisor[IO], log: Log)

  val mkCtx: Resource[IO, Ctx] =
    for
      id     <- Resource.eval(IO(1))
      sp     <- Supervisor[IO]
      logRef <- Resource.eval(Ref.of[IO, List[String]](List.empty))
    yield Ctx(id, sp, Log(logRef))

  def p1(using ctx: Ctx) =
    for
      _ <- ctx.log.log("Ni hao")
      _ <- ctx.log.log("dunia")
      _ <- ctx.log.get.flatMap(log => IO.println(s"Log: $log"))
      _ <- p2
    yield ()

  def p2(using ctx: Ctx) =
    IO.println("Inside p2") *>
      IO.println(s"Ctx: $ctx")

  def run: IO[Unit] =
    mkCtx.use(ctx => p1(using ctx))
