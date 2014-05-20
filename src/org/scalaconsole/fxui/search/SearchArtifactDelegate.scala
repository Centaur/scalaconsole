package org.scalaconsole.fxui.search

import javafx.animation.FadeTransition
import javafx.util.Duration
import org.scalaconsole.net.MavenIndexerClient
import com.google.gson.JsonElement
import javafx.collections.FXCollections
import javafx.scene.control._
import javafx.beans.value.{ObservableValue, ChangeListener}
import org.scalaconsole.fxui.{Variables, SemVersion, FxUtil}

class SearchArtifactDelegate(val controller: SearchArtifactController) {

  import collection.JavaConverters._

  import FxUtil._

  val matchedArtifacts = FXCollections.observableArrayList[java.util.Map.Entry[String, JsonElement]]()
  val artifactVersions = FXCollections.observableArrayList[SemVersion]()
  val selectedVersions = FXCollections.observableArrayList[String]()

  def init() = {
    controller.matchedList.setItems(matchedArtifacts)
    controller.versionList.setItems(artifactVersions)
    controller.selectedVersionList.setItems(selectedVersions)
  }

  private def animateErrorMsg() = {
    val ft = new FadeTransition(Duration.millis(200), controller.errorMsg)
    ft.setFromValue(0)
    ft.setToValue(1)
    ft.setCycleCount(5)
    ft.setAutoReverse(true)
    ft.play()
  }


  private def extractVersions(json: Set[java.util.Map.Entry[String, JsonElement]]): Set[String] = {
    json.map(_.getKey).collect {
      case SemVersion.R(v) => v
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
            artifactVersions.clear()
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
        val versionButtons = versions.filter(v => SemVersion(v).isDefined).map(version => {
          val btn = new ToggleButton(version)
          btn.setToggleGroup(versionGroup)
          btn
        })

        onEventThread {
          {
            val children = controller.crossBuildsPane.getChildren
            children.setAll(allBtn)
            children.addAll(versionButtons.toSeq.sortBy(btn => SemVersion(btn.getText).get).asJavaCollection)
          }
          val currentSemVersion = SemVersion(Variables.currentScalaVersion)
          versionButtons.find(btn => SemVersion(btn.getText).exists(_.fuzzyMatch(currentSemVersion))).
            orElse(versionButtons.find(btn => btn.getText == Variables.currentScalaVersion)).
            getOrElse(allBtn).setSelected(true)
          controller.loading.setValue(false)
        }
      }
    }
  }

  def onSelectArtifact(entry: java.util.Map.Entry[String, JsonElement]) = {
    val versions = for {
      artifact <- entry.getValue.getAsJsonArray.asScala
      version <- SemVersion.apply(artifact.getAsJsonObject.get("version").getAsString)
    } yield version

    artifactVersions.setAll(versions.toSeq.sorted.asJavaCollection)
  }

  def addVersion2Selection(ver: SemVersion) = {
    val dependencyString = s"${controller.matchedList.getSelectionModel.getSelectedItem.getKey.trim}:${ver.stringPresentation}"
    selectedVersions.add(dependencyString)
  }

}
