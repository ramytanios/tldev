import cats.effect.IO
import cats.effect.IOApp
import tldev.core.ProgressReporter

import scala.concurrent.duration.*

object ProgressReporterMain extends IOApp.Simple:

  override def run: IO[Unit] =
    cats.effect.std.Queue
      .unbounded[IO, String]
      .flatMap: logQueue =>
        ProgressReporter
          .toQueue[IO]("my_label", 1.seconds, logQueue)
          .use: reporter =>
            fs2.Stream
              .fixedDelay[IO](3.seconds)
              .zipWithIndex
              .map((_, idx) => idx.toDouble)
              .evalTap(reporter.setProgress(_))
              .takeWhile(_ <= 100)
              .concurrently:
                fs2.Stream.fromQueueUnterminated(logQueue).printlns
              .compile
              .drain
