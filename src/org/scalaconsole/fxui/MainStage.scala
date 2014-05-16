package org.scalaconsole.fxui
import javafx.application.Application
import javafx.stage.Stage
import javafx.fxml.FXMLLoader
import javafx.scene.{Parent, Scene}

class MainStage extends Application{
  override def start(pStage: Stage) = {
    val root:Parent = FXMLLoader.load(this.getClass.getResource("scalaconsole.fxml"))
    pStage.setScene(new Scene(root))
    pStage.show()
    MainStage.top = pStage
  }
}

object MainStage {
  var top: Stage = _

  def main(args: Array[String]) {
    Application.launch(classOf[MainStage], args:_*)
  }
}
