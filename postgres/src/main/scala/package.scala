package tldev

import cats.MonadThrow
import cats.syntax.all.*
import tldev.core.EnvProvider

import scala.concurrent.duration.FiniteDuration

package object postgres:

  enum ev:
    case PG_HOST, PG_PORT, PG_USER, PG_PASS, PG_URL, PG_MAX_CONNECTIONS

  object ev:
    given Conversion[ev, String] with
      override def apply(x: ev): String = x match
        case PG_HOST            => "PG_HOST"
        case PG_PORT            => "PG_PORT"
        case PG_USER            => "PG_USER"
        case PG_PASS            => "PG_PASS"
        case PG_URL             => "PG_URL"
        case PG_MAX_CONNECTIONS => "PG_MAX_CONNECTIONS"

  def configFromEnv[F[_]: MonadThrow](using
      env: EnvProvider[F]
  ): F[Either[EnvProvider.Error, Config]] =
    for
      host           <- env.get[String](ev.PG_HOST)
      port           <- env.get[Int](ev.PG_PORT)
      user           <- env.get[String](ev.PG_USER)
      pass           <- env.get[String](ev.PG_PASS)
      db             <- env.get[String](ev.PG_URL)
      maxConnections <- env.get[Int](ev.PG_MAX_CONNECTIONS)
    yield (host, port, user, pass, db, maxConnections).tupled.map(Config.apply _)
