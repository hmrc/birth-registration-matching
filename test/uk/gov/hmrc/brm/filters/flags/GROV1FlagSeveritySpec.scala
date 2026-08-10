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

package uk.gov.hmrc.brm.filters.flags

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatest.{OptionValues, Tag, TestData}
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.brm.config.BrmConfig
import uk.gov.hmrc.brm.models.response.gro.GROStatusV1

class GROFlagSeverityV1Spec
    extends AnyWordSpecLike with Matchers with OptionValues with MockitoSugar with GuiceOneAppPerTest {

  val allEnabledConfig: Map[String, _] = Map(
    "microservice.services.birth-registration-matching.features.gro.flags.potentiallyFictitious.process" -> true,
    "microservice.services.birth-registration-matching.features.gro.flags.blocked.process"               -> true,
    "microservice.services.birth-registration-matching.features.gro.flags.correction.process"            -> true,
    "microservice.services.birth-registration-matching.features.gro.flags.cancelled.process"             -> true,
    "microservice.services.birth-registration-matching.features.gro.flags.marginalNote.process"          -> true,
    "microservice.services.birth-registration-matching.features.gro.flags.reregistration.process"        -> true
  )

  val allDisabledConfig: Map[String, _] = Map(
    "microservice.services.birth-registration-matching.features.gro.flags.potentiallyFictitious.process" -> false,
    "microservice.services.birth-registration-matching.features.gro.flags.blocked.process"               -> false,
    "microservice.services.birth-registration-matching.features.gro.flags.correction.process"            -> false,
    "microservice.services.birth-registration-matching.features.gro.flags.cancelled.process"             -> false,
    "microservice.services.birth-registration-matching.features.gro.flags.marginalNote.process"          -> false,
    "microservice.services.birth-registration-matching.features.gro.flags.reregistration.process"        -> false
  )

  val potentiallyFictitiousConfig: Map[String, _] = Map(
    "microservice.services.birth-registration-matching.features.gro.flags.potentiallyFictitious.process" -> true,
    "microservice.services.birth-registration-matching.features.gro.flags.blocked.process"               -> false,
    "microservice.services.birth-registration-matching.features.gro.flags.correction.process"            -> false,
    "microservice.services.birth-registration-matching.features.gro.flags.cancelled.process"             -> false,
    "microservice.services.birth-registration-matching.features.gro.flags.marginalNote.process"          -> false
  )

  val blockedConfig: Map[String, _] = Map(
    "microservice.services.birth-registration-matching.features.gro.flags.potentiallyFictitious.process" -> false,
    "microservice.services.birth-registration-matching.features.gro.flags.blocked.process"               -> true,
    "microservice.services.birth-registration-matching.features.gro.flags.correction.process"            -> false,
    "microservice.services.birth-registration-matching.features.gro.flags.cancelled.process"             -> false,
    "microservice.services.birth-registration-matching.features.gro.flags.marginalNote.process"          -> false
  )

  val correctionConfig: Map[String, _] = Map(
    "microservice.services.birth-registration-matching.features.gro.flags.potentiallyFictitious.process" -> false,
    "microservice.services.birth-registration-matching.features.gro.flags.blocked.process"               -> false,
    "microservice.services.birth-registration-matching.features.gro.flags.correction.process"            -> true,
    "microservice.services.birth-registration-matching.features.gro.flags.cancelled.process"             -> false,
    "microservice.services.birth-registration-matching.features.gro.flags.marginalNote.process"          -> false
  )

  val cancelledConfig: Map[String, _] = Map(
    "microservice.services.birth-registration-matching.features.gro.flags.potentiallyFictitious.process" -> false,
    "microservice.services.birth-registration-matching.features.gro.flags.blocked.process"               -> false,
    "microservice.services.birth-registration-matching.features.gro.flags.correction.process"            -> false,
    "microservice.services.birth-registration-matching.features.gro.flags.cancelled.process"             -> true,
    "microservice.services.birth-registration-matching.features.gro.flags.marginalNote.process"          -> false
  )

  val marginalNoteConfig: Map[String, _] = Map(
    "microservice.services.birth-registration-matching.features.gro.flags.potentiallyFictitious.process" -> false,
    "microservice.services.birth-registration-matching.features.gro.flags.blocked.process"               -> false,
    "microservice.services.birth-registration-matching.features.gro.flags.correction.process"            -> false,
    "microservice.services.birth-registration-matching.features.gro.flags.cancelled.process"             -> false,
    "microservice.services.birth-registration-matching.features.gro.flags.marginalNote.process"          -> true
  )

  val reregistrationConfig: Map[String, _] = Map(
    "microservice.services.birth-registration-matching.features.gro.flags.potentiallyFictitious.process" -> false,
    "microservice.services.birth-registration-matching.features.gro.flags.blocked.process"               -> false,
    "microservice.services.birth-registration-matching.features.gro.flags.correction.process"            -> false,
    "microservice.services.birth-registration-matching.features.gro.flags.cancelled.process"             -> false,
    "microservice.services.birth-registration-matching.features.gro.flags.marginalNote.process"          -> false,
    "microservice.services.birth-registration-matching.features.gro.flags.reregistration.process"        -> true
  )

  override def newAppForTest(testData: TestData) =
    GuiceApplicationBuilder()
      .configure {
        if (testData.tags.contains("allEnabled")) {
          allEnabledConfig
        } else if (testData.tags.contains("allDisabled")) {
          allDisabledConfig
        } else if (testData.tags.contains("potentiallyFictitious")) {
          potentiallyFictitiousConfig
        } else if (testData.tags.contains("blocked")) {
          blockedConfig
        } else if (testData.tags.contains("correction")) {
          correctionConfig
        } else if (testData.tags.contains("cancelled")) {
          cancelledConfig
        } else if (testData.tags.contains("marginalNote")) {
          marginalNoteConfig
        } else if (testData.tags.contains("reregistration")) {
          reregistrationConfig
        } else {
          allEnabledConfig
        }
      }
      .build()

  val allFlagsGreen = GROStatusV1(
    potentiallyFictitious = false,
    correction = None,
    cancelled = false,
    blocked = false,
    marginalNote = None,
    reregistration = None
  )

  val allFlagsRed = GROStatusV1(
    potentiallyFictitious = true,
    correction = Some("reason here..."),
    cancelled = true,
    blocked = true,
    marginalNote = Some("reason here..."),
    reregistration = Some("reason here...")
  )

  val potentiallyFictitiousFlag = GROStatusV1(
    potentiallyFictitious = true,
    correction = None,
    cancelled = false,
    blocked = false,
    marginalNote = None,
    reregistration = None
  )

  val correctionFlag = GROStatusV1(
    potentiallyFictitious = false,
    correction = Some("correction on record"),
    cancelled = false,
    blocked = false,
    marginalNote = None,
    reregistration = None
  )

  val cancelledFlag = GROStatusV1(
    potentiallyFictitious = false,
    correction = None,
    cancelled = true,
    blocked = false,
    marginalNote = None,
    reregistration = None
  )

  val blockedFlag = GROStatusV1(
    potentiallyFictitious = false,
    correction = None,
    cancelled = false,
    blocked = true,
    marginalNote = None,
    reregistration = None
  )

  val marginalNoteFlag = GROStatusV1(
    potentiallyFictitious = false,
    correction = None,
    cancelled = false,
    blocked = false,
    marginalNote = Some("Other"),
    reregistration = None
  )

  val reregistrationFlag = GROStatusV1(
    potentiallyFictitious = false,
    correction = None,
    cancelled = false,
    blocked = false,
    marginalNote = None,
    reregistration = Some("Other")
  )

  "GROFlagSeverity.canProcessRecord for V1" when {

    "all flags are green" should {
      "return true" in {
        val config: BrmConfig = app.injector.instanceOf[BrmConfig]
        val groFlags          = allFlagsGreen.determineFlagSeverity
        groFlags.canProcessRecord(config) shouldBe true
      }
    }

    "all flags are red" should {

      "return false when all individual flags are enabled" taggedAs Tag("allEnabled") in {
        val config: BrmConfig = app.injector.instanceOf[BrmConfig]
        val groFlags          = allFlagsRed.determineFlagSeverity
        groFlags.canProcessRecord(config) shouldBe false
      }

      "return true when all individual flags are disabled" taggedAs Tag("allDisabled") in {
        val config: BrmConfig = app.injector.instanceOf[BrmConfig]
        val groFlags          = allFlagsRed.determineFlagSeverity
        groFlags.canProcessRecord(config) shouldBe true
      }
    }

    "potentiallyFictitious flag exists" should {

      "return false when flag exists and process flag is true" taggedAs Tag("potentiallyFictitious") in {
        val config: BrmConfig = app.injector.instanceOf[BrmConfig]
        val groFlags          = potentiallyFictitiousFlag.determineFlagSeverity
        groFlags.canProcessRecord(config) shouldBe false
      }

      "return true when flag exists and process flag is false" taggedAs Tag("allDisabled") in {
        val config: BrmConfig = app.injector.instanceOf[BrmConfig]
        val groFlags          = potentiallyFictitiousFlag.determineFlagSeverity
        groFlags.canProcessRecord(config) shouldBe true
      }
    }

    "blocked flag exists" should {

      "return false when flag exists and process flag is true" taggedAs Tag("blocked") in {
        val config: BrmConfig = app.injector.instanceOf[BrmConfig]
        val groFlags          = blockedFlag.determineFlagSeverity
        groFlags.canProcessRecord(config) shouldBe false
      }

      "return true when flag exists and process flag is false" taggedAs Tag("allDisabled") in {
        val config: BrmConfig = app.injector.instanceOf[BrmConfig]
        val groFlags          = blockedFlag.determineFlagSeverity
        groFlags.canProcessRecord(config) shouldBe true
      }
    }

    "correction exists" should {

      "return false when flag exists and process flag is true" taggedAs Tag("correction") in {
        val config: BrmConfig = app.injector.instanceOf[BrmConfig]
        val groFlags          = correctionFlag.determineFlagSeverity
        groFlags.canProcessRecord(config) shouldBe false
      }

      "return true when flag exists and process flag is false" taggedAs Tag("allDisabled") in {
        val config: BrmConfig = app.injector.instanceOf[BrmConfig]
        val groFlags          = correctionFlag.determineFlagSeverity
        groFlags.canProcessRecord(config) shouldBe true
      }
    }

    "cancelled flag exists" should {

      "return false when flag exists and process flag is true" taggedAs Tag("cancelled") in {
        val config: BrmConfig = app.injector.instanceOf[BrmConfig]
        val groFlags          = cancelledFlag.determineFlagSeverity
        groFlags.canProcessRecord(config) shouldBe false
      }

      "return true when flag exists and process flag is false" taggedAs Tag("allDisabled") in {
        val config: BrmConfig = app.injector.instanceOf[BrmConfig]
        val groFlags          = cancelledFlag.determineFlagSeverity
        groFlags.canProcessRecord(config) shouldBe true
      }
    }

    "marginalNote exists" should {

      "return false when flag exists and process flag is true" taggedAs Tag("marginalNote") in {
        val config: BrmConfig = app.injector.instanceOf[BrmConfig]
        val groFlags          = marginalNoteFlag.determineFlagSeverity
        groFlags.canProcessRecord(config) shouldBe false
      }

      "return true when flag exists and process flag is false" taggedAs Tag("allDisabled") in {
        val config: BrmConfig = app.injector.instanceOf[BrmConfig]
        val groFlags          = marginalNoteFlag.determineFlagSeverity
        groFlags.canProcessRecord(config) shouldBe true
      }
    }

    "reregistration exists" should {

      "return false when flag exists and process flag is true" taggedAs Tag("reregistration") in {
        val config: BrmConfig = app.injector.instanceOf[BrmConfig]
        val groFlags          = reregistrationFlag.determineFlagSeverity
        groFlags.canProcessRecord(config) shouldBe false
      }

      "return true when flag exists and process flag is false" taggedAs Tag("allDisabled") in {
        val config: BrmConfig = app.injector.instanceOf[BrmConfig]
        val groFlags          = reregistrationFlag.determineFlagSeverity
        groFlags.canProcessRecord(config) shouldBe true
      }
    }
  }

}
