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

  private val microserviceBootstrapVersion = "10.6.0"
  private val playuiVersion = "7.27.0-play-25"
  private val hmrcTestVersion = "3.3.0"
  private val mockito = "1.10.19"
  private val specs2 = "2.3.13"

  val compile = Seq(
    "uk.gov.hmrc" %% "microservice-bootstrap" % microserviceBootstrapVersion,
    "uk.gov.hmrc" %% "play-ui" % playuiVersion
  )

  trait TestDependencies {
    lazy val scope: String = "test"
    lazy val test : Seq[ModuleID] = ???
  }

  object Test {
    def apply() = new TestDependencies {
      override lazy val test = Seq(
        "uk.gov.hmrc" %% "hmrctest" % hmrcTestVersion % scope,
        "org.scalatest" %% "scalatest" % "2.2.6" % scope,
        "org.pegdown" % "pegdown" % "1.5.0" % scope,
        "com.typesafe.play" %% "play-test" % PlayVersion.current % scope,
        "org.mockito" % "mockito-all" % mockito,
        "org.specs2" % "specs2_2.10" % specs2,
        "org.scalatestplus.play" %% "scalatestplus-play" % "2.0.1"
      )
    }.test
  }

  object IntegrationTest {
    def apply() = new TestDependencies {

      override lazy val scope: String = "it"

      override lazy val test = Seq(
        "uk.gov.hmrc" %% "hmrctest" % hmrcTestVersion % scope,
        "org.scalatest" %% "scalatest" % "2.2.6" % scope,
        "org.pegdown" % "pegdown" % "1.5.0" % scope,
        "com.typesafe.play" %% "play-test" % PlayVersion.current % scope,
        "org.mockito" % "mockito-all" % mockito,
        "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1"
      )
    }.test
  }

  def apply() = compile ++ Test() ++ IntegrationTest()
}

