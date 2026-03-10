import uk.gov.hmrc.DefaultBuildSettings.{defaultSettings, scalaSettings}
import uk.gov.hmrc.versioning.SbtGitVersioning.autoImport.majorVersion

ThisBuild / scalaVersion := "3.3.7"
ThisBuild / majorVersion := 2

lazy val microservice = Project("birth-registration-matching", file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin)
  .settings(
    scalaSettings,
    defaultSettings(),
    PlayKeys.playDefaultPort := 8098,
    libraryDependencies ++= AppDependencies(),
    scalacOptions ++= Seq("-feature", "-Wconf:src=conf/.*\\.routes:s")
  )
  .settings(CodeCoverageSettings())

addCommandAlias("scalafmtAll", "all scalafmtSbt scalafmt Test/scalafmt")
