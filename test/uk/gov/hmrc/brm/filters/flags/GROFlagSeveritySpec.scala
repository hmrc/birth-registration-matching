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

package uk.gov.hmrc.brm.filters.flags

import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.brm.models.response.gro.{FlagSeverity, GROStatus}
import uk.gov.hmrc.play.test.UnitSpec

class GROFlagSeveritySpec extends UnitSpec with MockitoSugar {

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
    marginalNote = Some("marginal note"),
    reRegistered = None)

  val reRegistered = GROStatus(
    potentiallyFictitiousBirth = false,
    correction = None,
    cancelled = false,
    blockedRegistration = false,
    marginalNote = None,
    reRegistered = Some("re-registered note"))

  "determineFlagSeverity" should  {

    "return FlagSeverity" in {
     allFlagsGreen.determineFlagSeverity() shouldBe a[FlagSeverity]
    }

    "return true when all flags are default value" in {

      val groFlags = allFlagsGreen.determineFlagSeverity()
      groFlags.canProcessRecord() shouldBe true
    }

    "return false when all flags are red" in {

      val groFlags = allFlagsRed.determineFlagSeverity()
      groFlags.canProcessRecord() shouldBe false
    }

    "return false when potentiallyFictitiousBirthFlag is set" in {

      val groFlags = potentiallyFictitiousBirthFlag.determineFlagSeverity()
      groFlags.canProcessRecord() shouldBe false
    }

    "return true when correctionFlag is set" in {

      val groFlags = correctionFlag.determineFlagSeverity()
      groFlags.canProcessRecord() shouldBe true
    }

    "return true when cancelledFlag is set" in {

      val groFlags = cancelledFlag.determineFlagSeverity()
      groFlags.canProcessRecord() shouldBe true
    }

    "return false when blockedRegistration is set" in {

      val groFlags = blockedRegistration.determineFlagSeverity()
      groFlags.canProcessRecord() shouldBe false
    }

    "return true when marginalNote is set" in {

      val groFlags = marginalNote.determineFlagSeverity()
      groFlags.canProcessRecord() shouldBe true
    }

    "return true when reRegistered is set" in {

      val groFlags = reRegistered.determineFlagSeverity()
      groFlags.canProcessRecord() shouldBe true
    }

  }

}
