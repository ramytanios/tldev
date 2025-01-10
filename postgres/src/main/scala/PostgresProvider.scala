package tldev.postgres

import cats.effect.Temporal
import cats.effect.implicits.*
import cats.effect.kernel.Resource
import cats.effect.std.Console
import cats.syntax.all.*
import fs2.io.net.Network
import natchez.Trace.Implicits.noop
import skunk.Command
import skunk.Query
import skunk.Session
import tldev.core.EnvProvider

trait PostgresProvider[F[_]]:
  /** query that returns a unique row */
  def unique[A, B](q: Query[A, B], args: A): F[B]

  /** query that might return an row */
  def option[A, B](q: Query[A, B], args: A): F[Option[B]]

  /** query that returns a stream of rows */
  def stream[A, B](q: Query[A, B], args: A, chunk: Int = 128): fs2.Stream[F, B]

  /** command with args of type A */
  def command[A](c: Command[A], args: A): F[Unit]

object PostgresProvider:
  def resource[F[_]: Temporal: Network: Console](env: EnvProvider[F])
      : Resource[F, PostgresProvider[F]] =
    for
      _              <- Resource.pure[F, Unit](())
      host           <- env.get[String]("PG_HOST").toResource
      port           <- env.get[Int]("PG_PORT").toResource
      user           <- env.get[String]("PG_USR").toResource
      pass           <- env.get[String]("PG_PASS").toResource
      db             <- env.get[String]("PG_URL").toResource
      maxConnections <- env.get[Int]("PG_MAX_CONNECTIONS").toResource
      session        <- Session.pooled(host, port, user, db, Some(pass), maxConnections)
    yield new PostgresProvider[F]:
      override def unique[A, B](q: Query[A, B], args: A): F[B] =
        session.use(_.unique(q)(args))
      override def option[A, B](q: Query[A, B], args: A): F[Option[B]] =
        session.use(_.option(q)(args))
      override def stream[A, B](q: Query[A, B], args: A, chunk: Int = 128): fs2.Stream[F, B] =
        fs2.Stream.resource(session).flatMap(_.stream(q)(args, chunk))
      override def command[A](c: Command[A], args: A): F[Unit] =
        session.use(_.execute(c)(args)).as(())
