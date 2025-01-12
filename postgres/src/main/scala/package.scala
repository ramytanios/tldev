package tldev

import cats.MonadThrow
import cats.syntax.all.*
import tldev.core.EnvProvider

import scala.concurrent.duration.FiniteDuration

package object postgres:

  def configFromEnv[F[_]: MonadThrow](using
      env: EnvProvider[F]
  ): F[Either[EnvProvider.Error, Config]] =
    for
      host           <- env.get[String]("PG_HOST")
      port           <- env.get[Int]("PG_PORT")
      user           <- env.get[String]("PG_USR")
      pass           <- env.get[String]("PG_PASS")
      db             <- env.get[String]("PG_URL")
      maxConnections <- env.get[Int]("PG_MAX_CONNECTIONS")
    yield (host, port, user, pass, db, maxConnections).tupled.map(Config.apply _)
