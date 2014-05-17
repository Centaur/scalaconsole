package org.scalaconsole.net

import java.net._
import java.awt.Toolkit
import java.awt.datatransfer.{Transferable, Clipboard, ClipboardOwner, StringSelection}
import java.io.StringReader
import com.google.gson.JsonParser
import com.google.common.io.ByteStreams

object Gist extends ClipboardOwner {
  def post(content: String, accessToken: Option[String], description: String = "Post By ScalaConsole"):String = {

    val x = new URL("https://api.github.com/gists" + accessToken.fold("")("?access_token=" + _ + "&scope=gist"))
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

    val contentLength = conn.getContentLength
    val buff = new Array[Byte](contentLength)
    ByteStreams.readFully(conn.getInputStream, buff)
    val json = new JsonParser().parse(new String(buff)).getAsJsonObject
    conn.getResponseCode match {
      case 201 =>
        val url = json.get("html_url").getAsString
        Toolkit.getDefaultToolkit.getSystemClipboard.setContents(new StringSelection(url.toString), this)
        "New Gist created at %s. URL has been copied to clipboard.".format(url)
      case _ =>
        "Post to gist failed. Error: %s".format(json.get("message").getAsString)
    }
  }

  def lostOwnership(p1: Clipboard, p2: Transferable) {
    /* do nothing */
  }

}


