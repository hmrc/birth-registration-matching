import play.core.PlayVersion
import sbt.{ModuleID, _}

object AppDependencies {

  val silencerVersion = "1.7.1"

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc" %% "bootstrap-backend-play-27" % "5.3.0",
    "com.typesafe.play" %% "play-json-joda" % "2.9.2",
    compilerPlugin("com.github.ghik" % "silencer-plugin" % silencerVersion cross CrossVersion.full),
    "com.github.ghik" % "silencer-lib" % silencerVersion % Provided cross CrossVersion.full
  )

  val test: Seq[ModuleID] = Seq(
    "org.scalatest" %% "scalatest" % "3.0.9",
    "org.pegdown" % "pegdown" % "1.6.0",
    "com.typesafe.play" %% "play-test" % PlayVersion.current,
    "org.mockito" % "mockito-core" % "3.10.0",
    "org.specs2" % "specs2_2.12" % "2.5",
    "org.scalatestplus.play" %% "scalatestplus-play" % "4.0.3",
    "uk.gov.hmrc" %% "bootstrap-test-play-27" % "5.3.0"
  ).map(_ % "test")

  val all: Seq[ModuleID] = compile ++ test
}


