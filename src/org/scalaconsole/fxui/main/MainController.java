package org.scalaconsole.fxui.main;

import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;
import org.controlsfx.dialog.Dialogs;
import org.scalaconsole.fxui.JavaBridge;

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
    TabPane tabPane;

    @FXML
    void onRun(ActionEvent event) {
        delegate.run();
    }

    @FXML
    void onRunSelected(ActionEvent event) {
        delegate.runSelected();
    }

    @FXML
    void onRunInPasteMode(ActionEvent event) {
        delegate.runPaste();
    }

    @FXML
    void onRunSelectedInPasteMode(ActionEvent event) {
        delegate.runPaste();
    }

    @FXML
    public void onNewTab(ActionEvent event) {
        if (event == null) {
            Tab newTab = new Tab(String.format("Tab%d", tabPane.getTabs().size()));
            WebView newView = new WebView();
            newTab.setContent(newView);
            tabPane.getTabs().add(newTab);
            tabPane.getSelectionModel().select(newTab);
            initWebView(newView);
        }
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
        delegate.onManualArtifact();
    }

    @FXML
    void onDependencyCurrent(ActionEvent event) {

    }


    JavaBridge bridge;

    @FXML
    void initialize() throws IOException {
        bridge = new JavaBridge(this);
        delegate = new MainDelegate(this);
        delegate.setOutputAreaFont();
        initWebView(scriptArea);
        tabPane.getSelectionModel().selectedItemProperty().addListener((observableValue, old, selected) -> {
            Worker worker = ((WebView) selected.getContent()).getEngine().getLoadWorker();
            System.out.println("worker.state="+worker.getState());
            if (worker.getState() == Worker.State.SUCCEEDED) {
                selected.getContent().requestFocus();
                ((WebView) selected.getContent()).getEngine().executeScript("editor.focus()");
            }
        });
    }


    private void initWebView(WebView view) {
        WebEngine engine = view.getEngine();
        engine.setOnAlert(stringWebEvent -> Dialogs.create().masthead(null).message(stringWebEvent.getData()).showInformation());
        engine.getLoadWorker().stateProperty().addListener((observableValue, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                delegate.setScriptAreaFont(engine);
                view.requestFocus();
                JSObject window = (JSObject) engine.executeScript("window");
                window.setMember("javaBridge", bridge);
            }
        });
        engine.load(getClass().getResource("ace.html").toExternalForm());
    }


}
