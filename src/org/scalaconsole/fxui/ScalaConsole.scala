package org.scalaconsole.fxui

import javafx.application.{Application, Platform}
import javafx.stage.{Stage, WindowEvent}

import org.scalaconsole.fxui.FxUtil._
import org.scalaconsole.fxui.main.MainStage

class ScalaConsole extends Application {
  override def start(pStage: Stage) = {
    pStage.setScene(loadScene("main/MainStage.fxml", new MainStage))
    pStage.show()
    pStage.setOnCloseRequest{(_: WindowEvent) =>
        Platform.exit()
        System.exit(0)
    }
    ScalaConsole.top = pStage
    ScalaConsole.application = this
  }

}

object ScalaConsole {
  var top         : Stage        = _
  var application : Application = _

  def main(args: Array[String]) {
//    Localization.setLocale(Locale.ENGLISH)
    Application.launch(classOf[ScalaConsole], args: _*)
  }
}
