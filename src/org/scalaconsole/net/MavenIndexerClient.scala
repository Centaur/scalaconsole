package org.scalaconsole
package net

import java.net.URLEncoder
import com.google.gson.{JsonElement, JsonParser}
import collection.JavaConverters.asScalaSetConverter

/**
 * Created by IntelliJ IDEA.
 * User: xf
 * Date: 11-12-6
 * Time: 下午5:06
 */

object MavenIndexerClient {
  type Artifacts = Map[String, JsonElement]

  def search(keywords: String): (Artifacts, Artifacts) = {
    val response = io.Source.fromURL("http://maven-index.gtan.com/search?q=" + URLEncoder.encode(keywords, "UTF-8")).mkString
    val json = new JsonParser().parse(response).getAsJsonObject
    (entrySet2Scala(json.get("exact").getAsJsonObject.entrySet),
      entrySet2Scala(json.get("others").getAsJsonObject.entrySet))
  }

  private def entrySet2Scala(entrySet: java.util.Set[java.util.Map.Entry[String, JsonElement]]): Map[String, JsonElement] = {
    entrySet.asScala.map {
      case entry: java.util.Map.Entry[String, JsonElement] => (entry.getKey, entry.getValue)
    }.toMap
  }
}