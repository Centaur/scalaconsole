# ScalaConsole 2.0 Reloaded

## 版本要求
* 运行：要求 Java 8
* 编译：要求 Scala 2.11. 如果你使用的是2.10-，请使用ScalaConsole 1.x. 要修改ScalaConsole 2的代码适应Scala 2.10工作量也不大，

## 相对于1.x的变化
* UI实现由Swing转到JavaFX

  Scala 2.11的模块化将swing模块分离出来，JavaFX的开发体验比Swing好太多了，Java 8的Lambda对JavaFX更有加分。

* 编辑器使用Ace Editor

  `Ctrl-,` 可调出ace的配置界面，可以选择颜色主题。

* 使用 sbt assembly 打包

  现在运行`java -jar ScalaConsole-assembly-$VERSION.jar` 即可开始使用。
* 去掉了许多我自己基本不用的功能

  例如“打开”/“保存”文件，建议全面使用gist功能。又如切换Scala版本，这在几年以后，Scala2.8, 2.9的石器时代是一个很重要的功能。但是最近一两年我基本没用到过这个功能。添加本地Jar文件或classes目录作为依赖的功能也被取消了。

* 使用gson取代被deprecated的scala内置JSON parser
* artifacts crossbuild version 逻辑重新实现，更加完善，安全和易于扩展

## 使用说明

1. 运行ScalaConsole

  1.1 直接使用发布包

  从本仓库“附件”区下载 ScalaConsole-assembly-$VERSION.jar，运行 `java -jar ScalaConsole-assembly-$VERSION.jar`

  1.2 从源码编译

  要求有sbt

  ```
  $ git clone 本仓库
  $ cd scalaconsole
  $ git submodule init
  $ git submodule update
  以上两个操作是为了获取本项目所依赖的ace-builds仓库的内容。
  $ sbt assembly
  或直接
  $ sbt run
  ```
  以后每次 `git pull` 以后都需要运行`git submodule update`对`ace-builds`进行更新。
2. 代码编辑

3. 运行代码

4. 依赖管理

5. 其它
  5.1
