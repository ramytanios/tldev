package tldev.core

import cats.effect.Temporal
import cats.effect.implicits._
import cats.effect.kernel.Resource
import cats.effect.std.Console
import cats.effect.std.Queue
import cats.syntax.all._
import org.typelevel.log4cats.LoggerFactory

import scala.concurrent.duration.FiniteDuration
import scala.util.matching.Regex

sealed trait ProgressReporter[F[_]]:

  /** Sets the progress to a given percentage value in the range [0, 100] */
  def setProgress(pct: Double): F[Unit]

object ProgressReporter:

  final case class LabeledProgress(label: String, pct: Double)

  private val patternRegex: Regex =
    """\[__progress_reporter__\]\(pct=(\d?\d?\d\.\d\d)\)\(label=(.*)\)""".r

  case class ProgressReporterException(msg: String) extends RuntimeException(msg)

  case class ParsingException(msg: String) extends RuntimeException(msg)

  def tryParseProgress(progress: String): Either[ParsingException, LabeledProgress] =
    progress match
      case patternRegex(pct, label) =>
        pct.toDoubleOption
          .map(LabeledProgress(label, _))
          .toRight(new ParsingException("failed to parse progress"))
      case _ =>
        ParsingException(s"given string $progress does not match pattern $patternRegex").asLeft

  def default[F[_]: Temporal: LoggerFactory](
      label: String,
      logFreq: FiniteDuration
  ): Resource[F, ProgressReporter[F]] =
    toLogger(label, logFreq)

  def toLogger[F[_]: Temporal: LoggerFactory](
      label: String,
      logFreq: FiniteDuration
  ): Resource[F, ProgressReporter[F]] =
    LoggerFactory[F].create.toResource.flatMap: logger =>
      make(label, (s: String) => logger.info(s), logFreq)

  def toConsole[F[_]: Temporal: Console](
      label: String,
      logFreq: FiniteDuration
  ): Resource[F, ProgressReporter[F]] =
    make(label, Console[F].println, logFreq)

  def toQueue[F[_]: Temporal](
      label: String,
      logFreq: FiniteDuration,
      queue: Queue[F, String]
  ): Resource[F, ProgressReporter[F]] =
    make(label, queue.offer, logFreq)

  private def make[F[_]](
      label: String,
      progressAction: String => F[Unit],
      logFreq: FiniteDuration
  )(using F: Temporal[F]): Resource[F, ProgressReporter[F]] =
    for
      ref <- F.ref(0.0).toResource
      _ <- fs2.Stream
        .fixedDelay(logFreq)
        .evalMap: _ =>
          ref.get.flatMap: p =>
            progressAction(f"[__progress_reporter__](pct=$p%.2f)(label=$label)".toString)
        .compile
        .drain
        .background
    yield new ProgressReporter[F]:
      private def validateProgress(p: Double): F[Unit] =
        ref.get.flatMap: cp =>
          F.raiseUnless(p <= 100 && p >= cp)(
            new ProgressReporterException(
              "progress must be non-decreasing and not exceed 100.0"
            )
          )
      override def setProgress(pct: Double): F[Unit] =
        validateProgress(pct) *> ref.set(pct)
