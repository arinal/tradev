package lib.core

import monocle.Iso
import io.circe.Encoder
import io.circe.Decoder

import cats.Show
import cats.kernel.Eq
import cats.kernel.Order

import java.util.UUID
import java.time.Instant

abstract class Newtype[A](using
    eqv: Eq[A],
    ord: Order[A],
    shw: Show[A],
    enc: Encoder[A],
    dec: Decoder[A]
):
  opaque type Type = A

  inline def apply(a: A): Type = a

  protected inline final def derive[F[_]](using ev: F[A]): F[Type] = ev

  extension (t: Type) inline def value: A = t

  given Wrapper[A, Type] with
    def iso: Iso[A, Type] =
      Iso[A, Type](apply(_))(_.value)

  given Eq[Type]       = eqv
  given Order[Type]    = ord
  given Show[Type]     = shw
  given Encoder[Type]  = enc
  given Decoder[Type]  = dec
  given Ordering[Type] = ord.toOrdering

trait Wrapper[A, B]:
  def iso: Iso[A, B]

object Wrapper:
  def apply[A, B](using ev: Wrapper[A, B]): Wrapper[A, B] = ev

abstract class IdNewtype extends Newtype[UUID]:
  given IsUUID[Type] = derive[IsUUID]

abstract class NumNewtype[A: Eq: Order: Show: Encoder: Decoder](using
    num: Numeric[A]
) extends Newtype[A]:

  extension (n: Type)
    inline def -[T](using inv: T =:= Type)(y: T): Type = apply(num.minus(n.value, inv.apply(y).value))
    inline def +[T](using inv: T =:= Type)(y: T): Type = apply(num.plus(n.value, inv.apply(y).value))

object OrphanInstances:
  given Eq[Instant]    = Eq.by(_.getEpochSecond)
  given Order[Instant] = Order.by(_.getEpochSecond)
  given Show[Instant]  = Show.show[Instant](_.toString)

export OrphanInstances.given

type Timestamp = Timestamp.Type
object Timestamp extends Newtype[Instant]

type Cid = Cid.Type
object Cid extends IdNewtype
