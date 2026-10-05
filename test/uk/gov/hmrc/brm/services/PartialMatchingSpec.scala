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

package uk.gov.hmrc.brm.services

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.mockito.stubbing.OngoingStubbing
import uk.gov.hmrc.brm.models.brm.Payload
import uk.gov.hmrc.brm.services.matching.{MatchingService, PartialMatching}
import uk.gov.hmrc.brm.utils.Mocks.*
import uk.gov.hmrc.brm.utils.TestHelper.{validRecord, validRecordMiddleNames}
import uk.gov.hmrc.brm.utils.{BaseUnitSpec, BirthRegisterCountry, MatchingType}
import uk.gov.hmrc.play.audit.http.connector.AuditResult

import java.time.LocalDate
import scala.concurrent.Future

class PartialMatchingSpec extends BaseUnitSpec {

  def firstNameApp: OngoingStubbing[Boolean] = {
    when(mockConfig.matchFirstName).thenReturn(true)
    when(mockConfig.ignoreAdditionalNames).thenReturn(true)
    when(mockConfig.matchLastName).thenReturn(false)
    when(mockConfig.matchDateOfBirth).thenReturn(false)
  }

  def additionalNamesFirstNameApp: OngoingStubbing[Boolean] = {
    when(mockConfig.matchFirstName).thenReturn(true)
    when(mockConfig.ignoreAdditionalNames).thenReturn(false)
    when(mockConfig.matchLastName).thenReturn(false)
    when(mockConfig.matchDateOfBirth).thenReturn(false)
  }

  def additionalNamesApp: OngoingStubbing[Boolean] = {
    when(mockConfig.matchFirstName).thenReturn(false)
    when(mockConfig.ignoreAdditionalNames).thenReturn(false)
    when(mockConfig.matchLastName).thenReturn(false)
    when(mockConfig.matchDateOfBirth).thenReturn(false)
  }

  def lastNameApp: OngoingStubbing[Boolean] = {
    when(mockConfig.matchFirstName).thenReturn(false)
    when(mockConfig.ignoreAdditionalNames).thenReturn(true)
    when(mockConfig.matchLastName).thenReturn(true)
    when(mockConfig.matchDateOfBirth).thenReturn(false)
  }

  def dobApp: OngoingStubbing[Boolean] = {
    when(mockConfig.matchFirstName).thenReturn(false)
    when(mockConfig.ignoreAdditionalNames).thenReturn(true)
    when(mockConfig.matchLastName).thenReturn(false)
    when(mockConfig.matchDateOfBirth).thenReturn(true)
  }

  def firstNameLastNameApp: OngoingStubbing[Boolean] = {
    when(mockConfig.matchFirstName).thenReturn(true)
    when(mockConfig.ignoreAdditionalNames).thenReturn(true)
    when(mockConfig.matchLastName).thenReturn(true)
    when(mockConfig.matchDateOfBirth).thenReturn(false)
  }

  def allFlagsTrueApp: OngoingStubbing[Boolean] = {
    when(mockConfig.matchFirstName).thenReturn(true)
    when(mockConfig.ignoreAdditionalNames).thenReturn(true)
    when(mockConfig.matchLastName).thenReturn(true)
    when(mockConfig.matchDateOfBirth).thenReturn(true)
  }

  val dateOfBirth: LocalDate    = LocalDate.of(2008, 2, 16)
  val altDateOfBirth: LocalDate = LocalDate.of(2012, 2, 16)

  val partial: PartialMatching = new PartialMatching(mockConfig)

  val testMatchingService: MatchingService = new MatchingService(
    mockConfig,
    mockMatchingAudit,
    mockFullMatching,
    partial,
    mockBrmLogger
  )

