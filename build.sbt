import sbt.Keys._
import sbt._

organization := "org.scalaconsole"

name := "ScalaConsole"

version := "2.0.0-M11"

scalaVersion := "2.12.4"

scalaSource in Compile := baseDirectory.value / "src"

scalaSource in Test := baseDirectory.value / "test"

javaSource in Compile := baseDirectory.value / "src"

unmanagedResourceDirectories in Compile += baseDirectory.value / "resources"

fork := true

//javaOptions in run ++= Seq("-Xmx1024m", "-XX:+UseConcMarkSweepGC", "-XX:+CMSClassUnloadingEnabled")

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature", "-Xlint")

transitiveClassifiers in Global := Seq(Artifact.SourceClassifier)

libraryDependencies ++= Seq(
  "org.apache.ivy" % "ivy" % "2.4.0",
  "org.scala-lang" % "scala-compiler" % scalaVersion.value,
  "org.glassfish" % "javax.json" % "1.0.4" % "runtime",
  "javax.json" % "javax.json-api" % "1.0",
  "org.specs2" %% "specs2-core" % "3.8.6" %  "test",
)

updateOptions := updateOptions.value.withGigahorse(false)
//packageOptions in assembly ++= Seq(ManifestAttributes(("Specification-Version", "8.0.20")))

assemblyMergeStrategy in assembly := {
  case str@PathList("org", "scalaconsole", "fxui", "main", "ace-builds", remains@_*) => remains match {
    case Seq(sub) => MergeStrategy.discard
    case Seq(sub, xs@_*) if sub != "src-min-noconflict" => MergeStrategy.discard
    case Seq("src-min-noconflict", mode) if mode.startsWith("mode-") && mode != "mode-scala.js" || mode.startsWith("worker-") => MergeStrategy.discard
    case Seq("src-min-noconflict", "snippets", snippet) if snippet != "scala.js" => MergeStrategy.discard
    case _ => (assemblyMergeStrategy in assembly).value.apply(str)
  }
  case x => (assemblyMergeStrategy in assembly).value.apply(x)
}

// this has the same effect as .exclude clause in libraryDependencies config, but does not rely on scala version
assemblyExcludedJars in assembly := (fullClasspath in assembly).value filter { item =>
    val name = item.data.getName
    (name.startsWith("scala-xml") || name.startsWith("scala-parser-combinators")) && name.endsWith(".jar")
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

