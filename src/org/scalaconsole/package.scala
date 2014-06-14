package org.scalaconsole
import fxui.FxUtil

object `package` {
  type AIG = Map[String, List[Map[String, String]]]
  type AIMap = Map[String, AIG]

  private[scalaconsole] val ScalaCoreLibraries = Set("scala-compiler", "scala-library", "scala-swing", "scalap", "scala-dbc", "scala-reflect")
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