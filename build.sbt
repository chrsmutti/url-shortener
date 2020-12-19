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

Global / onChangedBuildSource := ReloadOnSourceChanges

lazy val root = (project in file("."))
  .settings(
    name := "url-shortener",
    libraryDependencies ++= Seq(
      ch.qos.logback,
      circe.`circe-generic`,
      org.http4s.`http4s-blaze-server`,
      org.http4s.`http4s-circe`,
      org.http4s.`http4s-dsl`,
      org.log4s.log4s,
      org.typelevel.`cats-core`,
      org.typelevel.`cats-effect`
    ),
    libraryDependencies ++= Seq(
      circe.`circe-literal` % Test,
      com.codecommit.`cats-effect-testing-utest` % Test,
      com.lihaoyi.utest % Test
    )
  )
