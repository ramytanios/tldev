package tldev

import cats.MonadThrow
import cats.syntax.all._
import tldev.core.EnvProvider

package object postgres:

  enum ev:
    case PG_HOST, PG_PORT, PG_USER, PG_PASS, PG_URL, PG_MAX_CONNECTIONS

  object ev:
    given Conversion[ev, String] with
      override def apply(x: ev): String = x.toString

  // expects `PG_HOST`, `PG_PORT`, `PG_USER`, `PG_PASS`, `PG_URL` and `PG_MAX_CONNECTIONS`
  // environment variables to be set
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
