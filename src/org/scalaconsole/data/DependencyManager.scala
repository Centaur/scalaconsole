package org.scalaconsole
package data

import java.lang.String
import collection.mutable.{ListBuffer, Buffer}
import ui.MainMenuBar
import swing.ComboBox
import javax.swing.JOptionPane
import java.io.{BufferedReader, FileReader, FilenameFilter, ObjectInputStream, FileInputStream, ObjectOutputStream, FileWriter, BufferedWriter, FileOutputStream, File}

/**
 * Created by IntelliJ IDEA.
 * User: xf
 * Date: 11-12-8
 * Time: 下午5:52
 */

object DependencyManager {
  def addArtifacts(artifacts: ListBuffer[Artifact]) = {
    artifacts.exists {
      addArtifact(_)
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

  def currentPaths = dependencies.get(version).getOrElse(Dependencies()).paths

  def currentArtifacts = dependencies.get(version).getOrElse(Dependencies()).artifacts

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

  def version = ScalaConsole.currentScalaVersion

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

  def boundedExtraClasspath(ver: String): Buffer[String] = {
    val dep = dependencies.get(ver).getOrElse(Dependencies())
    dep.paths ++ dep.artifacts.flatMap {
      case Artifact(gid, aid, v) => net.EmbeddedIvy.resolve(gid, aid, v).map(_.getAbsolutePath)
    }
  }

  val dependencies = collection.mutable.Map[String, Dependencies](SupportedScalaVersions.keys.toSeq.map(_ -> Dependencies()): _*)

  def saveCurrentAsProfile(name: String) {
    dependencies.get(version).map {dep =>
      val target = proFile(name, version)
      if (!target.exists() || JOptionPane.showConfirmDialog(null,
        "Profile %s for %s exists. Overwrite?".format(name, ScalaConsole.currentScalaVersion),
        "Confirm overwrite", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
        val writer = new BufferedWriter(new FileWriter(target))
        writer.write(dep.serialize)
        writer.close()
        JOptionPane.showMessageDialog(null, "Profile %s for Scala %s saved.".format(name, ScalaConsole.currentScalaVersion))
      }
    } orElse {
      JOptionPane.showMessageDialog(null, "No Dependencies configured.")
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
    ui.Actions.resetReplAction.apply()
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