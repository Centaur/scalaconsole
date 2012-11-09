package org.scalaconsole

import swing._
import akka.actor._
import event.{WindowIconified, Key, KeyPressed, SelectionChanged}
import javax.swing.event.{DocumentEvent, DocumentListener}
import java.awt.{Dimension, Font, Cursor}
import javax.swing.text.DefaultStyledDocument
import java.io.{FileFilter => _, _}
import javax.swing.SwingUtilities
import tools.nsc.{Properties, Settings}
import java.awt.event.{WindowEvent, WindowAdapter, WindowStateListener}
import ui._
import data._
import java.util.jar.JarFile
import xml.XML
import tools.nsc.plugins.PluginDescription
import akka.actor.ActorDSL._


object ScalaConsole extends SimpleSwingApplication {
  System.clearProperty("scala.home")

  val originScalaVersionNumber = Properties.scalaPropOrEmpty("version.number")
  val originScalaVersion = SupportedScalaVersions(originScalaVersionNumber)
  var currentScalaVersion = originScalaVersion

  def isOrigin = currentScalaVersion == originScalaVersion

  var lastFileOperationDirectory: Option[String] = None

  var displayFont = Font.decode(System.getProperty("font", "Monospaced-12"))

  val outputIs = new PipedInputStream(4096)
  val replOs = new PipedOutputStream(outputIs)

  val sysOutErr = new PrintStream(replOs) {
    override def write(buf: Array[Byte], off: Int, len: Int) {
      val str = new String(buf, off, len)
      actor(actorSystem)(new Act {
        replOs.write(str.getBytes)
        replOs.flush()
      })
    }
  }
  System.setOut(sysOutErr)

  val PluginXML = "scalac-plugin.xml"

  def startRepl() {
    actor(actorSystem)(new Act {
      val replIs = new PipedInputStream(4096)
      val scriptWriter = new PrintWriter(new OutputStreamWriter(new PipedOutputStream(replIs)))

      val settings = new Settings
      CommandLineOptions.value.map(settings.processArgumentString)

      for (path <- data.DependencyManager.boundedExtraClasspath(currentScalaVersion)) {
        settings.classpath.append(path)
        // enable plugins
        if (path.endsWith(".jar")) {
          val jar = new JarFile(path)
          for (xml <- Option(jar.getEntry(PluginXML))) {
            val in = jar getInputStream xml
            val packXML = XML load in
            in.close()
            if (PluginDescription.fromXML(packXML).exists {
              plugin => settings.pluginOptions.value.exists(_ == plugin.name + ":enable")
            }) {
              settings.plugin.appendToValue(path)
            }
          }
        }
      }

      if (isOrigin) {
        settings.usejavacp.value = true

        val iloop = new DetachedILoop(isToReader(replIs), osToWriter(replOs))
        writeToRepl = connectToRepl(scriptWriter, {
          s => iloop.intp.interpret(s)
        })
        iloop.process(settings)
      } else {
        val (cl, scalaBootPath) = ClassLoaderManager.forVersion(currentScalaVersion)
        settings.usejavacp.value = true
        settings.bootclasspath.value = scalaBootPath
        cl.asContext {
          settings.embeddedDefaults(cl)
          val remoteClazz = Class.forName("org.scalaconsole.DetachedILoop", false, cl)
          val _iloop = remoteClazz.getConstructor(classOf[java.io.BufferedReader], classOf[java.io.PrintWriter]).
            newInstance(isToReader(replIs), osToWriter(replOs))
          writeToRepl = connectToRepl(scriptWriter, {
            s =>
              val _intp = remoteClazz.getMethod("intp").invoke(_iloop)
              val remoteIMain = Class.forName("scala.tools.nsc.interpreter.IMain", false, cl)
              remoteIMain.getMethod("interpret", classOf[String]).invoke(_intp, s)
          })
          remoteClazz.getMethod("process", classOf[Array[String]]).invoke(_iloop, settings.recreateArgs.toArray)
        }
      }

      replIs.close()
      scriptWriter.close()

    })
  }

  var writeToRepl: ActorRef = _

