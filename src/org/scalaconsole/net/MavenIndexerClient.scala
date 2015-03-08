package org.scalaconsole
package net

import java.net.URLEncoder
import javax.json.{Json, JsonValue}

import scala.collection.JavaConverters.asScalaSetConverter

/**
 * Created by IntelliJ IDEA.
 * User: xf
 * Date: 11-12-6
 * Time: 下午5:06
 */

object MavenIndexerClient {
  type Artifact = Map[String, JsonValue]

  def search(keywords: String): (Artifact, Artifact) = {
    val response = io.Source.fromURL("http://maven-index.gtan.com/search?q=" + URLEncoder.encode(keywords, "UTF-8"))
    val reader = Json.createReader(response.reader())
    val json = reader.readObject()
    (entrySet2Scala(json.getJsonObject("exact").entrySet()),
      entrySet2Scala(json.getJsonObject("others").entrySet()))
  }

  private def entrySet2Scala(entrySet: java.util.Set[java.util.Map.Entry[String, JsonValue]]): Map[String, JsonValue] = {
    entrySet.asScala.map {
      case entry: java.util.Map.Entry[String, JsonValue] => (entry.getKey, entry.getValue)
    }.toMap
  }
}