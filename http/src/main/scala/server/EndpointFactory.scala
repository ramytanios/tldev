package tldev.http.server

import cats.effect.Concurrent
import cats.effect.Temporal
import cats.syntax.all.*
import io.circe.*
import io.circe.syntax.*
import org.http4s.*
import org.http4s.circe.CirceEntityCodec.*
import org.http4s.circe.*
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router
import org.http4s.server.websocket.WebSocketBuilder2
import org.http4s.websocket.WebSocketFrame
import scala.concurrent.duration.*
import io.circe.parser.*
import org.http4s.websocket.WebSocketFrame
import org.http4s.websocket.WebSocketFrame.Ping

final class EndpointFactory[F[_]: Concurrent: Temporal]() extends Http4sDsl[F]:

  def jsonGet[Response: Encoder](
      path: String,
      handler: F[Response],
      description: Option[String] = None
  ): Endpoint[F] =
    new Endpoint[F]:
      override def routes = (_: WebSocketBuilder2[F]) =>
        val r = HttpRoutes.of[F]:
          case GET -> Root => handler.flatMap(Ok(_))
        Router(path -> r)
      override def doc: Option[String]     = description
      override def relPath: Option[String] = Some(path)

  def jsonGetWithOneQueryParam[Response: Encoder](
      path: String,
      queryParam: String,
      handler: String => F[Response],
      description: Option[String] = None
  ): Endpoint[F] =
    object QueryParamMatcher extends QueryParamDecoderMatcher[String](queryParam)
    new Endpoint[F]:
      override def routes = (_: WebSocketBuilder2[F]) =>
        val r = HttpRoutes.of[F]:
          case GET -> Root / path :? QueryParamMatcher(param) =>
            handler(param).flatMap:
              Ok(_)
        Router(path -> r)
      override def doc: Option[String]     = description
      override def relPath: Option[String] = Some(path)

  def jsonPost[Request: Decoder, Response: Encoder](
      path: String,
      handler: Request => F[Response],
      description: Option[String] = None
  ): Endpoint[F] =
    new Endpoint[F]:
      override def routes = (_: WebSocketBuilder2[F]) =>
        val r = HttpRoutes.of[F]:
          case request @ POST -> Root =>
            for
              req  <- request.as[Request]
              resp <- handler(req)
              ok   <- Ok(resp)
            yield ok
        Router(path -> r)
      override def doc: Option[String]     = description
      override def relPath: Option[String] = Some(path)

  def jsonBidirectionalWebsocket[MessageIn: Decoder, MessageOut: Encoder](
      receiveSend: fs2.Pipe[F, MessageIn, MessageOut],
      prefix: String,
      description: Option[String] = None
  ): Endpoint[F] =
    val route = (wsb: WebSocketBuilder2[F]) =>
      HttpRoutes.of[F]:
        case GET -> Root =>
          val keepAlive = fs2.Stream.fixedDelay[F](5.seconds).map(_ => Ping())
          wsb.build: (in: fs2.Stream[F, WebSocketFrame]) =>
            in
              .mergeHaltL(keepAlive)
              .collect { case WebSocketFrame.Text(msg, _) => decode[MessageIn](msg) }
              .collect { case Right(msg) => msg }
              .through(receiveSend)
              .map(msgOut => WebSocketFrame.Text(msgOut.asJson.noSpaces))

    new Endpoint[F]:
      override def routes                  = (wsb: WebSocketBuilder2[F]) => Router(s"$prefix/ws" -> route(wsb))
      override def doc: Option[String]     = description
      override def relPath: Option[String] = None
