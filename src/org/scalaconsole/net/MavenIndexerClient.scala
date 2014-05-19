package org.scalaconsole
package net

import java.net.{URLEncoder, URL}
import util.parsing.json.JSON
import java.io.InputStreamReader
import com.google.gson.{JsonElement, JsonParser}
import com.google.common.io.{CharStreams, ByteStreams}
import com.google.common.base.Charsets


/**
 * Created by IntelliJ IDEA.
 * User: xf
 * Date: 11-12-6
 * Time: 下午5:06
 */

object MavenIndexerClient {
  type Artifacts = Set[java.util.Map.Entry[String, JsonElement]]

  def search(keywords: String): (Artifacts, Artifacts) = {
    val response = io.Source.fromURL("http://maven-index.gtan.com/search?q=" + URLEncoder.encode(keywords, "UTF-8")).mkString
    val json = new JsonParser().parse(response).getAsJsonObject
    import collection.JavaConverters.asScalaSetConverter
    (json.get("exact").getAsJsonObject.entrySet.asScala.toSet, json.get("others").getAsJsonObject.entrySet.asScala.toSet)
  }
}