fmt: 
  sbt scalafmtAll

fix: 
  sbt 'scalafixEnable; scalafixAll; scalafmtAll'

clean:
  git clean -Xdf

check-deps:
  sbt dependencyUpdates

publish-local:
  sbt 'publishLocal'

javakill: 
  killall java -9
