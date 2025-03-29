import cats.effect.IO

import tldev.core.IndexedHashMap
import java.time.Instant
import cats.syntax.all.*
import scala.concurrent.duration.*

object Example4:

  case class Instrument(
      id: Int,
      name: String,
      strike: Double,
      timestamp: Instant
  )

  def show(c: IndexedHashMap[Int, String, Instrument]) =
    println(
      s"""
      size: ${c.size}
      keys: ${c.keys.mkString(" | ")}
      attrs: ${c.attrs.mkString(" | ")}
      vals: ${c.values.mkString(" | ")}
      """
    )

  def insUpdates: fs2.Stream[IO, Instrument] =
    case class I(id: Int, name: String, strike: Double)
    val inss = List(
      I(1, "SPX/100/CALL", 100),  // new ins
      I(2, "AAPL/250/PUT", 250),  // new ins
      I(2, "AAPL/100/PUT", 100),  // ins change
      I(2, "AAPLX/250/PUT", 250), // name change
      I(3, "AAPLX/250/PUT", 250)  // id change
    )
    fs2.Stream
      .emits(inss)
      .covary[IO]
      .metered(3.seconds)
      .evalMap(ins => IO.realTimeInstant.tupleLeft(ins))
      .map((ins, t) => Instrument(ins.id, ins.name, ins.strike, t))

  def run: IO[Unit] =

    for
      insCache <- IO.ref(IndexedHashMap.empty[Int, String, Instrument](_.id, _.name))
      _ <- insUpdates
        .evalTap(ins => IO.println(s"instrument update received: $ins"))
        .evalMap(ins =>
          insCache.update {
            _.updateWith(ins) {
              case Some(ins0) if ins.timestamp.isBefore(ins0.timestamp) => Some(ins0)
              case _                                                    => Some(ins)
            }
          }
        ).compile.drain
      _ <- insCache.get.flatMap(c => IO.delay(show(c)))
    yield ()
