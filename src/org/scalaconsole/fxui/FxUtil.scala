package org.scalaconsole.fxui
import javafx.application.Platform
import javafx.concurrent.Task
import java.util.concurrent.Callable
import javafx.event.{EventHandler, Event}
import javafx.util.Callback

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

  implicit class FunctionAsCallable[R](func: () => R) extends Callable[R]{
    override def call() = func()
  }
  implicit class FunctionAsEventHandler[E <: Event](func: E => Any) extends EventHandler[E] {
    override def handle(e: E) = { func(e); () }
  }
  implicit class FunctionAsCallback[P, R](func: P => R) extends Callback[P, R] {
    override def call(p: P) = func(p)
  }
}
