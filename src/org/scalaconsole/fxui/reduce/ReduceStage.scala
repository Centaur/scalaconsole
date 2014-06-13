package org.scalaconsole.fxui.reduce

import javafx.fxml.FXML
import javafx.scene.control.ListView
import javafx.stage.Stage

import org.scalaconsole.fxui.FxUtil
import org.scalaconsole.fxui.main.MainStage

class ReduceStage(val mainDelegate: MainStage) extends Stage with ReduceController {

  @FXML var artifactList: ListView[String] = _

  setScene(FxUtil.loadScene("/org/scalaconsole/fxui/reduce/ReduceStage.fxml", controller = this))
  setTitle("Reduce Dependency")
}