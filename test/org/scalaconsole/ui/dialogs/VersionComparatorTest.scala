package org.scalaconsole.ui.dialogs

import org.scalaconsole.fxui.SemVersion
import org.specs2._

class VersionComparatorTest extends Specification {
  def is = s2"""VersionComparator.sorter
    2.9.0 < 2.10.0.RC1"  $e1
    2.9.0-SNAPSHOT < 2.9.0.RC1"  $e2
    2.9.0-SNAPSHOT < 2.9.0" ! $e3
    2.9.0-SNAPSHOT >= 2.8.1 $e4
    2.8.1 < 2.10.0" $e5
    2.8.1 < 2.9.0"  $e6"""

  def e1 = SemVersion("2.9.0").get < SemVersion("2.10.0.RC1").get must beTrue

  def e2 = SemVersion("2.9.0-SNAPSHOT").get < SemVersion("2.9.0.RC1").get must beTrue

  def e3 = SemVersion("2.9.0-SNAPSHOT").get < SemVersion("2.9.0").get must beTrue

  def e4 = SemVersion("2.9.0-SNAPSHOT").get < SemVersion("2.8.1").get must beFalse

  def e5 = SemVersion("2.8.1").get < SemVersion("2.10.0").get must beTrue

  def e6 = SemVersion("2.8.1").get < SemVersion("2.9.0").get must beTrue
}
