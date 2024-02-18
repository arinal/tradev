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
  }

  val cats        = "org.typelevel"        %% "cats-core"     % V.cats
  val catsEffect  = "org.typelevel"        %% "cats-effect"   % V.catsEffect
  val kittens     = "org.typelevel"        %% "kittens"       % V.kittens
  val monocle     = "dev.optics"           %% "monocle-core"  % V.monocle
  val neutron     = "dev.profunktor"       %% "neutron-core"  % V.neutron
  val fs2         = "co.fs2"               %% "fs2-core"      % V.fs2
  val refined     = "eu.timepit"           %% "refined"       % V.refined
  val iron        = "io.github.iltotore"   %% "iron"          % V.iron
  val ironCats    = "io.github.iltotore"   %% "iron-cats"     % V.iron
  val ironCirce   = "io.github.iltotore"   %% "iron-circe"    % V.iron
  val circe       = "io.circe"             %% s"circe-core"   % V.circe
  val circeParser = "io.circe"             %% s"circe-parser" % V.circe
  val odin        = "com.github.valskalla" %% "odin-core"     % V.odin
}
