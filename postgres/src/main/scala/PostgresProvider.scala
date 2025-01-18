package tldev.postgres

import cats.effect.Temporal
import cats.effect.kernel.Resource
import cats.effect.std.Console
import cats.syntax.all.*
import fs2.io.net.Network
import natchez.Trace.Implicits.noop
import skunk.Command
import skunk.Query
import skunk.Session

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
  def resource[F[_]: Temporal: Network: Console](config: Config)
      : Resource[F, PostgresProvider[F]] =
    for
      session <- Session.pooled(
        config.host,
        config.port,
        config.user,
        config.db,
        Some(config.pass),
        config.maxConnections
      )
    yield new PostgresProvider[F]:
      override def unique[A, B](q: Query[A, B], args: A): F[B] =
        session.use(_.unique(q)(args))
      override def option[A, B](q: Query[A, B], args: A): F[Option[B]] =
        session.use(_.option(q)(args))
      override def stream[A, B](q: Query[A, B], args: A, chunk: Int = 128): fs2.Stream[F, B] =
        fs2.Stream.resource(session).flatMap(_.stream(q)(args, chunk))
      override def command[A](c: Command[A], args: A): F[Unit] =
        session.use(_.execute(c)(args)).as(())
