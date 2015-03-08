package org.scalaconsole.fxui.search

import javafx.beans.binding.Bindings
import javafx.beans.property.{BooleanProperty, SimpleBooleanProperty}
import javafx.collections.FXCollections
import javafx.fxml.FXML
import javafx.scene.control.{Label, ListCell, ListView, TextField}
import javafx.scene.image.ImageView
import javafx.scene.input.MouseEvent
import javafx.scene.layout.FlowPane
import javafx.stage.{Stage, WindowEvent}
import javax.json.JsonValue

import org.scalaconsole.fxui.FxUtil._
import org.scalaconsole.fxui.SemVersion
import org.scalaconsole.fxui.main.MainStage

class SearchArtifactStage(val mainStage: MainStage) extends Stage with SearchArtifactController {

  @FXML var loadingImg: ImageView = _
  @FXML var errorMsg: Label = _
  @FXML var versionList: ListView[SemVersion] = _
  @FXML var crossBuildsPane: FlowPane = _
  @FXML var matchedList: ListView[(String, JsonValue)] = _
  @FXML var selectedVersionList: ListView[String] = _
  @FXML var searchBox: TextField = _

  val matchedArtifacts = FXCollections.observableArrayList[(String, JsonValue)]()
  val artifactVersions = FXCollections.observableArrayList[SemVersion]()
  val selectedVersions = FXCollections.observableArrayList[String]()

  val loading: BooleanProperty = new SimpleBooleanProperty(false)

  setScene(loadScene("/org/scalaconsole/fxui/search/SearchArtifactStage.fxml", controller = this))
  setTitle("Search and Add Artifacts")
  setOnShown((_: WindowEvent) => searchBox.requestFocus())


  class ArtifactCell extends ListCell[(String, JsonValue)] {
      textProperty.bind(Bindings.createStringBinding(() => {
        val item = getItem
        if (item != null) item._1
        else null
      }, itemProperty))
  }
  class VersionCell extends ListCell[SemVersion] {
      textProperty.bind(Bindings.createStringBinding(() => {
        val item = getItem
        if (item != null) item.stringPresentation
        else null
      }, itemProperty))
      setOnMouseClicked({evt:MouseEvent =>
        if (evt.getClickCount == 2 && getItem != null) {
          addVersion2Selection(getItem)
        }
      })
  }
}
