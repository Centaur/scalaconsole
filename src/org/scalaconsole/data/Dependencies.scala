package org.scalaconsole.data

import java.io.BufferedReader

import scala.collection.mutable

case class Dependencies(paths: mutable.Buffer[String] = mutable.Buffer.empty[String],
                        artifacts: mutable.Buffer[Artifact] = mutable.Buffer.empty[Artifact]) {
  def mkString =
    ("[Paths]" :: paths.toList) ::: ("[Artifacts]" :: artifacts.map(_.mkString).toList) mkString "\n"

  def serialize = mkString
}

object Dependencies {
  val READ_PATH      = 1
  val READ_ARTIFACTS = 2

  /**
   * Empty line or EOF as end
   */
  def deserialize(reader: BufferedReader) = {
    var line = reader.readLine()
    var state = 0
    val dep = Dependencies()
    while (line != null && !line.trim.isEmpty) {
      line match {
        case "[Paths]" => state = READ_PATH
        case "[Artifacts]" => state = READ_ARTIFACTS
        case _ =>
          state match {
            case READ_PATH => dep.paths.append(line)
            case READ_ARTIFACTS => Artifact(line).foreach(dep.artifacts.append(_))
            case _ =>
          }
      }
      line = reader.readLine()
    }
    dep
  }

}

case class Artifact(groupId: String, artifactId: String, version: String) {
  def mkString = List(groupId, artifactId, version).mkString(" : ")

  override def toString = mkString

  def contains(str: String) = groupId.contains(str) || artifactId.contains(str) || version.contains(str)
}

object Artifact {
  def apply(line: String): Option[Artifact] = line.split(":") match {
    case Array("", _, _) | Array(_, "", _) | Array(_, _, "") => None
    case Array(a1, a2, a3) => Some(Artifact(a1.trim, a2.trim, a3.trim))
  }
}