package org.scalaconsole
package ui

import javax.swing.filechooser.FileFilter
import java.io._
import swing._
import event.{ButtonClicked, SelectionChanged}
import java.beans.{PropertyChangeEvent, PropertyChangeListener}
import javax.swing.{JOptionPane, KeyStroke, SwingUtilities, JFileChooser}
import java.awt.{BorderLayout, Font}
import data.Artifact
import javax.swing.border.{EtchedBorder, BevelBorder}
import net.OAuthTinyServer
import akka.actor.ActorDSL._
import apple.laf.JRSUIUtils.TabbedPane

object Actions {

  import ScalaConsole._

  def registerAction(title: String, acce: String = "")(action: => Unit) = new Action(title) {
    val shortcut = if (isMac && acce.contains("control"))
      acce.replace("control", "meta")
    else acce

    accelerator = Some(KeyStroke.getKeyStroke(shortcut))

    def apply() {
      action
    }
  }


  def classPathAction(configure: JFileChooser => Unit) {
    val fileChooser = new JFileChooser(lastFileOperationDirectory.getOrElse("~"))
    configure(fileChooser)
    fileChooser.showOpenDialog(top.self) match {
      case JFileChooser.APPROVE_OPTION =>
        val dependencyNeedsReset = data.DependencyManager.addPaths(fileChooser.getSelectedFiles)
        if (dependencyNeedsReset) {
          data.ClassLoaderManager.reset()
          lastFileOperationDirectory = Some(fileChooser.getSelectedFiles.apply(0).getParent)
          resetReplAction.apply()
          updateStatusBar("Classpath changed. Repl reset.")
        } else {
          updateStatusBar("Paths already in classpath. Repl reset canceled.")
        }
      case _ =>
    }
  }

  val manualAddArtifactsAction = registerAction("Manually Add Artifacts ...") {
    val dialog = new dialogs.ManualAddArtifactDialog(top)
    dialog.centerOnScreen()
    dialog.pack()
    dialog.visible = true
  }

  val addArtifactsAction = registerAction("Add Artifacts ...", "control I") {
    val dialog = new dialogs.AddArtifactDialog(top)
    dialog.centerOnScreen()
    dialog.pack()
    dialog.visible = true

    updateStatusBar("Resolving artifacts...")
    actor(actorSystem)(new Act{
      var dependencyNeedsReset = false
      dialog.artifacts foreach {
        case (s1, s2, s3) =>
          val res = data.DependencyManager.addArtifact(Artifact(s1, s2, s3))
          dependencyNeedsReset ||= res
        case x => println("selected:%s".format(x))
      }
      if (dependencyNeedsReset) {
        data.ClassLoaderManager.reset()
        reset(cls = false)
        updateStatusBar("Classpath changed. Repl reset.")
      } else {
        updateStatusBar("Artifact already loaded. Reset canceled.")
      }
    })
  }

