package http4sutils.server

import cats.effect.Concurrent
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

final class EndpointFactory[F[_]: Concurrent]() extends Http4sDsl[F] {

  def jsonGet[Response: Encoder](
      path: String,
      handler: F[Response],
      description: Option[String] = None
  ) =
    new Endpoint[F]:
      override def routes = (_: WebSocketBuilder2[F]) =>
        val r = HttpRoutes.of[F]:
          case GET -> Root => handler.flatMap(Ok(_))
        Router(path -> r)
      override def doc: Option[String] = description
      override def relPath: Option[String] = Some(path)

  def jsonGetWithOneQueryParam[Response: Encoder](
      path: String,
      queryParam: String,
      handler: String => F[Response],
      description: Option[String] = None
  ) =
    object QueryParamMatcher extends QueryParamDecoderMatcher[String](queryParam)
    new Endpoint[F]:
      override def routes = (_: WebSocketBuilder2[F]) =>
        val r = HttpRoutes.of[F]:
          case GET -> Root / path :? QueryParamMatcher(param) =>
            handler(param).flatMap:
              Ok(_)
        Router(path -> r)
      override def doc: Option[String] = description
      override def relPath: Option[String] = Some(path)

  def jsonPost[Request: Decoder, Response: Encoder](
      path: String,
      handler: Request => F[Response],
      description: Option[String] = None
  ) =
    new Endpoint[F]:
      override def routes = (_: WebSocketBuilder2[F]) =>
        val r = HttpRoutes.of[F]:
          case request @ POST -> Root =>
            for
              req <- request.as[Request]
              resp <- handler(req)
              ok <- Ok(resp)
            yield ok
        Router(path -> r)
      override def doc: Option[String] = description
      override def relPath: Option[String] = Some(path)

  def jsonBidirectionalWebsocket[MessageIn: Decoder, MessageOut: Encoder](
      in: fs2.Pipe[F, MessageIn, Unit],
      out: fs2.Stream[F, MessageOut],
      description: Option[String] = None,
      prefix: String = "/ws"
  ): Endpoint[F] =
    val route = (wsb: WebSocketBuilder2[F]) =>
      HttpRoutes.of[F]:
        case GET -> Root =>
          val send: fs2.Stream[F, WebSocketFrame] =
            out.map: message =>
              WebSocketFrame.Text(message.asJson.noSpaces)

          val receive: fs2.Pipe[F, WebSocketFrame, Unit] =
            _.collect:
              case WebSocketFrame.Text(msg, _) => msg.asJson.as[MessageIn]
            .collect:
              case Right(msg) => msg
            .through(in)

          wsb.build(send, receive)

    new Endpoint[F]:
      override def routes = (wsb: WebSocketBuilder2[F]) => Router(prefix -> route(wsb))
      override def doc: Option[String] = description
      override def relPath: Option[String] = None
}
