
import sbt.Keys._
import sbt._
import scoverage.ScoverageKeys
import uk.gov.hmrc.DefaultBuildSettings.{defaultSettings, scalaSettings}
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin._
import uk.gov.hmrc.versioning.SbtGitVersioning.autoImport.majorVersion

val appName = "birth-registration-matching"
majorVersion := 2
PlayKeys.playDefaultPort := 8098


lazy val microservice = Project(appName, file("."))

enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)

defaultSettings()
scalaSettings
publishingSettings
scalaVersion := "2.12.12"

libraryDependencies ++= AppDependencies.all
resolvers += Resolver.jcenterRepo
retrieveManaged := true

ScoverageKeys.coverageExcludedPackages := "<empty>;uk.gov.hmrc.brm.config.*;testOnlyDoNotUseInAppConf.*;uk.gov.hmrc.brm.views.*;prod.*;uk.gov.hmrc.BuildInfo.*;app.Routes.*;"
ScoverageKeys.coverageMinimumStmtTotal := 90
ScoverageKeys.coverageFailOnMinimum := true
ScoverageKeys.coverageHighlighting := true

scalacOptions ++= Seq(
  "-P:silencer:pathFilters=views;routes"
)
