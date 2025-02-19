package tldev.http.client

import cats.effect.kernel.Async
import cats.effect.kernel.Resource
import cats.syntax.all.*
import fs2.io.net.Network
import io.circe.Decoder
import io.circe.Encoder
import io.circe.syntax.*
import org.http4s.Method
import org.http4s.Request
import org.http4s.Uri
import org.http4s.circe.CirceEntityCodec.*
import org.http4s.ember.client.EmberClientBuilder

import scala.concurrent.duration.*

sealed trait Client[F[_]]:

  def get[R: Decoder](
      baseUrl: String,
      path: Option[String] = None,
      queryParams: Map[String, String] = Map.empty,
      headers: Map[String, String] = Map.empty
  ): F[R]

  def post[P: Encoder, R: Decoder](
      baseUrl: String,
      path: Option[String] = None,
      payload: P,
      headers: Map[String, String] = Map.empty
  ): F[R]

object Client:
  def apply[F[_]: Network](config: Config)(using F: Async[F]): Resource[F, Client[F]] =
    EmberClientBuilder
      .default[F]
      .withTimeout(config.timeout.getOrElse(5.minutes))
      .build
      .map: client =>
        new Client[F]:

          override def get[R: Decoder](
              baseUrl: String,
              path: Option[String],
              queryParams: Map[String, String],
              headers: Map[String, String]
          ): F[R] =
            Uri
              .fromString(baseUrl)
              .liftTo[F]
              .map: uri =>
                path.fold(uri)(uri.addPath).withQueryParams(queryParams)
              .flatMap: uri =>
                val request = Request[F](Method.GET, uri)
                client.expect[R](request)

          override def post[P: Encoder, R: Decoder](
              baseUrl: String,
              path: Option[String],
              payload: P,
              headers: Map[String, String]
          ): F[R] =
            Uri
              .fromString(baseUrl)
              .liftTo[F]
              .map: uri =>
                path.fold(uri)(uri.addPath)
              .flatMap: uri =>
                val body =
                  fs2.Stream
                    .emit(payload.asJson.noSpaces)
                    .through(fs2.text.utf8.encode)
                val request = Request[F](Method.POST, uri, body = body)
                client.expect[R](request)
