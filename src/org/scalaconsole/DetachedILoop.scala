package org.scalaconsole

import tools.nsc.Settings
import tools.nsc.interpreter.{SimpleReader, ILoop}
import java.io.{BufferedReader, PrintWriter}


class DetachedILoop(_is: BufferedReader, _out: PrintWriter) extends ILoop(None, _out) {
  override def chooseReader(settings: Settings) = new SimpleReader(_is, _out, true) {
    override def readOneLine(): String = {
      val input = super.readOneLine()
      _out.println(input)
//      _out.flush()
      input
    }
  }

}