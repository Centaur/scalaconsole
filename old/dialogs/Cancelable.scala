package org.scalaconsole.ui.dialogs

import java.awt.event.{KeyEvent, ActionEvent, ActionListener}
import javax.swing.{JComponent, KeyStroke}
import swing.Dialog

/**
 * Created by IntelliJ IDEA.
 * User: xf
 * Date: 11-12-9
 * Time: 下午2:07
 */

trait Cancelable { self: Dialog =>
  peer.getRootPane.registerKeyboardAction(new ActionListener() {
    def actionPerformed(p1: ActionEvent) {
      onCancel()
    }
  }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW)

  def onCancel() {
    dispose()
  }

}