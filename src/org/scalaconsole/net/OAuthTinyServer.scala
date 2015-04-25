package org.scalaconsole
package net

import java.awt.EventQueue
import java.net._
import java.io._

import org.scalaconsole.fxui.{ScalaConsole, FxUtil}

object OAuthTinyServer {
  val client_id = "3d4d9d562d4fd186aa41"
  val client_secret = "d2ce4b5ea37b0bd6e3266868a8d38262b550302d"
  val socket = new ServerSocket(4568)
  val port = socket.getLocalPort

  val redirect_path = "/scalaconsole/callback"
  val redirect_uri = s"http://localhost:$port$redirect_path"
  val authorize_uri =
    s"https://github.com/login/oauth/authorize?client_id=$client_id&redirect_uri=${URLEncoder.encode(redirect_uri, "UTF-8")}&scope=gist"

  val exchange_template = "https://github.com/login/oauth/access_token?client_id=%s&client_secret=%s&code=%s"

  val ExtractCode = """.*code=(.*).*""".r
  val responseMessage = "ScalaConsole Authenticated. Please close this window."

  private[this] var access_token: Option[String] = None

  def accessToken: Option[String] = access_token

  private def accessToken_=(v: Option[String]) {
    access_token = v
  }


  def withAccessToken(callback: Option[String] => Unit) {
    if (accessToken.isDefined) {
      callback(accessToken)
    } else {
      FxUtil.startTask {
        val client = socket.accept()
        val reader = new BufferedReader(new InputStreamReader(client.getInputStream()))
        val request_line = reader.readLine
        request_line.split("\\s") match {
          case Array(method: String, path: String, version: String) if valid(method, path, version) =>
            path match {
              case ExtractCode(code) =>
                val exchange_uri = new URL(exchange_template.format(client_id, client_secret, code))
                val conn = exchange_uri.openConnection().asInstanceOf[HttpURLConnection]
                conn.setRequestMethod("POST")
                val content = io.Source.fromInputStream(conn.getInputStream()).mkString
                accessToken = content.split("&").map(_.split("=")).find(_(0) == "access_token").map(_(1))
                for (token <- accessToken) {
                  FxUtil.onEventThread {
                    callback(Some(token))
                  }
                  writeResponseMessage(client)
                }
                conn.disconnect()
                client.close()
            }
          case _ => throw new RuntimeException("Protocol Error")
        }
      }
      ScalaConsole.application.getHostServices.showDocument(authorize_uri)
    }
  }

  private def writeResponseMessage(client: Socket) {
    val writer = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()))
    writer.write(responseMessage)
    writer.flush()
  }

  def valid(method: String, path: String, version: String) = {
    method.equalsIgnoreCase("GET") && path.contains(redirect_path) && version.startsWith("HTTP/1.")
  }

}