  "Partial Matching (feature switch turned off)" when {

    "match with reference" should {

      "return true result for firstName only" in {
        firstNameApp
        when(mockMatchingAudit.audit(any(), any())(using any()))
          .thenReturn(Future.successful(AuditResult.Success))
        when(mockConfig.validateFlag(any(), any()))
          .thenReturn(true)

        val payload     =
          Payload(Some("123456789"), "Chris", Some("test"), "wrongLastName", dateOfBirth, BirthRegisterCountry.ENGLAND)
        val resultMatch = testMatchingService.performMatch(payload, List(validRecord), MatchingType.PARTIAL)

        resultMatch.matched shouldBe true
      }

      "return true result for additionalName only" in {
        additionalNamesApp
        val payload     = Payload(
          Some("123456789"),
          "wrongFirstname",
          Some("David"),
          "wrongLastName",
          dateOfBirth,
          BirthRegisterCountry.ENGLAND
        )
        val resultMatch = testMatchingService.performMatch(payload, List(validRecordMiddleNames), MatchingType.PARTIAL)

        resultMatch.matched shouldBe true
      }

      "return true result for firstName and additionalName  only" in {
        additionalNamesFirstNameApp
        val payload     = Payload(
          Some("123456789"),
          "Adam",
          Some("David"),
          "wrongLastName",
          dateOfBirth,
          BirthRegisterCountry.ENGLAND
        )
        val resultMatch = testMatchingService.performMatch(payload, List(validRecordMiddleNames), MatchingType.PARTIAL)

        resultMatch.matched shouldBe true
      }

      "return true result for lastName only" in {
        lastNameApp
        val payload     = Payload(
          Some("123456789"),
          "wrongFirstName",
          None,
          "Jones",
          dateOfBirth,
          BirthRegisterCountry.ENGLAND
        )
        val resultMatch = testMatchingService.performMatch(payload, List(validRecord), MatchingType.PARTIAL)

        resultMatch.matched shouldBe true
      }

      "return true result for date of birth only" in {
        dobApp

        val payload     = Payload(
          Some("123456789"),
          "wrongFirstName",
          None,
          "wrongLastName",
          altDateOfBirth,
          BirthRegisterCountry.ENGLAND
        )
        val resultMatch = testMatchingService.performMatch(payload, List(validRecord), MatchingType.PARTIAL)

        resultMatch.matched shouldBe true
      }

      "return true result for firstName and LastName only" in {
        firstNameLastNameApp

        val payload     =
          Payload(Some("123456789"), "chris", None, "Jones", altDateOfBirth, BirthRegisterCountry.ENGLAND)
        val resultMatch = testMatchingService.performMatch(payload, List(validRecord), MatchingType.PARTIAL)
        resultMatch.matched shouldBe true
      }

    }

    "match without reference" should {

      "return true result for firstName only" in {
        firstNameApp
        val payload     =
          Payload(None, "Chris", None, "wrongLastName", dateOfBirth, BirthRegisterCountry.ENGLAND)
        val resultMatch = testMatchingService.performMatch(payload, List(validRecord), MatchingType.PARTIAL)

        resultMatch.matched shouldBe true
      }

      "return true result for additionalName only" in {
        additionalNamesApp
        val payload     = Payload(
          None,
          "wrongFirstname",
          Some("David"),
          "wrongLastName",
          dateOfBirth,
          BirthRegisterCountry.ENGLAND
        )
        val resultMatch = testMatchingService.performMatch(payload, List(validRecordMiddleNames), MatchingType.PARTIAL)

        resultMatch.matched shouldBe true
      }

      "return true result for firstName and additionalName  only" in {
        additionalNamesFirstNameApp
        val payload     = Payload(
          None,
          "Adam",
          Some("David"),
          "wrongLastName",
          dateOfBirth,
          BirthRegisterCountry.ENGLAND
        )
        val resultMatch = testMatchingService.performMatch(payload, List(validRecordMiddleNames), MatchingType.PARTIAL)

        resultMatch.matched shouldBe true
      }

      "return true result for lastName only" in {
        lastNameApp

        val payload     =
          Payload(None, "wrongFirstName", None, "Jones", dateOfBirth, BirthRegisterCountry.ENGLAND)
        val resultMatch = testMatchingService.performMatch(payload, List(validRecord), MatchingType.PARTIAL)

        resultMatch.matched shouldBe true
      }

      "return true result for date of birth only" in {
        dobApp

        val payload     = Payload(
          None,
          "wrongFirstName",
          None,
          "wrongLastName",
          altDateOfBirth,
          BirthRegisterCountry.ENGLAND
        )
        val resultMatch = testMatchingService.performMatch(payload, List(validRecord), MatchingType.PARTIAL)

        resultMatch.matched shouldBe true
      }

      "return true result for firstName and LastName only" in {
        firstNameLastNameApp

        val payload     = Payload(None, "chris", None, "Jones", altDateOfBirth, BirthRegisterCountry.ENGLAND)
        val resultMatch = testMatchingService.performMatch(payload, List(validRecord), MatchingType.PARTIAL)
        resultMatch.matched shouldBe true
      }

    }

    "return true when all config flags are true" in {
      allFlagsTrueApp
      testMatchingService.getMatchingType shouldBe MatchingType.FULL
    }
  }

}
