import play.core.PlayVersion
import sbt.{ModuleID, _}

object AppDependencies {

  private val silencerVersion = "1.7.1"
  private val bootstrapPlayVersion = "5.12.0"

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc" %% "bootstrap-backend-play-27" % bootstrapPlayVersion,
    "com.typesafe.play" %% "play-json-joda" % "2.9.2",
    compilerPlugin("com.github.ghik" % "silencer-plugin" % silencerVersion cross CrossVersion.full),
    "com.github.ghik" % "silencer-lib" % silencerVersion % Provided cross CrossVersion.full
  )

  val test: Seq[ModuleID] = Seq(
    "com.typesafe.play" %% "play-test" % PlayVersion.current,
    "uk.gov.hmrc" %% "bootstrap-test-play-27" % bootstrapPlayVersion,
    "org.scalatestplus.play" %% "scalatestplus-play" % "4.0.3",
    "org.mockito" % "mockito-core" % "3.12.4",
    "org.pegdown" % "pegdown" % "1.6.0",
    "org.specs2" % "specs2_2.12" % "2.5"
  ).map(_ % "test")

  val all: Seq[ModuleID] = compile ++ test
}


