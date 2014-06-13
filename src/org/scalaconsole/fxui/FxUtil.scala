package org.scalaconsole.fxui
import javafx.application.Platform
import javafx.concurrent.Task
import java.util.concurrent.Callable
import javafx.event.{EventHandler, Event}
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.util.Callback
import javafx.beans.value.{ChangeListener, ObservableValue}
import java.util.Optional

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

  def loadScene(fxml: String, controller: AnyRef): Scene = {
    val loader = new FXMLLoader(getClass.getResource(fxml))
    loader.setController(controller)
    new Scene(loader.load())
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

  type ChangeListenerFunc[T, U] = (ObservableValue[_ <: T], T, T) => U
  implicit class FunctionAsChangeListener[T, U](func: ChangeListenerFunc[T, U]) extends ChangeListener[T] {
    override def changed(p1: ObservableValue[_ <: T], p2: T, p3: T) = {
      func(p1, p2, p3)
      ()
    }
  }

  implicit def OptionalAsOption[T](optional: Optional[T]) =
    if(optional.isPresent) Some(optional.get) else None
}
