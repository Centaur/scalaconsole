package org.scalaconsole.fxui.search;

import com.google.gson.JsonElement;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.FlowPane;
import org.scalaconsole.fxui.SemVersion;

import java.util.Map;

import static javafx.scene.input.KeyCode.ENTER;

public class SearchArtifactController {
    SearchArtifactDelegate delegate;

    @FXML
    ImageView loadingImg;

    @FXML
    Label errorMsg;

    @FXML
    ListView<SemVersion> versionList;

    @FXML
    FlowPane crossBuildsPane;

    @FXML
    ListView<Map.Entry<String, JsonElement>> matchedList;

    @FXML
    ListView<String> selectedVersionList;

    @FXML
    TextField searchBox;

    @FXML
    void onOK(ActionEvent event) {
        SearchArtifactStage window = (SearchArtifactStage) errorMsg.getScene().getWindow();
        window.onOK();
        window.close();
    }

    @FXML
    void onCancel(ActionEvent event) {
        SearchArtifactStage window = (SearchArtifactStage) errorMsg.getScene().getWindow();
        window.close();
    }

    @FXML
    void onEnterInSearchBox(KeyEvent e) {
        if (e.getCode() == ENTER) {
            delegate.onSearch();
        }
    }

    BooleanProperty loading = new SimpleBooleanProperty(false);

    class ArtifactCell extends ListCell<Map.Entry<String, JsonElement>> {
        public ArtifactCell() {
            textProperty().bind(Bindings.createStringBinding(() -> {
                        Map.Entry<String, JsonElement> item = getItem();
                        if (item != null) return item.getKey();
                        else return null;
                    }, itemProperty()
            ));
        }
    }

    class VersionCell extends ListCell<SemVersion> {
        public VersionCell() {
            textProperty().bind(Bindings.createStringBinding(() -> {
                        SemVersion item = getItem();
                        if (item != null) return item.stringPresentation();
                        else return null;
                    }, itemProperty()
            ));
        }
    }

    @FXML
    void initialize() {
        delegate = new SearchArtifactDelegate(this);
        loadingImg.visibleProperty().bind(loading);
        matchedList.setCellFactory(entryListView -> new ArtifactCell());
        matchedList.getSelectionModel().selectedItemProperty().addListener((observableValue, oldEntry, newEntry) -> {
            if (newEntry != null)
                delegate.onSelectArtifact(newEntry);
        });
        versionList.setCellFactory(versionListView -> {
            VersionCell cell = new VersionCell();
            cell.setOnMouseClicked(evt -> {
                if (evt.getClickCount() == 2 && cell.getItem() != null) {
                    delegate.addVersion2Selection(cell.getItem());
                }
            });
            return cell;
        });
        delegate.init();
    }

}
