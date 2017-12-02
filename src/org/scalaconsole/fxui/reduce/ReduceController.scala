package org.scalaconsole.fxui.reduce
import javafx.collections.FXCollections
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.scene.control.ListView
import javafx.scene.input.KeyCode._
import javafx.scene.input.KeyEvent

import org.scalaconsole.data.DependencyManager
import org.scalaconsole.fxui.SelectedVersionCell

trait ReduceController { self: ReduceStage =>
  @FXML def onOK(event: ActionEvent) {
    import collection.JavaConverters._
    mainDelegate.updateArtifacts(artifactList.getItems.asScala)
    close()
  }

  @FXML def onCancel(event: ActionEvent) {
    close()
  }

  @FXML def initialize():Unit = {
    artifactList.setCellFactory((_: ListView[String]) => new SelectedVersionCell())
    artifactList.setItems(FXCollections.observableArrayList(DependencyManager.currentArtifactsAsJavaCollection))
  }

  @FXML def onKeyUp(e: KeyEvent) {
    e.getCode match {
      case ESCAPE => close()
      case _ =>
    }
  }
}
