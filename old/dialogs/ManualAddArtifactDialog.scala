package org.scalaconsole
package ui.dialogs

/**
 * Created by IntelliJ IDEA.
 * User: xf
 * Date: 11-12-12
 * Time: 下午2:37
 */

import swing._
import collection.mutable.ListBuffer
import event.ButtonClicked
import java.awt.event.{KeyEvent, ActionEvent, ActionListener}
import javax.swing.{FocusManager, KeyStroke, JComponent}

class ManualAddArtifactDialog(parent: Window) extends Dialog(parent) with Cancelable {dialog =>
  title = "Manually Add Artifacts"
  modal = true
  val artifacts = new ListBuffer[data.Artifact]

  def oneLine = new BoxPanel(Orientation.Horizontal) {
    contents +=
      new Label("groupId:") +=
      new TextField(10) +=
      new Label("artifactId:") +=
      new TextField(10) +=
      new Label("version:") +=
      new TextField(10)
  }

  val lines = ListBuffer(oneLine)

  object anotherBtn extends Button("Another")

  object okBtn extends Button("OK")

  object cancelBtn extends Button("Cancel")

  def onOK() {
    for (line <- lines) {
      val c = line.contents
      artifacts append data.Artifact(c(1).asInstanceOf[TextField].text, c(3).asInstanceOf[TextField].text, c(5).asInstanceOf[TextField].text)
    }
    dialog.dispose()
    data.ClassLoaderManager.reset()
    if (data.DependencyManager.addArtifacts(artifacts))
      ui.Actions.resetReplAction.apply()
  }

  def onAnother() {
    lines += oneLine
    verticalBox.contents += lines.last
    dialog.pack()
  }

  reactions += {
    case ButtonClicked(`anotherBtn`) => onAnother()
    case ButtonClicked(`okBtn`) => onOK()
    case ButtonClicked(`cancelBtn`) => onCancel()
  }

  val verticalBox = new BoxPanel(Orientation.Vertical) {
    contents += lines.last
  }

  contents = new BorderPanel() {
    add(verticalBox, BorderPanel.Position.Center)
    val btnFlow = new FlowPanel(FlowPanel.Alignment.Right)(
      anotherBtn,
      okBtn,
      cancelBtn
    )
    add(btnFlow, BorderPanel.Position.South)

    btnFlow.contents.foreach {dialog.listenTo(_)}
  }

  peer.getRootPane.registerKeyboardAction(new ActionListener() {
    def actionPerformed(p1: ActionEvent) {
      FocusManager.getCurrentManager.getFocusOwner match {
        case `cancelBtn`.peer => onCancel()
        case `anotherBtn`.peer => onAnother()
        case _ => onOK()
      }
    }
  }, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), JComponent.WHEN_IN_FOCUSED_WINDOW)

}