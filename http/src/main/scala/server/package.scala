package tldev.http

import cats.MonadThrow
import cats.syntax.all.*
import tldev.core.EnvProvider

package object server:

  def configFromEnv[F[_]: MonadThrow](using
      env: EnvProvider[F]
  ): F[Either[EnvProvider.Error, Config]] =
    (
      env.get[String]("BE_HOST"),
      env.get[Int]("BE_PORT"),
      env.get[Int]("BE_MAX_CONNECTIONS"),
      env.get[String]("BE_API_PREFIX")
    ).tupled.map(
      (
          host,
          port,
          maxConnections,
          prefix
      ) =>
        (
          host,
          port,
          maxConnections,
          prefix match
            case Left(EnvProvider.Error.MissingEnv(_)) =>
              Option.empty[String].asRight[EnvProvider.Error]
            case Left(s @ EnvProvider.Error.ParsingError(msg)) =>
              s.asLeft[Option[String]]
            case Right(v) => Option(v).asRight[EnvProvider.Error]
        ).tupled.map(Config.apply _)
    )
