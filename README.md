# ScalaConsole Reloaded (V2.0)

## ScalaConsole 是什么
ScalaConsole是Scala语言REPL的图形界面替代者。

## 为什么需要 ScalaConsole
* 命令行REPL的编辑功能有限，只能基于行来进行编辑，如果输入错误，修改起来很痛苦。
    
    ScalaConsole使用全功能编辑器，支持语法高亮。
  
* 命令行REPL功能受限，例如对case class的类名支持不好，官方推荐的做法是用scalac先将case class编译好，再用REPL导入，非常麻烦。
    
    ScalaConsole直接使用scala.nsc，对case class完美支持，并支持所有的REPL选项。
  
* ScalaConsole支持基于搜索的依赖添加
    
    scala REPL无法方便地添加依赖。sbt console可以使用sbt配置中的依赖，但是依赖于sbt项目。ScalaConsole随时启动，随时添加依赖，不需要先创建项目。

## 何时使用ScalaConsole
当你需要临时验证一个想法，一小段代码的正确性，临时了解某一个Java/Scala库的API，又不想为它建立一个完整的sbt项目时。

## 版本要求
* 运行：要求 Java 8
* 编译：要求 Scala 2.11. 如果你使用的是 2.10-，请使用 ScalaConsole 1.x，现在有 [2.9](http://git.oschina.net/43284683/scalaconsole/tree/master/) 和 [2.10](http://git.oschina.net/43284683/scalaconsole/tree/2.10/) 两个分支. 其实要修改 ScalaConsole 2 的代码适应Scala 2.10工作量也不大，只是我本人没什么兴趣。欢迎提交 pull request.

## 2.0相对于1.x的变化
* UI 实现由 Swing 转到 JavaFX

  Scala 2.11的模块化将 Swing 模块分离出来，将来的版本中可能根本就没有了。JavaFX 的开发体验比 Swing 好得太多，Java 8 的 Lambda Expression 对 JavaFX 更有加分。

* 编辑器使用 [Ace Editor](https://github.com/ajaxorg/ace)

  `Ctrl-,` 可调出 ace 的配置界面，可以选择颜色主题，键盘模式(ace, vim, emacs)以及其它细节设置。

* 使用 [sbt assembly](https://github.com/sbt/sbt-assembly) 打包

  现在运行 `java -jar ScalaConsole-assembly-$VERSION.jar` 即可开始使用。
* 删减了许多我自己基本不用的功能

  例如“打开”/“保存”文件(建议全面使用 [gist](https://gist.github.com/) )。又如切换Scala版本(这在几年以前，Scala 2.8, 2.9 的石器时代是一个很重要的功能。但是最近一两年我基本没用到过它)。添加本地Jar文件或classes目录作为依赖的功能也被取消了。

* 使用 gson 取代被 deprecated 的 scala 内置 JSON parser

* artifacts crossbuild version 逻辑重新实现，更加完善，安全和易于扩展

* 不再使用akka actor实现并发

  ScalaConsole 中的并发操作非常简单，为它创建一个ActorSystem完全是杀鸡用牛刀，还会增加启动时间。

## 使用说明

1. 运行 ScalaConsole

  1.1 直接使用发布包

  从本仓库[附件](http://git.oschina.net/43284683/scalaconsole/attach_files)下载 ScalaConsole-assembly-$VERSION.jar，运行
  ```
  java -jar ScalaConsole-assembly-$VERSION.jar
  ```

  1.2 从源码编译

  要求有`sbt`，这是玩Scala的标配，没的选。

  ```
  $ git clone 本仓库
  $ cd scalaconsole
  $ git checkout 2.11
  $ git submodule init
  $ git submodule update
  以上两个操作是为了获取本项目所依赖的 ace-builds 仓库的内容。
  $ sbt assembly
  $ java -jar target/scala-2.11/ScalaConsole-assembly-$VERSION.jar
  或直接
  $ sbt run
  ```
  以后每次 `git pull` 以后都需要运行`git submodule update`对`ace-builds`进行更新。

2. 代码编辑

  **以下快捷键均为 Linux/Windows 上的键定义，在 Mac 上请将`Ctrl`换成`Command`**

  ScalaConsole 快捷键定义尽量保持与 IDEA 一致。

  2.1 `Ctrl-C` | `Ctrl-Insert` **Smart Copy**  

  2.2 `Ctrl-D` **Smart Duplicate**

  2.3 `Ctrl-X` | `Shift-Delete` **Smart Cut**

  2.4 `Ctrl-Y` **删除当前行**

  2.5 `Ctrl-/` **切换行注释**

  2.6 [Ace Editor](https://github.com/ajaxorg/ace) 是一个完整功能的编辑器，很强大，请参考 [它的其它快捷键](https://github.com/ajaxorg/ace/wiki/Default-Keyboard-Shortcuts)。

  其中比较重要的是`Ctrl-,`，弹出 Ace Editor 的设置窗口。

3. 运行代码

  3.1 `Ctrl-R` **运行当前编辑区代码**

  3.2 `Ctrl-Shift-R` **运行当前选中代码**

  3.3 `Ctrl-P` **以 Paste 模式运行当前编辑区代码**

  3.4 `Ctrl-Shift-P` **以 Paste 模式运行当前选中代码**

  3.5 `Ctrl-E` **输出区域清屏**

  3.6 `Ctrl-Shift-E` **重置REPL，保留当前的依赖设置**

4. 依赖管理

  这是 ScalaConsole 的最大亮点，强烈推荐。

  4.1 `Ctrl-I` **以关键字搜索并添加依赖**

  ScalaConsole 使用中央库的 maven 索引，每日更新。

  4.2 `Ctrl-Shift-I` **手动添加依赖**

  对于没有提交到中央库的 Artifact，如 typesafe 的某些包，可以手动添加。目前 ScalaConsole 包含三个 Resolver，依次为: `oschina`, `typesafe`, `central`

  4.3 Menu -> Dependencies -> Reduce **减少依赖**

  已经添加的依赖可以通过这个对话框进行删减。操作方式与搜索添加依赖窗口中的一样，鼠标双击要操作的项。

5. 标签管理

  ScalaConsole支持多标签，**添加标签**用`Ctrl-T`，**关闭当前标签**使用`Ctrl-F4`

6. 当前代码发布到Gist

  发布成功后ScalaConsole会自动将 gist 链接复制到系统剪贴板上。

  6.1 `Ctrl-G` 会弹出系统浏览器到 github.com 去认证你的 github 帐号

  6.2 `Ctrl-Shift-G` **匿名发布 gist**

6. 其它

  5.1 Menu -> Edit -> Set Font **修改字体**

  字体格式为 `FamilyName-Size`，如 `Ubuntu Mono-13`, `Menlo-14`等。

  5.2 Menu -> Repl -> Command line options **添加 REPL 的命令行选项**

  把要添加的命令行选项放到一个字符串里，如 `-Xprint:typer`

  5.3 `Ctrl-W` **改变窗口排列方式**

  默认代码窗口和输出窗口左右排列，使用此键可在左右排列和上下排列之间切换。

## 将来
Scala-IDE 推出 worksheet 功能后，我曾一度以为 ScalaConsole 完成了它的历史使命，该进垃圾箱了。但是几年下来，我发现我仍然每天都在使用它。它非常轻快，随时开始编码，不依赖于一个完整的项目，我用它在StackOverflow上的Scala主题下抢了不少分。它又很贴心，基于 Maven 索引的依赖搜索及添加机制使得它能够快速地引用到我所感兴趣的任何库，这也使得 ScalaConsole 成为探索 Scala/Java 库的功能和 API 的必备利器。

ScalaConsole 2.0现在的形态基本能满足我每天的编码需求，目前能想到的新功能只有代码格式化和 AceJump，会抽空慢慢实现。如果你有新的想法，也欢迎提交到 [issue tracker](http://git.oschina.net/43284683/scalaconsole/issues).
