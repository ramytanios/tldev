/*
 * Copyright 2024 ramytanios
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tldev.http.server

import cats.data.Kleisli
import cats.effect.kernel.Async
import cats.implicits.*
import com.comcast.ip4s.Host
import com.comcast.ip4s.Port
import fs2.io.net.Network
import org.http4s.HttpRoutes
import org.http4s.Response
import org.http4s.dsl.Http4sDsl
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Router
import org.http4s.server.websocket.WebSocketBuilder2
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.LoggerFactory
import org.typelevel.log4cats.syntax.*

final class Server[F[_]: Async: Network: LoggerFactory](
    config: Config,
    endpoints: List[Endpoint[F]],
    description: Option[String],
    version: Option[String]
) extends Http4sDsl[F]:

  case class HttpServerException(msg: String) extends RuntimeException(msg)

  private val metaEndpoints =

    val epf = EndpointFactory[F]

    /** alive endpoint */
    val aliveEp = epf.jsonGet("alive", Async[F].pure("I am alive!"))

    /** description endpoint */
    val descriptionEp = epf.jsonGet("description", Async[F].pure(description))

    /** version endpoint */
    val versionEp = epf.jsonGet("version", Async[F].pure(version))

    aliveEp :: descriptionEp :: versionEp :: Nil

  private def constructRoutes(wsb: WebSocketBuilder2[F]): HttpRoutes[F] =
    val routes = (endpoints ::: metaEndpoints).map(_.routes(wsb)).reduce(_ <+> _)
    config.prefix.fold(routes): prefix =>
      Router(prefix -> routes)

  def run: F[Unit] =
    for
      given Logger[F] <- LoggerFactory[F].create
      host <- Host
        .fromString(config.host)
        .liftTo[F](HttpServerException(s"Invalid host ${config.host}"))
      port <- Port
        .fromInt(config.port)
        .liftTo[F](HttpServerException(s"Invalid port ${config.port}"))
      _ <- EmberServerBuilder
        .default[F]
        .withHost(host)
        .withPort(port)
        .withHttpWebSocketApp(wsb => constructRoutes(wsb).orNotFound)
        .withMaxConnections(config.maxConnections)
        .build
        .evalTap(_ => info"Server listening on port $port")
        .use(_ => Async[F].never)
    yield ()
