package org.lamedh.voltrad.core

import lib.core.Newtype
import cats.*
import io.circe.*
import io.github.iltotore.iron.*
import io.github.iltotore.iron.cats.given
import io.github.iltotore.iron.circe.given
import io.github.iltotore.iron.constraint.any.DescribedAs
import io.github.iltotore.iron.constraint.string.Match

type SymbolR = Match["^[a-zA-Z0-9]{6}$"] DescribedAs "A Symbol should be an alphanumeric of 6 digits"

type Symbol = Symbol.Type
object Symbol extends Newtype[String :| SymbolR]:
  val CHFEUR = Symbol("CHFEUR")
  val EURPLN = Symbol("EURPLN")
  val EURUSD = Symbol("EURUSD")
  val GBPUSD = Symbol("GBPUSD")
  val AUDCAD = Symbol("AUDCAD")
  val USDCAD = Symbol("USDCAD")
  val CHFGBP = Symbol("CHFGBP")
  val XEMPTY = Symbol("XXXXXX")

  given Monoid[Symbol] with
    def empty: Symbol = XEMPTY
    def combine(x: Symbol, y: Symbol): Symbol =
      (x, y) match
        case (XEMPTY, z) => z
        case (z, XEMPTY) => z
        case (_, z)      => z
