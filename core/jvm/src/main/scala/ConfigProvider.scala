package tldev

import cats.effect.kernel.Sync
import cats.syntax.all.*
import pureconfig.ConfigReader
import pureconfig.ConfigSource
import pureconfig.module.catseffect.syntax.*
import pureconfig.module.yaml.*

import scala.reflect.ClassTag

trait ConfigProvider[F[_], C]:
  /** expects `application.conf` in `resources` directory */
  def load: F[C]

object ConfigProvider:

  def hocon[F[_]: Sync, C: ConfigReader: ClassTag]: ConfigProvider[F, C] =
    new ConfigProvider[F, C]:
      override def load: F[C] = ConfigSource.default.loadF[F, C]()

  def yaml[F[_]: Sync, C: ConfigReader: ClassTag](file: String =
    "config.yml"): ConfigProvider[F, C] =
    new ConfigProvider[F, C]:
      override def load: F[C] =
        fs2.Stream.iterable(getClass.getClassLoader.getResourceAsStream(file).readAllBytes())
          .covary[F]
          .through(fs2.text.utf8.decode)
          .compile
          .foldMonoid
          .flatMap: yml =>
            YamlConfigSource.string(yml.toString).loadF[F, C]()
