package org.scalaconsole.fxui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ArrayBlockingQueue;

public class ScalaConsoleController {
    ArrayBlockingQueue<String> codeQueue = new ArrayBlockingQueue<>(10);
    ArrayBlockingQueue<String> outputQueue = new ArrayBlockingQueue<>(1024);


    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private TextArea scriptArea;

    @FXML
    private TextArea outputArea;

    @FXML
    private Label statusBar;

    @FXML
    void onRun(ActionEvent event) {

    }

    @FXML
    void onRunSelected(ActionEvent event) {
        String script = scriptArea.getSelectedText();
        codeQueue.add(script);
    }

    @FXML
    void onRunInPasteMode(ActionEvent event) {

    }

    @FXML
    void onRunSelectedInPasteMode(ActionEvent event) {

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
    void onTextAreaClicked(ActionEvent event) {

    }

    @FXML
    void initialize() {
        assert scriptArea != null : "fx:id=\"scriptArea\" was not injected: check your FXML file 'scalaconsole.fxml'.";
        assert outputArea != null : "fx:id=\"outputArea\" was not injected: check your FXML file 'scalaconsole.fxml'.";
        assert statusBar != null : "fx:id=\"statusBar\" was not injected: check your FXML file 'scalaconsole.fxml'.";
    }
}
