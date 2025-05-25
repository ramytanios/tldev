package tldev.core

import cats.Monad
import cats.data.State
import cats.syntax.all.*

import java.time.LocalDate
import java.util.UUID
import scala.collection.View
import scala.util.Random

opaque type Rand[V] = State[Long, V]

object Rand:

  given (using m: Monad[State[Long, *]]): Monad[Rand] = m

  extension [V](r: Rand[V])
    def listOfN(n: Int): Rand[List[V]] = r.replicateA(n)
    def runA: V                        = r.runA(init).value
    def view: View[V] = View.unfold(init): s =>
      val (sNext, v) = r.run(s).value
      Some(v -> sNext)

  val init: Long = 0L

  val reset: Rand[Unit] = State.set(init)

  def setSeed(s: Long): Rand[Unit] = State.set(s)

  def pure[V](v: V): Rand[V] = State.pure(v)

  def constant[V](v: V): Rand[V] = pure(v)

  def normal: Rand[Double] =
    State: s =>
      val r = Random(s)
      r.nextLong -> r.nextGaussian

  def int: Rand[Int] =
    State: s =>
      val r = Random(s)
      r.nextLong -> r.nextInt

  def boolean: Rand[Boolean] =
    State: s =>
      val r = Random(s)
      r.nextLong -> r.nextBoolean

  def between(minInclusive: Double, maxInclusive: Double): Rand[Double] =
    State: s =>
      val r = Random(s)
      r.nextLong -> r.between(minInclusive, maxInclusive)

  def between(minInclusive: Int, maxInclusive: Int): Rand[Int] =
    State: s =>
      val r = Random(s)
      r.nextLong -> r.between(minInclusive, maxInclusive)

  def date(start: LocalDate, end: LocalDate): Rand[LocalDate] =
    State: s =>
      val r       = Random(s)
      val maxDays = java.time.temporal.ChronoUnit.DAYS.between(start, end)
      r.nextLong -> start.plusDays(r.nextLong(maxDays))

  def choose[E](es: E*): Rand[E] =
    State: s =>
      val r = Random(s)
      val i = r.nextInt(es.length)
      r.nextLong -> es(i)

  def uuid: Rand[UUID] =
    State: s =>
      val r = Random(s)
      r.nextLong -> new UUID(r.nextLong, r.nextLong)
