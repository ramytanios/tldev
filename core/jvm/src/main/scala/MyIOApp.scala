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

package tldev.core

import cats.effect.ExitCode
import cats.effect.IO
import cats.effect.IOApp
import com.monovore.decline.Opts
import com.monovore.decline.effect as de

object MyIOApp:

  trait Simple extends IOApp.Simple:
    def runMain: IO[Unit]
    override def run: IO[Unit] = IO.println(logo) *> runMain

  abstract class CommandIOApp(val name: String, val header: String)
      extends de.CommandIOApp(name, header):
    def runMain: Opts[IO[ExitCode]]
    override def main: Opts[IO[ExitCode]] = runMain.map(IO.println(logo) *> _)
