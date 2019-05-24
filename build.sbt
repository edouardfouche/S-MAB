name := "S-MAB"

organization:= "com.edouardfouche"

version := "1.0"
//scalaVersion := "2.12.7"
//scalaVersion := "2.11.8"
scalaVersion := "2.12.8"
fork in run := true

//javaOptions += "-Xmx126G"
//javaOptions += "-Xms10G"
//javaOptions += "-XX:-UseGCOverheadLimit" // may lead to some problems

scalacOptions ++= Seq("-feature")

libraryDependencies += "de.lmu.ifi.dbs.elki" % "elki" % "0.7.5"

libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.1" % "test"

libraryDependencies ++= Seq(
  // Last stable release
  "org.scalanlp" %% "breeze" % "0.13.1",

  // Native libraries are not included by default. add this if you want them (as of 0.7)
  // Native libraries greatly improve performance, but increase jar sizes.
  // It also packages various blas implementations, which have licenses that may or may not
  // be compatible with the Apache License. No GPL code, as best I know.
  "org.scalanlp" %% "breeze-natives" % "0.13.1"

  // The visualization library is distributed separately as well.
  // It depends on LGPL code
  // "org.scalanlp" %% "breeze-viz" % "0.13.1"
)

//libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0"
//libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.1.2"
libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2"
libraryDependencies += "ch.qos.logback" % "logback-classic" %  "1.1.2"//"1.2.3" // this is for the logging backend
// Note: from logback 1.1.5, threads do not inherit the MDC anymore q

//resolvers += "Jzy3d Maven Release Repository" at "http://maven.jzy3d.org/releases"
//libraryDependencies += "org.jzy3d" % "jzy3d-api" % "1.0.0" // libraryDependencies += "org.jzy3d" % "jzy3d-api" % "0.9.1"
//resolvers += "Sonatype OSS Releases" at "http://oss.sonatype.org/content/repositories/releases/"

resolvers += "Sonatype Releases" at "https://oss.sonatype.org/content/repositories/releases/"


//assemblySettings

//import sbtassembly.Plugin.AssemblyKeys._
assemblyJarName in assembly := s"${name.value}-${version.value}.jar"
test in assembly := {}

// No working somehow, leads to required parts of ELKI not being included in the assembly
//mergeStrategy in assembly := {
//  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
//  case x => MergeStrategy.first
//}

// Avoid that elki collide with org/apache/commons/io somehow
assemblyMergeStrategy in assembly := {
  case x if x.startsWith("org/apache/commons/io") => MergeStrategy.first
  case PathList("META-INF", "elki", xs @ _*) => MergeStrategy.first
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}

javacOptions ++= Seq("-encoding", "UTF-8")

//logLevel := Level.Debug
