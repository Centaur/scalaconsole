package org.scalaconsole.fxui.search

import javafx.animation.FadeTransition
import javafx.beans.value.ObservableValue
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.scene.control._
import javafx.scene.input.KeyCode._
import javafx.scene.input.KeyEvent
import javafx.util.Duration
import javax.json.{JsonArray, JsonValue, JsonObject}

import org.scalaconsole.fxui.FxUtil._
import org.scalaconsole.fxui.{SelectedVersionCell, SemVersion, Variables}
import org.scalaconsole.net.MavenIndexerClient

import scala.collection.JavaConverters._

trait SearchArtifactController { self: SearchArtifactStage =>

  @FXML def onOK(event: ActionEvent) {
    import scala.collection.JavaConverters._
    mainStage.addArtifacts(selectedVersionList.getItems.asScala)
    close()
  }

  @FXML def onCancel(event: ActionEvent) {
    close()
  }

  @FXML def onEnterInSearchBox(e: KeyEvent): Unit = {
    if (e.getCode eq ENTER) {
      val keyword = searchBox.getText
      if (keyword.length < 3) {
        animateErrorMsg()
      } else {
        errorMsg.setOpacity(0)
        loading.setValue(true)
        startTask {
          val (exactMatch, otherMatch) = MavenIndexerClient.search(keyword)
          val allMatches = exactMatch ++ otherMatch
          val versions = extractVersions(exactMatch) | extractVersions(otherMatch)
          val versionGroup = new ToggleGroup

          versionGroup.selectedToggleProperty.addListener { (p1: ObservableValue[_ <: Toggle], old: Toggle, selected: Toggle) =>
            val selectedBtn = selected.asInstanceOf[ToggleButton]
            artifactVersions.clear()
            if (selectedBtn.getText == "All") {
              matchedArtifacts.setAll(allMatches.asJavaCollection)
            } else {
              matchedArtifacts.setAll(allMatches.filter { case (k, v) => k.endsWith(selectedBtn.getText)}.asJavaCollection)
            }
          }
          val allBtn = new ToggleButton("All")
          allBtn.setToggleGroup(versionGroup)
          case class VersionButton(btn: ToggleButton, version: SemVersion)
          val versionButtons = for (version <- versions.toSeq; semVersion <- SemVersion(version)) yield {
            val btn = new ToggleButton(version)
            btn.setToggleGroup(versionGroup)
            VersionButton(btn, semVersion)
          }

          onEventThread {
            val children = crossBuildsPane.getChildren
            children.setAll(allBtn)
            children.addAll(versionButtons.toSeq.sortBy(_.version).map(_.btn).asJavaCollection)
            val currentSemVersion = SemVersion(Variables.currentScalaVersion)
            versionButtons
              .find(_.version.fuzzyMatch(currentSemVersion))
              .orElse(versionButtons.find(vb => vb.btn.getText == Variables.currentScalaVersion))
              .map(_.btn)
              .getOrElse(allBtn)
              .setSelected(true)
            loading.setValue(false)
          }
        }
      }
    }
  }

  @FXML def initialize(): Unit = {
    loadingImg.visibleProperty.bind(loading)
    matchedList.setCellFactory((_: ListView[(String, JsonValue)]) => new ArtifactCell())
    matchedList.getSelectionModel.selectedItemProperty.addListener { (p1: ObservableValue[_ <: (String, JsonValue)], oldEntry: (String, JsonValue), newEntry: (String, JsonValue)) =>
      if (newEntry != null)
        onSelectArtifact(newEntry.asInstanceOf[(String, JsonArray)])
    }
    versionList.setCellFactory((_: ListView[SemVersion]) => new VersionCell())
    selectedVersionList.setCellFactory((_: ListView[String]) => new SelectedVersionCell())

    matchedList.setItems(matchedArtifacts)
    versionList.setItems(artifactVersions)
    selectedVersionList.setItems(selectedVersions)
  }

  @FXML def onKeyUp(e: KeyEvent) {
    e.getCode match {
      case ESCAPE =>
        onCancel(null)
      case _ =>
    }
  }

  private def animateErrorMsg() = {
    val ft = new FadeTransition(Duration.millis(200), errorMsg)
    ft.setFromValue(0)
    ft.setToValue(1)
    ft.setCycleCount(5)
    ft.setAutoReverse(true)
    ft.play()
  }

  private def extractVersions(json: Map[String, JsonValue]): Set[String] = {
    json.collect {
      case (SemVersion.R(v), _) => v
    }.toSet
  }

  private def onSelectArtifact(entry: (String, JsonArray)) = {
    val versions = for {
      artifact <- entry._2.asScala
      version <- SemVersion.apply(artifact.asInstanceOf[JsonObject].getString("version"))
    } yield version

    artifactVersions.setAll(versions.toSeq.sorted.asJavaCollection)
  }

  protected def addVersion2Selection(ver: SemVersion) = {
    val dependencyString = s"${matchedList.getSelectionModel.getSelectedItem._1.trim}:${ver.stringPresentation}"
    selectedVersions.add(dependencyString)
  }

}
