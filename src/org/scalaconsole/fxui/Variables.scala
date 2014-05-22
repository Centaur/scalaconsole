package org.scalaconsole.fxui
import javafx.scene.text.Font

object Variables {
  var commandlineOption = Option.empty[String]

  def decodeFont(desc: String) = {
    val Array(family, size) = desc.split("-")
    Font.font(family, size.toDouble)
  }
  def encodeFont(f: Font) = s"${f.getFamily}-${f.getSize.toInt}"

  val defaultFont = {
    val osname = System.getProperty("os.name").toLowerCase
    if(osname.contains("mac")) "Menlo-13"
    else if(osname.contains("linux")) "Dejavu Sans Mono-13"
    else if(osname.contains("windows")) "Consolas-14"
    else "Monospaced-13"
  }
  var displayFont = decodeFont(System.getProperty("font", defaultFont))

  var currentScalaVersion = Constants.originScalaVersion

}
