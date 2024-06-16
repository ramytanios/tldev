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

import cats.effect.IO
import cats.effect.IOApp
import http4sutils.server.Server
import http4sutils.server.Config
import http4sutils.server.EndpointFactory
import scala.concurrent.duration.*

object WebsocketMain extends IOApp.Simple:

  override def run: IO[Unit] =

    val ef = EndpointFactory[IO]

    // websocket
    val ws = ef.jsonBidirectionalWebsocket[String, String](
      _.printlns,
      fs2.Stream
        .fixedDelay[IO](5.seconds)
        .map: _ =>
          "Hello World!"
    )

    // config
    val config = Config("localhost", 8090, 12)

    // server
    val httpServer = Server[IO](config, ws :: Nil, None, None)

    httpServer.run
