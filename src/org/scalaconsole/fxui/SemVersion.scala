package org.scalaconsole.fxui

sealed trait ExtraVersion
//case class GA(slash: String) extends Ordered[ExtraVersion] {
//  override def compare(that: ExtraVersion) = that match {
//    case GA(slash2) =>
//    case RC(_) =>
//  }
//}
//case class Beta(no: Int) extends Ordered[ExtraVersion] {
//  override def compare(that: ExtraVersion) = that match {
//    case GA(_) => -1
//    case RC(_) => -1
//    case Beta(no2) => no - no2
//}
//case class RC(no: Int) extends Ordered[ExtraVersion] {
//  override def compare(that: ExtraVersion) = that match {
//    case GA(_) => -1
//    case Beta(_) => 1
//    case RC(no2) => no - no2
//  }
//}


case class SemVersion(major: Int, minor: Int, patch: Option[Int], extra: ExtraVersion) /*extends Ordered[SemVersion]*/{
  def canMatch(another: SemVersion): Boolean = another match {
    case SemVersion(maj, min, pa, ex) => true
    case _ => false
  }

  def main = (major, minor, patch)

//  override def compare(that: SemVersion):Int = that match {
//    case SemVersion(maj, min, pa, ex) =>
//      if(major > maj) 1
//      else if(major < maj) -1
//      else if(minor > min) 1
//      else if(minor < min) -1
//      else if(patch.isDefined && pa.isEmpty)
//  }
}

object SemVersion {
  def apply(str: String): SemVersion = ???
}
