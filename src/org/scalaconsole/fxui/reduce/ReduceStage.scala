package org.scalaconsole.fxui.reduce

import javafx.stage.Stage
import org.scalaconsole.fxui.main.MainStage
import javafx.scene.Scene
import javafx.scene.control.ListView
import javafx.fxml.{FXMLLoader, FXML}

class ReduceStage(val mainDelegate: MainStage) extends Stage with ReduceController {

  @FXML var artifactList: ListView[String] = _

  val loader = new FXMLLoader(getClass.getResource("/org/scalaconsole/fxui/reduce/ReduceStage.fxml"))
  loader.setController(this)
  setScene(new Scene(loader.load()))
  setTitle("Reduce Dependency")
}