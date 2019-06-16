# ScalaConsole Reloaded (V2.0)

## What is ScalaConsole?
ScalaConsole is a graphical interface replacement for the Scala language REPL.

## Why do I need ScalaConsole?
* The command line REPL has limited editing functions and can only be edited based on lines. If the input is wrong, it is painful to modify.
    
    ScalaConsole uses a full-featured editor that supports syntax highlighting.
  
* Command line REPL function is limited, for example, the class name of the case class is not well supported. The official recommendation is to use scalac to compile the case class first, and then use REPL to import, which is very troublesome.
    
    ScalaConsole uses scala.nsc directly, which is perfect for case classes and supports all REPL options.
  
* ScalaConsole supports search-based dependency addition
    
    Scala REPL cannot easily add dependencies. The sbt console can use dependencies in the sbt configuration, but it depends on the sbt project. ScalaConsole starts at any time, adding dependencies at any time, without having to create a project first.

## When to use ScalaConsole
When you need to temporarily verify an idea, the correctness of a small piece of code, temporarily understand the API of a Java/Scala library, and don't want to build a complete sbt project for it.

## Version Requirements
* Run: Requires Java 8, `8u40` branch requires jdk8u40+
* Compile: Require Scala 2.11. If you are using 2.10-, please use ScalaConsole 1.x, now [2.9] (http://git.oschina.net/43284683/scalaconsole/tree/master/) and [2.10 ] (http://git.oschina.net/43284683/scalaconsole/tree/2.10/) Two branches. In fact, to modify the code of ScalaConsole 2 to adapt to Scala 2.10 workload is not large, but I personally have no interest. Welcome to submit a pull request.

## 2.0 Changes from 1.x
* UI implementation from Swing to JavaFX

  The modularity of Scala 2.11 separates the Swing module and may not be available in future versions. JavaFX's development experience is much better than Swing, and Java 8's Lambda Expression adds more points to JavaFX.

* Editor uses [Ace Editor] (https://github.com/ajaxorg/ace)

  `Ctrl-,` can be used to adjust the configuration interface of ace, you can choose color theme, keyboard mode (ace, vim, emacs) and other details.

* Packaged using [sbt assembly](https://github.com/sbt/sbt-assembly)

  Now run `java -jar ScalaConsole-assembly-$VERSION.jar` to get started.
* I have deleted many features that I have not used at all.

  For example, "Open" / "Save" files (recommended to use [gist] (https://gist.github.com/)). Another example is switching the Scala version (this was a few years ago, the Stone Age of Scala 2.8, 2.9 was a very important feature. But I have never used it in the last year or two). The ability to add a local Jar file or classes directory as a dependency has also been removed.

* Use gson instead of deprecated scala built-in JSON parser

* artifacts crossbuild version logic reimplementation, more complete, secure and easy to extend

* No longer use akka actor to achieve concurrency

  The concurrency operation in ScalaConsole is very simple, creating an ActorSystem for it is completely killing the knife and increasing the startup time.

## Instructions for use

1. Run ScalaConsole

  1.1 Direct use of the release package

  Download ScalaConsole-assembly-$VERSION.jar from this repository [attachment] (http://git.oschina.net/43284683/scalaconsole/attach_files)
  ```
  Java -jar ScalaConsole-assembly-$VERSION.jar
  ```

  1.2 Compiling from source

  There is a requirement for `sbt`, which is standard for playing Scala, no choice.

  ```
  $ git clone this repository
  $ cd scalaconsole
  $ git checkout 8u40
  $ git submodule init
  $ git submodule update
  The above two operations are to obtain the contents of the ace-builds repository that the project depends on.
  $ sbt assembly
  $ java -jar target/scala-2.11/ScalaConsole-assembly-$VERSION.jar
  Or directly
  $ sbt run
  ```
  After each `git pull`, you need to run `git submodule update` to update `ace-builds`.

2. Code editing

  **The following shortcuts are key definitions on Linux/Windows. On the Mac, please change `Ctrl` to `Command`**

  The ScalaConsole shortcut definition is as consistent as possible with IDEA.

  2.1 `Ctrl-C` | `Ctrl-Insert` **Smart Copy**

  2.2 `Ctrl-D` **Smart Duplicate**

  2.3 `Ctrl-X` | `Shift-Delete` **Smart Cut**

  2.4 `Ctrl-Y` **Delete current line**

  2.5 `Ctrl-/` **Switch line comment**

  2.6 [Ace Editor] (https://github.com/ajaxorg/ace) is a full-featured editor, very powerful, please refer to [its other shortcuts] (https://github.com/ajaxorg/ace /wiki/Default-Keyboard-Shortcuts).

  The most important one is `Ctrl-,`, which pops up the Ace Editor's settings window.

3. Run the code

  3.1 `Ctrl-R` **Run the current editing area code**

  3.2 `Ctrl-Shift-R` **Run the currently selected code**

  3.3 `Ctrl-P` **Run the current editing area code in Paste mode**

  3.4 `Ctrl-Shift-P` **Run the currently selected code in Paste mode**

  3.5 `Ctrl-E` ** Output area clear screen**

  3.6 `Ctrl-Shift-E` **Reset the REPL and keep the current dependency settings**

4. Dependency management

  This is the biggest highlight of ScalaConsole and is highly recommended.

  4.1 `Ctrl-I` **Search by keyword and add dependencies**

  ScalaConsole uses the maven index of the central library and is updated daily.

  4.2 `Ctrl-Shift-I` ** Add dependencies manually**

  For Artifacts that are not submitted to the central repository, such as some packages of typesafe, you can add them manually. Currently ScalaConsole contains three Resolvers, in order: `oschina`, `typesafe`, `central`

  4.3 Menu -> Dependencies -> Reduce ** Reduce Dependencies**

  The dependencies that have been added can be truncated through this dialog. The operation is the same as in the Search Add Dependency window, and the mouse double clicks on the item to be operated.

5. Label management

  ScalaConsole supports multiple tags, **add tags**Use `Ctrl-T`, **Close current tag**Use `Ctrl-F4`

6. The current code is posted to Gist

  After the release is successful, ScalaConsole will automatically copy the gist link to the system clipboard.

  6.1 `Ctrl-G` will pop up the system browser to github.com to authenticate your github account

  6.2 `Ctrl-Shift-G` ** Anonymous release gist**

6. Other

  5.1 Menu -> Edit -> Set Font **Modify Font**

  The font format is `FamilyName-Size`, such as `Ubuntu Mono-13`, `Menlo-14` and so on.

  5.2 Menu -> Repl -> Command line options **Add REPL command line options**

  Put the command line options to be added into a string, such as `-Xprint:typer`

  5.3 `Ctrl-W` **Change window arrangement**

  The default code window and output window are arranged side by side. Use this button to switch between left and right and top and bottom.

## Future
After Scala-IDE introduced the worksheet function, I once thought that ScalaConsole had completed its historical mission, and it was in the trash. But after a few years, I found that I still use it every day. It's very brisk, ready to start coding, not relying on a complete project, I used it to grab a lot of points under the Scala theme on StackOverflow. It's very intimate, and the Maven index-based dependency search and add mechanism makes it quick to reference any library I'm interested in, which makes ScalaConsole a must-have for exploring the capabilities and APIs of the Scala/Java library.

The current form of ScalaConsole 2.0 can basically meet my daily coding needs. The new functions that can be thought of at present are only code formatting and AceJump, which will take time to implement slowly. If you have new ideas, please feel free to submit them to [issue tracker] (http://git.oschina.net/43284683/scalaconsole/issues).

