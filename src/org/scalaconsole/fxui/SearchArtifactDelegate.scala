package org.scalaconsole.fxui
import javafx.animation.FadeTransition
import javafx.util.Duration

class SearchArtifactDelegate(val controller: SearchArtifactController) {
  import FxUtil._


  private def animateErrorMsg() = {
    val  ft = new FadeTransition(Duration.millis(200), controller.errorMsg)
    ft.setFromValue(0)
    ft.setToValue(1)
    ft.setCycleCount(5)
    ft.setAutoReverse(true)
    ft.play()
  }

  def onSearch() = {
    val keyword = controller.searchBox.getText
    if(keyword.length < 3) {
      animateErrorMsg()
    } else {
      controller.errorMsg.setOpacity(0)
    }
  }
}
