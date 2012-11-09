package org.scalaconsole.ui

import java.awt.Color
import javax.swing.text.{StyleConstants, StyleContext, DefaultStyledDocument}

class ScalaFilter(val doc: DefaultStyledDocument) extends StructuredSyntaxDocumentFilter(doc) {

  import ScalaFilter._

  val sc = StyleContext.getDefaultStyleContext
  val defaultStyle = sc.getStyle(StyleContext.DEFAULT_STYLE)

  val comment = new CustomStyle('COMMENT) {
    italic(true)
    foreground(Color.LIGHT_GRAY.darker.darker)
  }
  val quotes = new CustomStyle('QUOTES) {
    foreground(Color.MAGENTA.darker.darker)
  }
  val digits = new CustomStyle('DIGITS) {
    foreground(Color.RED.darker)
  }
  val operation = new CustomStyle('OPERATION) {
    bold(true)
  }
  val ident = new CustomStyle('IDENT)
  val reservedWords = new CustomStyle('RESERVED_WORD) {
    bold(true)
    foreground(Color.BLUE.darker.darker)
  }
  val leftParens = new CustomStyle('LEFT_PARENS)

  class CustomStyle(name: Symbol) {
    val self = sc.addStyle(name.name, defaultStyle)

    def italic(i: Boolean) {
      StyleConstants.setItalic(self, i)
    }

    def foreground(color: Color) {
      StyleConstants.setForeground(self, color)
    }

    def bold(i: Boolean) {
      StyleConstants.setBold(self, i)
    }
  }

  implicit def customStyle2Style(c: CustomStyle) = c.self

  for((regex, style) <- (SLASH_STAR_COMMENT -> comment) :: (SLASH_SLASH_COMMENT -> comment) ::
    (QUOTES -> quotes) :: (DIGITS -> digits) :: (OPERATION -> operation) ::
    (IDENT -> ident) :: Nil) {
    lexer.putStyle(regex, style)
  }
  lexer.putChild(OPERATION, new LexerNode().putStyle(RESERVED_WORDS, reservedWords).putStyle(LEFT_PARENS, leftParens))
  lexer.putChild(IDENT, new LexerNode().putStyle(RESERVED_WORDS, reservedWords))
}

object ScalaFilter {
  val COMMENT_COLOR = Color.LIGHT_GRAY.darker.darker
  val SLASH_STAR_COMMENT = "/\\*(?s:.)*?(?:\\*/|\\z)"
  val SLASH_SLASH_COMMENT = "//.*"
  val QUOTES = "(?ms:\"{3}(?!\\\"{1,3}).*?(?:\"{3}|\\z))|(?:\"{1}(?!\\\").*?(?:\"|\\Z))"
  val IDENT = "[\\w\\$&&[\\D]][\\w\\$]*"
  val OPERATION = "[\\w\\$&&[\\D]][\\w\\$]* *\\("
  val LEFT_PARENS = "\\("
  val DIGITS = "\\d+?[efld]?"
  val RESERVED_WORDS = Seq("abstract","case","catch","class","def","do","else","extends",
                            "false", "final", "finally", "for", "forSome", "if", "implicit",
                            "import", "lazy", "match", "new", "null", "object", "override",
                            "package", "private", "protected", "return", "sealed", "super",
                            "this", "throw", "trait", "try", "true", "type", "val", "var",
                            "while", "with", "yield"
                          ).map("\\b" + _ + "\\b")


}