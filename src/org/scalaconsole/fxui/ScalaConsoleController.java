package org.scalaconsole.fxui;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.controlsfx.dialog.Dialogs;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class ScalaConsoleController {
    CoreDelegate delegate;

    @FXML
    ResourceBundle resources;

    @FXML
    URL location;

    @FXML
    WebView scriptArea;

    @FXML
    TextArea outputArea;

    @FXML
    Label statusBar;

    @FXML
    void onRun(ActionEvent event) {
        delegate.run(scriptArea.getEngine().executeScript("editor.getValue()").toString());
    }

    @FXML
    void onRunSelected(ActionEvent event) {
        delegate.run(scriptArea.getEngine().executeScript("editor.session.getTextRange(editor.getSelectionRange())").toString());
    }

    @FXML
    void onRunInPasteMode(ActionEvent event) {
        delegate.runPaste(scriptArea.getEngine().executeScript("editor.getValue()").toString());
    }

    @FXML
    void onRunSelectedInPasteMode(ActionEvent event) {
        delegate.runPaste(scriptArea.getEngine().executeScript("editor.session.getTextRange(editor.getSelectionRange())").toString());
    }

    @FXML
    void onScriptClear(ActionEvent event) {

//        scriptArea.clear();
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
        outputArea.clear();
    }

    @FXML
    void onReplReset(ActionEvent event) {

    }

    @FXML
    void onSetCommandlineOptions(ActionEvent event) {
        delegate.setFont();
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
    void initialize() throws IOException {
        delegate = new CoreDelegate(this);
        WebEngine engine = scriptArea.getEngine();
        engine.setOnAlert(stringWebEvent -> Dialogs.create().message(stringWebEvent.getData()).showInformation());
        engine.getLoadWorker().stateProperty().addListener(new ChangeListener<Worker.State>() {
            @Override
            public void changed(ObservableValue<? extends Worker.State> observableValue, Worker.State oldState, Worker.State newState) {
                if (newState == Worker.State.SUCCEEDED) {
                    delegate.setFont();
                    scriptArea.requestFocus();
                }
            }
        });
        engine.load(getClass().getResource("ace.html").toExternalForm());
    }

}
