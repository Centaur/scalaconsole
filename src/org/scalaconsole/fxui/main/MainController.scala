package org.scalaconsole.fxui.main

import java.net.URL
import java.util.ResourceBundle
import javafx.concurrent.Worker
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.scene.control.Alert.AlertType
import javafx.scene.control._
import javafx.scene.web.{WebEngine, WebEvent, WebView}
import javafx.stage.Stage

import netscape.javascript.JSObject
import org.scalaconsole.data.{Artifact, ClassLoaderManager, DependencyManager}
import org.scalaconsole.fxui.FxUtil._
import org.scalaconsole.fxui.manual.ManualStage
import org.scalaconsole.fxui.reduce.ReduceStage
import org.scalaconsole.fxui.search.SearchArtifactStage
import org.scalaconsole.fxui.{JavaBridge, Variables}
import org.scalaconsole.net.{Gist, OAuthTinyServer}

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext

trait MainController {
  self: MainStage =>
  @FXML var resources: ResourceBundle = _
  @FXML var location: URL = _
  @FXML var scriptArea: WebView = _
  @FXML var outputArea: TextArea = _
  @FXML var statusBar: Label = _
  @FXML var splitPane: SplitPane = _
  @FXML var tabPane: TabPane = _

  private def currentEngine = tabPane.getSelectionModel.getSelectedItem.getContent.asInstanceOf[WebView].getEngine

