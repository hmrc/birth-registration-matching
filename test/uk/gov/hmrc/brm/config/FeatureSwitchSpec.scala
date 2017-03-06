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

package uk.gov.hmrc.brm.config

import org.mockito.Matchers
import org.mockito.Matchers.{eq => mockEq}
import org.scalatest.BeforeAndAfter
import uk.gov.hmrc.play.http._
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import org.scalatestplus.play.OneAppPerTest
import play.api.inject.guice.GuiceApplicationBuilder
import org.scalatest.{Tag, TestData}
import com.kenshoo.play.metrics.PlayModule

class FeatureSwitchSpec extends UnitSpec with BeforeAndAfter with OneAppPerTest {

  import uk.gov.hmrc.brm.utils.TestHelper._
  import uk.gov.hmrc.brm.config.BrmConfig._

  lazy val switchDisabled: Map[String, _] = Map(
    "microservice.services.birth-registration-matching.features.gro.enabled" -> false,
    "microservice.services.birth-registration-matching.features.gro.reference.enabled" -> false,
    "microservice.services.birth-registration-matching.features.gro.details.enabled" -> false,
    "microservice.services.birth-registration-matching.features.nrs.enabled" -> false,
    "microservice.services.birth-registration-matching.features.nrs.reference.enabled" -> false,
    "microservice.services.birth-registration-matching.features.nrs.details.enabled" -> false
  )

  lazy val switchEnabled: Map[String, _] = Map(
    "microservice.services.birth-registration-matching.features.gro.enabled" -> true,
    "microservice.services.birth-registration-matching.features.gro.reference.enabled" -> true,
    "microservice.services.birth-registration-matching.features.gro.details.enabled" -> true,
    "microservice.services.birth-registration-matching.features.nrs.enabled" -> true,
    "microservice.services.birth-registration-matching.features.nrs.reference.enabled" -> true,
    "microservice.services.birth-registration-matching.features.nrs.details.enabled" -> true
  )

  override def newAppForTest(testData: TestData) = new GuiceApplicationBuilder(
    disabled = Seq(classOf[com.kenshoo.play.metrics.PlayModule])
  ).configure {
    if (testData.tags.contains("enabled")) {
      switchEnabled
    } else if (testData.tags.contains("disabled")) {
      switchDisabled
    } else {
      Map(
        "microservice.services.birth-registration-matching.features.gro.enabled" -> ""
      )
    }
  }.build()

  "FeatureSwitch" when {

    "GRO" when {

      "enabled" should {
        "have switch for GRO" taggedAs Tag("enabled") in {
          GROFeature().enabled shouldBe true
        }

        "have reference switch for GRO" taggedAs Tag("enabled") in {
          GROReferenceFeature().enabled shouldBe true
        }

        "have details switch for GRO" taggedAs Tag("enabled") in {
          GRODetailsFeature().enabled shouldBe true
        }
      }

      "disabled" should {
        "have switch for GRO" taggedAs Tag("disabled") in {
          GROFeature().enabled shouldBe false
        }

        "have reference switch for GRO" taggedAs Tag("disabled") in {
          GROReferenceFeature().enabled shouldBe false
        }

        "have details switch for GRO" taggedAs Tag("disabled") in {
          GRODetailsFeature().enabled shouldBe false
        }
      }

      // "not implemented" should {
      //
      //   "throw a BirthConfigurationException for gro.enabled" in {
      //     GROFeature().enabled shouldBe a[BirthConfigurationException]
      //   }
      //
      // }

    }

    "NRS" when {

      "enabled" should {

        "have switch for NRS" taggedAs Tag("enabled") in {
          NRSFeature().enabled shouldBe true
        }

        "have reference switch for NRS" taggedAs Tag("enabled") in {
          NRSReferenceFeature().enabled shouldBe true
        }

        "have details switch for NRS" taggedAs Tag("enabled") in {
          NRSDetailsFeature().enabled shouldBe true
        }

      }

      "disabled" should {

        "have switch for NRS" taggedAs Tag("disabled") in {
          NRSFeature().enabled shouldBe false
        }

        "have reference switch for NRS" taggedAs Tag("disabled") in {
          NRSReferenceFeature().enabled shouldBe false
        }

        "have details switch for NRS" taggedAs Tag("disabled") in {
          NRSDetailsFeature().enabled shouldBe false
        }

      }

    }

  }

}
