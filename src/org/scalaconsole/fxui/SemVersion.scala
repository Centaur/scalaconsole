package org.scalaconsole.fxui


sealed trait ExtraVersion extends Ordered[ExtraVersion] {
  override def compare(that: ExtraVersion): Int = (this, that) match {
    case (GA(s1), GA(s2)) => s1 - s2
    case (GA(_), _) => 1
    case (RC(_), GA(_)) => -1
    case (RC(rc1), RC(rc2)) => rc1 - rc2
    case (RC(_), _) => 1
    case (BETA(_), SNAPSHOT(_)) => 1
    case (BETA(_), M(_)) => 1
    case (BETA(b1), BETA(b2)) => b1 - b2
    case (BETA(_), _) => -1
    case (M(_), SNAPSHOT(_)) => 1
    case (M(m1), M(m2)) => m1 - m2
    case (M(_), _) => -1
    case (SNAPSHOT(s1), SNAPSHOT(s2)) => s1.compareTo(s2)
    case (SNAPSHOT(_), _) => -1
  }
}

case class GA(minus: Int) extends ExtraVersion {
  override def toString: String = if (minus == 0) "" else s"-$minus"
}

case class BETA(no: Int) extends ExtraVersion {
  override def toString: String = s"-Beta$no"
}

case class RC(no: Int) extends ExtraVersion {
  override def toString: String = s"-RC$no"
}

case class M(no: Int) extends ExtraVersion {
  override def toString: String = s"M$no"
}

case class SNAPSHOT(date: String) extends ExtraVersion {
  override def toString: String = s"SNAPSHOT$date"
}

case class SemVersion(major: Int, minor: Int, patch: Option[Int], extra: ExtraVersion) extends Ordered[SemVersion] {
  def canMatch(another: SemVersion): Boolean = another match {
    case SemVersion(maj, min, pa, ex) => true
    case _ => false
  }

  def fuzzyMatch(that: SemVersion): Boolean = {
    this match {
      case SemVersion(maj, min, None, GA(_)) if maj == that.major && min == that.minor => true
      case _ => false
    }
  }

  def fuzzyMatch(that: Option[SemVersion]): Boolean = that match {
    case None => false
    case Some(v) => fuzzyMatch(v)
  }

  def stringPresentation = s"$major-$minor" + patch.fold("")("-" + _.toString) + extra.toString

  override def compare(that: SemVersion): Int =
    if (major > that.major) 1
    else if (major < that.major) -1
    else if (minor > that.minor) 1
    else if (minor < that.minor) -1
    else {
      val thisPatch = patch.getOrElse(0)
      val thatPatch = that.patch.getOrElse(0)
      if (thisPatch > thatPatch) 1
      else if (thisPatch < thatPatch) -1
      else extra.compare(that.extra)
    }
}

object SemVersion {
  val R             = """.*_(\d+\.\d+(?:\.\d+)?(?:[-\.].+)?)""".r
  val SplitExtra    = """(\d+)\.(\d+)(\.(\d+))?([-\.](.*))?""".r
  val ExtraRC       = """RC(\d+)""".r
  val ExtraBETA     = """Beta(\d+)""".r
  val ExtraM        = """M(\d+)""".r
  val ExtraSNAPSHOT = """SNAPSHOT(\d+)?""".r
  val Minus         = """(\d+)""".r

  def apply(str: String): Option[SemVersion] = str match {
    case SplitExtra(maj, min, _, patch, _, extra) => Option(extra) match {
      case None => Some(new SemVersion(maj.toInt, min.toInt, Option(patch).map(_.toInt), GA(0)) {
        override def stringPresentation: String = str
      })
      case Some(e) =>
        val extraVersion: ExtraVersion = e match {
          case ExtraRC(no) => RC(no.toInt)
          case ExtraBETA(no) => BETA(no.toInt)
          case ExtraSNAPSHOT(date) => SNAPSHOT(date)
          case ExtraM(no) => M(no.toInt)
          case Minus(no) => GA(no.toInt)
        }
        Some(new SemVersion(maj.toInt, min.toInt, Option(patch).map(_.toInt), extraVersion) {
          override def stringPresentation: String = str
        })
    }
    case _ => None
  }
}
