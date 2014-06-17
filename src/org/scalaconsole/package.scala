package org.scalaconsole
import fxui.FxUtil

object `package` {

  private[scalaconsole] val ScalaCoreLibraries = Set("scala-compiler", "scala-library", "scalap", "scala-reflect")
  val isMac = System.getProperty("os.name").toLowerCase.contains("mac")

  implicit class Times(val n: Int) {
    def times(b: => Unit) {
      var count = 0
      def worker(): Unit = FxUtil.startTask {
        if (count < n) {
          b
          count += 1
          worker()
        }
      }
      worker()
    }
  }

}