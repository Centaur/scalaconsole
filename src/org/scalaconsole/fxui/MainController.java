package org.scalaconsole.fxui;

import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.controlsfx.dialog.Dialogs;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MainController {
    MainDelegate delegate;

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
    SplitPane splitPane;

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
        delegate.clearScript();
    }

    @FXML
    void onNewTab(ActionEvent event) {

    }

    @FXML
    void onCloseTab(ActionEvent event) {

    }

    @FXML
    void onPostAnonymousGist(ActionEvent event) {
        delegate.postAnonymousGist();
    }

    @FXML
    void onPostGistWithAccount(ActionEvent event) {
        delegate.postGistWithAccount();
    }

    @FXML
    void onReplClear(ActionEvent event) {
        outputArea.clear();
    }

    @FXML
    void onReplReset(ActionEvent event) {
        delegate.resetRepl();
    }

    @FXML
    void onSetCommandlineOptions(ActionEvent event) {
        delegate.setCommandlineOptions();
    }

    @FXML
    void onSetFont(ActionEvent event) {
        delegate.onSetFont();
    }

    @FXML
    void onToggleSplitterOrientation(ActionEvent event) {
        delegate.onToggleSplitterOrientation();
    }

    @FXML
    void onDependencySearch(ActionEvent event) {
        delegate.onSearchArtifacts();
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
        delegate = new MainDelegate(this);
        WebEngine engine = scriptArea.getEngine();
        engine.setOnAlert(stringWebEvent -> Dialogs.create().masthead(null).message(stringWebEvent.getData()).showInformation());
        engine.getLoadWorker().stateProperty().addListener((observableValue, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                delegate.setFont();
                scriptArea.requestFocus();
            }
        });
        engine.load(getClass().getResource("ace.html").toExternalForm());
    }

}
