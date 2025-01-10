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

  /** Throws in F if env var does not exist or can't be cast */
  def get[V](name: String)(using Parser[V]): F[V]

object EnvProvider:

  trait Parser[V]:
    /** Attemtps at casting a string to V */
    def parse(env: String): Either[Throwable, V]

  object Parser:

    def apply[V](using Parser[V]) = summon[Parser[V]]

    case class ParsingError(msg: String) extends IllegalArgumentException(msg) with NoStackTrace

    given Parser[String] with
      override def parse(env: String): Either[Throwable, String] = env.asRight[Throwable]

    given Parser[Int] with
      override def parse(env: String): Either[Throwable, Int] =
        Either.catchOnly[NumberFormatException](env.toInt).leftMap(t =>
          ParsingError(s"failed to parse $env to `Int`: $t")
        )

    given Parser[Long] with
      override def parse(env: String): Either[Throwable, Long] =
        Either.catchOnly[NumberFormatException](env.toLong).leftMap(t =>
          ParsingError(s"failed to parse $env to `Long`: $t")
        )

    given Parser[Double] with
      override def parse(env: String): Either[Throwable, Double] =
        Either.catchOnly[NumberFormatException](env.toDouble).leftMap(t =>
          new IllegalArgumentException(s"failed to parse $env to `Double`")
        )

    given Parser[FiniteDuration] with
      override def parse(env: String): Either[Throwable, FiniteDuration] =
        Either.catchOnly[NumberFormatException](Duration(env)).flatMap {
          case d: FiniteDuration => Right(d)
          case _                 => Left(ParsingError(s"failed to parse $env to `FiniteDuration`"))
        }

  def resource[F[_]: Env: MonadThrow]: Resource[F, EnvProvider[F]] =
    for
      _ <- Resource.pure[F, Unit](())
    yield new EnvProvider[F]:
      override def get[V](name: String)(using p: Parser[V]): F[V] =
        Env[F].get(name).flatMap(strMaybe =>
          MonadThrow[F].fromOption(
            strMaybe,
            new RuntimeException(s"missing `${name}` env var")
          )
        ).flatMap(env => MonadThrow[F].fromEither(p.parse(env)))
