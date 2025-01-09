# tldev

A set of utils for developing microservices using the [typelevel](https://typelevel.org/) stack.
The utilities provided are designed for use cases of mine. For more advanced features, 
use directly the relevant libraries!

> [!NOTE]
> Currently only supports scala 3. You can try a [snapshot](https://github.com/ramytanios/tldev/packages)

> [!IMPORTANT]
> Unauthorized access to the Github Registry is not currently possible. The preferred method 
> is to generate a Personal Access Token with `read:packages` permissions and set it as an 
> environment variable `GITHUB_TOKEN`.
```scala
Global / resolvers += "GitHub Package Registry" at "https://maven.pkg.github.com/ramytanios/tldev"
ThisBuild / credentials += Credentials(
  "GitHub Package Registry",
  "maven.pkg.github.com",
  "<GITHUB_USERNAME>",
  sys.env.getOrElse("GITHUB_TOKEN", "")
)
```

```scala
"io.github.ramytanios" %% "tldev-http" % <VERSION>
"io.github.ramytanios" %% "tldev-core" % <VERSION>
```

## Examples

Try the examples by running `examples/run` in sbt.
