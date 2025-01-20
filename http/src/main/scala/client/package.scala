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

import scala.concurrent.duration.FiniteDuration

package object client:

  enum ev:
    case HTTP_CLIENT_TIMEOUT

  object ev:
    given Conversion[ev, String] with
      override def apply(x: ev): String = x.toString

  // expects `HTTP_CLIENT_TIMEOUT` environment variables to be set
  def configFromEnv[F[_]: MonadThrow](using
      env: EnvProvider[F]
  ): F[Either[EnvProvider.Error, Config]] =
    env.get[FiniteDuration](ev.HTTP_CLIENT_TIMEOUT).map {
      case Left(EnvProvider.Error.MissingEnv(_)) =>
        Option.empty[FiniteDuration].asRight[EnvProvider.Error]
      case Left(s @ EnvProvider.Error.ParsingError(msg)) =>
        s.asLeft[Option[FiniteDuration]]
      case Right(v) =>
        Option(v).asRight[EnvProvider.Error]
    }.map(_.map(Config(_)))
