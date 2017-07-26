/*
 * Copyright 2017 HM Revenue & Customs
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
import org.scalatestplus.play.OneAppPerTest
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.play.test.UnitSpec

/**
  * Created by mew on 15/05/2017.
  */
class SwitchSpec extends UnitSpec with BeforeAndAfter with OneAppPerTest {

  object TestSwitch extends Switch {
    override val name = "test"
  }

  object NonExistingSwitch extends Switch {
    override val name = "invalid"
  }

  object NonExistingValue extends SwitchValue {
    override val name: String = "invalid"
  }

  def switchDisabled: Map[String, _] = Map(
    "microservice.services.birth-registration-matching.features.test.enabled" -> false,
    "microservice.services.birth-registration-matching.features.dobValidation.enabled" -> false
  )

  def switchEnabled: Map[String, _] = Map(
    "microservice.services.birth-registration-matching.features.test.enabled" -> true,
    "microservice.services.birth-registration-matching.features.dobValidation.enabled" -> true
  )

  override def newAppForTest(testData: TestData) = GuiceApplicationBuilder()
    .configure {
    if (testData.tags.contains("enabled")) {
      switchEnabled
    } else if (testData.tags.contains("disabled")) {
      switchDisabled
    } else {
      Map.empty[String, Any]
    }
  }
    .disable[com.kenshoo.play.metrics.PlayModule]
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
      GROSwitch.isEnabled shouldBe true
    }

    "be enabled for GRO Reference" in {
      GROReferenceSwitch.isEnabled shouldBe true
    }

    "be enabled for GRO Details" in {
      GRODetailsSwitch.isEnabled shouldBe true
    }

  }

  "NRS" should {

    "be enabled for NRS" in {
      NRSSwitch.isEnabled shouldBe true
    }

    "be enabled for NRS Reference" in {
      NRSReferenceSwitch.isEnabled shouldBe true
    }

    "be enabled for NRS Details" in {
      NRSDetailsSwitch.isEnabled shouldBe true
    }

  }

  "GRO-NI" should {
    "be disabled for GRO-NI" in {
      GRONISwitch.isEnabled shouldBe false
    }

    "be disabled for GRO-NI Reference" in {
      GRONIReferenceSwitch.isEnabled shouldBe false
    }

    "be disabled for GRO-NI Details" in {
      GRONIDetailsSwitch.isEnabled shouldBe false
    }
  }

  "DateOfBirth" when {

    "enabled" should {
      "have a switch" taggedAs Tag("enabled") in {
        DateOfBirthSwitch.isEnabled shouldBe true
      }

      "have a value" taggedAs Tag("enabled") in {
        DateOfBirthSwitchValue.value should not be empty
      }
    }

    "disabled" should {
      "have a switch" taggedAs Tag("disabled") in {
        DateOfBirthSwitch.isEnabled shouldBe false
      }

      "have a value" taggedAs Tag("disabled") in {
        DateOfBirthSwitchValue.value should not be empty
      }
    }

  }

}
