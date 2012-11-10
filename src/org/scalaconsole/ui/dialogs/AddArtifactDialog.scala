package org.scalaconsole
package ui
package dialogs

import swing._
import collection.mutable.ListBuffer
import swing.event._
import java.awt.event.{KeyEvent, ActionEvent, ActionListener}
import javax.swing.border.TitledBorder
import javax.swing.{JOptionPane, SwingUtilities, ImageIcon, KeyStroke, JComponent}
import java.io.IOException
import akka.actor.{Actor, Props}
import akka.actor.ActorDSL._



object VersionComparator {
  val SNAPSHOT = """(.*)-SNAPSHOT""".r
  val RC = """(.*)\.RC(.+)""".r
  val I = """(\d+)(?:\-\d+)?""".r

  def lt(s1: String, s2: String) = {
    def splitToInts(str: String) = str.split("\\.").toList.map {
      case I(i) => i.toInt
      case _ => 0
    }
    import Ordering.Implicits._
    if (s1.length < s2.length && s2.startsWith(s1)) true
    else splitToInts(s1) < splitToInts(s2)
  }

  def sorter:(String, String)=>Boolean = {
    case (SNAPSHOT(v1), SNAPSHOT(v2)) => lt(v1, v2)
    case (SNAPSHOT(v1), RC(v2, _)) if (v1 == v2) => true
    case (RC(v2, _), SNAPSHOT(v1)) if (v1 == v2) => false
    case (SNAPSHOT(v1), RC(v2, _)) => lt(v1, v2)
    case (RC(v1, _), SNAPSHOT(v2)) => lt(v1, v2)
    case (SNAPSHOT(v1), v2) if (v1 == v2) => true
    case (v1, SNAPSHOT(v2)) if (v1 == v2) => false
    case (SNAPSHOT(v1), v2) => lt(v1, v2)
    case (v1, SNAPSHOT(v2)) => lt(v1, v2)
    case (RC(v1, rc1), RC(v2, rc2)) if (v1 == v2) => rc1.toInt < rc2.toInt
    case (RC(v1, _), RC(v2, _)) => lt(v1, v2)
    case (v1, RC(v2, _)) if (v1 == v2) => false
    case (RC(v1, _), v2) if (v1 == v2) => true
    case (v1, RC(v2, _)) => lt(v1, v2)
    case (RC(v1, _), v2) => lt(v1, v2)
    case (v1, v2) => lt(v1, v2)
  }
}

class AddArtifactDialog(parent: Window) extends Dialog(parent) with Cancelable {
  dialog =>


  val dimension_0_0 = new swing.Dimension(0, 0)
  val dimension_100_100 = new swing.Dimension(100, 100)
  val dimension_100_300 = new swing.Dimension(100, 300)
  val dimension_150_400 = new swing.Dimension(150, 400)
  val dimension_600_400 = new swing.Dimension(600, 400)

  var exactRef: AIG = _
  var othersRef: AIG = _
  val R = """.*_(\d+\.\d+\.\d+(?:\-\d+)?(?:\.RC\d+|-SNAPSHOT)?)""".r


  val searchBox = new FlowPanel {
    val searchLabel = new Label("Search:")
    val promptLabel = new Label("(3 chars at least)") {
      visible = false
    }

    def blinkPrompt() {
      3 times {
        promptLabel.foreground = java.awt.Color.red
        Thread.sleep(200)
        promptLabel.foreground = java.awt.Color.black
        Thread.sleep(200)
      }
    }

    val loadingImage = new Label("", new ImageIcon(classOf[AddArtifactDialog].getResource("/loading.gif")), Alignment.Center) {
      visible = false
      opaque = false
    }
    val keywords = new TextField() {
      columns = 32
      listenTo(this.keys)
      listenTo(this)

      def getVersion(aig: AIG) = {
        aig.keySet.collect {
          case R(v) => v
        }
      }

      reactions += {
        case FocusGained(_, _, _) => selectAll()
        case KeyPressed(src: TextField, Key.Enter, _, _) =>
          if (src.text.length < 3) {
            promptLabel.text = "(3 chars at least)"
            promptLabel.visible = true
            blinkPrompt()
          } else {
            src.enabled = false
            promptLabel.visible = false
            loadingImage.visible = true
            actor(actorSystem)(new Act {
              try {
                val result = net.MavenIndexerClient.search(src.text)
                exactRef = result._1
                othersRef = result._2
                crossBuiltBox.contents.clear()
                (exactRef.size, othersRef.size) match {
                  case (0, 0) =>
                    dataBox.splitter.dividerLocation = 80
                    promptLabel.text = "Not found."
                    promptLabel.visible = true
                    blinkPrompt()
                  case (s1, s2) => dataBox.splitter.dividerLocation = 400 * s1 / (s1 + s2)
                }
                dataBox.exactMatch.listData = exactRef.keys.toSeq.sorted
                dataBox.others.listData = othersRef.keys.toSeq.sorted
                dataBox.version.listData = Nil
                val group = new ButtonGroup
                (getVersion(exactRef) | getVersion(othersRef)).toSeq.sortWith(VersionComparator.sorter).foreach {
                  v =>
                    val newBtn = new ToggleButton(v)
                    crossBuiltBox.contents += newBtn
                    group.buttons += newBtn
                    listenTo(newBtn)
                    if (v == ScalaConsole.currentScalaVersion) {
                      newBtn.doClick()
                    }
                }
                crossBuiltBox.preferredSize = if (group.buttons.size > 0) {
                  val all = new ToggleButton("All")
                  crossBuiltBox.contents.insert(0, all)
                  group.buttons += all
                  listenTo(all)
                  new swing.Dimension(600, 35 * (group.buttons.size / 7 + 1))
                } else {
                  dimension_0_0
                }
                src.selectAll()
                dialog.pack()
                dialog.centerOnScreen()
              } catch {
                case e: IOException => JOptionPane.showMessageDialog(null, "Maven index server not available at the moment. Please try again later.")
              }
              loadingImage.visible = false
              src.enabled = true

            })
          }
          src.requestFocus()
        case ButtonClicked(src) =>
          if (src.text == "All") {
            dataBox.exactMatch.listData = exactRef.keys.toSeq.sorted
            dataBox.others.listData = othersRef.keys.toSeq.sorted
          } else {
            dataBox.exactMatch.listData = exactRef.keys.toSeq.filter(_.endsWith(src.text)).sorted
            dataBox.others.listData = othersRef.keys.toSeq.filter(_.endsWith(src.text)).sorted
          }
          dataBox.version.listData = Nil
      }
    }
    contents ++= searchLabel :: keywords :: promptLabel :: loadingImage :: Nil
  }

