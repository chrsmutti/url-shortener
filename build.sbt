import Dependencies._

ThisBuild / scalaVersion := "2.13.4"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "dev.chrs"
ThisBuild / organizationName := "chrs"

ThisBuild / scalacOptions ++= Seq("-Xfatal-warnings")
ThisBuild / testFrameworks += new TestFramework("utest.runner.Framework")

ThisBuild / semanticdbEnabled := true
ThisBuild / semanticdbVersion := scalafixSemanticdb.revision
ThisBuild / scalafixScalaBinaryVersion := CrossVersion.binaryScalaVersion(scalaVersion.value)
ThisBuild / scalafixDependencies += com.github.liancheng.`organize-imports`

ThisBuild / publish / skip := true
ThisBuild / githubWorkflowPublishTargetBranches := Seq()

Docker / dockerExposedPorts ++= Seq(8080)

Global / onChangedBuildSource := ReloadOnSourceChanges

lazy val root = (project in file("."))
  .settings(
    name := "url-shortener",
    libraryDependencies ++= Seq(
      ch.qos.logback,
      circe.`circe-generic`,
      chrisdavenport.`log4cats-slf4j`,
      com.github.pureconfig.pureconfig,
      dev.profunktor.`redis4cats-effects`,
      dev.profunktor.`redis4cats-log4cats`,
      org.http4s.`http4s-blaze-server`,
      org.http4s.`http4s-circe`,
      org.http4s.`http4s-dsl`,
      org.typelevel.`cats-core`,
      org.typelevel.`cats-effect`
    ),
    libraryDependencies ++= Seq(
      circe.`circe-literal` % Test,
      com.codecommit.`cats-effect-testing-utest` % Test,
      com.lihaoyi.utest % Test
    )
  )
  .enablePlugins(JavaAppPackaging, DockerPlugin)
