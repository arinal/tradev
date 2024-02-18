package demos

import cats.syntax.all.*
import eu.timepit.refined.types.numeric.PosInt
import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.all.*
import io.github.iltotore.iron.constraint.any.DescribedAs
import io.github.iltotore.iron.constraint.numeric.Greater
import io.github.iltotore.iron.constraint.numeric.Less

@main def refinedDemo =
  val person = Person("John", 5)
  println(person)
  
type AgeR = DescribedAs[
  Greater[0] & Less[151],
  "Alien's age must be an integer between 1 and 150"
]

type NameR = DescribedAs[
  Alphanumeric & MinLength[1] & MaxLength[50],
  "Person's name must be an alphanumeric of max length 50"
]

case class Person(name: String :| NameR, age: Int :| AgeR)
