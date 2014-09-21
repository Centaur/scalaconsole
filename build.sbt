import sbt.Package.{JarManifest, ManifestAttributes}
import sbtassembly.Plugin._
import AssemblyKeys._
import sbt._
import Keys._

organization := "org.scalaconsole"

name := "ScalaConsole"

version := "2.0.0-M11"

scalaVersion := "2.11.2"

scalaSource in Compile := baseDirectory.value / "src"

scalaSource in Test := baseDirectory.value / "test"

javaSource in Compile := baseDirectory.value / "src"

unmanagedResourceDirectories in Compile += baseDirectory.value / "resources"

fork := true

//javaOptions in run ++= Seq("-Xmx1024m", "-XX:+UseConcMarkSweepGC", "-XX:+CMSClassUnloadingEnabled")

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")

incOptions := incOptions.value.withNameHashing(nameHashing = true)

transitiveClassifiers in Global := Seq(Artifact.SourceClassifier)

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

resolvers += Resolver.typesafeRepo("releases")

libraryDependencies ++= Seq(
  "org.apache.ivy" % "ivy" % "2.3.0",
  "org.scala-lang" % "scala-compiler" % scalaVersion.value
  //    .exclude("org.scala-lang.modules", "scala-parser-combinators_2.11")
  //    .exclude("org.scala-lang.modules", "scala-xml_2.11")
  , "org.controlsfx" % "controlsfx" % "8.0.6_20"
  , "com.google.code.gson" % "gson" % "2.2.4"
  , "org.specs2" %% "specs2" % "2.3.11" % "test"
)


assemblySettings

packageOptions in assembly ++= Seq(ManifestAttributes(("Specification-Version", "8.0.20")))

mergeStrategy in assembly := {
  case str@PathList("org", "scalaconsole", "fxui", "main", "ace-builds", remains@_*) => remains match {
    case Seq(sub) => MergeStrategy.discard
    case Seq(sub, xs@_*) if sub != "src-min-noconflict" => MergeStrategy.discard
    case Seq("src-min-noconflict", mode) if mode.startsWith("mode-") && mode != "mode-scala.js" || mode.startsWith("worker-") => MergeStrategy.discard
    case Seq("src-min-noconflict", "snippets", snippet) if snippet != "scala.js" => MergeStrategy.discard
    case _ => (mergeStrategy in assembly).value.apply(str)
  }
  case x => (mergeStrategy in assembly).value.apply(x)
}

// this has the same effect as .exclude clause in libraryDependencies config, but does not rely on scala version
excludedJars in assembly <<= (fullClasspath in assembly) map { cp =>
  cp filter { item =>
    val name = item.data.getName
    (name.startsWith("scala-xml") || name.startsWith("scala-parser-combinators")) && name.endsWith(".jar")
  }
}


//wartremoverErrors ++= Warts.allBut(Wart.Var,
//  Wart.MutableDataStructures,
//  Wart.Null,
//  Wart.NonUnitStatements,
//  Wart.DefaultArguments,
//  Wart.Nothing,
//  Wart.Any,
//  Wart.AsInstanceOf,
//  Wart.IsInstanceOf
//)

