package org.scalaconsole.fxui.manual

import javafx.stage.Stage
import javafx.scene.{Scene, Parent}
import org.scalaconsole.fxui.main.MainStage
import javafx.scene.control.TextField
import javafx.fxml.{FXMLLoader, FXML}

class ManualStage(val mainStage: MainStage) extends Stage with ManualController{
  @FXML  var groupId: TextField = _
  @FXML  var artifactId: TextField = _
  @FXML  var version: TextField = _

  val loader = new FXMLLoader(getClass.getResource("/org/scalaconsole/fxui/manual/ManualStage.fxml"))
  loader.setController(this)
  setScene(new Scene(loader.load()))
  setTitle("Add Artifact Manually")

}
