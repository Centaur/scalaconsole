package org.scalaconsole.ui

import javax.swing.text.DocumentFilter.FilterBypass
import javax.swing.text._
import java.lang.String
import java.nio.CharBuffer
import collection.immutable.TreeSet
import util.matching.Regex

class StructuredSyntaxDocumentFilter(val styledDocument: DefaultStyledDocument) extends DocumentFilter {
//  implicit def int2Orderable(i: Int) = new Orderable{
//    def value = i
//  }

  val TAB_REPLACEMENT = "  "
  val segment = new Segment
  protected val lexer = new LexerNode

  override def replace(fb: FilterBypass, offset: Int, length: Int, text: String, attrs: AttributeSet) {
    val _text = replaceMetaCharacters(text)
    fb.replace(offset, length, _text, attrs)
    parseDocument(offset, _text.length)
  }

  override def insertString(fb: FilterBypass, offset: Int, text: String, attr: AttributeSet) {
    val _text = replaceMetaCharacters(text)
    fb.insertString(offset, _text, attr)
    parseDocument(offset, _text.length)
  }

  override def remove(fb: FilterBypass, offset: Int, length: Int) {
    // FRICKIN' HACK!!!!! For some reason, deleting a string at offset 0
    // does not get done properly, so first replace and remove after parsing
    if (offset == 0 && length != fb.getDocument.getLength) {
      fb.replace(0, length, "\n", lexer.defaultStyle)
      parseDocument(offset, 2)
      fb.remove(offset, 1)
    }
    else {
      fb.remove(offset, length)
      if (offset + 1 < fb.getDocument.getLength) {
        parseDocument(offset, 1)
      } else if (offset - 1 > 0) {
        parseDocument(offset - 1, 1)
      } else {
        mlTextRunSet = new TreeSet[Int]
      }
    }
  }

  private def getMultiLineRun(o: Int): Option[MultiLineRun] = {
    if (o > 0) {
      val set = mlTextRunSet.until(o)
      if (!set.isEmpty) {
        val last = set.last.asInstanceOf[MultiLineRun]
        if (last.end >= o) Some(last) else None
      } else None
    } else None
  }

  protected def parseDocument(offset: Int, length: Int) {
    def calcBeginParse: Int = {
      getMultiLineRun(offset).map(_.start).getOrElse {
        val paragraphBegin = paragraphStart(offset)
        getMultiLineRun(paragraphBegin) match {
          case None => paragraphBegin
          case Some(line) => line.end + 1
        }
      }
    }
    def calcEndParse: Int = {
      val v = offset + length
      getMultiLineRun(v).map(_.end).getOrElse {
        val paragraphFinish = paragraphEnd(v)
        getMultiLineRun(paragraphFinish) match {
          case None => paragraphFinish
          case Some(line) => line.end
        }
      }
    }
    // initialize the segment with the complete document so the segment doesn't
    // have an underlying gap in the buffer
    styledDocument.getText(0, styledDocument.getLength, segment)
    val buffer = CharBuffer.wrap(segment.array).asReadOnlyBuffer
    val begin = calcBeginParse
    val end = calcEndParse
    mlTextRunSet --= mlTextRunSet.from(begin).to(end)
    val (offsetToParse, lengthToParse) = (begin, end - begin)
    // parse the document
    lexer.parse(buffer, offsetToParse, lengthToParse)
  }

  private def replaceMetaCharacters(string: String) = if(string != null)
    string.replaceAll("\\t", TAB_REPLACEMENT) else null

  private def paragraphStart(pos: Int) = styledDocument.getParagraphElement(pos).getStartOffset
  private def paragraphEnd(pos: Int) = styledDocument.getParagraphElement(pos).getEndOffset

  final class LexerNode {
    val defaultStyle = StyleContext.getDefaultStyleContext.getStyle(StyleContext.DEFAULT_STYLE)
    val children = collection.mutable.HashMap[String, LexerNode]()
    val styleMap = collection.mutable.HashMap[String, Style]()

