/*
 * Copyright 2021 HM Revenue & Customs
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

import org.scalatest.{Tag, TestData}
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.brm.config.BrmConfig
import uk.gov.hmrc.brm.models.response.gro.GROStatus
import org.scalatest.{Matchers, OptionValues, WordSpecLike}

trait GROFlagSeveritySpec extends WordSpecLike with Matchers with OptionValues with MockitoSugar with GuiceOneAppPerTest {

  val config: BrmConfig = app.injector.instanceOf[BrmConfig]

  val allEnabledConfig: Map[String, _] = Map(
    "microservice.services.birth-registration-matching.features.gro.flags.potentiallyFictitiousBirth.process" -> true,
    "microservice.services.birth-registration-matching.features.gro.flags.blockedRegistration.process" -> true,
    "microservice.services.birth-registration-matching.features.gro.flags.correction.process" -> true,
    "microservice.services.birth-registration-matching.features.gro.flags.cancelled.process" -> true,
    "microservice.services.birth-registration-matching.features.gro.flags.marginalNote.process" -> true,
    "microservice.services.birth-registration-matching.features.gro.flags.reRegistered.process" -> true
  )

  val allDisabledConfig: Map[String, _] = Map(
    "microservice.services.birth-registration-matching.features.gro.flags.potentiallyFictitiousBirth.process" -> false,
    "microservice.services.birth-registration-matching.features.gro.flags.blockedRegistration.process" -> false,
    "microservice.services.birth-registration-matching.features.gro.flags.correction.process" -> false,
    "microservice.services.birth-registration-matching.features.gro.flags.cancelled.process" -> false,
    "microservice.services.birth-registration-matching.features.gro.flags.marginalNote.process" -> false,
    "microservice.services.birth-registration-matching.features.gro.flags.reRegistered.process" -> false
  )

  val potentiallyFictitiousBirthConfig: Map[String, _] = Map(
    "microservice.services.birth-registration-matching.features.gro.flags.potentiallyFictitiousBirth.process" -> true,
    "microservice.services.birth-registration-matching.features.gro.flags.blockedRegistration.process" -> false,
    "microservice.services.birth-registration-matching.features.gro.flags.correction.process" -> false,
    "microservice.services.birth-registration-matching.features.gro.flags.cancelled.process" -> false,
    "microservice.services.birth-registration-matching.features.gro.flags.marginalNote.process" -> false
  )

  val blockedRegistrationConfig: Map[String, _] = Map(
    "microservice.services.birth-registration-matching.features.gro.flags.potentiallyFictitiousBirth.process" -> false,
    "microservice.services.birth-registration-matching.features.gro.flags.blockedRegistration.process" -> true,
    "microservice.services.birth-registration-matching.features.gro.flags.correction.process" -> false,
    "microservice.services.birth-registration-matching.features.gro.flags.cancelled.process" -> false,
    "microservice.services.birth-registration-matching.features.gro.flags.marginalNote.process" -> false
  )

  val correctionConfig: Map[String, _] = Map(
    "microservice.services.birth-registration-matching.features.gro.flags.potentiallyFictitiousBirth.process" -> false,
    "microservice.services.birth-registration-matching.features.gro.flags.blockedRegistration.process" -> false,
    "microservice.services.birth-registration-matching.features.gro.flags.correction.process" -> true,
    "microservice.services.birth-registration-matching.features.gro.flags.cancelled.process" -> false,
    "microservice.services.birth-registration-matching.features.gro.flags.marginalNote.process" -> false
  )

  val cancelledConfig: Map[String, _] = Map(
    "microservice.services.birth-registration-matching.features.gro.flags.potentiallyFictitiousBirth.process" -> false,
    "microservice.services.birth-registration-matching.features.gro.flags.blockedRegistration.process" -> false,
    "microservice.services.birth-registration-matching.features.gro.flags.correction.process" -> false,
    "microservice.services.birth-registration-matching.features.gro.flags.cancelled.process" -> true,
    "microservice.services.birth-registration-matching.features.gro.flags.marginalNote.process" -> false
  )

  val marginalNoteConfig: Map[String, _] = Map(
    "microservice.services.birth-registration-matching.features.gro.flags.potentiallyFictitiousBirth.process" -> false,
    "microservice.services.birth-registration-matching.features.gro.flags.blockedRegistration.process" -> false,
    "microservice.services.birth-registration-matching.features.gro.flags.correction.process" -> false,
    "microservice.services.birth-registration-matching.features.gro.flags.cancelled.process" -> false,
    "microservice.services.birth-registration-matching.features.gro.flags.marginalNote.process" -> true
  )


  val reRegisteredConfig: Map[String, _] = Map(
    "microservice.services.birth-registration-matching.features.gro.flags.potentiallyFictitiousBirth.process" -> false,
    "microservice.services.birth-registration-matching.features.gro.flags.blockedRegistration.process" -> false,
    "microservice.services.birth-registration-matching.features.gro.flags.correction.process" -> false,
    "microservice.services.birth-registration-matching.features.gro.flags.cancelled.process" -> false,
    "microservice.services.birth-registration-matching.features.gro.flags.marginalNote.process" -> false,
    "microservice.services.birth-registration-matching.features.gro.flags.reRegistered.process" -> true
  )


  override def newAppForTest(testData: TestData) = GuiceApplicationBuilder()
    .configure {
      if (testData.tags.contains("allEnabled")) {
        allEnabledConfig
      }
      else if (testData.tags.contains("allDisabled")) {
        allDisabledConfig
      }
      else if (testData.tags.contains("potentiallyFictitiousBirth")) {
        potentiallyFictitiousBirthConfig
      }
      else if (testData.tags.contains("blockedRegistration")) {
        blockedRegistrationConfig
      }
      else if (testData.tags.contains("correction")) {
        correctionConfig
      }
      else if (testData.tags.contains("cancelled")) {
        cancelledConfig
      }
      else if (testData.tags.contains("marginalNote")) {
        marginalNoteConfig
      }
      else if (testData.tags.contains("reRegistered")) {
        reRegisteredConfig
      }
      else {
        allEnabledConfig
      }
    }.build()

  val allFlagsGreen = GROStatus(
    potentiallyFictitiousBirth = false,
    correction = None,
    cancelled = false,
    blockedRegistration = false,
    marginalNote = None,
    reRegistered = None)

  val allFlagsRed = GROStatus(
    potentiallyFictitiousBirth = true,
    correction = Some("reason here..."),
    cancelled = true,
    blockedRegistration = true,
    marginalNote = Some("reason here..."),
    reRegistered = Some("reason here..."))

  val potentiallyFictitiousBirthFlag = GROStatus(
    potentiallyFictitiousBirth = true,
    correction = None,
    cancelled = false,
    blockedRegistration = false,
    marginalNote = None,
    reRegistered = None)

  val correctionFlag = GROStatus(
    potentiallyFictitiousBirth = false,
    correction = Some("correction on record"),
    cancelled = false,
    blockedRegistration = false,
    marginalNote = None,
    reRegistered = None)

  val cancelledFlag = GROStatus(
    potentiallyFictitiousBirth = false,
    correction = None,
    cancelled = true,
    blockedRegistration = false,
    marginalNote = None,
    reRegistered = None)

  val blockedRegistration = GROStatus(
    potentiallyFictitiousBirth = false,
    correction = None,
    cancelled = false,
    blockedRegistration = true,
    marginalNote = None,
    reRegistered = None)

  val marginalNote = GROStatus(
    potentiallyFictitiousBirth = false,
    correction = None,
    cancelled = false,
    blockedRegistration = false,
    marginalNote = Some("Other"),
    reRegistered = None)

  val reRegistered = GROStatus(
    potentiallyFictitiousBirth = false,
    correction = None,
    cancelled = false,
    blockedRegistration = false,
    marginalNote = None,
    reRegistered = Some("Other"))

  "GROFlagSeverity.canProcessRecord" when {

    "all flags are green" should {
      "return true " in {
        val groFlags = allFlagsGreen.determineFlagSeverity
        groFlags.canProcessRecord(config) shouldBe true
      }
    }

    "all flags are red" should {
      "return false when all individual flags are set to true" taggedAs Tag("allEnabled") in {
        val groFlags = allFlagsRed.determineFlagSeverity
        groFlags.canProcessRecord(config) shouldBe false
      }

      "return true when all individual flags are set to  false" taggedAs Tag("allDisabled") in {
        val groFlags = allFlagsRed.determineFlagSeverity
        groFlags.canProcessRecord(config) shouldBe true
      }
    }

    "potentiallyFictitiousBirthFlag exists" should {
      "return false when flag is set and process flag is true" taggedAs Tag("potentiallyFictitiousBirth") in {
        val groFlags = potentiallyFictitiousBirthFlag.determineFlagSeverity
        groFlags.canProcessRecord(config) shouldBe false
      }

      "return true when when flag is set and process flag is false" taggedAs Tag("allDisabled") in {
        val groFlags = potentiallyFictitiousBirthFlag.determineFlagSeverity
        groFlags.canProcessRecord(config) shouldBe true
      }
    }

    "blockedRegistration exists" should {
      "return false when flag is set and process flag is true" taggedAs Tag("blockedRegistration") in {
        val groFlags = blockedRegistration.determineFlagSeverity
        groFlags.canProcessRecord(config) shouldBe false
      }

      "return true when flag is set and process flag is false" taggedAs Tag("allDisabled") in {
        val groFlags = blockedRegistration.determineFlagSeverity
        groFlags.canProcessRecord(config) shouldBe true
      }
    }

    "correction exists" should {
      "return false when flag is set and process flag is true" taggedAs Tag("correction") in {
        val groFlags = correctionFlag.determineFlagSeverity
        groFlags.canProcessRecord(config) shouldBe false
      }

      "return true when flag is set and process flag is false" taggedAs Tag("allDisabled") in {
        val groFlags = correctionFlag.determineFlagSeverity
        groFlags.canProcessRecord(config) shouldBe true
      }
    }

    "cancelled exists" should {
      "return false when flag is set and process flag is true" taggedAs Tag("cancelled") in {
        val groFlags = cancelledFlag.determineFlagSeverity
        groFlags.canProcessRecord(config) shouldBe false
      }

      "return true when flag is set and process flag is false" taggedAs Tag("allDisabled") in {
        val groFlags = cancelledFlag.determineFlagSeverity
        groFlags.canProcessRecord(config) shouldBe true
      }
    }

    "marginalNote exists" should {
      "return false when flag is set and process flag is true" taggedAs Tag("marginalNote") in {
        val groFlags = marginalNote.determineFlagSeverity
        groFlags.canProcessRecord(config) shouldBe false
      }

      "return true when flag is set and process flag is false" taggedAs Tag("allDisabled") in {
        val groFlags = marginalNote.determineFlagSeverity
        groFlags.canProcessRecord(config) shouldBe true
      }
    }

    "reRegistered exists" should {
      "return false when flag is set and process flag is true" taggedAs Tag("reRegistered") in {
        val groFlags = reRegistered.determineFlagSeverity
        groFlags.canProcessRecord(config) shouldBe false
      }

      "return true when flag is set and process flag is false" taggedAs Tag("allDisabled") in {
        val groFlags = reRegistered.determineFlagSeverity
        groFlags.canProcessRecord(config) shouldBe true
      }

    }

  }

}
