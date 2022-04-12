import play.core.PlayVersion
import sbt.{ModuleID, _}

object AppDependencies {

  private val silencerVersion = "1.7.1"
  private val bootstrapPlayVersion = "5.22.0"

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc" %% "bootstrap-backend-play-28" % bootstrapPlayVersion,
    "com.typesafe.play" %% "play-json-joda" % "2.9.2",
    compilerPlugin("com.github.ghik" % "silencer-plugin" % silencerVersion cross CrossVersion.full),
    "com.github.ghik" % "silencer-lib" % silencerVersion % Provided cross CrossVersion.full
  )

  val test: Seq[ModuleID] = Seq(
    "com.typesafe.play" %% "play-test" % PlayVersion.current,
    "uk.gov.hmrc" %% "bootstrap-test-play-28" % bootstrapPlayVersion,
    "org.scalatestplus" %% "mockito-3-4" % "3.2.9.0",
    "org.specs2" %% "specs2-core" % "4.12.12",
    "org.specs2" %% "specs2-mock" % "4.12.12"
  ).map(_ % "test")

  val all: Seq[ModuleID] = compile ++ test
}


