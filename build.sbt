name := "grado_informatica_TFG_NaturalLanguageProcessing"

version := "1.0"

scalaVersion := "2.11.8"

scalacOptions := Seq(
  "-encoding", "UTF-8", "-optimise",
  "-deprecation", "-unchecked", "-feature", "-Xlint", "-Ywarn-infer-any")

libraryDependencies ++= Seq(
  "com.datumbox" % "libsvm" % "3.21",
  "org.log4s" %% "log4s" % "1.3.2",
  "ch.qos.logback" % "logback-classic" % "1.0.13"
)

javacOptions ++= Seq("-Xlint:unchecked", "-Xlint:deprecation", "-Xmx10g")