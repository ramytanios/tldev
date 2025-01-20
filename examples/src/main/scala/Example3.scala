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
import tldev.core.ProgressReporter

import scala.concurrent.duration.*

object Example3:

  def run: IO[Unit] =
    cats.effect.std.Queue
      .unbounded[IO, String]
      .flatMap: logQueue =>
        ProgressReporter
          .toQueue[IO]("my_label", 1.seconds, logQueue)
          .use: reporter =>
            fs2.Stream
              .fixedDelay[IO](3.seconds)
              .zipWithIndex
              .map((_, idx) => idx.toDouble)
              .evalTap(reporter.setProgress(_))
              .takeWhile(_ <= 100)
              .concurrently:
                fs2.Stream.fromQueueUnterminated(logQueue).printlns
              .compile
              .drain
