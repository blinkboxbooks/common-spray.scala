name := "common-spray"

organization := "com.blinkbox.books"

version := scala.util.Try(scala.io.Source.fromFile("VERSION").mkString.trim).getOrElse("0.0.0")

crossScalaVersions := Seq("2.10.4", "2.11.2")

scalacOptions := Seq("-unchecked", "-deprecation", "-feature", "-encoding", "utf8", "-target:jvm-1.7")

resolvers += "spray" at "http://repo.spray.io/"

libraryDependencies ++= {
  val akkaV = "2.3.6"
  val sprayV = "1.3.1"
  val metricsV = "3.0.2"
  Seq(
    "io.spray"              %%  "spray-can"             % sprayV,
    "io.spray"              %%  "spray-routing"         % sprayV,
    "com.typesafe.akka"     %%  "akka-actor"            % akkaV,
    "com.typesafe.akka"     %%  "akka-slf4j"            % akkaV,
    "com.codahale.metrics"  %   "metrics-core"          % metricsV,
    "com.codahale.metrics"  %   "metrics-healthchecks"  % metricsV,
    "com.codahale.metrics"  %   "metrics-json"          % metricsV,
    "com.codahale.metrics"  %   "metrics-jvm"           % metricsV,
    "com.blinkbox.books"    %%  "common-lang"           % "0.2.0",
    "com.blinkbox.books"    %%  "common-config"         % "1.4.1",
    "com.blinkbox.books"    %%  "common-json"           % "0.2.2-SNAPSHOT",
    "com.blinkbox.books"    %%  "common-scala-test"     % "0.3.0"   % "test",
    "io.spray"              %%  "spray-testkit"         % sprayV    % "test",
    "com.typesafe.akka"     %%  "akka-testkit"          % akkaV     % "test"
  )
}
