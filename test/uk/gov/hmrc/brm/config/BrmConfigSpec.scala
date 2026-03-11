/*
 * Copyright 2026 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.brm.config

import java.time.LocalDate
import org.scalatest.OptionValues
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.brm.models.brm.Payload
import uk.gov.hmrc.brm.utils.BirthRegisterCountry

class BrmConfigSpec extends AnyWordSpecLike with Matchers with OptionValues with GuiceOneAppPerTest {

  override def fakeApplication(): Application = GuiceApplicationBuilder()
    .configure(
      "microservice.services.birth-registration-matching.host"                             -> "localhost",
      "microservice.services.birth-registration-matching.port"                             -> 9000,
      "microservice.services.des.host"                                                     -> "localhost",
      "microservice.services.des.port"                                                     -> 9001,
      "microservice.services.birth-registration-matching.matching.firstName"               -> true,
      "microservice.services.birth-registration-matching.matching.lastName"                -> true,
      "microservice.services.birth-registration-matching.matching.dateOfBirth"             -> true,
      "microservice.services.birth-registration-matching.matching.matchOnMultiple"         -> false,
      "microservice.services.birth-registration-matching.matching.ignoreAdditionalNames"   -> true,
      "microservice.services.birth-registration-matching.features.flags.logging"           -> true,
      "microservice.services.birth-registration-matching.features.flags.process"           -> true,
      "microservice.services.birth-registration-matching.features.gro.enabled"             -> true,
      "microservice.services.birth-registration-matching.features.gro.reference.enabled"   -> true,
      "microservice.services.birth-registration-matching.features.gro.details.enabled"     -> true,
      "microservice.services.birth-registration-matching.features.nrs.enabled"             -> true,
      "microservice.services.birth-registration-matching.features.nrs.reference.enabled"   -> true,
      "microservice.services.birth-registration-matching.features.nrs.details.enabled"     -> true,
      "microservice.services.birth-registration-matching.features.groni.enabled"           -> false,
      "microservice.services.birth-registration-matching.features.groni.reference.enabled" -> false,
      "microservice.services.birth-registration-matching.features.groni.details.enabled"   -> false,
      "microservice.services.birth-registration-matching.features.gro.flags.test.process"  -> true,
      "des.env"                                                                            -> "dev",
      "des.auth-token"                                                                     -> "token-123"
    )
    .build()

  private def config: BrmConfig =
    app.injector.instanceOf[BrmConfig]

  private def payloadEngland: Payload =
    Payload(Some("123456789"), "Adam", None, "Smith", LocalDate.now, BirthRegisterCountry.ENGLAND)

  private def payloadScotland: Payload =
    Payload(Some("1234567890"), "Adam", None, "Smith", LocalDate.now, BirthRegisterCountry.SCOTLAND)

  private def payloadNorthernIreland: Payload =
    Payload(Some("1234567890"), "Adam", None, "Smith", LocalDate.now, BirthRegisterCountry.NORTHERN_IRELAND)

  "BrmConfig" should {

    "return matching config values" in {
      config.matchFirstName        shouldBe true
      config.matchLastName         shouldBe true
      config.matchDateOfBirth      shouldBe true
      config.matchOnMultiple       shouldBe false
      config.ignoreAdditionalNames shouldBe true
    }

    "return flags config values" in {
      config.logFlags                       shouldBe true
      config.processFlags                   shouldBe true
      config.validateFlag("gro", "test")    shouldBe true
      config.validateFlag("gro", "missing") shouldBe false
    }

    "return audit map for england payload" in {
      val audit = config.audit(Some(payloadEngland))

      audit("features.matchFirstName")     shouldBe "true"
      audit("features.matchLastName")      shouldBe "true"
      audit("features.matchDateOfBirth")   shouldBe "true"
      audit("features.matchOnMultiple")    shouldBe "false"
      audit("features.ignoreMiddleNames")  shouldBe "true"
      audit("features.downstream.enabled") shouldBe "true"
      audit("features.reference.enabled")  shouldBe "true"
      audit("features.details.enabled")    shouldBe "true"
      audit("features.flags.logging")      shouldBe "true"
      audit("features.flags.process")      shouldBe "true"
    }

    "return audit map for scotland payload" in {
      val audit = config.audit(Some(payloadScotland))

      audit("features.downstream.enabled") shouldBe "true"
      audit("features.reference.enabled")  shouldBe "true"
      audit("features.details.enabled")    shouldBe "true"
    }

    "return audit map for northern ireland payload" in {
      val audit = config.audit(Some(payloadNorthernIreland))

      audit("features.downstream.enabled") shouldBe "false"
      audit("features.reference.enabled")  shouldBe "false"
      audit("features.details.enabled")    shouldBe "false"
    }

    "return audit map for missing payload" in {
      val audit = config.audit(None)

      audit("features.downstream.enabled") shouldBe "false"
      audit("features.reference.enabled")  shouldBe "false"
      audit("features.details.enabled")    shouldBe "false"
    }

    "return des config values" in {
      config.desEnv shouldBe "dev"
      config.desToken should not be empty
    }

    "return service urls" in {
      config.serviceUrl should include("localhost")
      config.desUrl     should include("localhost")
    }

    "throw MatchingConfigurationException when ignoreAdditionalNames config is missing" in {
      val brokenConfig = new BrmConfig(config.conf) {
        override def ignoreAdditionalNames: Boolean =
          throw MatchingConfigurationException("ignoreAdditionalNames")
      }

      val e = intercept[RuntimeException] {
        brokenConfig.ignoreAdditionalNames
      }

      e.getMessage shouldBe "birth-registration-matching.matching.ignoreAdditionalNames configuration not found"
    }

    "throw DesException when des env config is missing" in {
      val brokenConfig = new BrmConfig(config.conf) {
        override def desEnv: String =
          throw DesException("env")
      }

      val e = intercept[RuntimeException] {
        brokenConfig.desEnv
      }

      e.getMessage shouldBe "des.env configuration not found"
    }

    "throw DesException when des auth token config is missing" in {
      val brokenConfig = new BrmConfig(config.conf) {
        override def desToken: String =
          throw DesException("auth-token")
      }

      val e = intercept[RuntimeException] {
        brokenConfig.desToken
      }

      e.getMessage shouldBe "des.auth-token configuration not found"
    }
  }

}