  val selectedBox = new ListView[String] {
    listenTo(this.mouse.clicks)
    reactions += {
      case MouseClicked(_, _, _, 2, _) =>
        selection.indices.map {
          selected =>
            val split = listData.splitAt(selected)
            listData = split._1 ++ split._2.tail
        }
    }
  }

  val dataBox = new BorderPanel {

    val version: VersionListView = new VersionListView

    class VersionListView extends ListView[String] {
      peer.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION)
      preferredSize = dimension_100_300
      listenTo(this.mouse.clicks)
      reactions += {
        case MouseClicked(_, _, _, 2, _) =>
          for (index <- selection.indices) {
            val newOne = (exactMatch.selection.items, others.selection.items) match {
              case (Seq(), Seq(o)) => o
              case (Seq(e), Seq()) => e
            }

            selectedBox.listData = (selectedBox.listData :+ (newOne + " : " + listData(index))).distinct
          }
      }
    }

    class ExactMatchListView extends ListView[String] {
      peer.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION)
      listenTo(ExactMatchListView.this.selection)
      reactions += {
        case ListSelectionChanged(_, _, _) =>
          selection.items.map {
            aig =>
              val list = exactRef(aig)
              version.listData = list map {
                _("version")
              } sortWith (_ > _)
              others.peer.clearSelection()
              version.selectIndices(version.listData.indexWhere(_ == ScalaConsole.currentScalaVersion))
          }
      }
    }

    val exactMatch: ExactMatchListView = new ExactMatchListView
    val others = new ListView[String]() {
      peer.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION)
      listenTo(this.selection)
      reactions += {
        case ListSelectionChanged(_, _, _) =>
          selection.items.map {
            aig =>
              val list = othersRef(aig)
              version.listData = list map {
                _("version")
              } sortWith (_ > _)
              exactMatch.peer.clearSelection()
              version.selectIndices(version.listData.indexWhere(_ == ScalaConsole.currentScalaVersion))
          }
      }
    }
    add(new ScrollPane() {
      border = new TitledBorder("Version")
      contents = version;
      preferredSize = dimension_150_400
    }, BorderPanel.Position.East)
    val splitter = new SplitPane(Orientation.Horizontal) {
      preferredSize = dimension_600_400
      topComponent = new ScrollPane() {
        border = new TitledBorder("Exact Match")
        contents = exactMatch
      }
      bottomComponent = new ScrollPane() {
        border = new TitledBorder("Others")
        contents = others
      }
      dividerLocation = 80
      dividerSize = 3

    }
    add(splitter, BorderPanel.Position.Center)
  }
  title = "Add Artifacts"
  modal = true
  val artifacts = new ListBuffer[(String, String, String)]

  def oneLine = new BoxPanel(Orientation.Horizontal) {
    contents +=
      new Label("groupId:") +=
      new TextField(10) +=
      new Label("artifactId:") +=
      new TextField(10) +=
      new Label("version:") +=
      new TextField(10)
  }

  val lines = ListBuffer(oneLine)

  object clearBtn extends Button("Clear")

  object okBtn extends Button("OK")

  object cancelBtn extends Button("Cancel")

  def onOK() {
    for (line <- selectedBox.listData) {
      val c = line.split(":").map(_.trim)
      artifacts append ((c(0), c(1), c(2)))
    }
    AddArtifactDialog.this.dispose()
  }


  def onClear() {
    selectedBox.listData = Nil
  }

  reactions += {
    case ButtonClicked(`clearBtn`) => onClear()
    case ButtonClicked(`okBtn`) => onOK()
    case ButtonClicked(`cancelBtn`) => onCancel()
  }

  val verticalBox = new BoxPanel(Orientation.Vertical) {
    contents += lines.last
  }

  val btnFlow = new FlowPanel(FlowPanel.Alignment.Right)(
    clearBtn,
    okBtn,
    cancelBtn
  )

  val crossBuiltBox = new FlowPanel() {
  }

  contents = new BoxPanel(Orientation.Vertical) {

    contents +=
      searchBox +=
      dataBox +=
      crossBuiltBox +=
      new ScrollPane() {
        border = new TitledBorder("Selected")
        contents = selectedBox
        preferredSize = dimension_100_100
      } +=
      btnFlow
    btnFlow.contents.foreach {
      dialog.listenTo(_)
    }
  }

  /*
  peer.getRootPane.registerKeyboardAction(new ActionListener() {
    def actionPerformed(p1: ActionEvent) {
      FocusManager.getCurrentManager.getFocusOwner match {
        case `cancelBtn`.peer => onCancel()
        case `anotherBtn`.peer => onAnother()
        case _ => onOK()
      }
    }
  }, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), JComponent.WHEN_IN_FOCUSED_WINDOW)
  */

}