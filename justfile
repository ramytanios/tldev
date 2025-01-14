@default:
    just --list

publish-local:
    sbt 'publishLocal'

doc:
    sbt 'docs/mdoc'

fmt:
    just --fmt --unstable

scalafmt:
    sbt scalafmtAll

scalafix:
    sbt 'scalafixEnable; scalafixAll'

fix:
    just scalafmt scalafix

[confirm]
clean:
    git clean -Xdf

deps:
    sbt dependencyUpdates

examples:
    sbt 'examples/run'

gen-workflow:
  sbt githubWorkflowGenerate

check-workflow:
  sbt githubWorkflowCheck

publish: 
  sbt publish

readme: 
  glow README.md
