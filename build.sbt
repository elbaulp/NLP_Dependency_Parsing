name := "grado_informatica_TFG_NaturalLanguageProcessing"

version := "1.0"

scalaVersion := "2.11.8"

scalacOptions := Seq(
  "-encoding", "UTF-8", "-optimise",
  "-deprecation", "-unchecked", "-feature", "-Xlint", "-Ywarn-infer-any")

javacOptions ++= Seq("-Xlint:unchecked", "-Xlint:deprecation")