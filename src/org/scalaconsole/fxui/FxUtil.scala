package org.scalaconsole.fxui
import javafx.application.Platform
import javafx.concurrent.Task

object FxUtil {
  def onEventThread(r: => Unit) = Platform.runLater(new Runnable() {
    override def run() = r
  })

  def startTask[T](t: => T): Unit = {
    val task = new Task[T] {
      override def call() = t
    }
    val thread = new Thread(task)
    thread.setDaemon(true)
    thread.start()
  }
}
