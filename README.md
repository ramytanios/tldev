# tldev

A set of utils for developing microservices using the [typelevel](https://typelevel.org/) stack.
The utilities provided are designed for use cases of mine. For more advanced features, 
use directly the relevant libraries!

> [!NOTE]
> Currently only supports scala 3. You can try a [snapshot](https://github.com/ramytanios/tldev/packages)

> [!IMPORTANT]
> Unauthorized access to the Github Registry is currently not possible (See Github [discussion](https://github.com/orgs/community/discussions/26634)). The preferred method 
> is to generate a [Personal Access Token](https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/managing-your-personal-access-tokens) with `read:packages` permissions and set it as an 
> environment variable, i.e: `GITHUB_TOKEN`.

Add to `build.sbt`:
```scala
Global / resolvers += "GitHub Package Registry" at "https://maven.pkg.github.com/ramytanios/tldev"
ThisBuild / credentials += Credentials(
  "GitHub Package Registry",
  "maven.pkg.github.com",
  "<YOUR_GITHUB_USERNAME>",
  sys.env.getOrElse("GITHUB_TOKEN", "")
)
```

```scala
"io.github.ramytanios" %% "tldev-http" % <VERSION>
"io.github.ramytanios" %% "tldev-core" % <VERSION>
```

## Examples

Try the examples by running `examples/run` in sbt.
