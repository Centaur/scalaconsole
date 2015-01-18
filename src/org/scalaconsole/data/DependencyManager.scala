package org.scalaconsole
package data

import java.io.{BufferedReader, BufferedWriter, File, FileReader, FileWriter, FilenameFilter}
import javafx.scene.control.Alert.AlertType
import javafx.scene.control.{Alert, ButtonType}

import org.scalaconsole.fxui.{Constants, Variables}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

/**
 * Created by IntelliJ IDEA.
 * User: xf
 * Date: 11-12-8
 * Time: 下午5:52
 */

object DependencyManager {
  def addArtifacts(artifacts: ListBuffer[Artifact]) = {
    artifacts.exists {
      addArtifact
    }
  }

  def removeAllProfiles() {
    storage.listFiles().foreach(_.delete())
  }

  def replaceCurrentArtifacts(newValues: Seq[Artifact]) {
    dependencies(version).artifacts.clear()
    dependencies(version).artifacts.appendAll(newValues)
  }

  def replaceCurrentPaths(newValues: Seq[String]) {
    dependencies(version).paths.clear()
    dependencies(version).paths.appendAll(newValues)
  }

  def currentPaths = dependencies.getOrElse(version, Dependencies()).paths

  def currentArtifacts = dependencies.getOrElse(version, Dependencies()).artifacts

  import scala.collection.JavaConverters.asJavaCollectionConverter
  def currentArtifactsAsJavaCollection = currentArtifacts.map(_.mkString).asJavaCollection

  /**
   * return : classpathNeedsReset
   */
  def addPaths(files: Array[File]): Boolean = dependencies.get(version) match {
    case None =>
      dependencies(version) = Dependencies(ListBuffer(files.map(_.getAbsolutePath): _*), ListBuffer[Artifact]())
      true
    case Some(dep) =>
      var needsReset = false
      for (file <- files) {
        if (!dep.paths.contains(file.getAbsolutePath)) {
          dep.paths.append(file.getAbsolutePath)
          needsReset = true
        }
      }
      needsReset
  }

  def version = Variables.currentScalaVersion

  /**
   * return : classpathNeedsReset
   */
  def addArtifact(artifact: Artifact): Boolean = dependencies.get(version) match {
    case None =>
      dependencies(version) = Dependencies(ListBuffer[String](), ListBuffer(artifact))
      true
    case Some(dep) =>
      if (dep.artifacts.contains(artifact))
        false
      else {
        dep.artifacts.append(artifact)
        true
      }
  }

  def boundedExtraClasspath(ver: String): mutable.Buffer[String] = {
    val dep = dependencies.getOrElse(ver, Dependencies())
    dep.paths ++ dep.artifacts.flatMap {
      case Artifact(gid, aid, v) => net.EmbeddedIvy.resolve(gid, aid, v).map(_.getAbsolutePath)
    }
  }

  val dependencies = collection.mutable.Map[String, Dependencies](Constants.SupportedScalaVersions.keys.toSeq.map(_ -> Dependencies()): _*)

  def saveCurrentAsProfile(name: String) {
    def confirmOverwrite() = {
    val dialog = new Alert(AlertType.CONFIRMATION)
      dialog.setTitle("Confirm overwrite")
      dialog.setHeaderText(null)
      dialog.setContentText(s"Profile $name for $version exists. Overwrite?")
      val result = dialog.showAndWait()
      result.get() == ButtonType.OK
    }

    dependencies.get(version).map {dep =>
      val target = proFile(name, version)
      if (!target.exists() || confirmOverwrite()) {
        val writer = new BufferedWriter(new FileWriter(target))
        writer.write(dep.serialize)
        writer.close()
        val dialog = new Alert(AlertType.INFORMATION)
        dialog.setHeaderText(null)
        dialog.setContentText(s"Profile $name for Scala $version saved.")
        dialog.showAndWait()
      }
    } orElse {
      val dialog = new Alert(AlertType.INFORMATION)
      dialog.setHeaderText(null)
      dialog.setContentText("No Dependencies configured.")
      dialog.showAndWait()
      None
    }
  }

  val storage = new File(System.getProperty("user.home") + "/.scalaconsole")
  storage.mkdirs()

  def proFile(userSpecified: String, ver: String) = new File(storage.getPath + "/" + userSpecified + "_" + ver + ".prl")

  def loadProfile(name: String) {
    val reader = new BufferedReader(new FileReader(proFile(name, version)))
    val dep = Dependencies.deserialize(reader)
    dependencies(version) = dep
    reader.close()
    data.ClassLoaderManager.reset()
  }

  def loadProfiles = {
    val suffix = "_" + version + ".prl"
    storage.list(new FilenameFilter() {
      def accept(dir: File, name: String) = name.endsWith(suffix)
    }).map {filepath =>
      filepath.slice(0, filepath.indexOfSlice(suffix))
    }
  }

}