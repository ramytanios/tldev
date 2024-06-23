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
import io.circe.*
import io.circe.generic.semiauto.*
import io.circe.syntax.*
import tldev.http.client.*

import java.time.LocalDate

object ClientMain extends IOApp.Simple:

  case class FX(
      amount: Double,
      date: LocalDate,
      base: String,
      rates: Map[String, Double]
  )
  object FX:
    given Codec[FX] = deriveCodec[FX]

  override def run: IO[Unit] =

    val config = Config.default

    Client[IO](config).use: httpClient =>
      httpClient
        .get[FX](
          baseUrl = "https://api.frankfurter.app",
          path = Some("latest"),
          Map("from" -> "USD", "to" -> "CHF")
        )
        .flatMap: fx =>
          IO.consoleForIO.print(s"Received response ${fx.asJson.spaces2}")
