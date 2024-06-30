import cats.data.NonEmptyList
import cats.effect.IO
import cats.effect.IOApp
import cats.syntax.all.*
import fs2.data.csv.CsvRowEncoder
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.LoggerFactory
import tldev.core.implicits.given_LoggerFactory_F
import tldev.core.utils.Rand
import tldev.core.utils.csv
import tldev.core.utils.literals.*

import java.util.UUID
import scala.concurrent.duration.*
import cats.Show

object UtilsMain extends IOApp.Simple:

  case class EmployeeRecord(
      uuid: UUID,
      startDate: java.time.LocalDate,
      level: EmployeeRecord.Level
  )

  object EmployeeRecord:

    enum Level:
      case Junior
      case Senior

    given Show[EmployeeRecord] = Show: elem =>
      s"uuid: ${elem.uuid}, start: ${elem.startDate}, level: ${elem.level}"

    given CsvRowEncoder[EmployeeRecord, String] =
      CsvRowEncoder.instance[EmployeeRecord, String](row =>
        NonEmptyList.of(
          ("UUID", row.uuid.toString),
          ("START_DATE", row.startDate.toString),
          ("LEVEL", row.level.toString)
        )
      )

    def random: Rand[EmployeeRecord] =
      (
        Rand.uuid,
        Rand.date(date"2000-01-01", date"2024-01-01"),
        Rand.`enum`(Level.Junior, Level.Senior)
      ).mapN(EmployeeRecord.apply)

  override def run: IO[Unit] =
    for
      given Logger[IO] <- LoggerFactory[IO].create
      _ <- Logger[IO].info("starting ...")
      _ <- Logger[IO].info(EmployeeRecord.random.runA(Rand.init).show)
      _ <- fs2.Stream
        .emits(EmployeeRecord.random.listOfN(100).runA(Rand.init))
        .spaced[IO](1.second)
        .evalTap(e => Logger[IO].info(e.show))
        .through(csv.pipes.saveTo("dumps", "data", createPath = true))
        .compile
        .drain
    yield ()