  val addJarsAction = registerAction("Add Jars ...", "control J")(classPathAction {
    fileChooser =>
      fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY)
      fileChooser.setFileHidingEnabled(false)
      fileChooser.setMultiSelectionEnabled(true)
      fileChooser.setFileFilter(new FileFilter() {
        def accept(pathname: File) = pathname.isDirectory || pathname.getName.endsWith(".jar")

        def getDescription = "*.jar"
      })
  })
  val addClassesAction = registerAction("Add Classes ...", "control d")(classPathAction {
    fileChooser =>
      fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY)
      fileChooser.setFileHidingEnabled(false)
      fileChooser.setMultiSelectionEnabled(true)
  })

  val openAction = registerAction("Open ...", "control O") {
    val fileChooser = new JFileChooser(lastFileOperationDirectory.getOrElse("~"))

    fileChooser.showOpenDialog(top.self) match {
      case JFileChooser.APPROVE_OPTION =>
        val file = fileChooser.getSelectedFile
        val reader = new BufferedReader(new FileReader(file))
        currentScript.text.text = ""
        var eof = false
        while (!eof) {
          val line = reader.readLine()
          if (line == null) eof = true
          else currentScript.text.text = currentScript.text.text + line + "\n"
        }
        reader.close()
        lastFileOperationDirectory = Some(file.getParent)
      case _ =>
    }

  }

  val saveAction = registerAction("Save ...", "control S") {
    val fileChooser = new JFileChooser(lastFileOperationDirectory.getOrElse("~"))
    fileChooser.showSaveDialog(top.self) match {
      case JFileChooser.APPROVE_OPTION =>
        val file = fileChooser.getSelectedFile
        val writer = new BufferedWriter(new FileWriter(file))
        writer.write(currentScript.text.text)
        writer.close()
        lastFileOperationDirectory = Some(file.getParent)
      case _ =>
    }
  }

  def postGist(token: Option[String]) {
    val toPost = currentScript.text.text
    val description = JOptionPane.showInputDialog("Gist Description")
    if (toPost.trim.nonEmpty) {
      updateStatusBar("Posting to gist ....")
      SwingUtilities.invokeLater(new Runnable() {
        def run() {
          val msg = net.Gist.post(toPost, token, description)
          updateStatusBar(msg)
        }
      })
    } else {
      updateStatusBar("Empty Content. Not posting.")
    }
  }

  val gistAction = registerAction("Post Anonymous Gist", "control G") {
    postGist(None)
  }

  //  lazy val accessToken = net.Gist.requestAccessToken
  val oauthGistAction = registerAction("Login And Post Gist", "control shift G") {
    OAuthTinyServer.withAccessToken(postGist)
  }

  val setFontAction = registerAction("Set Font ...", "control N") {
    def encodeFont(f: Font) = {
      val attr = collection.mutable.Buffer(f.getName, f.getSize)
      f.getStyle match {
        case Font.BOLD => attr.insert(1, "bold")
        case Font.ITALIC => attr.insert(1, "italic")
        case _ =>
      }
      attr.mkString("-")
    }
    val font = JOptionPane.showInputDialog("Font(current: %s):".format(encodeFont(displayFont)))
    if (font != null) {
      displayFont = java.awt.Font.decode(font)
      outputPane.font = displayFont
      for (page <- tabPane.pages) {
        val t = page.content.asInstanceOf[ScriptScrollPane].text
        t.font = displayFont
        t.numbersPane.font = displayFont
        t.numbersPane.fontSizeChanged()
      }
      updateStatusBar("Font set to %s".format(encodeFont(displayFont)))
    }
  }
  val runAction = registerAction("Run", "control R") {
    runScript('Normal)
  }
  val runSelectedAction = registerAction("Run selected", "control shift R") {
    runScript('Normal, true)
  }
  val clearScriptAction = registerAction("Clear", "control L") {
    currentScript.text.text = ""
  }
  val clearOutputAction = registerAction("Clear", "control E") {
    outputPane.text = ""
  }
  val runInPasteModeAction = registerAction("Run in paste mode", "control P") {
    runScript('Paste)
  }
  val runSelectedInPasteModeAction = registerAction("Run selected in paste mode", "control shift P") {
    runScript('Paste, true)
  }
  val cmdOptionsAction = registerAction("Commandline Options", "control M") {
    import data.CommandLineOptions._
    val result = JOptionPane.showInputDialog(
      "Ex: -Xprint:typer(current: %s)".format(value.getOrElse("none")),
      value.getOrElse("")
    )
    if (result != null && data.CommandLineOptions.value.getOrElse("") != result) {
      data.CommandLineOptions.value = Some(result)
      resetReplAction.apply()
    }
  }

  def reset(cls: Boolean = true) {
    writeToRepl !('Normal, ":q")
    startRepl()
    if (cls) outputPane.text = ""
    updateStatusBar("Repl reset.")
  }

  val resetReplAction = registerAction("Reset", "control shift E") {
    reset()
  }
  val newTabAction = registerAction("New Tab", "control T") {
    tabPane.pages += new TabbedPane.Page("Tab" + tabPane.pages.length, new ScriptScrollPane)
    tabPane.selection.index = tabPane.pages.length - 1
  }
  val closeTabAction = registerAction("Close Tab", "control F4") {
    if (tabPane.pages.length > 1)
      tabPane.pages.remove(tabPane.selection.index)
    else updateStatusBar("At lease 1 tab needed.")
  }
  val toggleSplitterOrientationAction = registerAction("Toggle Splitter Orientation", "control W") {
    val splitter = top.splitPane
    splitter.orientation match {
      case Orientation.Vertical => {
        val newHeight = splitter.size.getHeight * (splitter.dividerLocation / splitter.size.getWidth)
        splitter.orientation = Orientation.Horizontal
        splitter.dividerLocation = newHeight.toInt
      }
      case Orientation.Horizontal => {
        val newWidth = splitter.size.getWidth * (splitter.dividerLocation / splitter.size.getHeight)
        splitter.orientation = Orientation.Vertical
        splitter.dividerLocation = newWidth.toInt
      }
    }
  }

  val manageDependencyAction = registerAction("Manage Current ...") {
    val dialog = new dialogs.ManageDependenciesDialog(top)
    dialog.centerOnScreen()
    dialog.pack()
    dialog.visible = true
  }
  // See key reaction definition in textPane. shortcut defined here has no effect.
  val switchTabAction = registerAction("Switch Tab", "control TAB") {
    tabPane.selection.index = tabPane.selection.index match {
      case c if c == tabPane.pages.length - 1 => 0
      case current => current + 1
    }
  }

  val removeAllDependencyProfilesAction = registerAction("Remove All Dependency Profiles") {
    if (JOptionPane.showConfirmDialog(null, "It's dangerous. Remove them all?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
      data.DependencyManager.removeAllProfiles()
  }

  object UndoAction extends UpdateCaretListener("Undo") with PropertyChangeListener {
    if (isMac) accelerator = Some(KeyStroke.getKeyStroke("meta Z"))
    else accelerator = Some(KeyStroke.getKeyStroke("control Z"))
    enabled = false

    override def apply() {
      val undoManager = currentScript.text.undoManager
      undoManager.undo()
      enabled = undoManager.canUndo
      RedoAction.enabled = undoManager.canRedo
      super.apply()
    }

    override def propertyChange(evt: PropertyChangeEvent) {
      val undoManager = currentScript.text.undoManager
      enabled = undoManager.canUndo
    }
  }

  object RedoAction extends UpdateCaretListener("Redo") with PropertyChangeListener {
    title = "Redo"
    if (isMac) accelerator = Some(KeyStroke.getKeyStroke("meta Y"))
    else accelerator = Some(KeyStroke.getKeyStroke("control Y"))
    enabled = false

    override def apply() {
      val undoManager = currentScript.text.undoManager
      undoManager.redo()
      enabled = undoManager.canRedo
      UndoAction.enabled = undoManager.canUndo
      super.apply()
    }

    override def propertyChange(evt: PropertyChangeEvent) {
      val undoManager = currentScript.text.undoManager
      enabled = undoManager.canRedo
    }
  }

}

object MainMenuBar extends MenuBar {
  val versions = new ComboBox[String](SupportedScalaVersions.keys.toSeq) {
    selection.item = ScalaConsole.originScalaVersionNumber
    focusable = false
  }

  val loadAction = new Action("load") {
    def apply() {
      val popupMenu = new PopupMenu {
        for (profile <- data.DependencyManager.loadProfiles) {
          contents += new MenuItem(new Action(profile) {
            def apply() {
              data.DependencyManager.loadProfile(profile)
            }
          })
        }
      }
      if (popupMenu.contents.size > 0)
        popupMenu.show(loadBtn, 0, loadBtn.size.getHeight.toInt)
    }
  }
  val saveBtnAction = new Action("save") {
    def apply() {
      val name = JOptionPane.showInputDialog("Give new profile a name:")
      if (name != null && !name.trim.isEmpty)
        data.DependencyManager.saveCurrentAsProfile(name)
    }
  }
  val loadBtn: Button = new Button(loadAction)
  val profiles = new FlowPanel(
    new Label("Dependency Profile:"),
    loadBtn,
    new Button(saveBtnAction)
  ) {
    border = new EtchedBorder(EtchedBorder.LOWERED)
    opaque = false
  }

  val dependencyPopupMenu = new PopupMenu() {

    import Actions._

    contents +=
      new MenuItem(addArtifactsAction) +=
      new MenuItem(manualAddArtifactsAction) +=
      new MenuItem(addJarsAction) +=
      new MenuItem(addClassesAction) +=
      new MenuItem(manageDependencyAction)
  }
  val versionsAndProfiles = new FlowPanel(new Label("Target Version: "),
    versions,
    profiles,
    new Button {self =>
      action = new Action("Dependency") {
        def apply() {
          dependencyPopupMenu.show(self, 0, self.size.getHeight.toInt)
        }
      }
    }
  ) {
    opaque = false
  }

  peer.setLayout(new BorderLayout)
  peer.add(versionsAndProfiles.peer.asInstanceOf[java.awt.Component], BorderLayout.EAST)
  peer.add(new MenuBar {

    import Actions._

    contents +=
      new Menu("Script") {
        contents += new MenuItem(runAction) +=
          new MenuItem(runSelectedAction) +=
          new MenuItem(runInPasteModeAction) +=
          new MenuItem(runSelectedInPasteModeAction) +=
          new Separator +=
          new MenuItem(openAction) +=
          new MenuItem(saveAction) +=
          new MenuItem(clearScriptAction) +=
          new MenuItem(newTabAction) +=
          new MenuItem(closeTabAction) +=
          new Separator +=
          new MenuItem(gistAction) +=
          new MenuItem(oauthGistAction)
      }

    contents += new Menu("Repl") {
      contents +=
        new MenuItem(clearOutputAction) +=
        new MenuItem(resetReplAction) +=
        new MenuItem(cmdOptionsAction)
    }
    contents += new Menu("Edit") {
      contents +=
        new MenuItem(UndoAction) +=
        new MenuItem(RedoAction) +=
        new Separator +=
        new MenuItem(setFontAction) +=
        new Separator +=
        new MenuItem(toggleSplitterOrientationAction) +=
        new Separator +=
        new MenuItem(removeAllDependencyProfilesAction)
    }
  }.peer, BorderLayout.CENTER)
}