    lazy val groupList = "" :: styleMap.keys.toList

    private lazy val overall: Regex = groupList.tail.map("(" + _ + ")").mkString("|").r
    lazy val matcher = overall.pattern.matcher("")

    def parse(buffer: CharBuffer, offset: Int, length: Int) {
      val checkPoint = offset + length
      // we don't need lastBuffer check because buffer is always a new one
      matcher.reset(buffer)

      var matchEnd = offset
      var pointer = offset
      while (matchEnd < checkPoint && matcher.find(pointer)) {
        var groupNum = 0
        do {
          groupNum += 1
          pointer = matcher.start(groupNum)
        } while (pointer == -1)
        if (pointer != matchEnd) {
          pointer = math.min(checkPoint, pointer)
          styledDocument.setCharacterAttributes(matchEnd, pointer - matchEnd, defaultStyle, true)
          if (pointer >= checkPoint) return
        }
        matchEnd = matcher.end(groupNum)
        val style = styleMap(groupList(groupNum))
        styledDocument.setCharacterAttributes(pointer, matchEnd - pointer, style, true)
        if (paragraphStart(pointer) != paragraphStart(matchEnd)) {
          mlTextRunSet += new MultiLineRun(pointer, matchEnd).start
        }

        // parse the child regexps, if any, within a matched block
        for(childNode <- children.get(groupList(groupNum))) {
          childNode.parse(buffer, pointer, matchEnd - pointer)
        }
        // set the offset to start where we left off
        pointer = matchEnd
      }
      if (matchEnd < checkPoint) {
        // if we finished before hitting the end of the checkpoint from
        // no mroe matches, then set ensure the text is reset to the
        // defaultStyle
        styledDocument.setCharacterAttributes(matchEnd, checkPoint - matchEnd, defaultStyle, true)
      }
    }

    def putChild(regex: String, child: LexerNode) {
      def checkRegexp() {
        val checking = regex.replaceAll("\\\\\\(", "X").replaceAll("\\(\\?", "X")
        val checked: Int = checking.indexOf('(')
        if (checked > -1) {
          var msg: String = "Only non-capturing groups allowed:\r\n" + regex + "\r\n"
          msg += " " * checked
          msg += "^"
          throw new IllegalArgumentException(msg)
        }
      }
      checkRegexp()
      children(regex) = child
    }

    def putStyle(regex: String, style: Style): LexerNode = {
      styleMap(regex) = style
      this
    }

    def putStyle(regexs: Seq[String], style: Style): LexerNode = {
      putStyle(regexs.mkString("|"), style)
    }

    def removeStyle(regex: String) {
      styleMap.remove(regex)
      children.remove(regex)
    }
  }

  /**
   * The position tree of multi-line comments.
   */
//  implicit object Orderable extends OrderableOrdering

  protected var mlTextRunSet = new TreeSet[Int]()
//  trait Orderable {def value: Int}

//  trait OrderableOrdering extends Ordering[Orderable] {
//    def compare(x: Orderable, y: Orderable) = x.value - y.value
//  }

//  class MultiLineRun(_start: Int, _end: Int, _delimeterSize: Int = 2) extends Orderable{
  class MultiLineRun(_start: Int, _end: Int, _delimeterSize: Int = 2) {
    if (_start > _end) {
      val msg = "Start offset is after end: "
      throw new BadLocationException(msg, _start)
    }
    require(_delimeterSize >= 1, "Delimiters be at least size 1: " + _delimeterSize)

    val delimeterSize = _delimeterSize
    val end: Int = styledDocument.createPosition(_end).getOffset
    val start = styledDocument.createPosition(_start).getOffset
    val length = end - start

//    override def value = start
  }


//  implicit def position2Orderable(p: Position) = new {
//    def value = p.getOffset
//  }

}