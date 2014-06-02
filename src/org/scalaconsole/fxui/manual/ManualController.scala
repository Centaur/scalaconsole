package org.scalaconsole.fxui.manual
import javafx.stage.Stage
import javafx.fxml.FXML
import javafx.event.ActionEvent
import javafx.scene.input.{KeyCode, KeyEvent}
import KeyCode._

trait ManualController { self: ManualStage =>
  @FXML  def onOK(e: ActionEvent) {
    try {
      val artifactString = s"${groupId.getText.trim}:${artifactId.getText.trim}:${version.getText.trim}"
      mainStage.addArtifacts(artifactString :: Nil)
    } catch {
      case e: Throwable => e.printStackTrace()
    }
    close()
  }

  @FXML  def onCancel(e: ActionEvent) {
    close()
  }

  @FXML  def onKeyUp(e: KeyEvent) {
    e.getCode match {
      case ENTER =>
        if (!(groupId.getText.isEmpty || artifactId.getText.isEmpty || version.getText.isEmpty)) onOK(null)
      case ESCAPE =>
        close()
      case _ =>
    }
  }
}
