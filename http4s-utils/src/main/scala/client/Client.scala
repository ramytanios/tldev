package http4sutils.client

import io.circe.Decoder
import io.circe.Encoder
import cats.effect.kernel.Async
import org.http4s.ember.client.EmberClientBuilder
import cats.effect.kernel.Resource
import org.http4s.Uri
import cats.syntax.all.*
import org.http4s.circe.CirceEntityCodec._
import org.http4s.client.dsl.Http4sClientDsl
import fs2.io.net.Network
import org.http4s.Method

sealed trait Client[F[_]]:
  /** basic `GET` request */
  def get[R: Decoder](url: String): F[R]

  /** basic `POST` request */
  def post[P: Encoder, R: Decoder](url: String, payload: P): F[R]

object Client:
  def apply[F[_]: Network](config: Config)(using F: Async[F]): Resource[F, Client[F]] =

    val dsl = Http4sClientDsl[F]
    import dsl._

    EmberClientBuilder
      .default[F]
      .withTimeout(config.timeout)
      .build
      .map: client =>
        new Client[F]:

          override def get[R: Decoder](url: String): F[R] =
            F.fromEither(Uri.fromString(url))
              .flatMap: uri =>
                client.expect[R](uri)

          override def post[P: Encoder, R: Decoder](url: String, payload: P): F[R] =
            F.fromEither(Uri.fromString(url))
              .flatMap: uri =>
                client.expect[R](Method.POST(payload, uri))
