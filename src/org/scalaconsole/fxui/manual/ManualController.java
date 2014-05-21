package org.scalaconsole.fxui.manual;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

public class ManualController {
    ManualDelegate delegate;

    @FXML
    TextField groupId;

    @FXML
    TextField artifactId;

    @FXML
    TextField version;

    @FXML
    void initialize() {
        delegate = new ManualDelegate();
    }

    @FXML
    void onOK(ActionEvent e) {
        ManualStage window = (ManualStage) groupId.getScene().getWindow();
        window.onOK();
        window.close();
    }

    @FXML
    void onCancel(ActionEvent e) {
        Stage manualStage = (Stage) groupId.getScene().getWindow();
        manualStage.close();
    }

    @FXML
    void onKeyUp(KeyEvent e) {
        switch (e.getCode()) {
            case ENTER:
                if (!(groupId.getText().isEmpty() || artifactId.getText().isEmpty() || version.getText().isEmpty()))
                    onOK(null);
                break;
            case ESCAPE:
                onCancel(null);
                break;
        }
    }

}
