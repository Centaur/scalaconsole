package org.scalaconsole.fxui.search

import javafx.stage.{WindowEvent, Stage}
import javafx.scene.{Parent, Scene}
import javafx.event.EventHandler
import org.scalaconsole.fxui.main.MainDelegate

class SearchArtifactStage(val root: Parent, val mainDelegate: MainDelegate, val searchArtifactController: SearchArtifactController) extends Stage {

  this.setScene(new Scene(root))
  this.setOnShown(new EventHandler[WindowEvent] {
    override def handle(p1: WindowEvent) = {
      root.lookup("#searchBox").requestFocus()
    }
  })

  def onOK() = {
    import collection.JavaConverters._
    mainDelegate.updateArtifacts(searchArtifactController.selectedVersionList.getItems.asScala)
  }


}
