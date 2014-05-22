package org.scalaconsole.fxui.reduce

import javafx.stage.Stage
import org.scalaconsole.fxui.main.MainDelegate
import javafx.scene.{Scene, Parent}
import collection.JavaConverters._

class ReduceStage(val root: Parent, val mainDelegate: MainDelegate, val controller: ReduceController) extends Stage {
  setScene(new Scene(root))
  setTitle("Reduce Dependency")

  def onOK() = {
    mainDelegate.updateArtifacts(controller.artifactList.getItems.asScala)
  }
}