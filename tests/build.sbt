organization := "io.scrapeyard"

name := "tests"

version := "0.1.0-SNAPSHOT"

scalaVersion := "2.11.6"

val scalaCompiler = "org.scala-lang" % "scala-compiler" % scalaVersion.toString

libraryDependencies ++=
  Seq(
    "org.scalatest" % "scalatest_2.11" % "2.2.2" % "test",
    "io.scrapeyard" %% "scrapeyard" % "0.1.0-SNAPSHOT" % "test",
    "me.lessis"               % "courier_2.11"      % "0.1.3" % "test",
    "io.spray"      % "spray-client_2.11" % "1.3.2"
  )
    