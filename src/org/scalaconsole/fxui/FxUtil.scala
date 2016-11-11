package org.scalaconsole.fxui

import javafx.application.Platform
import javafx.concurrent.Task
import javafx.fxml.FXMLLoader
import javafx.scene.Scene


object FxUtil {
  def onEventThread(r: => Unit): Unit = Platform.runLater(() => r)

  def startTask[T](t: => T): Unit = {
    val task = new Task[T] {
      override def call(): T = t
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
}
