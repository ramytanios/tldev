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

package tldev

import cats.effect.kernel.Temporal
import cats.syntax.all.*
import org.typelevel.log4cats.Logger

import scala.concurrent.duration.*

package object core:

  def logo = """
      | ____ __   ____ ____ _  _ 
      |(_  _(  ) (    (  __/ )( \
      |  )( / (_/\) D () _)\ \/ /
      | (__)\____(____(____)\__/
      | 
      | Using tldev
    """.stripMargin

  def logProgress[F[_]: Temporal: Logger, V](
      label: String,
      totalElements: Option[Long] = None,
      frequency: FiniteDuration = 3.seconds
  ): fs2.Pipe[F, V, V] =
    (in: fs2.Stream[F, V]) =>
      fs2.Stream
        .eval(Logger[F].info(s"$label: starting ..."))
        .evalMap(_ => Temporal[F].ref[Long](0L))
        .flatMap: counter =>
          in.chunks
            .evalTap: chunk =>
              counter.update(_ + chunk.size)
            .unchunks
            .concurrently(
              fs2.Stream
                .fixedDelay(frequency)
                .evalTap: _ =>
                  counter.get.flatMap: k =>
                    Logger[F].info(
                      totalElements.fold(s"$label: $k")(n => s"$label: $k/$n")
                    )
            ) ++ fs2.Stream.exec(Logger[F].info(s"$label: done!"))
