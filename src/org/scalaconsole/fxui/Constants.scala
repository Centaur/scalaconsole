package org.scalaconsole
package fxui
import scala.util.Properties

object Constants {
  val PluginXML = "scalac-plugin.xml"

  private val hostVersion = util.Properties.versionString.split(" ")(1)
  val SupportedScalaVersions = Map(
    hostVersion -> hostVersion
  )

  val originScalaVersionNumber = Properties.scalaPropOrEmpty("version.number")
  val originScalaVersion = SupportedScalaVersions(originScalaVersionNumber)

}
