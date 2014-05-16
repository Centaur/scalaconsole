package org.scalaconsole
package ui

import javax.imageio.ImageIO
import java.awt.{TrayIcon, SystemTray}
import java.awt.event.{MouseEvent, MouseAdapter}
import org.scalaconsole.fxui.MainStage

object SysIcon {
  val icon = ImageIO.read(getClass.getResource("/scala.ico"))

  val supported = SystemTray.isSupported && !isMac

  def init() {
    if (supported) {
      val trayIcon = new TrayIcon(icon, "ScalaConsole")
      SystemTray.getSystemTray.add(trayIcon)
      trayIcon.addMouseListener(new MouseAdapter() {
        override def mouseClicked(e: MouseEvent) {
          if (e.getClickCount == 2) {
            val main = MainStage.top
            if(main.isIconified) main.show()
            else main.setIconified(true)
          }
        }
      })
    }
  }

}