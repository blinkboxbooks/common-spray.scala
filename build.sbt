lazy val root = (project in file(".")).
  settings(
    name := "common-spray",
    organization := "com.blinkbox.books",
    version := scala.util.Try(scala.io.Source.fromFile("VERSION").mkString.trim).getOrElse("0.0.0"),
    scalaVersion := "2.11.4",
    crossScalaVersions := Seq("2.11.4"),
    scalacOptions := Seq("-unchecked", "-deprecation", "-feature", "-encoding", "utf8", "-target:jvm-1.7", "-Xfatal-warnings", "-Xfuture"),
    resolvers += "spray" at "http://repo.spray.io/",
    libraryDependencies ++= {
      val akkaV = "2.3.7"
      val sprayV = "1.3.2"
      val metricsV = "3.1.0"
      Seq(
        "io.spray"              %%  "spray-can"             % sprayV,
        "io.spray"              %%  "spray-routing"         % sprayV,
        "com.typesafe.akka"     %%  "akka-actor"            % akkaV,
        "com.typesafe.akka"     %%  "akka-slf4j"            % akkaV,
        "io.dropwizard.metrics" %   "metrics-core"          % metricsV,
        "io.dropwizard.metrics" %   "metrics-healthchecks"  % metricsV,
        "io.dropwizard.metrics" %   "metrics-json"          % metricsV,
        "io.dropwizard.metrics" %   "metrics-jvm"           % metricsV,
        "com.blinkbox.books"    %%  "common-lang"           % "0.2.1",
        "com.blinkbox.books"    %%  "common-config"         % "2.1.0",
        "com.blinkbox.books"    %%  "common-json"           % "0.2.5",
        "com.blinkbox.books"    %%  "common-scala-test"     % "0.3.0"   % Test,
        "io.spray"              %%  "spray-testkit"         % sprayV    % Test,
        "com.typesafe.akka"     %%  "akka-testkit"          % akkaV     % Test
      )
    }
  )