  private def runScript(script: String) = {
    commandQueue.put('Normal -> script)
  }

  @FXML def onRun(event: ActionEvent) {
    val script = currentEngine.executeScript("editor.getValue()").toString
    runScript(script)
  }

  @FXML def onRunSelected(event: ActionEvent) {
    val script = currentEngine.executeScript("editor.session.getTextRange(editor.getSelectionRange())").toString
    runScript(script)
  }

  private def runPaste(script: String) {
    commandQueue.put('Paste -> script)
  }

  @FXML def onRunSelectedInPasteMode(event: ActionEvent) {
    val script = currentEngine.executeScript("editor.session.getTextRange(editor.getSelectionRange())").toString
    runPaste(script)
  }

  @FXML def onRunInPasteMode(event: ActionEvent) {
    val script = currentEngine.executeScript("editor.getValue()").toString
    runPaste(script)
  }

  @FXML def onNewTab(event: ActionEvent) {
    val newTab: Tab = new Tab(s"Tab${tabPane.getTabs.size}")
    val newView: WebView = new WebView
    newTab.setContent(newView)
    tabPane.getTabs.add(newTab)
    tabPane.getSelectionModel.select(newTab)
    initWebView(newView)
  }

  @FXML def onNewWindow(event: ActionEvent) {
    val newStage = new Stage
    newStage.setScene(loadScene("main/MainStage.fxml", new MainStage))
    newStage.show()
  }

  @FXML def onCloseTab(event: ActionEvent) {
    val currentTab: Int = tabPane.getSelectionModel.getSelectedIndex
    if (currentTab != 0) tabPane.getTabs.remove(currentTab)
  }

  @FXML def onPostAnonymousGist(event: ActionEvent) {
    postGist(None)
  }

  @FXML def onPostGistWithAccount(event: ActionEvent) {
    OAuthTinyServer.withAccessToken(postGist)
  }

  @FXML def onReplClear(event: ActionEvent) {
    outputArea.clear()
  }

  @FXML def onShowAceSettings(event: ActionEvent) {
    currentEngine.executeScript("editor.commands.exec('showSettingsMenu', editor)")
  }

  @FXML def onReplReset(event: ActionEvent) {
    resetRepl()
  }

  @FXML def onSetCommandlineOptions(event: ActionEvent) {
    val current = Variables.commandlineOption
    val masth = "Example: -Xprint:typer"
    val msg = s"current: ${current.getOrElse("none")}"
    val dlg = new TextInputDialog()
    dlg.setTitle("Set Commandline Options")
    dlg.setHeaderText(masth)
    dlg.setContentText(msg)
    val result = dlg.showAndWait()
    result.ifPresent(res => if (current.getOrElse("") != res) {
      Variables.commandlineOption = Some(res)
      resetRepl()
    }
    )
  }

  @FXML def onSetFont(event: ActionEvent) {
    val masth = "Example: Consolas-14 or Ubuntu Mono-17"
    val f = Variables.displayFont
    val fontAsString = Variables.encodeFont(f)
    val msg = s"current: $fontAsString"
    val dlg = new TextInputDialog(fontAsString)
    dlg.setTitle("Set Display Font")
    dlg.setHeaderText(masth)
    dlg.setContentText(msg)
    val result = dlg.showAndWait()
    result.ifPresent(res => {
      Variables.displayFont = Variables.decodeFont(res)
      setOutputAreaFont()
      setFontForAllScriptArea()
      setStatus(s"Font set to $result")
    })
  }

  @FXML def onToggleSplitterOrientation(event: ActionEvent) {
    import javafx.geometry.Orientation._
    splitPane.getOrientation match {
      case HORIZONTAL =>
        splitPane.setOrientation(VERTICAL)
      case VERTICAL =>
        splitPane.setOrientation(HORIZONTAL)
    }
  }

  @FXML def onDependencySearch(event: ActionEvent) {
    val searchArtifactsStage = new SearchArtifactStage(this)
    searchArtifactsStage.show()
  }

  @FXML def onDependencyManually(event: ActionEvent) {
    val manualStage = new ManualStage(this)
    manualStage.show()
  }

  @FXML def onDependencyReduce(event: ActionEvent) {
    val reduceStage = new ReduceStage(this)
    reduceStage.show()
  }

  val bridge: JavaBridge = new JavaBridge(this)

  @FXML def initialize() {
    setOutputAreaFont()
    initWebView(scriptArea)
    tabPane.getSelectionModel.selectedItemProperty().addListener((observable, oldValue, newValue) => {
      Variables.theme = oldValue.getContent.asInstanceOf[WebView].getEngine.executeScript("editor.getTheme()").asInstanceOf[String]
    })
  }

  private def setOutputAreaFont() = {
    val f = Variables.displayFont
    outputArea.setFont(f)
  }

  private def initWebView(view: WebView) {
    val engine: WebEngine = view.getEngine
    view.visibleProperty.addListener((observable, oldValue, newValue) => {
      if (newValue) {
        view.getEngine.executeScript( s"""editor.setTheme("${Variables.theme}")""")
        view.requestFocus()
      }
    })

    engine.setOnAlert((ev: WebEvent[String]) => {
      val dlg = new Alert(AlertType.INFORMATION)
      dlg.setContentText(ev.getData)
      dlg.showAndWait()
    })
    engine.getLoadWorker.stateProperty.addListener((observable, oldValue, newValue) => {
      if (newValue == Worker.State.SUCCEEDED) {
        setScriptAreaFont(engine)
        view.requestFocus()
        val window = engine.executeScript("window").asInstanceOf[JSObject]
        window.setMember("javaBridge", bridge)
        engine.executeScript( s"""editor.setTheme("${Variables.theme}")""")
      }
    })

    engine.load(getClass.getResource("ace.html").toExternalForm)
  }

  private def setStatus(s: String) = onEventThread {
    statusBar.setText(s)
  }

  private def postGist(token: Option[String]) = {
    val scriptArea = tabPane.getSelectionModel.getSelectedItem.getContent.asInstanceOf[WebView]
    val code = scriptArea.getEngine.executeScript("editor.getValue()").toString
    val dlg = new TextInputDialog()
    dlg.setTitle("Gist Description")
    dlg.setHeaderText(null)

    val result = dlg.showAndWait()
    result.ifPresent(description => if (code != null && code.nonEmpty) {
      setStatus("Posting to gist...")
      startTask {
        val msg = Gist.post(code, token, description)
        setStatus(msg)
      }
    } else {
      setStatus("Empty Content. Not posting.")
    })
  }

  private def resetRepl(cls: Boolean = true) = {
    commandQueue.put('Normal -> "\n\n:q")
    implicit val ec = ExecutionContext.fromExecutor(_.run())
    synchronizer.foreach { _ =>
      synchronizer = startRepl()
      if (cls) onEventThread {
        outputArea.clear()
      }
      setStatus("Repl reset.")
    }
  }

  def addArtifacts(strs: Seq[String]): Unit = {
    if (strs.nonEmpty) {
      for (str <- strs; artifact <- Artifact(str)) {
        DependencyManager.addArtifact(artifact)
      }
      ClassLoaderManager.reset()
      setStatus("Resolving artifacts...")
      resetRepl(cls = false)
    } else {
      setStatus("No new artifacts.")
    }
  }

  def updateArtifacts(strs: Seq[String]): Unit = {
    val reduced = for (str <- strs;
                       artifact <- Artifact(str)) yield artifact
    if (DependencyManager.currentArtifacts.length > reduced.length) {
      // 有变化
      DependencyManager.replaceCurrentArtifacts(reduced)
      ClassLoaderManager.reset()
      setStatus("Resolving artifacts...")
      resetRepl(cls = false)
    } else {
      setStatus("No artifacts reduced.")
    }
  }

  def setScriptAreaFont(engine: WebEngine): Unit = {
    val f = Variables.displayFont
    onEventThread {
      val doc = engine.getDocument
      val editor = doc.getElementById("editor")
      val css = s"font-family:${f.getFamily}; font-size: ${f.getSize}px"
      editor.setAttribute("style", css)
    }
  }

  def setFontForAllScriptArea(): Unit = {
    for (tab <- tabPane.getTabs.asScala) {
      val engine = tab.getContent.asInstanceOf[WebView].getEngine
      setScriptAreaFont(engine)
    }
  }

}
