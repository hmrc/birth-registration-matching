import play.core.PlayVersion
import sbt.{ModuleID, _}

object AppDependencies {

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc" %% "bootstrap-play-26" % "2.3.0",
    "uk.gov.hmrc" %% "play-ui" % "8.19.0-play-26"
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc" %% "hmrctest" % "3.10.0-play-26",
    "org.scalatest" %% "scalatest" % "3.0.9",
    "org.pegdown" % "pegdown" % "1.6.0",
    "com.typesafe.play" %% "play-test" % PlayVersion.current,
    "org.mockito" % "mockito-core" % "3.3.3",
    "org.specs2" % "specs2_2.12" % "2.5",
    "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.3"
  ).map(_ % "test")

  val all: Seq[ModuleID] = compile ++ test
}


