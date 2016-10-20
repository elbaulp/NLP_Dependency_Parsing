name := "grado_informatica_TFG_NaturalLanguageProcessing"

version := "1.0"

scalaVersion := "2.11.8"

scalacOptions := Seq(
  "-encoding", "UTF-8", "-optimise",
  "-deprecation", "-unchecked", "-feature", "-Xlint", "-Ywarn-infer-any")

libraryDependencies += "com.datumbox" % "libsvm" % "3.21"

javacOptions ++= Seq("-Xlint:unchecked", "-Xlint:deprecation", "-Xmx10g")