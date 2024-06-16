package implied.interest.rates.http4s.server

import org.http4s.HttpRoutes
import org.http4s.server.websocket.WebSocketBuilder2

trait Endpoint[F[_]]:
  /** http4s routes */
  def routes: WebSocketBuilder2[F] => HttpRoutes[F]

  /** description of the endpoint */
  def doc: Option[String]

  /** relative path of the endpoint */
  def relPath: Option[String]
