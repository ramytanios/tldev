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

import cats.syntax.all.*
import org.typelevel.literally.Literally

import java.time.LocalDate

object literals:
  extension (inline ctx: StringContext)
    inline def date(inline args: Any*): LocalDate =
      ${ DateLiteral('ctx, 'args) }

  object DateLiteral extends Literally[LocalDate]:
    def validate(s: String)(using Quotes) =
      Either
        .catchNonFatal(LocalDate.parse(s))
        .leftMap: err =>
          s"Failed to parse string to `java.time.LocalDate`: $err"
        .map: date =>
          '{ LocalDate.parse(${ Expr(s) }) }
