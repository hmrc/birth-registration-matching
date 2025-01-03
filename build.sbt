import sbt.Keys.*
import sbt.*

import scoverage.ScoverageKeys
import uk.gov.hmrc.DefaultBuildSettings.{defaultSettings, scalaSettings}
import uk.gov.hmrc.versioning.SbtGitVersioning.autoImport.majorVersion

val appName = "birth-registration-matching"

lazy val scoverageSettings: Seq[Def.Setting[?]] =
  Seq(
    ScoverageKeys.coverageExcludedPackages := "<empty>;uk.gov.hmrc.brm.config.*;testOnlyDoNotUseInAppConf.*;" +
      "uk.gov.hmrc.brm.views.*;prod.*;uk.gov.hmrc.BuildInfo.*;app.Routes.*;",
    ScoverageKeys.coverageMinimumStmtTotal := 89,
    ScoverageKeys.coverageFailOnMinimum := true,
    ScoverageKeys.coverageHighlighting := true
  )

lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin)
  .settings(
    scoverageSettings,
    scalaSettings,
    scalaVersion := "2.13.12",
    defaultSettings(),
    majorVersion := 2,
    PlayKeys.playDefaultPort := 8098,
    libraryDependencies ++= AppDependencies(),
    scalacOptions += "-Wconf:cat=unused-imports&src=routes/.*:s"
  )

addCommandAlias("scalafmtAll", "all scalafmtSbt scalafmt Test/scalafmt")
addCommandAlias("scalastyleAll", "all scalastyle Test/scalastyle")
