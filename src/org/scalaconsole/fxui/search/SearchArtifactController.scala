package org.scalaconsole.fxui.search
import javafx.scene.control._
import javafx.scene.input.KeyCode._
import javafx.fxml.FXML
import org.scalaconsole.fxui.{SelectedVersionCell, Variables, SemVersion}
import com.google.gson.JsonElement
import javafx.event.ActionEvent
import javafx.scene.input.KeyEvent
import org.scalaconsole.fxui.FxUtil._
import org.scalaconsole.net.MavenIndexerClient
import javafx.beans.value.{ObservableValue, ChangeListener}
import javafx.animation.FadeTransition
import javafx.util.Duration
import collection.JavaConverters._

trait SearchArtifactController {self: SearchArtifactStage =>

  @FXML def onOK(event: ActionEvent) {
    import collection.JavaConverters._
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

          versionGroup.selectedToggleProperty.addListener(new ChangeListener[Toggle] {
            override def changed(p1: ObservableValue[_ <: Toggle], old: Toggle, selected: Toggle) = {
              val selectedBtn = selected.asInstanceOf[ToggleButton]
              artifactVersions.clear()
              if (selectedBtn.getText == "All") {
                matchedArtifacts.setAll(allMatches.asJavaCollection)
              } else {
                matchedArtifacts.setAll(allMatches.filter { case (k, v) => k.endsWith(selectedBtn.getText)}.asJavaCollection)
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
              val children = crossBuildsPane.getChildren
              children.setAll(allBtn)
              children.addAll(versionButtons.toSeq.sortBy(btn => SemVersion(btn.getText).get).asJavaCollection)
            }
            val currentSemVersion = SemVersion(Variables.currentScalaVersion)
            versionButtons.find(btn => SemVersion(btn.getText).exists(_.fuzzyMatch(currentSemVersion))).
            orElse(versionButtons.find(btn => btn.getText == Variables.currentScalaVersion)).
            getOrElse(allBtn).setSelected(true)
            loading.setValue(false)
          }
        }
      }
    }
  }

  @FXML def initialize(): Unit = {
    loadingImg.visibleProperty.bind(loading)
    matchedList.setCellFactory((_: ListView[(String, JsonElement)]) => new ArtifactCell())
    matchedList.getSelectionModel.selectedItemProperty.addListener(new ChangeListener[(String, JsonElement)] {
      override def changed(p1: ObservableValue[_ <: (String, JsonElement)], oldEntry: (String, JsonElement), newEntry: (String, JsonElement)) = {
        if (newEntry != null)
          onSelectArtifact(newEntry)
      }
    })
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

  private def extractVersions(json: Map[String, JsonElement]): Set[String] = {
    json.collect {
      case (SemVersion.R(v), _) => v
    }.toSet
  }

  private def onSelectArtifact(entry: (String, JsonElement)) = {
    val versions = for {
      artifact <- entry._2.getAsJsonArray.asScala
      version <- SemVersion.apply(artifact.getAsJsonObject.get("version").getAsString)
    } yield version

    artifactVersions.setAll(versions.toSeq.sorted.asJavaCollection)
  }

  protected def addVersion2Selection(ver: SemVersion) = {
    val dependencyString = s"${matchedList.getSelectionModel.getSelectedItem._1.trim}:${ver.stringPresentation}"
    selectedVersions.add(dependencyString)
  }

}
