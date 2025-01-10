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

  /**
   * access an environment variable,
   * throws if it does not exist nor can't be parsed
   */
  def get[V](name: String)(using Parser[V]): F[V]

object EnvProvider:

  case class ParsingError(msg: String) extends IllegalArgumentException(msg) with NoStackTrace

  trait Parser[V]:

    def parse(env: String): Either[ParsingError, V]

  object Parser:

    def apply[V](using Parser[V]) = summon[Parser[V]]

    given Parser[String] with
      override def parse(env: String): Either[ParsingError, String] = env.asRight[ParsingError]

    given Parser[Int] with
      override def parse(env: String): Either[ParsingError, Int] =
        Either.catchOnly[NumberFormatException](env.toInt).leftMap(t =>
          ParsingError(s"failed to parse $env to `Int`: $t")
        )

    given Parser[Long] with
      override def parse(env: String): Either[ParsingError, Long] =
        Either.catchOnly[NumberFormatException](env.toLong).leftMap(t =>
          ParsingError(s"failed to parse $env to `Long`: $t")
        )

    given Parser[Double] with
      override def parse(env: String): Either[ParsingError, Double] =
        Either.catchOnly[NumberFormatException](env.toDouble).leftMap(t =>
          ParsingError(s"failed to parse $env to `Double`")
        )

    given Parser[FiniteDuration] with
      override def parse(env: String): Either[ParsingError, FiniteDuration] =
        Either.catchOnly[IllegalArgumentException](Duration(env))
          .leftMap(e => ParsingError(e.getMessage))
          .flatMap {
            case d: FiniteDuration => d.asRight[ParsingError]
            case _                 => Left(ParsingError(s"failed to parse $env to `FiniteDuration`"))
          }

  def default[F[_]: Env: MonadThrow]: EnvProvider[F] =
    new EnvProvider[F]:
      override def get[V](name: String)(using p: Parser[V]): F[V] =
        Env[F].get(name).flatMap(strMaybe =>
          MonadThrow[F].fromOption(
            strMaybe,
            new RuntimeException(s"missing `${name}` env var")
          )
        ).flatMap(env => MonadThrow[F].fromEither(p.parse(env)))

  def resource[F[_]: Env: MonadThrow]: Resource[F, EnvProvider[F]] =
    Resource.pure(this.default[F])
