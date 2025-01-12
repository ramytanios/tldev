package tldev.http

import cats.MonadThrow
import cats.syntax.all.*
import tldev.core.EnvProvider

import scala.concurrent.duration.FiniteDuration

package object client:

  def configFromEnv[F[_]: MonadThrow](using
      env: EnvProvider[F]
  ): F[Either[EnvProvider.Error, Config]] =
    env.get[FiniteDuration]("HTTP_CLIENT_TIMEOUT").map {
      case Left(EnvProvider.Error.MissingEnv(_)) =>
        Option.empty[FiniteDuration].asRight[EnvProvider.Error]
      case Left(s @ EnvProvider.Error.ParsingError(msg)) =>
        s.asLeft[Option[FiniteDuration]]
      case Right(v) =>
        Option(v).asRight[EnvProvider.Error]
    }.map(_.map(Config(_)))
