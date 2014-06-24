package org.scalaconsole
package fxui
import scala.util.Properties

object Constants {
  val PluginXML = "scalac-plugin.xml"

  val isWindows = System.getProperty("os.name").toLowerCase.contains("windows")

  private val hostVersion = util.Properties.versionString.split(" ")(1)
  val SupportedScalaVersions = Map(
    hostVersion -> hostVersion
  )

  val originScalaVersionNumber = Properties.scalaPropOrEmpty("version.number")
  val originScalaVersion = SupportedScalaVersions(originScalaVersionNumber)

  val defaultFont = {
    val osname = System.getProperty("os.name").toLowerCase
    if(osname.contains("mac")) "Menlo-14"
    else if(osname.contains("linux")) "Dejavu Sans Mono-13"
    else if(osname.contains("windows")) "Consolas-14"
    else "Monospaced-13"
  }

}
