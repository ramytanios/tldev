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

package http4sutils.server

import org.http4s.HttpRoutes
import org.http4s.server.websocket.WebSocketBuilder2

trait Endpoint[F[_]]:
  /** http4s routes */
  def routes: WebSocketBuilder2[F] => HttpRoutes[F]

  /** description of the endpoint */
  def doc: Option[String]

  /** relative path of the endpoint */
  def relPath: Option[String]
