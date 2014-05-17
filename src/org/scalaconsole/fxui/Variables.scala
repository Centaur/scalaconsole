package org.scalaconsole.fxui
import javafx.scene.text.Font

object Variables {
  def decodeFont(desc: String) = {
    val Array(family, size) = desc.split("-")
    Font.font(family, size.toDouble)
  }
  var displayFont = decodeFont(System.getProperty("font", "Monospaced-12"))
}
