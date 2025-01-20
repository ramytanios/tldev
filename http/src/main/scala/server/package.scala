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

package tldev.http

import cats.MonadThrow
import cats.syntax.all.*
import tldev.core.EnvProvider

package object server:

  enum ev:
    case BE_HOST, BE_PORT, BE_MAX_CONNECTIONS, BE_API_PREFIX

  object ev:
    given Conversion[ev, String] with
      override def apply(x: ev): String = x.toString

  // expects `BE_HOST`, `BE_PORT`, `BE_MAX_CONNECTIONS` and `BE_API_PREFIX`
  // environment variables to be set
  def configFromEnv[F[_]: MonadThrow](using
      env: EnvProvider[F]
  ): F[Either[EnvProvider.Error, Config]] =
    (
      env.get[String](ev.BE_HOST),
      env.get[Int](ev.BE_PORT),
      env.get[Int](ev.BE_MAX_CONNECTIONS),
      env.get[String](ev.BE_API_PREFIX)
    ).tupled.map(
      (
          host,
          port,
          maxConnections,
          prefix
      ) =>
        (
          host,
          port,
          maxConnections,
          prefix match
            case Left(EnvProvider.Error.MissingEnv(_)) =>
              Option.empty[String].asRight[EnvProvider.Error]
            case Left(s @ EnvProvider.Error.ParsingError(msg)) =>
              s.asLeft[Option[String]]
            case Right(v) => Option(v).asRight[EnvProvider.Error]
        ).tupled.map(Config.apply)
    )
