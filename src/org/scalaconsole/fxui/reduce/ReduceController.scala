package org.scalaconsole.fxui.reduce
import javafx.collections.FXCollections
import org.scalaconsole.data.DependencyManager
import javafx.event.ActionEvent
import javafx.fxml.FXML
import org.scalaconsole.fxui.{FxUtil, SelectedVersionCell}
import javafx.scene.control.ListView

import FxUtil._
import javafx.scene.input.{KeyCode, KeyEvent}
import KeyCode._

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
