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

import cats.MonadThrow
import cats.syntax.all.*
import tldev.core.EnvProvider

package object postgres:

  enum ev:
    case PG_HOST, PG_PORT, PG_USER, PG_PASS, PG_URL, PG_MAX_CONNECTIONS

  object ev:
    given Conversion[ev, String] with
      override def apply(x: ev): String = x.toString

  // expects `PG_HOST`, `PG_PORT`, `PG_USER`, `PG_PASS`, `PG_URL` and `PG_MAX_CONNECTIONS`
  // environment variables to be set
  def configFromEnv[F[_]: MonadThrow](using
      env: EnvProvider[F]
  ): F[Either[EnvProvider.Error, Config]] =
    for
      host           <- env.get[String](ev.PG_HOST)
      port           <- env.get[Int](ev.PG_PORT)
      user           <- env.get[String](ev.PG_USER)
      pass           <- env.get[String](ev.PG_PASS)
      db             <- env.get[String](ev.PG_URL)
      maxConnections <- env.get[Int](ev.PG_MAX_CONNECTIONS)
    yield (host, port, user, pass, db, maxConnections).tupled.map(Config.apply)
