name := """calendar sync"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"
resolvers += "google-api-services" at "http://google-api-client-libraries.appspot.com/mavenrepo"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  "org.postgresql" % "postgresql" % "9.4-1201-jdbc41",
  ws,
  "org.apache.httpcomponents" % "httpclient" % "4.5.1",
  "com.google.api-client" % "google-api-client" % "1.20.0",
  "com.google.apis" % "google-api-services-calendar" % "v3-rev145-1.20.0",
  "com.google.oauth-client" % "google-oauth-client" % "1.20.0"
)

libraryDependencies <+= scalaVersion("org.scala-lang" % "scala-compiler" % _ )
