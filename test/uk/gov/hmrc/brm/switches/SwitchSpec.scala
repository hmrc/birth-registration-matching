/*
 * Copyright 2025 HM Revenue & Customs
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

package uk.gov.hmrc.brm.switches

import org.scalatest.{BeforeAndAfter, Tag, TestData}
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.brm.config.BrmConfig
import uk.gov.hmrc.brm.filters._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatest.OptionValues

/** Created by mew on 15/05/2017.
  */
trait SwitchSpec extends AnyWordSpecLike with Matchers with OptionValues with BeforeAndAfter with GuiceOneAppPerTest {

  object TestSwitch extends Switch {
    val config: BrmConfig = app.injector.instanceOf[BrmConfig]
    override val name     = "test"
  }

  object NonExistingSwitch extends Switch {
    val config: BrmConfig = app.injector.instanceOf[BrmConfig]
    override val name     = "invalid"
  }

  object NonExistingValue extends SwitchValue {
    val config: BrmConfig     = app.injector.instanceOf[BrmConfig]
    override val name: String = "invalid"
  }

  def switchDisabled: Map[String, _] = Map(
    "microservice.services.birth-registration-matching.features.test.enabled"          -> false,
    "microservice.services.birth-registration-matching.features.dobValidation.enabled" -> false
  )

  def switchEnabled: Map[String, _] = Map(
    "microservice.services.birth-registration-matching.features.test.enabled"          -> true,
    "microservice.services.birth-registration-matching.features.dobValidation.enabled" -> true
  )

  override def newAppForTest(testData: TestData): Application = GuiceApplicationBuilder()
    .configure {
      if (testData.tags.contains("enabled")) {
        switchEnabled
      } else if (testData.tags.contains("disabled")) {
        switchDisabled
      } else {
        Map.empty[String, Any]
      }
    }
    .disable[com.codahale.metrics.MetricRegistry]
    .build()

  "Switch" should {

    "load configuration for a feature and return true for isEnabled" taggedAs Tag("enabled") in {
      val switch = TestSwitch
      switch.isEnabled shouldBe true
    }

    "load configuration for a feature and return false for isEnabled" taggedAs Tag("disabled") in {
      val switch = TestSwitch
      switch.isEnabled shouldBe false
    }

    "throw FeatureSwitchException for configuration that doesn't exist" in {
      val e = intercept[RuntimeException](
        NonExistingSwitch.isEnabled
      )
      e.getMessage shouldBe "birth-registration-matching.features.invalid.enabled configuration not found"
    }

    "throw FeatureSwitchException for configuration value that doesn't exist" in {
      val e = intercept[RuntimeException](
        NonExistingValue.value
      )
      e.getMessage shouldBe "birth-registration-matching.features.invalid.enabled configuration not found"
    }

  }

  "GRO" should {

    "be enabled for GRO" in {
      app.injector.instanceOf[GROFilter].switch.isEnabled shouldBe true
    }

    "be enabled for GRO Reference" in {
      app.injector.instanceOf[GROReferenceFilter].switch.isEnabled shouldBe true
    }

    "be enabled for GRO Details" in {
      app.injector.instanceOf[GRODetailsFilter].switch.isEnabled shouldBe true
    }

  }

  "NRS" should {

    "be enabled for NRS" in {
      app.injector.instanceOf[GRODetailsFilter].switch.isEnabled shouldBe true
    }

    "be enabled for NRS Reference" in {
      app.injector.instanceOf[NRSReferenceFilter].switch.isEnabled shouldBe true
    }

    "be enabled for NRS Details" in {
      app.injector.instanceOf[NRSDetailsFilter].switch.isEnabled shouldBe true
    }

  }

  "GRO-NI" should {
    "be disabled for GRO-NI" in {
      app.injector.instanceOf[GRONIFilter].switch.isEnabled shouldBe false
    }

    "be disabled for GRO-NI Reference" in {
      app.injector.instanceOf[GRONIReferenceFilter].switch.isEnabled shouldBe false
    }

    "be disabled for GRO-NI Details" in {
      app.injector.instanceOf[GRONIDetailsFilter].switch.isEnabled shouldBe false
    }
  }

  "DateOfBirth" when {

    "enabled" should {
      "have a switch" taggedAs Tag("enabled") in {
        app.injector.instanceOf[DateOfBirthFilter].switch.isEnabled shouldBe true
      }

      "have a value" taggedAs Tag("enabled") in {
        app.injector.instanceOf[DateOfBirthFilter].switchValue should not be empty
      }
    }

    "disabled" should {
      "have a switch" taggedAs Tag("disabled") in {
        app.injector.instanceOf[DateOfBirthFilter].switch.isEnabled shouldBe false
      }

      "have a value" taggedAs Tag("disabled") in {
        app.injector.instanceOf[DateOfBirthFilter].switchValue should not be empty
      }
    }

  }

}
