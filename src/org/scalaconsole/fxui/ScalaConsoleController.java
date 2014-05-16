package org.scalaconsole.fxui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;

import java.net.URL;
import java.util.ResourceBundle;

public class ScalaConsoleController {
    CoreDelegate delegate;

    @FXML
    ResourceBundle resources;

    @FXML
    URL location;

    @FXML
    TextArea scriptArea;

    @FXML
    TextArea outputArea;

    @FXML
    Label statusBar;

    @FXML
    void onRun(ActionEvent event) {
        delegate.run(scriptArea.getText());
    }

    @FXML
    void onRunSelected(ActionEvent event) {
        delegate.run(scriptArea.getSelectedText());
    }

    @FXML
    void onRunInPasteMode(ActionEvent event) {
        delegate.runPaste(scriptArea.getText());
    }

    @FXML
    void onRunSelectedInPasteMode(ActionEvent event) {
        delegate.runPaste(scriptArea.getSelectedText());
    }

    @FXML
    void onScriptClear(ActionEvent event) {

    }

    @FXML
    void onNewTab(ActionEvent event) {

    }

    @FXML
    void onCloseTab(ActionEvent event) {

    }

    @FXML
    void onPostAnonymousGist(ActionEvent event) {

    }

    @FXML
    void onPostGistWithAccount(ActionEvent event) {

    }

    @FXML
    void onReplClear(ActionEvent event) {

    }

    @FXML
    void onReplReset(ActionEvent event) {

    }

    @FXML
    void onSetCommandlineOptions(ActionEvent event) {

    }

    @FXML
    void onDependencySearch(ActionEvent event) {

    }

    @FXML
    void onDependencyManually(ActionEvent event) {

    }

    @FXML
    void onDependencyLocal(ActionEvent event) {

    }

    @FXML
    void onDependencyCurrent(ActionEvent event) {

    }

    @FXML
    void onTextAreaClicked(MouseEvent event) {

    }

    @FXML
    void initialize() {
        assert scriptArea != null : "fx:id=\"scriptArea\" was not injected: check your FXML file 'scalaconsole.fxml'.";
        assert outputArea != null : "fx:id=\"outputArea\" was not injected: check your FXML file 'scalaconsole.fxml'.";
        assert statusBar != null : "fx:id=\"statusBar\" was not injected: check your FXML file 'scalaconsole.fxml'.";
        delegate = new CoreDelegate(this);
    }

}
