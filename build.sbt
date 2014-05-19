name := "common-spray"

organization := "com.blinkbox.books"

version := "0.1.0"

scalaVersion  := "2.10.4"

scalacOptions := Seq("-unchecked", "-deprecation", "-feature", "-encoding", "utf8", "-target:jvm-1.7")

resolvers += "spray" at "http://repo.spray.io/"

libraryDependencies ++= {
  val akkaV = "2.3.2"
  val sprayV = "1.3.1"
  val json4sV = "3.2.9"
  Seq(
    "io.spray"            %   "spray-can"       % sprayV,
    "io.spray"            %   "spray-routing"   % sprayV,
    "io.spray"            %%  "spray-json"      % "1.2.6",
    "io.spray"            %   "spray-testkit"   % sprayV  % "test",
    "com.typesafe.akka"   %%  "akka-actor"      % akkaV,
    "com.typesafe.akka"   %%  "akka-testkit"    % akkaV   % "test",
    "org.scalatest"       %%  "scalatest"       % "2.1.4" % "test",
    "org.json4s"          %%  "json4s-native"   % json4sV,
    "org.mockito"         %   "mockito-core"    % "1.9.5" % "test",
    "junit"               %   "junit"           % "4.11" % "test",
    "com.novocode"        %   "junit-interface" % "0.10" % "test"
  )
}
