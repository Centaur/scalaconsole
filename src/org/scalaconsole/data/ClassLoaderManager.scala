package org.scalaconsole
package data

import scala.reflect.internal.util.ScalaClassLoader
import tools.nsc.Settings
import tools.util.PathResolver
import org.scalaconsole.fxui.{Constants, Variables}

object ClassLoaderManager {
  val classLoaders = collection.mutable.Map[String, (ScalaClassLoader, String)]()

  val myself = new java.io.File(getClass.getProtectionDomain.getCodeSource.getLocation.getPath)

  def forVersion(v: String) = classLoaders.get(v).getOrElse {
    val scalaLibraries = myself :: net.EmbeddedIvy.resolveScala(v) ++
      DependencyManager.boundedExtraClasspath(v).map(new java.io.File(_))
    val scalaBootPath = scalaLibraries mkString System.getProperty("path.separator")
    val newClassPath = new PathResolver(new Settings).containers.filterNot { cp =>
      ScalaCoreLibraries exists {
        cp.asClasspathString.contains
      }
    }.flatMap(_.asURLs) ++ scalaLibraries.map(_.toURI.toURL)

    val cl = new ChildFirstClassLoader(newClassPath.toArray) with ScalaClassLoader
    val result = (cl, scalaBootPath)
    classLoaders(v) = result
    (cl, scalaBootPath)
  }

  def isOrigin = Variables.currentScalaVersion == Constants.originScalaVersion

  def reset() = classLoaders.remove(Variables.currentScalaVersion)
}