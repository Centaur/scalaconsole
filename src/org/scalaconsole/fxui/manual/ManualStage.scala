package org.scalaconsole.fxui.manual

import javafx.fxml.FXML
import javafx.scene.control.TextField
import javafx.stage.Stage

import org.scalaconsole.fxui.FxUtil
import org.scalaconsole.fxui.main.MainStage

class ManualStage(val mainStage: MainStage) extends Stage with ManualController{
  @FXML  var groupId: TextField = _
  @FXML  var artifactId: TextField = _
  @FXML  var version: TextField = _

  setScene(FxUtil.loadScene("/org/scalaconsole/fxui/manual/ManualStage.fxml", controller = this))
  setTitle("Add Artifact Manually")

}
