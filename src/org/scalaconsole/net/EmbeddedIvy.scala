package org.scalaconsole
package net

import org.apache.ivy._
import org.apache.ivy.core.settings._
import org.apache.ivy.core.module.descriptor._
import org.apache.ivy.core.module.id._
import org.apache.ivy.core.resolve._
import org.scalaconsole.fxui.Constants
import plugins.resolver._


object EmbeddedIvy {

  val repositories =
    ('repox, "http://114.80.200.226:8078/") ::
    Nil

  case class TransitiveResolver(m2Compatible: Boolean, name: String, patternRoot: String) extends IBiblioResolver {
    setM2compatible(m2Compatible)
    setName(name)
    setRoot(patternRoot)
  }

  def resolve(groupId: String, artifactId: String, version: String) = {
    //creates clear ivy settings
    val ivySettings = new IvySettings()
    //adding maven repo resolver
    //url resolver for configuration of maven repo
    val chainResolver = new ChainResolver
    for ((name, url) <- repositories) {
      chainResolver.add(TransitiveResolver(m2Compatible = true, name = name.name, patternRoot = url))
    }
    ivySettings.addResolver(chainResolver)
    //set to the default resolver
    ivySettings.setDefaultResolver(chainResolver.getName)
    //creates an Ivy instance with settings
    val ivy = Ivy.newInstance(ivySettings)
    val md = if(Constants.isWindows)
      DefaultModuleDescriptor.newCallerInstance(
      Array(ModuleRevisionId.newInstance(groupId, artifactId, version)),
      true, false
    ) else
      DefaultModuleDescriptor.newCallerInstance(
      ModuleRevisionId.newInstance(groupId, artifactId, version),
      Array("*->*,!sources,!javadoc"), true, false
    )
    //init resolve report
    val options = new ResolveOptions
    val report = ivy.resolve(md, options)
    //so you can get the jar library
    report.getAllArtifactsReports map (_.getLocalFile)
  }

  def resolveScala(version: String): List[java.io.File] = {
    for {lib <- ScalaCoreLibraries.toList
         file <- resolve("org.scala-lang", lib, version)} yield file
  }
}
