import sbt.Keys.libraryDependencies

name := "swordfish"

version := "0.1"

scalaVersion := "2.13.4"
val akkaVersion = "2.6.10"

scalacOptions ++= Seq("-language:postfixOps")

libraryDependencies += "org.scala-lang.modules" %% "scala-parser-combinators" % "1.1.2"
libraryDependencies += "org.scalactic" %% "scalactic" % "3.2.2"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
  "com.typesafe.akka" %% "akka-actor-testkit-typed" % akkaVersion % Test,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
)
libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.2" % "test"
libraryDependencies += "org.opendaylight.odlparent" % "odl-apache-commons-codec" % "8.0.2"


