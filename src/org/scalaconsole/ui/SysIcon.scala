package org.scalaconsole
package ui

import java.awt.event.{MouseEvent, MouseAdapter}
import java.awt.{SystemTray, Toolkit, TrayIcon}

object SysIcon {
  val icon = Toolkit.getDefaultToolkit.createImage(getClass.getResource("/scala.ico"))

  val supported = SystemTray.isSupported && !isMac

  def init() {
    if (supported) {
      val trayIcon = new TrayIcon(icon, "ScalaConsole")
      SystemTray.getSystemTray.add(trayIcon)
      trayIcon.addMouseListener(new MouseAdapter() {
        override def mouseClicked(e: MouseEvent) {
          if (e.getClickCount == 2) {
            val main = ScalaConsole.top
            main.visible = !main.visible
            if ((main.peer.getExtendedState & java.awt.Frame.ICONIFIED) != 0) main.uniconify()
          }
        }
      })
    }
  }

}