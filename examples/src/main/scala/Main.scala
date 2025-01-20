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

import cats.effect.ExitCode
import cats.effect.IO
import com.monovore.decline.Opts
import tldev.core.MyIOApp

object Main extends MyIOApp.CommandIOApp("Examples", "Examples"):

  override def runMain: Opts[IO[ExitCode]] =

    Opts
      .option[Int]("example", "example number")
      .withDefault(1)
      .map {
        case 1 => Example1.run
        case 2 => Example2.run
        case 3 => Example3.run
      }
      .map(_.as(ExitCode.Success))
