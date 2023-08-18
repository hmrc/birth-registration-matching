import sbt.{ModuleID, *}
import play.sbt.PlayImport.*

object AppDependencies {

  private val bootstrapPlayVersion = "7.21.0"

  val compile: Seq[ModuleID] = Seq(
    ws,
    "uk.gov.hmrc"       %% "bootstrap-backend-play-28" % bootstrapPlayVersion,
    "com.typesafe.play" %% "play-json-joda"            % "2.9.4"
  )

  val test: Seq[ModuleID]    = Seq(
    "org.scalatest"       %% "scalatest"              % "3.2.16",
    "uk.gov.hmrc"         %% "bootstrap-test-play-28" % bootstrapPlayVersion,
    "org.scalatestplus"   %% "mockito-3-4"            % "3.2.10.0",
    "org.specs2"          %% "specs2-core"            % "4.20.2",
    "org.specs2"          %% "specs2-mock"            % "4.20.2",
    "com.vladsch.flexmark" % "flexmark-all"           % "0.64.8"
  ).map(_ % "test")

  def apply(): Seq[ModuleID] = compile ++ test
}
