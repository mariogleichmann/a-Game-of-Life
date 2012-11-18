import sbt._
import sbt.Keys._

object AsynchronousGameOfLifeBuild extends Build {

  lazy val asynchronousGameOfLife = Project(
    id = "asynchronous-game-of-life",
    base = file("."),
    settings = Project.defaultSettings ++ Seq(
      name := "Asynchronous Game of Life",
      organization := "org.agol",
      version := "0.1-SNAPSHOT",
      scalaVersion := "2.9.2",
      resolvers += "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases",
      libraryDependencies += "com.typesafe.akka" % "akka-actor" % "2.0.1",
	  libraryDependencies += "org.scala-lang" % "scala-swing" % "2.9.1"
    )
  )
}
