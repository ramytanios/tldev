package http4sutils.client

import io.circe.Decoder
import io.circe.Encoder
import cats.effect.kernel.Async

sealed trait Client[F[_]]:
  /** basic `GET` request */
  def get[Response: Decoder](url: String): F[Response]

  /** basic `POST` request */
  def post[Request: Encoder, Response: Decoder](url: String, body: Request): F[Response]

object Client:
  def apply[F[_]](using F: Async[F]) = new Client[F]:
    override def get[Response: Decoder](url: String): F[Response] = ???
    override def post[Request: Encoder, Response: Decoder](
        url: String,
        body: Request
    ): F[Response] = ???