  def connectToRepl(writer: PrintWriter, pasteFunc: String => Unit) = actor(actorSystem)(new Act{
    become {
      case ('Normal, script: String) =>
        writer.write(script)
        if (!script.endsWith("\n")) writer.write("\n")
        writer.flush()
        if (script == ":q") context.stop(self)
      case ('Paste, script: String) =>
        println("// Interpreting in paste mode ")
        pasteFunc(script)
        println("// Exiting paste mode. ")
    }
  })

  val readFromRepl = new Thread() {
    override def run() {
      for (line <- io.Source.fromInputStream(outputIs).getLines) {
        outputPane.text += (line + "\n")
        outputPane.caret.position = outputPane.text.length
      }
    }
  }
  readFromRepl.start()

  private def isToReader(is: InputStream) = new BufferedReader(new InputStreamReader(is))

  private def osToWriter(os: OutputStream) = new PrintWriter(new OutputStreamWriter(os))


  def textPane = new TextPane {
    peerText =>
    val undoManager = new TextUndoManager
    undoManager.addPropertyChangeListener(Actions.UndoAction)
    undoManager.addPropertyChangeListener(Actions.RedoAction)

    var documentChangedSinceLastRepaint = false;

    font = displayFont
    cursor = Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR)
    var doc: DefaultStyledDocument = new DefaultStyledDocument
    doc.setDocumentFilter(new ScalaFilter(doc))
    doc.addUndoableEditListener(undoManager)
    doc.addDocumentListener(Actions.UndoAction)
    doc.addDocumentListener(Actions.RedoAction)
    doc.addDocumentListener(new DocumentListener {
      def changedUpdate(e: DocumentEvent) {
        documentChangedSinceLastRepaint = true
      }

      def removeUpdate(e: DocumentEvent) {
        documentChangedSinceLastRepaint = true
      }

      def insertUpdate(e: DocumentEvent) {
        documentChangedSinceLastRepaint = true
      }
    })
    peer.setStyledDocument(doc)

    override protected def paintComponent(g: Graphics2D) {
      super.paintComponent(g)
      if (documentChangedSinceLastRepaint) {
        numbersPane.repaint()
        documentChangedSinceLastRepaint = false
      }
    }


    peer.setFocusTraversalKeysEnabled(false)
    listenTo(peerText.keys)

    reactions += {
      case KeyPressed(_, Key.Tab, Key.Modifier.Control, _) =>
        Actions.switchTabAction.apply()
    }

    val numbersPane = new Panel {
      val fontSizeChanged = {
        () =>
          val initialSize = 3 * displayFont.getSize
          minimumSize = new Dimension(initialSize, initialSize)
          preferredSize = new Dimension(initialSize, initialSize)
      }

      fontSizeChanged()

      override protected def paintComponent(g: Graphics2D) {
        super.paintComponent(g)
        val start = peerText.viewToModel(currentScript.peer.getViewport.getViewPosition)
        val end = peerText.viewToModel(new Point(10, currentScript.peer.getViewport.getViewPosition.y + peerText.peer.getVisibleRect.getHeight.toInt))
        // translate offsets to lines
        val doc = peerText.peer.getDocument
        val startline = doc.getDefaultRootElement.getElementIndex(start) + 1
        val endline = doc.getDefaultRootElement.getElementIndex(end) + 1
        val fontHeight = g.getFontMetrics(displayFont).getHeight
        val fontDesc = g.getFontMetrics(displayFont).getDescent
        val startingY = peerText.modelToView(start).y + fontHeight - fontDesc
        g.setFont(displayFont)
        var line = startline
        var y = startingY
        while (line <= endline) {
          g.drawString("% 4d".format(line), 0, y)
          y += fontHeight
          line += 1
        }
      }
    }

  }


  val tabPane: TabbedPane = new TabbedPane {
    focusable = false
  }

  def currentScript = tabPane.selection.page.content.asInstanceOf[ScriptScrollPane]

  class ScriptScrollPane extends ScrollPane {
    val text = textPane
    viewportView = new BorderPanel {

      import BorderPanel.Position

      add(text.numbersPane, Position.West)
      add(text, Position.Center)
    }
  }

  val outputPane = new EditorPane {
    font = displayFont
    editable = false
    background = new Color(255, 255, 218)
  }

  val statusBar = new Label {
    xAlignment = Alignment.Left
  }

  def updateStatusBar(text: String) {
    statusBar.text = "  " + text
  }

  lazy val top = new MainFrame {
    iconImage = SysIcon.icon
    title = "ScalaConsole"
    menuBar = MainMenuBar

    val splitPane = new SplitPane(Orientation.Vertical) {
      topComponent = tabPane

      bottomComponent = new ScrollPane {
        viewportView = outputPane

      }
      preferredSize = new Dimension(1024, 633)

      dividerLocation = 633
      resizeWeight = 1.0
      dividerSize = 3
    }
    contents = new BorderPanel {
      add(splitPane, BorderPanel.Position.Center)
      add(statusBar, BorderPanel.Position.South)
    }

    Actions.newTabAction.apply()

    listenTo(MainMenuBar.versions.selection)
    listenTo(this)
    reactions += {
      case SelectionChanged(_) =>
        updateStatusBar("Switching Scala Version...")
        SwingUtilities.invokeLater(new Runnable() {
          def run() {
            val selected = MainMenuBar.versions.selection.item
            currentScalaVersion = SupportedScalaVersions(selected)
            Actions.resetReplAction.apply()
            updateStatusBar("Ready")
          }
        })
        currentScript.text.requestFocus()
      case WindowIconified(w) =>
        if (SysIcon.supported) w.visible = false
    }
    SysIcon.init()
    centerOnScreen()
    startRepl()
    updateStatusBar("Ready")
    currentScript.text.requestFocus()
  }


  def runScript(mode: Symbol, selectedOnly: Boolean = false) {
    val toRun = if (selectedOnly) {
      Option(currentScript.text.selected) getOrElse ""
    } else currentScript.text.text
    if (toRun.nonEmpty)
      writeToRepl !(mode, toRun)
  }


  class UpdateCaretListener(title: String) extends Action(title) with DocumentListener {
    var lastUpdate: Int = _

    def apply() {
      currentScript.text.caret.position = lastUpdate
    }

    def changedUpdate(e: DocumentEvent) {}

    def removeUpdate(e: DocumentEvent) {
      lastUpdate = e.getOffset
    }

    def insertUpdate(e: DocumentEvent) {
      lastUpdate = e.getOffset + e.getLength
    }

  }

}

