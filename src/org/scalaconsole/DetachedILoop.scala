package org.scalaconsole

import tools.nsc.Settings
import tools.nsc.interpreter.{SimpleReader, ILoop}
import java.io.{BufferedReader, PrintWriter}


class DetachedILoop(_is:BufferedReader, _out:PrintWriter) extends ILoop(None, _out){
  override def chooseReader(settings: Settings) = new SimpleReader(_is, this.out, true) {
    override def readOneLine(prompt: String) = {
      val input = super.readOneLine(prompt)
      out.write(input)
      out.write("\n")
      out.flush()
      input
    }
  }

}