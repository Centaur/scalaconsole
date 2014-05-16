package org.scalaconsole
package ui.dialogs

import swing._
import event.{ButtonClicked, Key, KeyPressed}
import javax.swing.border.TitledBorder

/**
 * Created by IntelliJ IDEA.
 * User: xf
 * Date: 11-12-9
 * Time: 上午11:08
 */

class ManageDependenciesDialog(parent: Window) extends Dialog(parent) with Cancelable {dialog =>
  val dimension_400_200 = new swing.Dimension(400, 200)

  title = "Dependencies loaded for Scala %s".format(ScalaConsole.currentScalaVersion)
  val filterText = new TextField() {
    columns = 20
    listenTo(this.keys)
    reactions += {
      case KeyPressed(src, Key.Enter, _, _) =>
        text match {
          case "" =>
          case _ =>
            pathBox.listData = pathBox.listData.filter(_.contains(text))
            artifactBox.listData = artifactBox.listData.filter(_.contains(text))
        }
    }
  }
  val filterBox = new FlowPanel(
    new Label("Filter:"),
    filterText
  )
  val pathBox = new ListView[String]() {
    listData = data.DependencyManager.currentPaths
  }

  val artifactBox = new ListView[data.Artifact] {
    listData = data.DependencyManager.currentArtifacts
  }

  object removeBtn extends Button("Remove Selected")

  object okBtn extends Button("OK")

  object cancelBtn extends Button("Cancel")

  def onOK() {
    data.DependencyManager.replaceCurrentPaths(pathBox.listData)
    data.DependencyManager.replaceCurrentArtifacts(artifactBox.listData)
    dialog.dispose()
    data.ClassLoaderManager.reset()
    ui.Actions.resetReplAction.apply()
  }

  def onRemove() {
    pathBox.listData = pathBox.listData.toList filterNot {
      pathBox.selection.items.toList.contains
    }
    artifactBox.listData = artifactBox.listData.toList filterNot {
      artifactBox.selection.items.toList.contains
    }
  }

  val btnFlow = new FlowPanel(FlowPanel.Alignment.Right)(
    removeBtn,
    okBtn,
    cancelBtn
  )

  btnFlow.contents.foreach(listenTo(_))
  reactions += {
    case ButtonClicked(`removeBtn`) => onRemove()
    case ButtonClicked(`okBtn`) => onOK()
    case ButtonClicked(`cancelBtn`) => onCancel()
  }

  def scrollable(l: ListView[_], title: String) = new ScrollPane(l) {
    border = new TitledBorder(title)
    preferredSize = dimension_400_200
  }

  contents = new BoxPanel(Orientation.Vertical) {
    contents +=
      filterBox +=
      scrollable(pathBox, "Paths") +=
      scrollable(artifactBox, "Artifacts") +=
      btnFlow
  }

}