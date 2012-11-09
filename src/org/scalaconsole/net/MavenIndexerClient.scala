package org.scalaconsole
package net

import java.net.{URLEncoder, URL}
import util.parsing.json.JSON
import java.io.InputStreamReader


/**
 * Created by IntelliJ IDEA.
 * User: xf
 * Date: 11-12-6
 * Time: 下午5:06
 */

object MavenIndexerClient {
  def search(keywords: String) = {
    //    val conn = new URL("http://localhost:8080/search?q="+ URLEncoder.encode(keywords, "UTF-8")).openConnection()
    val conn = new URL("http://maven-index.gtan.com/search?q=" + URLEncoder.encode(keywords, "UTF-8")).openConnection()
    val is = conn.getInputStream
    val reader = new InputStreamReader(is)
    val buff = new Array[Char](1024)
    val sb = new StringBuilder
    var read = reader.read(buff)
    while (read != -1) {
      sb.appendAll(buff, 0, read)
      read = reader.read(buff)
    }
    is.close()
    val json = JSON.parseFull(sb.toString()).get.asInstanceOf[AIMap]
    (json("exact"), json("others"))
  }
}