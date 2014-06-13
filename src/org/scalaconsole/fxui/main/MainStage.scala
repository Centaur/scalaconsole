package org.scalaconsole.fxui.main

import java.io._
import javafx.concurrent.Task
import org.scalaconsole.data.{DependencyManager, ClassLoaderManager}
import java.util.jar.JarFile
import scala.concurrent.{Future, Promise}
import scala.tools.nsc.plugins.PluginDescription
import org.scalaconsole.DetachedILoop
import scala.tools.nsc.Settings
import java.util.concurrent.ArrayBlockingQueue
import org.scalaconsole.fxui.{Constants, Variables, FxUtil}
import javafx.fxml.FXMLLoader

import scala.util.Success

class MainStage extends MainController {

  import FxUtil._

  val commandQueue = new ArrayBlockingQueue[(Symbol, String)](10)

  // 这一对stream是从repl的输出到右侧的textarea的数据通道，不变
  val outputIs = new PipedInputStream(4096)
  val replOs   = new PipedOutputStream(outputIs)

  val sysOutErr = new PrintStream(replOs) {
    override def write(buf: Array[Byte], off: Int, len: Int) {
      val str = new String(buf, off, len)
      replOs.write(str.getBytes)
      replOs.flush()
    }
  }
  System.setOut(sysOutErr)

  def startOutputRenderer() = startTask {
    for (line <- io.Source.fromInputStream(outputIs).getLines()) {
      onEventThread {
        outputArea.appendText(s"$line\n")
      }
    }
  }

  /**
   * 启动一个新的repl，用于切换scala版本和reset时。每次调用它，就生成一对stream和一个Task来运行scala ILoop
   * @return
   */
  def connectToRepl(writer: PrintWriter, pasteFunc: String => Unit) = startTask {
    var quitCommand = false
    while (!quitCommand) {
      commandQueue.take() match {
        case ('Normal, script: String) =>
          writer.write(script)
          if (!script.endsWith("\n")) writer.write("\n")
          writer.flush()
          if (script == ":q")
            quitCommand = true
        case ('Paste, script: String) =>
          println("// Interpreting in paste mode ")
          pasteFunc(script)
          println("// Exiting paste mode. ")
      }
    }
  }

  def isToReader(is: InputStream) = new BufferedReader(new InputStreamReader(is))

  def osToWriter(os: OutputStream) = new PrintWriter(new OutputStreamWriter(os))

  def startRepl(): Future[Unit] = {
    val promise = Promise[Unit]()
    startTask {
      val replIs = new PipedInputStream(4096)
      val scriptWriter = new PrintWriter(new OutputStreamWriter(new PipedOutputStream(replIs)))
      val settings = new Settings
      Variables.commandlineOption.map(settings.processArgumentString)

      for (path <- DependencyManager.boundedExtraClasspath(Variables.currentScalaVersion)) {
        settings.classpath.append(path)
        settings.classpath.value = settings.classpath.value // set settings.classpath.isDefault to false
        // enable plugins
        if (path.endsWith(".jar")) {
          val jar = new JarFile(path)
          for (xml <- Option(jar.getEntry(Constants.PluginXML))) {
            val in = jar getInputStream xml
            val plugin = PluginDescription.fromXML(in)
            if (settings.pluginOptions.value.contains(plugin.name + ":enable")) {
              settings.plugin.appendToValue(path)
            }
            in.close()
          }
        }
      }

      if (ClassLoaderManager.isOrigin) {
        settings.usejavacp.value = true
        val iloop = new DetachedILoop(isToReader(replIs), osToWriter(replOs))
        connectToRepl(scriptWriter, s => iloop.intp.interpret(s))
        iloop.process(settings)
      } else {
        val (cl, scalaBootPath) = ClassLoaderManager.forVersion(Variables.currentScalaVersion)
        settings.usejavacp.value = true
        settings.bootclasspath.value = scalaBootPath
        cl.asContext {
          settings.embeddedDefaults(cl)
          val remoteClazz = Class.forName("org.scalaconsole.DetachedILoop", false, cl)
          val _iloop = remoteClazz.getConstructor(classOf[java.io.BufferedReader], classOf[java.io.PrintWriter]).
                       newInstance(isToReader(replIs), osToWriter(replOs))
          connectToRepl(scriptWriter, { s =>
            val _intp = remoteClazz.getMethod("intp").invoke(_iloop)
            val remoteIMain = Class.forName("scala.tools.nsc.interpreter.IMain", false, cl)
            remoteIMain.getMethod("interpret", classOf[String]).invoke(_intp, s)
          })
          remoteClazz.getMethod("process", classOf[Array[String]]).invoke(_iloop, settings.recreateArgs.toArray)
        }
      }

      replIs.close()
      scriptWriter.close()
      System.gc()
      promise.complete(Success())
    }
    promise.future
  }

  startOutputRenderer()
  var synchronizer = startRepl()

}