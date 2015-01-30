organization := "io.scrapeyard"

name := "scrapeyard"

version := "0.1.0-SNAPSHOT"

scalaVersion := "2.11.5"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

libraryDependencies ++= {
  val akkaV = "2.3.6"
  val sprayV = "1.3.2"
  Seq(
    "org.scalatest"           % "scalatest_2.11"    % "2.2.2",
    "org.seleniumhq.selenium" % "selenium-java"     % "2.44.0",
    "com.codeborne"           % "phantomjsdriver"   % "1.2.1",
    "com.github.nscala-time"  % "nscala-time_2.11"  % "1.6.0",
    "org.jvnet.mock-javamail" % "mock-javamail"     % "1.9" % "test",
    "me.lessis"               % "courier_2.11"      % "0.1.3",

    "io.spray"            %%  "spray-can"     % sprayV,
    "io.spray"            %%  "spray-routing" % sprayV,
    "io.spray"            %%  "spray-json"    % "1.3.1",
    "io.spray"            %%  "spray-testkit" % sprayV  % "test",
    "com.typesafe.akka"   %%  "akka-actor"    % akkaV,
    "com.typesafe.akka"   %%  "akka-testkit"  % akkaV   % "test",
    "org.specs2"          %%  "specs2-core"   % "2.3.11" % "test"

  )
}

Revolver.settings.settings

    