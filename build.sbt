import Dependencies._
import sbtwelcome._

ThisBuild / scalaVersion     := "3.3.1"
ThisBuild / version          := "0.1.0"
ThisBuild / organization     := "dev.lamedh"
ThisBuild / organizationName := "lamedh"

Compile / run / fork := true

logo := mainLogo(scalaVersion.value, "root")

def mainLogo(scalaVersion: String, project: String) =
  // format: off
  s"""
   |${scala.Console.YELLOW}   ██                         ██
   |${scala.Console.YELLOW}  ░██                        ░██
   |${scala.Console.YELLOW} ██████ ██████  ██████       ░██  █████  ██    ██
   |${scala.Console.RED   }░░░██░ ░░██░░█ ░░░░░░██   ██████ ██░░░██░██   ░██
   |${scala.Console.RED   }  ░██   ░██ ░   ███████  ██░░░██░███████░░██ ░██
   |${scala.Console.RED   }  ░██   ░██    ██░░░░██ ░██  ░██░██░░░░  ░░████
   |${scala.Console.CYAN  }  ░░██ ░███   ░░████████░░██████░░██████  ░░██
   |${scala.Console.CYAN  }   ░░  ░░░     ░░░░░░░░  ░░░░░░  ░░░░░░    ░░
   |Powered by ${scala.Console.YELLOW}Scala ${scalaVersion}${scala.Console.RESET}
   |Project: ${scala.Console.CYAN}$project ${scala.Console.RESET}
  """.stripMargin
  // format: on

val commonSettings = List(
  logo := "",
  libraryDependencies ++= List(
    cats,
    catsEffect,
    kittens,
    monocle,
    neutron,
    fs2,
    refined,
    iron,
    ironCats,
    ironCirce,
    circe,
    circeParser,
    odin
  )
)

lazy val root = (project in file("."))
  .settings(name := "tradev")
  .aggregate(lib, core, processor)

// add libraryDependencies
lazy val lib = (project in file("modules/lib"))
  .settings(commonSettings: _*)

lazy val core = (project in file("modules/core"))
  .settings(commonSettings: _*)
  .dependsOn(lib)

lazy val processor = (project in file("modules/app/eda.processor"))
  .settings(commonSettings: _*)
  .settings(libraryDependencies ++= List(neutron))
  .dependsOn(lib, core)

lazy val demos = (project in file("modules/app/konsole.demos"))
  .settings(commonSettings: _*)
  .settings(libraryDependencies ++= List(natchezCore, natchezHc, natchez4s, http4sDsl, http4sSrv))
  .dependsOn(lib, core)
