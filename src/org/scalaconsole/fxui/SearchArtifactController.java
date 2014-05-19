package org.scalaconsole.fxui;

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

import java.util.Map;

import static javafx.scene.input.KeyCode.ENTER;

public class SearchArtifactController {
    SearchArtifactDelegate delegate;

    @FXML
    ImageView loadingImg;

    @FXML
    Label errorMsg;

    @FXML
    ListView<?> versions;

    @FXML
    FlowPane crossBuildsPane;

    @FXML
    ListView<Map.Entry<String, JsonElement>> matchedList;

    @FXML
    TextField searchBox;

    @FXML
    void onCancel(ActionEvent event) {

    }

    @FXML
    void onOK(ActionEvent event) {

    }

    @FXML
    void onEnterInSearchBox(KeyEvent e) {
        if (e.getCode() == ENTER) {
            delegate.onSearch();
        }
    }

    BooleanProperty loading = new SimpleBooleanProperty(false);

    class MatchesCell extends ListCell<Map.Entry<String, JsonElement>> {
        public MatchesCell() {
            textProperty().bind(Bindings.createStringBinding(() -> {
                        Map.Entry<String, JsonElement> item = getItem();
                        if (item != null) return item.getKey();
                        else return null;
                    }, itemProperty()
            ));
        }
    }

    @FXML
    void initialize() {
        delegate = new SearchArtifactDelegate(this);
        loadingImg.visibleProperty().bind(loading);
        matchedList.setCellFactory(entryListView -> new MatchesCell());
        delegate.init();
    }


}
