name := "Spanish Dep Parser"

version := "1.0"

scalaVersion := "2.12.0"

scalacOptions := Seq(
  "-encoding", "UTF-8", "-opt:l:classpath",
  "-deprecation", "-unchecked", "-feature", "-Xlint", "-Ywarn-infer-any")

libraryDependencies ++= Seq(
  "com.datumbox" % "libsvm" % "3.21",
  "org.log4s" %% "log4s" % "latest.release",
  "ch.qos.logback" % "logback-classic" % "latest.release",
  "com.softwaremill.quicklens" %% "quicklens" % "latest.release"
)

javacOptions ++= Seq("-Xlint:unchecked", "-Xlint:deprecation", "-Xmx10g")