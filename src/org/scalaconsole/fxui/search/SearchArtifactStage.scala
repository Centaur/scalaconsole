package org.scalaconsole.fxui.search

import javafx.stage.{WindowEvent, Stage}
import javafx.scene.{Parent, Scene}
import javafx.event.EventHandler
import org.scalaconsole.fxui.main.MainStage
import javafx.fxml.{FXML, FXMLLoader}
import javafx.scene.image.ImageView
import javafx.scene.control.{ListCell, TextField, ListView, Label}
import org.scalaconsole.fxui.SemVersion
import javafx.scene.layout.FlowPane
import com.google.gson.JsonElement
import javafx.collections.FXCollections
import javafx.beans.property.{SimpleBooleanProperty, BooleanProperty}
import javafx.beans.binding.Bindings
import javafx.scene.input.MouseEvent
import org.scalaconsole.fxui.FxUtil._

class SearchArtifactStage(val mainDelegate: MainStage) extends Stage with SearchArtifactController {

  @FXML var loadingImg: ImageView = _
  @FXML var errorMsg: Label = _
  @FXML var versionList: ListView[SemVersion] = _
  @FXML var crossBuildsPane: FlowPane = _
  @FXML var matchedList: ListView[(String, JsonElement)] = _
  @FXML var selectedVersionList: ListView[String] = _
  @FXML var searchBox: TextField = _

  val matchedArtifacts = FXCollections.observableArrayList[(String, JsonElement)]()
  val artifactVersions = FXCollections.observableArrayList[SemVersion]()
  val selectedVersions = FXCollections.observableArrayList[String]()

  val loading: BooleanProperty = new SimpleBooleanProperty(false)

  private val loader = new FXMLLoader(getClass.getResource("/org/scalaconsole/fxui/search/SearchArtifactStage.fxml"))
  loader.setController(this)
  setScene(new Scene(loader.load()))
  setTitle("Search and Add Artifacts")
  setOnShown((_: WindowEvent) => searchBox.requestFocus())


  class ArtifactCell extends ListCell[(String, JsonElement)] {
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
