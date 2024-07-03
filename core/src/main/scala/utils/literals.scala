package tldev.core.utils

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
