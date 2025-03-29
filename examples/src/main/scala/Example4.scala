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
      timestamp: Option[Instant] = None
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
    val inss = List(
      Instrument(1, "SPX/100/CALL", 100),  // new ins
      Instrument(2, "AAPL/250/PUT", 250),  // new ins
      Instrument(2, "AAPL/100/PUT", 100),  // ins change
      Instrument(2, "AAPLX/250/PUT", 250), // name change
      Instrument(3, "AAPLX/250/PUT", 250)  // id change
    )
    fs2.Stream
      .emits(inss)
      .covary[IO]
      .metered(3.seconds)
      .evalMap(ins => IO.realTimeInstant.map(t => ins.copy(timestamp = Some(t))))

  def run: IO[Unit] =

    for
      insCache <- IO.ref(IndexedHashMap.empty[Int, String, Instrument](_.id, _.name))
      _ <- insUpdates
        .evalTap(ins => IO.println(s"received update: $ins"))
        .evalMap(ins =>
          insCache.update {
            oldCache =>
              oldCache.updateWith(ins) {
                case Some(ins0)
                    if (ins0.timestamp, ins.timestamp).tupled.exists((tsIns0, tsIns) =>
                      tsIns.isBefore(tsIns0)
                    ) => Some(ins0)
                case _ => Some(ins)
              }
          }
        ).compile.drain
      _ <- insCache.get.flatMap(c => IO.delay(show(c)))
    yield ()
