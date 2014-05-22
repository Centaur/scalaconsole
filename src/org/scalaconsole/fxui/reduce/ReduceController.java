package org.scalaconsole.fxui.reduce;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyEvent;
import org.scalaconsole.data.DependencyManager;
import org.scalaconsole.fxui.SelectedVersionCell;

public class ReduceController {
    @FXML
    ListView<String> artifactList;

    @FXML
    void onOK(ActionEvent event) {
        ReduceStage window = (ReduceStage) artifactList.getScene().getWindow();
        window.onOK();
        window.close();
    }

    @FXML
    void onCancel(ActionEvent event) {
        ReduceStage window = (ReduceStage) artifactList.getScene().getWindow();
        window.close();
    }

    @FXML
    void initialize() {
        System.out.println("artifactList.setItems");
        artifactList.setCellFactory(artifactListView -> new SelectedVersionCell());
        artifactList.setItems(FXCollections.observableArrayList(DependencyManager.currentArtifactsAsJavaCollection()));
    }

    @FXML
    void onKeyUp(KeyEvent e){
        switch(e.getCode()) {
            case ESCAPE:
                onCancel(null);
        }
    }


}
