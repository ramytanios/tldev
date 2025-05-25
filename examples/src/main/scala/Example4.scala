import cats.effect.IO
import cats.syntax.all.*
import tldev.core.Rand

import scala.math.abs

object Example4:

  case class Case(xs: List[Double], ys: List[Double])

  val genCase: Rand[Case] =
    for
      n  <- Rand.between(3, 20)
      x0 <- Rand.normal
      xs <- Rand.normal.listOfN(n).map(_.map(abs(_)).scanLeft(x0)(_ + _))
      y0 <- Rand.normal
      ys <- Rand.normal.listOfN(n).map(_.scanLeft(y0)(_ + _))
    yield Case(xs, ys)

  def run: IO[Unit] = IO.pure(
    genCase
      .view
      .take(10)
      .zipWithIndex
      .foreach((c, i) => println(c))
  )
