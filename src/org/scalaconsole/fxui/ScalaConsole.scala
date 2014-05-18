package org.scalaconsole.fxui
import javafx.application.{Platform, Application}
import javafx.stage.{WindowEvent, Stage}
import javafx.fxml.FXMLLoader
import javafx.scene.{Parent, Scene}
import javafx.event.EventHandler

class ScalaConsole extends Application {
  override def start(pStage: Stage) = {
    val root: Parent = FXMLLoader.load(this.getClass.getResource("MainStage.fxml"))
    pStage.setScene(new Scene(root))
    pStage.show()
    pStage.setOnCloseRequest(new EventHandler[WindowEvent]() {
      override def handle(p1: WindowEvent) = {
        Platform.exit()
        System.exit(0)
      }
    })

    ScalaConsole.top = pStage
  }

}

object ScalaConsole {
  var top         : Stage        = _

  def main(args: Array[String]) {
    Application.launch(classOf[ScalaConsole], args: _*)
  }
}
