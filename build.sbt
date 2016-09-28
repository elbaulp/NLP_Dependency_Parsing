name := "grado_informatica_TFG_NaturalLanguageProcessing"

version := "1.0"

libraryDependencies  ++= Seq(
  // Last stable release
  "org.scalanlp" %% "breeze" % "0.12"

  // Native libraries are not included by default. add this if you want them (as of 0.7)
  // Native libraries greatly improve performance, but increase jar sizes.
  // It also packages various blas implementations, which have licenses that may or may not
  // be compatible with the Apache License. No GPL code, as best I know.
  //"org.scalanlp" %% "breeze-natives" % "0.12"

  // The visualization library is distributed separately as well.
  // It depends on LGPL code
//  "org.scalanlp" %% "breeze-viz" % "0.12"
)


resolvers += "Sonatype Releases" at "https://oss.sonatype.org/content/repositories/releases/"

scalaVersion := "2.11.8"