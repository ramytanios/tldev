package tldev.core

import cats.Monad
import cats.data.State
import cats.syntax.all.*

import java.util.UUID

opaque type Rand[V] = State[Rand.S, V]

private val stateMonadInstance: Monad[State[Rand.S, _]] = Monad[State[Rand.S, _]]

object Rand:

  opaque type S = Long

  extension [V](r: Rand[V])
    def listOfN(n: Int): Rand[List[V]] = r.replicateA(n)
    def runA(s: S): V = r.runA(s).value

  given Monad[Rand] = stateMonadInstance

  val init: S = 0L

  def setSeed(seed: Long): Rand[Unit] = State.set(seed)

  def constant[V](v: V): Rand[V] = State.pure(v)

  def date(start: java.time.LocalDate, end: java.time.LocalDate): Rand[java.time.LocalDate] =
    State: s =>
      val r = scala.util.Random(s)
      val maxDays = java.time.temporal.ChronoUnit.DAYS.between(start, end)
      (r.nextLong, start.plusDays(r.nextLong(maxDays)))

  def `enum`[E](elems: E*): Rand[E] = State: s =>
    val r = scala.util.Random(s)
    (r.nextLong, elems(r.nextInt(elems.size)))

  def uuid: Rand[UUID] = State: s =>
    val r = scala.util.Random(s)
    (r.nextLong, new UUID(r.nextLong, r.nextLong))
