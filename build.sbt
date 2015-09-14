name := "sekoor-client"
 
version := "1.0"

organization := "com.sekoor"
 
scalaVersion := "2.11.6"

scalacOptions ++= Seq("-deprecation", "-feature")
 
resolvers ++= Seq(
  //  "Typesafe Repository" at "https://repo.typesafe.com/typesafe/releases/",
  "Sonatype OSS Releases" at "https://oss.sonatype.org/content/repositories/releases",
  "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
  "Scala SBT releases" at "https://repo.scala-sbt.org/scalasbt/sbt-plugin-releases"
)
 
libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "" % "test",
  "org.mockito" % "mockito-core" % "1.8.5" % "test",
  "com.typesafe" % "config" % "1.2.1",
  "ch.qos.logback" % "logback-classic" % "1.0.3",
  "com.typesafe.scala-logging" %% "scala-logging-slf4j" % "2.1.2"
)

