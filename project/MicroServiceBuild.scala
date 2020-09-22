import sbt._
import uk.gov.hmrc.{SbtArtifactory, SbtAutoBuildPlugin}
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin
import uk.gov.hmrc.versioning.SbtGitVersioning

object MicroServiceBuild extends Build with MicroService {

  val appName = "birth-registration-matching"

  override lazy val plugins: Seq[Plugins] = Seq(
    SbtAutoBuildPlugin, SbtGitVersioning, SbtDistributablesPlugin, SbtArtifactory
  )

  override lazy val appDependencies: Seq[ModuleID] = AppDependencies()
}

private object AppDependencies {
  import play.core.PlayVersion

  private val bootstrapVersion = "1.9.0"
  private val playuiVersion = "7.27.0-play-26"

  val compile = Seq(
    "uk.gov.hmrc" %% "bootstrap-play-26" % bootstrapVersion,
    "uk.gov.hmrc" %% "play-ui" % playuiVersion
  )

  trait TestDependencies {
    lazy val scope: String = "test"
    lazy val test : Seq[ModuleID] = ???
  }

  object Test {
    def apply(): Seq[ModuleID] = new TestDependencies {
      override lazy val test = Seq(
        "uk.gov.hmrc" %% "hmrctest" % "3.9.0-play-26" % scope,
        "org.scalatest" %% "scalatest" % "2.2.6" % scope,
        "org.pegdown" % "pegdown" % "1.5.0" % scope,
        "com.typesafe.play" %% "play-test" % PlayVersion.current % scope,
        "org.mockito" % "mockito-core" % "3.3.3",
        "org.specs2" % "specs2_2.10" % "2.3.13",
        "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.3"
      )
    }.test
  }

  def apply(): Seq[ModuleID] = compile ++ Test()
}

