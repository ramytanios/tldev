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
import io.circe.Codec
import io.circe.generic.semiauto.*
import tldev.core.implicits.given_LoggerFactory_F
import tldev.http.server.Config
import tldev.http.server.EndpointFactory
import tldev.http.server.Server

object ServerMain extends IOApp.Simple:

  case class Foo(x: String)
  object Foo:
    given Codec[Foo] = deriveCodec[Foo]

  case class Bar(y: Int)
  object Bar:
    given Codec[Bar] = deriveCodec[Bar]

  override def run: IO[Unit] =

    // endpoints
    val ef = EndpointFactory[IO]
    val foo = ef.jsonPost[Foo, Bar](
      "foo",
      foo =>
        IO.fromOption(foo.x.toIntOption)(
          new IllegalStateException("Input must be convertible to an integer")
        ).map(Bar(_))
    )

    val endpoints = foo :: Nil

    // config
    val config = Config("localhost", 8090, 12)

    // server
    val httpServer = Server[IO](config, endpoints, None, None)

    httpServer.run
