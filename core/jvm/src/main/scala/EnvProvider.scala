package tldev.core

import cats.MonadThrow
import cats.effect.kernel.Resource
import cats.effect.std.Env
import cats.syntax.all.*

import scala.concurrent.duration.Duration
import scala.concurrent.duration.FiniteDuration
import scala.util.control.NoStackTrace

import EnvProvider.*

trait EnvProvider[F[_]]:

  /** access an environment variable */
  def get[V](name: String)(using Parser[V]): F[Either[Error, V]]

object EnvProvider:

  enum Error(msg: String) extends IllegalArgumentException(msg):
    case MissingEnv(env: String)   extends Error(s"missing env variable $env")
    case ParsingError(msg: String) extends Error(msg)

  trait Parser[V]:

    def parse(env: String): Either[Error, V]

  object Parser:

    def apply[V](using Parser[V]) = summon[Parser[V]]

    given Parser[String] with
      override def parse(env: String): Either[Error, String] = env.asRight[Error]

    given Parser[Int] with
      override def parse(env: String): Either[Error, Int] =
        Either.catchOnly[NumberFormatException](env.toInt).leftMap(t =>
          Error.ParsingError(s"failed to parse $env to `Int`: $t")
        )

    given Parser[Long] with
      override def parse(env: String): Either[Error, Long] =
        Either.catchOnly[NumberFormatException](env.toLong).leftMap(t =>
          Error.ParsingError(s"failed to parse $env to `Long`: $t")
        )

    given Parser[Double] with
      override def parse(env: String): Either[Error, Double] =
        Either.catchOnly[NumberFormatException](env.toDouble).leftMap(t =>
          Error.ParsingError(s"failed to parse $env to `Double`")
        )

    given Parser[FiniteDuration] with
      override def parse(env: String): Either[Error, FiniteDuration] =
        Either.catchOnly[IllegalArgumentException](Duration(env))
          .leftMap(e => Error.ParsingError(e.getMessage))
          .flatMap {
            case d: FiniteDuration => d.asRight[Error]
            case _                 => Left(Error.ParsingError(s"failed to parse $env to `FiniteDuration`"))
          }

  def default[F[_]: Env: MonadThrow]: EnvProvider[F] =
    new EnvProvider[F]:
      override def get[V](name: String)(using p: Parser[V]): F[Either[Error, V]] =
        Env[F].get(name).map {
          case Some(str) => p.parse(str)
          case None      => Error.MissingEnv(s"missing `${name}` env var").asLeft[V]
        }

  def resource[F[_]: Env: MonadThrow]: Resource[F, EnvProvider[F]] =
    Resource.pure(this.default[F])
