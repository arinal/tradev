package lib.core.ext

import fs2.Stream

extension [A, B](src: Either[A, B])
  def union: A | B = src.fold(identity, identity)

extension [A, B, C](src: Either[Either[A, B], C])
  def union2: A | B | C = src.fold(_.union, identity)

extension [F[_], A, B](src: Stream[F, Either[A, B]])
  def union: Stream[F, A | B] =
    src.map(_.union)

extension [F[_], A, B, C](src: Stream[F, Either[Either[A, B], C]])
  def union2: Stream[F, A | B | C] =
    src.map(_.union2)
