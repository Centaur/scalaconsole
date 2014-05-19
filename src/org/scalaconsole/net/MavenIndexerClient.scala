package org.scalaconsole
package net

import java.net.{URLEncoder, URL}
import util.parsing.json.JSON
import java.io.InputStreamReader
import com.google.gson.JsonParser
import com.google.common.io.{CharStreams, ByteStreams}
import com.google.common.base.Charsets


/**
 * Created by IntelliJ IDEA.
 * User: xf
 * Date: 11-12-6
 * Time: 下午5:06
 */

object MavenIndexerClient {
  def search(keywords: String) = {
    val response = io.Source.fromURL("http://maven-index.gtan.com/search?q=" + URLEncoder.encode(keywords, "UTF-8")).mkString
    val json = new JsonParser().parse(response).getAsJsonObject
    (json.get("exact").getAsJsonObject, json.get("others").getAsJsonObject)
  }
}