import sbt.{ModuleID, *}
import play.sbt.PlayImport.*

object AppDependencies {

  private val bootstrapPlayVersion = "9.0.0"

  val compile: Seq[ModuleID] = Seq(
    ws,
    "uk.gov.hmrc" %% "bootstrap-backend-play-30" % bootstrapPlayVersion
  )

  val test: Seq[ModuleID]    = Seq(
    "org.scalatest"                %% "scalatest"              % "3.2.19",
    "uk.gov.hmrc"                  %% "bootstrap-test-play-30" % bootstrapPlayVersion,
    "org.scalatestplus"            %% "mockito-3-4"            % "3.2.10.0",
    "org.specs2"                   %% "specs2-core"            % "4.20.5",
    "org.specs2"                   %% "specs2-mock"            % "4.20.5",
    "com.vladsch.flexmark"          % "flexmark-all"           % "0.64.8",
    "com.fasterxml.jackson.module" %% "jackson-module-scala"   % "2.16.1",
    "org.apache.pekko"             %% "pekko-connectors-xml"   % "1.0.2"
  ).map(_ % "test")

  def apply(): Seq[ModuleID] = compile ++ test
}
