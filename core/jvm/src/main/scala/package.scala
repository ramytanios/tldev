package tldev

import cats.effect.kernel.Temporal
import cats.syntax.all.*
import org.typelevel.log4cats.Logger

import scala.concurrent.duration.*

package object core:

  def logo = """
      | ____ __   ____ ____ _  _ 
      |(_  _(  ) (    (  __/ )( \
      |  )( / (_/\) D () _)\ \/ /
      | (__)\____(____(____)\__/
      | 
      | Using tldev
    """.stripMargin

  def logProgress[F[_]: Temporal: Logger, V](
      label: String,
      totalElements: Option[Long] = None,
      frequency: FiniteDuration = 3.seconds
  ): fs2.Pipe[F, V, V] =
    (in: fs2.Stream[F, V]) =>
      fs2.Stream
        .eval(Logger[F].info(s"$label: starting ..."))
        .evalMap(_ => Temporal[F].ref[Long](0L))
        .flatMap: counter =>
          in.chunks
            .evalTap: chunk =>
              counter.update(_ + chunk.size)
            .unchunks
            .concurrently(
              fs2.Stream
                .fixedDelay(frequency)
                .evalTap: _ =>
                  counter.get.flatMap: k =>
                    Logger[F].info(
                      totalElements.fold(s"$label: $k")(n => s"$label: $k/$n")
                    )
            ) ++ fs2.Stream.exec(Logger[F].info(s"$label: done!"))
