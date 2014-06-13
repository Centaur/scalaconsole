package org.scalaconsole.fxui.main
import java.util.ResourceBundle
import java.net.URL
import java.util.concurrent.Executor
import javafx.scene.web.{WebEvent, WebEngine, WebView}
import javafx.scene.control._
import org.scalaconsole.fxui.{Variables, JavaBridge}
import javafx.fxml.FXML
import javafx.event.ActionEvent
import org.controlsfx.dialog.Dialogs
import org.scalaconsole.fxui.reduce.ReduceStage
import org.scalaconsole.fxui.manual.ManualStage
import org.scalaconsole.fxui.search.SearchArtifactStage
import javafx.geometry.Orientation
import org.scalaconsole.fxui.FxUtil._
import org.scalaconsole.net.{OAuthTinyServer, Gist}
import org.scalaconsole.data.{ClassLoaderManager, DependencyManager, Artifact}

import collection.JavaConverters._
import javafx.beans.value.{ObservableValue, ChangeListener}
import javafx.concurrent.Worker
import javafx.concurrent.Worker.State
import netscape.javascript.JSObject

import scala.concurrent.ExecutionContext

trait MainController {self: MainStage =>
  @FXML var resources : ResourceBundle = _
  @FXML var location  : URL            = _
  @FXML var scriptArea: WebView        = _
  @FXML var outputArea: TextArea       = _
  @FXML var statusBar : Label          = _
  @FXML var splitPane : SplitPane      = _
  @FXML var tabPane   : TabPane        = _

  private def currentEngine = tabPane.getSelectionModel.getSelectedItem.getContent.asInstanceOf[WebView].getEngine

  private def runScript(script: String) = {
    commandQueue.put('Normal, script)
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
    commandQueue.put('Paste, script)
  }

  @FXML def onRunInPasteMode(event: ActionEvent) {
    val script = currentEngine.executeScript("editor.session.getTextRange(editor.getSelectionRange())").toString
    runPaste(script)
  }

  @FXML def onRunSelectedInPasteMode(event: ActionEvent) {
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

  @FXML def onReplReset(event: ActionEvent) {
    resetRepl()
  }

  @FXML def onSetCommandlineOptions(event: ActionEvent) {
    val current = Variables.commandlineOption
    val masth = "Example: -Xprint:typer"
    val msg = s"current: ${current.getOrElse("none")}"
    val result = Dialogs.create().title("Set Commandline Options").masthead(masth).message(msg).showTextInput(current.getOrElse(""))
    if (result != null && Variables.commandlineOption.getOrElse("") != result) {
      Variables.commandlineOption = Some(result)
      resetRepl()
    }
  }

  @FXML def onSetFont(event: ActionEvent) {
    val masth = "Example: Consolas-14 or Ubuntu Mono-17"
    val f = Variables.displayFont
    val fontAsString = Variables.encodeFont(f)
    val msg = s"current: $fontAsString"
    val result = Dialogs.create().title("Set Display Font").masthead(masth).message(msg).showTextInput(fontAsString)
    if (result != null) {
      Variables.displayFont = Variables.decodeFont(result)
      setOutputAreaFont()
      setFontForAllScriptArea()
      setStatus(s"Font set to $result")
    }
  }

  @FXML def onToggleSplitterOrientation(event: ActionEvent) {
    import Orientation._
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
  }

  private def setOutputAreaFont() = {
    val f = Variables.displayFont
    outputArea.setFont(f)
  }

  private def initWebView(view: WebView) {
    val engine: WebEngine = view.getEngine
    view.visibleProperty.addListener { (p1: ObservableValue[_ <: java.lang.Boolean], old: java.lang.Boolean, visible: java.lang.Boolean) =>
      if (visible) view.requestFocus()
    }
    engine.setOnAlert((ev: WebEvent[String]) => Dialogs.create().masthead(null).message(ev.getData).showInformation())
    engine.getLoadWorker.stateProperty.addListener { (p1: ObservableValue[_ <: State], oldState: State, newState: State) =>
      if (newState == Worker.State.SUCCEEDED) {
        setScriptAreaFont(engine)
        view.requestFocus()
        val window = engine.executeScript("window").asInstanceOf[JSObject]
        window.setMember("javaBridge", bridge)
      }
    }
    engine.load(getClass.getResource("ace.html").toExternalForm)
  }

  private def setStatus(s: String) = onEventThread {
    statusBar.setText(s)
  }

  private def postGist(token: Option[String]) = {
    val scriptArea = tabPane.getSelectionModel.getSelectedItem.getContent.asInstanceOf[WebView]
    val code = scriptArea.getEngine.executeScript("editor.getValue()").toString
    val description = Dialogs.create().title("Gist Description").masthead(null).showTextInput()
    if (code != null && code.nonEmpty) {
      setStatus("Posting to gist...")
      startTask {
        val msg = Gist.post(code, token, description)
        setStatus(msg)
      }
    } else {
      setStatus("Empty Content. Not posting.")
    }
  }

  private def resetRepl(cls: Boolean = true) = {
    commandQueue.put('Normal -> ":q")
    implicit val ec = ExecutionContext.fromExecutor(new Executor {
      override def execute(command: Runnable) = command.run()
    })
    synchronizer.onSuccess { case _ =>
        synchronizer = startRepl()
        if (cls) onEventThread {
          outputArea.clear()
        }
        setStatus("Repl reset.")
    }
  }

  def addArtifacts(strs: Seq[String]) = {
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

  def updateArtifacts(strs: Seq[String]) = {
    val reduced = strs.map(Artifact.apply).filter(_.nonEmpty).map(_.get)
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

  def setScriptAreaFont(engine: WebEngine) = {
    val f = Variables.displayFont
    onEventThread {
      val doc = engine.getDocument
      val editor = doc.getElementById("editor")
      val css = s"font-family:${f.getFamily}; font-size: ${f.getSize}px"
      editor.setAttribute("style", css)
    }
  }

  def setFontForAllScriptArea() = {
    for (tab <- tabPane.getTabs.asScala) {
      val engine = tab.getContent.asInstanceOf[WebView].getEngine
      setScriptAreaFont(engine)
    }
  }

}
