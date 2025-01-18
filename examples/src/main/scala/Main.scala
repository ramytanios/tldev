import cats.effect.ExitCode
import cats.effect.IO
import com.monovore.decline.Opts
import tldev.core.MyIOApp

object Main extends MyIOApp.CommandIOApp("Examples", "Examples"):

  override def runMain: Opts[IO[ExitCode]] =

    Opts
      .option[Int]("example", "example number")
      .withDefault(1)
      .map {
        case 1 => Example1.run
        case 2 => Example2.run
        case 3 => Example3.run
      }
      .map(_.as(ExitCode.Success))
