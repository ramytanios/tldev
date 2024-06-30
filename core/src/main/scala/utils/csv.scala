package tldev.core.utils

import cats.effect.kernel.Async
import cats.effect.kernel.Temporal
import fs2.data.csv.CsvRowEncoder
import fs2.data.csv.encodeUsingFirstHeaders
import fs2.io.file.Files
import fs2.io.file.Path
import org.typelevel.log4cats.Logger
import tldev.core.utils.streaming.logProgress
import cats.syntax.all.*

object csv:

  object pipes:

    def saveTo[F[_]: Async: Temporal: Logger: Files, V: CsvRowEncoder[*, String]](
        path: String,
        filename: String,
        createPath: Boolean = false
    ): fs2.Pipe[F, V, Nothing] =
      (in: fs2.Stream[F, V]) =>
        fs2.Stream
          .eval(
            Async[F].whenA(createPath)(
              Files[F]
                .createDirectories(Path(path))
                .flatTap(_ => Logger[F].info(s"created path $path"))
                .handleErrorWith(e =>
                  Logger[F].warn(e)(s"failed to create directories, perhaps $path exists?")
                )
            )
          )
          .flatMap(_ =>
            in.through(
              logProgress(s"writing to csv at $path/$filename.csv")
                .andThen(encodeUsingFirstHeaders[V](fullRows = true, separator = ';'))
                .andThen(fs2.text.utf8.encode)
                .andThen(Files[F].writeAll(Path(s"$path/$filename.csv")))
            )
          )
