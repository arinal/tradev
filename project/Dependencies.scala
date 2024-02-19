import sbt._

object Dependencies {

  object V {
    val cats       = "2.10.0"
    val catsEffect = "3.5.3"
    val circe      = "0.14.6"
    val fs2        = "3.9.2"
    val kittens    = "3.0.0"
    val monocle    = "3.2.0"
    val neutron    = "0.8.0"
    val refined    = "0.11.0"
    val iron       = "2.4.0"
    val odin       = "0.13.0"
    val http4s     = "0.23.23"
    val natchez    = "0.3.5"
    val natchez4s  = "0.5.0"
  }

  val cats        = "org.typelevel"        %% "cats-core"            % V.cats
  val catsEffect  = "org.typelevel"        %% "cats-effect"          % V.catsEffect
  val kittens     = "org.typelevel"        %% "kittens"              % V.kittens
  val monocle     = "dev.optics"           %% "monocle-core"         % V.monocle
  val neutron     = "dev.profunktor"       %% "neutron-core"         % V.neutron
  val fs2         = "co.fs2"               %% "fs2-core"             % V.fs2
  val refined     = "eu.timepit"           %% "refined"              % V.refined
  val iron        = "io.github.iltotore"   %% "iron"                 % V.iron
  val ironCats    = "io.github.iltotore"   %% "iron-cats"            % V.iron
  val ironCirce   = "io.github.iltotore"   %% "iron-circe"           % V.iron
  val circe       = "io.circe"             %% s"circe-core"          % V.circe
  val circeParser = "io.circe"             %% s"circe-parser"        % V.circe
  val odin        = "com.github.valskalla" %% "odin-core"            % V.odin
  val natchezCore = "org.tpolecat"         %% "natchez-core"         % V.natchez
  val natchezHc   = "org.tpolecat"         %% "natchez-honeycomb"    % V.natchez
  val natchez4s   = "org.tpolecat"         %% "natchez-http4s"       % V.natchez4s
  val http4sDsl   = "org.http4s"           %% s"http4s-dsl"          % V.http4s
  val http4sSrv   = "org.http4s"           %% s"http4s-ember-server" % V.http4s
}
