import sbt._
import sbt.Keys._

object HaltyBuild extends Build {

  import com.typesafe.sbt.SbtScalariform.scalariformSettings
  import xerial.sbt.Sonatype.SonatypeKeys.sonatypeProfileName
  import scala.Console.{CYAN, RESET}

  object Version {
    val scalaLangModule = "1.0.4"
    val specs2 = "3.6.2"
    val jsoup = "1.8.2"
  }

  lazy val halty = Project(
    id = "halty",
    base = file("."),
    settings = Defaults.coreDefaultSettings ++
      scalariformSettings ++
      publishSettings ++
      Seq(
        name := "halty",
        organization := "com.krrrr38",
        version := "0.1.3",
        description := "Text-to-HTML converter with Halty syntax.",
        scalaVersion := "2.11.7",
        crossScalaVersions := scalaVersion.value :: "2.10.5" :: Nil,
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
        libraryDependencies := {
          val commonDependencies = Seq(
            "org.jsoup" % "jsoup" % Version.jsoup,
            "org.specs2" %% "specs2-core" % Version.specs2 % "test",
            "org.specs2" %% "specs2-matcher-extra" % Version.specs2 % "test",
            "org.specs2" %% "specs2-scalacheck" % Version.specs2 % "test"
          )
          CrossVersion.partialVersion(scalaVersion.value) match {
            case Some((2, scalaMajor)) if scalaMajor >= 11 =>
              libraryDependencies.value ++ commonDependencies ++ Seq(
                "org.scala-lang.modules" %% "scala-parser-combinators" % Version.scalaLangModule,
                "org.scala-lang.modules" %% "scala-xml" % Version.scalaLangModule
              )
            case _ => libraryDependencies.value ++ commonDependencies
          }
        },
        resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"
      )
  )

  lazy val publishSettings = Seq(
    sonatypeProfileName := "com.krrrr38",
    pomExtra := {
      <url>http://github.com/krrrr38/halty</url>
        <scm>
          <url>git@github.com:krrrr38/halty.git</url>
          <connection>scm:git:git@github.com:krrrr38/halty.git</connection>
        </scm>
        <licenses>
          <license>
            <name>MIT License</name>
            <url>http://www.opensource.org/licenses/mit-license.php</url>
            <distribution>repo</distribution>
          </license>
        </licenses>
        <developers>
          <developer>
            <id>krrrr38</id>
            <name>Ken Kaizu</name>
            <url>http://www.krrrr38.com</url>
          </developer>
        </developers>
    },
    publishArtifact in Test := false,
    publishMavenStyle := true,
    pomIncludeRepository := { _ => false },
    publishTo <<= version { (v: String) =>
      val nexus = "https://oss.sonatype.org/"
      if (v.trim.endsWith("SNAPSHOT"))
        Some("snapshots" at nexus + "content/repositories/snapshots")
      else
        Some("releases" at nexus + "service/local/staging/deploy/maven2")
    }
  )
}
