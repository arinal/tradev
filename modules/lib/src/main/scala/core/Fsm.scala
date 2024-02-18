package lib.core

import cats.Id

class Fsm[F[_], S, I, O](val run: (S, I) => F[(S, O)])

object Fsm:
  def id[S, I, O](run: (S, I) => Id[(S, O)]): Fsm[Id, S, I, O] = new Fsm(run)
