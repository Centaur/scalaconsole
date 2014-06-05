import AssemblyKeys._

organization := "org.scalaconsole"

name := "ScalaConsole"

version := "2.0.0-M4"

scalaVersion := "2.11.1"

scalaSource in Compile := baseDirectory.value / "src"

scalaSource in Test := baseDirectory.value / "test"

javaSource in Compile := baseDirectory.value / "src"

unmanagedResourceDirectories in Compile += baseDirectory.value / "resources"

fork := true

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")

incOptions := incOptions.value.withNameHashing(true)

//mainResourcesPath := "src"
//
// testScalaSourcePath := "test"
//
//testResourcesPath := "test"
//
//managedDependencyPath := "lib"
//
////  override def mainSources = super.mainSources --- ("src" ** "CommaSeperatedData.scala")
//mainResources := super.mainResources --- ("src" ** ("*.scala" | "*.java"))
//
//scalacOptions += "-optimise"
//

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies ++= Seq(
  "org.apache.ivy" % "ivy" % "2.3.0",
  "org.scala-lang" % "scala-compiler" % scalaVersion.value,
  "org.controlsfx" % "controlsfx" % "8.0.5",
  "com.google.code.gson" % "gson" % "2.2.4",
  "org.specs2" %% "specs2" % "2.3.11" % "test"
)


assemblySettings

mergeStrategy in assembly :=  {
  case PathList("org", "scalaconsole", "fxui", "main", "ace-builds", sub, xs@_*) if sub != "src-min-noconflict" => MergeStrategy.discard
  case x => (mergeStrategy in assembly).value.apply(x)
}



