package org.scalaconsole.fxui;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.FlowPane;

import static javafx.scene.input.KeyCode.ENTER;

public class SearchArtifactController {
    SearchArtifactDelegate delegate;

    @FXML
    ListView<?> exactMatch;

    @FXML
    ImageView loadingImg;

    @FXML
    Label errorMsg;

    @FXML
    ListView<?> versions;

    @FXML
    FlowPane crossBuildsPane;

    @FXML
    ListView<?> otherMatch;

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
        if(e.getCode() == ENTER) {
            delegate.onSearch();
        }
    }

    BooleanProperty loading = new SimpleBooleanProperty(false);

    @FXML
    void initialize() {
        delegate = new SearchArtifactDelegate(this);
        loadingImg.visibleProperty().bind(loading);
    }


}
