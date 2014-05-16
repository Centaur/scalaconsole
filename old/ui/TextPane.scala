package org.scalaconsole
package ui

import swing.TextComponent
import javax.swing.JTextPane
import java.awt.Point
import swing.event.{Key, KeyPressed}

class TextPane extends TextComponent {
  override lazy val peer = new JTextPane() with SuperMixin {
    def getCaretLineRange = {
      val pos = this.getCaretPosition
      var start = pos - 1
      while (start >= 0 && getText(start, 1) != "\n") start -= 1
      var end = pos
      while (end < this.getDocument.getLength && getText(end, 1) != "\n") end += 1
      (start + 1, end + 1)
    }

    def selectLineIfNoSelection() {
      if (this.getSelectedText == null) {
        val (start, end) = getCaretLineRange
        this.setSelectionStart(start)
        this.setSelectionEnd(end)
      }
    }

    override def copy() {
      selectLineIfNoSelection()
      super.copy()
    }

    override def cut() {
      selectLineIfNoSelection()
      super.cut()
    }

    def duplicateText() {
      if (!(getCaretPosition == getDocument.getLength && getText(getCaretPosition-1, 1)=="\n")) {
        val (savedStart, savedEnd) = (getSelectionStart, getSelectionEnd)
        copy()
        paste()
        paste()
        setSelectionStart(savedStart)
        setSelectionEnd(savedEnd)
      }
    }
  }

  def toggleComments() {
    val prefix = "//"
    var start = if (peer.getSelectedText == null) peer.getCaretPosition - 1 else peer.getSelectionStart - 1
    while (start >= 0 && peer.getText(start, 1) != "\n") start -= 1
    var end = if (peer.getSelectedText == null) peer.getCaretPosition else peer.getSelectionEnd - 1
    val offset = end - peer.getCaretLineRange._1 + 1
    var caretDownOneLine = end
    while (caretDownOneLine < peer.getDocument.getLength && peer.getText(caretDownOneLine, 1) != "\n") caretDownOneLine += 1
    var nextLine = 0
    do {
      caretDownOneLine += 1;
      nextLine += 1
    } while (nextLine < offset && caretDownOneLine < peer.getDocument.getLength && peer.getText(caretDownOneLine, 1) != "\n")
    var p = start
    while (p < end) {
      if (p == -1 || peer.getText(p, 1) == "\n") {
        var temp = p + 1
        var candidate = peer.getText(temp, 1)
        while (candidate.forall(_.isWhitespace) && !candidate.contains("\n")) {
          temp += 1
          candidate = peer.getText(temp, 1)
        }
        if (temp + prefix.length < peer.getDocument.getLength && peer.getText(temp, prefix.length) == prefix) {
          // delete prefix
          peer.getDocument.remove(temp, prefix.length)
          end -= prefix.length
          caretDownOneLine -= prefix.length
          do {
            p += 1
          } while (peer.getText(p, 1) != "\n") // forward to next line
        } else {
          // prepend prefix
          peer.getDocument.insertString(p + 1, prefix, null)
          p += prefix.length
          end += prefix.length
          caretDownOneLine += prefix.length
        }
      } else {
        p += 1
      }
    }
    peer.setCaretPosition(caretDownOneLine.min(peer.getDocument.getLength))
  }

  def duplicateText() {
    peer.duplicateText()
  }

  def deleteLine() {
    val range = peer.getCaretLineRange
    val end = if (range._2 > peer.getDocument.getLength) peer.getDocument.getLength else range._2
    peer.getDocument.remove(range._1, end - range._1)
  }

  def viewToModel(p: Point) = peer.viewToModel(p)

  def modelToView(pos: Int) = peer.modelToView(pos)

  listenTo(this.keys)
  reactions += {
    case KeyPressed(_, Key.D, ControlOrMeta, _) => duplicateText()
    case KeyPressed(_, Key.Slash, ControlOrMeta, _) => toggleComments()
    case KeyPressed(_, Key.K, ControlOrMeta, _) => deleteLine()
  }

}