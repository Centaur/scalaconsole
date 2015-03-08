package org.scalaconsole.net

import java.net._
import javafx.scene.input.{Clipboard, ClipboardContent}
import javax.json.Json

import org.scalaconsole.fxui.FxUtil

object Gist {
  def post(content: String, accessToken: Option[String], description: String = "Post By ScalaConsole"): String = {

    val x = new URL("https://api.github.com/gists" + accessToken.fold("")("?access_token=" + _ + "&scope=gist"))
    val conn = x.openConnection.asInstanceOf[HttpURLConnection]

    conn.setDoInput(true)
    conn.setDoOutput(true)
    conn.setUseCaches(false)
    conn.setRequestMethod("POST")
    conn.setRequestProperty("Content-type", "text/json; charset=UTF-8")
    val requestJSON = """{
    "description": "%s",
    "public": true,
    "files": {
      "fromScalaConsole.scala": {"content": "%s"}
    }}""".format(description,
                 content
                   .replaceAll( """\\""", """\\\\""")
                   .replaceAll( """\"""", """\\"""")
                   .replaceAll("\n", "\\\\n")
                   .replaceAll("\r", "\\\\r"))

    conn.getOutputStream.write(requestJSON.getBytes("UTF-8"))

    val json = Json.createReader(conn.getInputStream).readObject()
    conn.getResponseCode match {
      case 201 =>
        val url = json.getString("html_url")
        val copyContent = new ClipboardContent
        copyContent.putString(url)

        FxUtil.onEventThread {
                               Clipboard.getSystemClipboard.setContent(copyContent)
                             }

        "New Gist created at %s. URL has been copied to clipboard.".format(url)
      case _ =>
        "Post to gist failed. Error: %s".format(json.getString("message"))
    }
  }
}


