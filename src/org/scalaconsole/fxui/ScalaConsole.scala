package org.scalaconsole.fxui
import javafx.application.{Platform, Application}
import javafx.stage.{WindowEvent, Stage}
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.event.EventHandler
import org.scalaconsole.fxui.main.MainStage
import java.util.Locale

class ScalaConsole extends Application {
  override def start(pStage: Stage) = {
    val loader = new FXMLLoader(this.getClass.getResource("main/MainStage.fxml"))
    val mainStage = new MainStage
    loader.setController(mainStage)
    pStage.setScene(new Scene(loader.load()))
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
    Locale.setDefault(Locale.ENGLISH)
    Application.launch(classOf[ScalaConsole], args: _*)
  }
}
