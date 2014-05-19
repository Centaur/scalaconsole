package org.scalaconsole.fxui

import javafx.animation.FadeTransition
import javafx.util.Duration
import org.scalaconsole.net.MavenIndexerClient
import com.google.gson.{JsonObject, JsonElement}
import javafx.collections.FXCollections
import javafx.scene.control.Button

class SearchArtifactDelegate(val controller: SearchArtifactController) {
  import collection.JavaConverters._

  import FxUtil._

  val exactMatches = FXCollections.observableArrayList[java.util.Map.Entry[String, JsonElement]]()
  val otherMatches = FXCollections.observableArrayList[java.util.Map.Entry[String, JsonElement]]()

  def init() = {
    controller.exactMatch.setItems(exactMatches)
    controller.otherMatch.setItems(otherMatches)
  }

  private def animateErrorMsg() = {
    val ft = new FadeTransition(Duration.millis(200), controller.errorMsg)
    ft.setFromValue(0)
    ft.setToValue(1)
    ft.setCycleCount(5)
    ft.setAutoReverse(true)
    ft.play()
  }

  val R = """.*_(\d+\.\d+(?:\.\d+)?(?:\-\d+)?(?:\.RC\d+|-SNAPSHOT)?)""".r

  private def extractVersions(json: JsonObject): Set[String] = {
    json.entrySet().asScala.map(_.getKey).collect {
      case R(v) => v
    }.toSet
  }

  def onSearch() = {
    val keyword = controller.searchBox.getText
    if (keyword.length < 3) {
      animateErrorMsg()
    } else {
      controller.loadingImg.setVisible(true)

      onEventThread {
        controller.errorMsg.setOpacity(0)
        controller.crossBuildsPane.getChildren.clear()
        //        controller.loading.setValue(true)
      }
      startTask {
        val (exact, others) = MavenIndexerClient.search(keyword)
        val versions = extractVersions(exact) | extractVersions(others)
        val versionButtons = versions.map(version => {
          val btn = new Button()
          btn.setText(version)
          btn
        })

        onEventThread {
          controller.crossBuildsPane.getChildren.addAll(versionButtons.asJavaCollection)
          exactMatches.setAll(exact.entrySet())
          otherMatches.setAll(others.entrySet())
          //          controller.loading.setValue(false)
          controller.loadingImg.setVisible(false)
        }
      }
    }
  }
}
