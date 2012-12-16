package org.scalaconsole

import swing.event.Key
import akka.actor._


object `package` {
  type AIG = Map[String, List[Map[String, String]]]
  type AIMap = Map[String, AIG]

  private[scalaconsole] val ScalaCoreLibraries = Set("scala-compiler", "scala-library", "scala-swing", "scalap", "scala-dbc", "scala-reflect")
  val SupportedScalaVersions = Map(
    "2.10.0-RC5" -> "2.10.0-RC5"
  )
  val isMac = System.getProperty("os.name").toLowerCase.contains("mac")
  val ControlOrMeta = if (isMac) Key.Modifier.Meta else Key.Modifier.Control

  implicit class Times(val n: Int) {
    def times(b: => Unit) {
      var count = 0
      def worker: javax.swing.SwingWorker[Unit, Unit] = new javax.swing.SwingWorker[Unit, Unit]() {
        override def doInBackground() {
          if (count < n) {
            b
            count += 1
            worker.execute()
          }
        }
      }
      worker.execute()
    }
  }


  val actorSystem = ActorSystem()

}