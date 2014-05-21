package org.scalaconsole.fxui.manual

import javafx.stage.Stage
import javafx.scene.{Scene, Parent}
import org.scalaconsole.fxui.main.MainDelegate

class ManualStage(val root: Parent, val mainDelegate: MainDelegate, val controller: ManualController) extends Stage {

  setScene(new Scene(root))
  setTitle("Add Artifact Manually")

  def onOK() = {
    try {
      val artifactString = s"${controller.groupId.getText.trim}:${controller.artifactId.getText.trim}:${controller.version.getText.trim}"
      mainDelegate.addArtifacts(artifactString :: Nil)
    } catch {
      case e: Throwable => e.printStackTrace()
    }
  }

}
