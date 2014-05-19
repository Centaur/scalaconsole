package org.scalaconsole.fxui

import javafx.animation.FadeTransition
import javafx.util.Duration
import org.scalaconsole.net.MavenIndexerClient
import com.google.gson.JsonElement
import javafx.collections.{ObservableList, FXCollections}
import javafx.scene.control._
import javafx.beans.value.{ObservableValue, ChangeListener}
import javafx.scene.Node

class SearchArtifactDelegate(val controller: SearchArtifactController) {
  import collection.JavaConverters._

  import FxUtil._

  val matchedArtifacts = FXCollections.observableArrayList[java.util.Map.Entry[String, JsonElement]]()

  def init() = {
    controller.matchedList.setItems(matchedArtifacts)
  }

  private def animateErrorMsg() = {
    val ft = new FadeTransition(Duration.millis(200), controller.errorMsg)
    ft.setFromValue(0)
    ft.setToValue(1)
    ft.setCycleCount(5)
    ft.setAutoReverse(true)
    ft.play()
  }

  val R = """.*_(\d+\.\d+(?:\.\d+)?(?:\-\d+)?(?:[-\.]RC\d+|-SNAPSHOT)?)""".r

  private def extractVersions(json: Set[java.util.Map.Entry[String, JsonElement]]): Set[String] = {
    json.map(_.getKey).collect {
      case R(v) => v
    }.toSet
  }

  def onSearch() = {
    val keyword = controller.searchBox.getText
    if (keyword.length < 3) {
      animateErrorMsg()
    } else {
      controller.errorMsg.setOpacity(0)
      controller.loading.setValue(true)
      startTask {
        val (exactMatch, otherMatch) = MavenIndexerClient.search(keyword)
        val versions = extractVersions(exactMatch) | extractVersions(otherMatch)
        val versionGroup = new ToggleGroup
        versionGroup.selectedToggleProperty.addListener(new ChangeListener[Toggle] {
          override def changed(p1: ObservableValue[_ <: Toggle], old: Toggle, selected: Toggle) = {
            val selectedBtn = selected.asInstanceOf[ToggleButton]
            if (selectedBtn.getText == "All") {
              matchedArtifacts.setAll(exactMatch.asJavaCollection)
              matchedArtifacts.addAll(otherMatch.asJavaCollection)
            } else {
              matchedArtifacts.setAll(exactMatch.filter(_.getKey.endsWith(selectedBtn.getText)).asJavaCollection)
              matchedArtifacts.addAll(otherMatch.filter(_.getKey.endsWith(selectedBtn.getText)).asJavaCollection)
            }
          }
        })
        val allBtn = new ToggleButton("All")
        allBtn.setToggleGroup(versionGroup)
        val versionButtons = versions.map(version => {
          val btn = new ToggleButton(version)
          btn.setToggleGroup(versionGroup)
          btn
        })

        onEventThread {
          {
            val children = controller.crossBuildsPane.getChildren
            children.setAll(allBtn)
            children.addAll(versionButtons.toSeq.asJavaCollection)
          }
          versionButtons.find(btn => SemVersion(btn.getText) == SemVersion(Variables.currentScalaVersion)).getOrElse(allBtn).setSelected(true)
          controller.loading.setValue(false)
        }
      }
    }
  }
}
