import sbt._
import sbt.Keys._
import com.typesafe.sbt.SbtScalariform.scalariformSettings

object HaltyBuild extends Build {

  import scala.Console.{CYAN, RESET}

  object Version {
    val scalaLangModule = "1.0.4"
    val specs2 = "3.6.2"
    val jsoup = "1.8.2"
  }

  lazy val halty = Project(
    id = "halty",
    base = file("."),
    settings = Defaults.coreDefaultSettings ++ scalariformSettings ++ Seq(
      name := "halty",
      organization := "com.krrrr38",
      version := "0.1-SNAPSHOT",
      scalaVersion := "2.11.7",
      scalacOptions ++= (
        "-deprecation" ::
          "-feature" ::
          "-unchecked" ::
          "-Xlint" ::
          Nil
        ),
      scalacOptions ++= {
        if (scalaVersion.value.startsWith("2.11"))
          Seq("-Ywarn-unused", "-Ywarn-unused-import")
        else
          Nil
      },
      scalacOptions in Test ++= Seq("-Yrangepos"),
      shellPrompt := { state => s"$CYAN${name.value}$RESET > " },
      libraryDependencies ++= Seq(
        "org.scala-lang.modules" %% "scala-parser-combinators" % Version.scalaLangModule,
        "org.scala-lang.modules" %% "scala-xml" % Version.scalaLangModule,
        "org.jsoup" % "jsoup" % Version.jsoup,
        "org.specs2" %% "specs2-core" % Version.specs2 % "test",
        "org.specs2" %% "specs2-matcher-extra" % Version.specs2 % "test",
        "org.specs2" %% "specs2-scalacheck" % Version.specs2 % "test"
      ),
      resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"
    )
  )
}
