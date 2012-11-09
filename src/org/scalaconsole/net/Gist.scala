package org.scalaconsole.net

import java.net._
import util.parsing.json.JSON
import java.awt.Toolkit
import java.awt.datatransfer.{Transferable, Clipboard, ClipboardOwner, StringSelection}

object Gist extends ClipboardOwner {
  def post(content: String, accessToken: Option[String], description: String = "Post By ScalaConsole") = {

    val x = new URL("https://api.github.com/gists" + accessToken.map("?access_token=" + _ +"&scope=gist").getOrElse(""))
    val conn = x.openConnection.asInstanceOf[HttpURLConnection]

    conn.setDoInput(true)
    conn.setDoOutput(true)
    conn.setUseCaches(false)
    conn.setRequestMethod("POST")
    conn.setRequestProperty("Content-type", "text/json")
    conn.connect()
    val requestJSON = """{
    "description": "%s",
    "public": true,
    "files": {
      "fromScalaConsole.scala": {"content": "%s"}
    }}""".format(description, content.replaceAll( """\\""", """\\\\""").replaceAll( """\"""", """\\"""").replaceAll("\n", "\\\\n"))

    conn.getOutputStream.write(requestJSON.getBytes)

    val buff = new Array[Byte](conn.getContentLength)
    conn.getInputStream.read(buff)
    val json = JSON.parseFull(new String(buff))
    conn.getResponseCode match {
      case 201 =>
        val url = json.get.asInstanceOf[Map[String, Any]]("html_url")
        Toolkit.getDefaultToolkit.getSystemClipboard.setContents(new StringSelection(url.toString), this)
        "New Gist created at %s.\nURL has been copied to clipboard.".format(url)
      case _ =>
        "Post to gist failed. Error: %s".format(json.get.asInstanceOf[Map[String, Any]]("message"))
    }
  }

  def lostOwnership(p1: Clipboard, p2: Transferable) {
    /* do nothing */
  }

}


