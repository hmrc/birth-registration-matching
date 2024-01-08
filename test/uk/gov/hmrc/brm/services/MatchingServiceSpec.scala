/*
 * Copyright 2023 HM Revenue & Customs
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

import java.time.LocalDate
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import org.mockito.stubbing.OngoingStubbing
import org.scalatest._
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.{GuiceOneAppPerSuite, GuiceOneAppPerTest}
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers._
import uk.gov.hmrc.brm.models.brm.Payload
import uk.gov.hmrc.brm.services.matching.{FullMatching, Good, MatchingService, PartialMatching}
import uk.gov.hmrc.brm.utils.FlagsHelper._
import uk.gov.hmrc.brm.utils.TestHelper._
import uk.gov.hmrc.brm.utils.{BirthRegisterCountry, MatchingType}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditResult
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatest.OptionValues

import scala.concurrent.Future

class PartialMatchingSpec
    extends AnyWordSpecLike
    with Matchers
    with OptionValues
    with MockitoSugar
    with BeforeAndAfterAll
    with GuiceOneAppPerSuite {

  import uk.gov.hmrc.brm.utils.Mocks._

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

  implicit val hc: HeaderCarrier = HeaderCarrier()

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
        when(mockMatchingAudit.audit(any(), any())(any()))
          .thenReturn(Future.successful(AuditResult.Success))
        when(mockConfig.validateFlag(any(), any()))
          .thenReturn(true)

        val payload     = Payload(
          Some("123456789"),
          "Chris",
          Some("test"),
          "wrongLastName",
          dateOfBirth,
          BirthRegisterCountry.ENGLAND
        )
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

  }

}

//TODO Just FYI, this spec doesn't run because it's a trait
trait MatchingServiceSpec
    extends AnyWordSpecLike
    with Matchers
    with OptionValues
    with MockitoSugar
    with GuiceOneAppPerTest {

  import uk.gov.hmrc.brm.utils.Mocks._

  implicit val hc: HeaderCarrier      = HeaderCarrier()
  val references: Seq[Option[String]] = List(Some("123456789"), None)

  val configIgnoreAdditionalNames: Map[String, _] = Map(
    "microservice.services.birth-registration-matching.matching.ignoreAdditionalNames" -> false,
    "microservice.services.birth-registration-matching.features.flags.process"         -> false
  )

  val processFlags: Map[String, _] = Map(
    "microservice.services.birth-registration-matching.matching.ignoreAdditionalNames" -> false,
    "microservice.services.birth-registration-matching.features.flags.process"         -> true
  )

  def switchEnabled: Map[String, _] = Map(
    "microservice.services.birth-registration-matching.matching.ignoreAdditionalNames"                        -> false,
    "microservice.services.birth-registration-matching.features.flags.process"                                -> true,
    "microservice.services.birth-registration-matching.features.gro.flags.potentiallyFictitiousBirth.process" -> true,
    "microservice.services.birth-registration-matching.features.gro.flags.blockedRegistration.process"        -> true,
    "microservice.services.birth-registration-matching.features.gro.flags.correction.process"                 -> true,
    "microservice.services.birth-registration-matching.features.gro.flags.cancelled.process"                  -> true,
    "microservice.services.birth-registration-matching.features.gro.flags.marginalNote.process"               -> true,
    "microservice.services.birth-registration-matching.features.gro.flags.reRegistered.process"               -> true
  )

  def switchDisabled: Map[String, _] = Map(
    "microservice.services.birth-registration-matching.matching.ignoreAdditionalNames" -> false,
    "microservice.services.birth-registration-matching.features.flags.process"         -> false
  )

  override def newAppForTest(testData: TestData): Application = GuiceApplicationBuilder()
    .configure(
      if (testData.tags.contains("enabled")) {
        switchEnabled
      } else if (testData.tags.contains("disabled")) {
        switchDisabled
      } else {
        switchEnabled
      }
    )
    .build()

  def getApp(config: Map[String, _]): Application = GuiceApplicationBuilder(
    disabled = Seq(classOf[com.kenshoo.play.metrics.PlayModule])
  )
    .configure(configIgnoreAdditionalNames)
    .build()

  private val marginalNoteInvalidFlagValues = List("Other", "Re-registered", "Court order in place")
  private val marginalNoteValidFlagValues   = List("Court order revoked", "None")

  val full: FullMatching = new FullMatching(mockConfig)

  val testMatchingService: MatchingService = new MatchingService(
    mockConfig,
    mockMatchingAudit,
    full,
    mockPartialMatching,
    mockBrmLogger
  )

  references.foreach { reference =>
    val name = reference match {
      case Some(x) => "with reference"
      case None    => "without reference"
    }

    val altDateOfBirth: LocalDate = LocalDate.of(2012, 2, 16)

    "MatchingService.performMatch" when {

      "record contains a fictitious birth" should {
        s"($name) not match when processFlags is true" taggedAs Tag("enabled") in {
          when(mockAuditConnector.sendEvent(any())(any(), any())).thenReturn(Future.successful(AuditResult.Success))
          val payload     =
            Payload(reference, "Chris", None, "Jones", altDateOfBirth, BirthRegisterCountry.ENGLAND)
          val resultMatch = testMatchingService.performMatch(payload, List(flaggedFictitiousBirth), MatchingType.FULL)
          resultMatch.matched                shouldBe false
          resultMatch.firstNamesMatched      shouldBe "Bad()"
          resultMatch.additionalNamesMatched shouldBe Good()
          resultMatch.lastNameMatched        shouldBe Good()
          resultMatch.dateOfBirthMatched     shouldBe Good()
        }

        s"($name) match when processFlags is false" taggedAs Tag("disabled") in {
          when(mockAuditConnector.sendEvent(any())(any(), any())).thenReturn(Future.successful(AuditResult.Success))
          val payload     =
            Payload(reference, "Chris", None, "Jones", altDateOfBirth, BirthRegisterCountry.ENGLAND)
          val resultMatch = testMatchingService.performMatch(payload, List(flaggedFictitiousBirth), MatchingType.FULL)
          resultMatch.matched                shouldBe true
          resultMatch.firstNamesMatched      shouldBe Good()
          resultMatch.additionalNamesMatched shouldBe Good()
          resultMatch.lastNameMatched        shouldBe Good()
          resultMatch.dateOfBirthMatched     shouldBe Good()
        }
      }

      "record contains a blocked birth" should {
        s"($name) not match when processFlags is true" taggedAs Tag("enabled") in {
          when(mockAuditConnector.sendEvent(any())(any(), any())).thenReturn(Future.successful(AuditResult.Success))
          val payload     =
            Payload(reference, "Chris", None, "Jones", altDateOfBirth, BirthRegisterCountry.ENGLAND)
          val resultMatch =
            testMatchingService.performMatch(payload, List(flaggedBlockedRegistration), MatchingType.FULL)
          resultMatch.matched                shouldBe false
          resultMatch.firstNamesMatched      shouldBe Good()
          resultMatch.additionalNamesMatched shouldBe Good()
          resultMatch.lastNameMatched        shouldBe Good()
          resultMatch.dateOfBirthMatched     shouldBe Good()
        }

        s"($name) match when processFlags is false" taggedAs Tag("disabled") in {
          when(mockAuditConnector.sendEvent(any())(any(), any())).thenReturn(Future.successful(AuditResult.Success))
          val payload     =
            Payload(reference, "Chris", None, "Jones", altDateOfBirth, BirthRegisterCountry.ENGLAND)
          val resultMatch =
            testMatchingService.performMatch(payload, List(flaggedBlockedRegistration), MatchingType.FULL)
          resultMatch.matched                shouldBe true
          resultMatch.firstNamesMatched      shouldBe Good()
          resultMatch.additionalNamesMatched shouldBe Good()
          resultMatch.lastNameMatched        shouldBe Good()
          resultMatch.dateOfBirthMatched     shouldBe Good()
        }

      }

      "record contains a correction" should {
        s"($name) not match when processFlags is true" taggedAs Tag("enabled") in {
          when(mockAuditConnector.sendEvent(any())(any(), any())).thenReturn(Future.successful(AuditResult.Success))
          val payload     =
            Payload(reference, "Chris", None, "Jones", altDateOfBirth, BirthRegisterCountry.ENGLAND)
          val resultMatch = testMatchingService.performMatch(payload, List(correction), MatchingType.FULL)
          resultMatch.matched                shouldBe false
          resultMatch.firstNamesMatched      shouldBe Good()
          resultMatch.additionalNamesMatched shouldBe Good()
          resultMatch.lastNameMatched        shouldBe Good()
          resultMatch.dateOfBirthMatched     shouldBe Good()
        }

        s"($name) match when processFlags is false" taggedAs Tag("disabled") in {
          when(mockAuditConnector.sendEvent(any())(any(), any())).thenReturn(Future.successful(AuditResult.Success))
          val payload     =
            Payload(reference, "Chris", None, "Jones", altDateOfBirth, BirthRegisterCountry.ENGLAND)
          val resultMatch = testMatchingService.performMatch(payload, List(correction), MatchingType.FULL)
          resultMatch.matched                shouldBe true
          resultMatch.firstNamesMatched      shouldBe Good()
          resultMatch.additionalNamesMatched shouldBe Good()
          resultMatch.lastNameMatched        shouldBe Good()
          resultMatch.dateOfBirthMatched     shouldBe Good()
        }

      }

      "record contains a cancelled flag" should {
        s"($name) not match when processFlags is true" taggedAs Tag("enabled") in {
          when(mockAuditConnector.sendEvent(any())(any(), any())).thenReturn(Future.successful(AuditResult.Success))
          val payload     =
            Payload(reference, "Chris", None, "Jones", altDateOfBirth, BirthRegisterCountry.ENGLAND)
          val resultMatch = testMatchingService.performMatch(payload, List(cancelled), MatchingType.FULL)
          resultMatch.matched                shouldBe false
          resultMatch.firstNamesMatched      shouldBe Good()
          resultMatch.additionalNamesMatched shouldBe Good()
          resultMatch.lastNameMatched        shouldBe Good()
          resultMatch.dateOfBirthMatched     shouldBe Good()
        }

        s"($name) match when processFlags is false" taggedAs Tag("disabled") in {
          when(mockAuditConnector.sendEvent(any())(any(), any())).thenReturn(Future.successful(AuditResult.Success))
          val payload     =
            Payload(reference, "Chris", None, "Jones", altDateOfBirth, BirthRegisterCountry.ENGLAND)
          val resultMatch = testMatchingService.performMatch(payload, List(cancelled), MatchingType.FULL)
          resultMatch.matched                shouldBe true
          resultMatch.firstNamesMatched      shouldBe Good()
          resultMatch.additionalNamesMatched shouldBe Good()
          resultMatch.lastNameMatched        shouldBe Good()
          resultMatch.dateOfBirthMatched     shouldBe Good()
        }

      }

      for (validFlagValue <- marginalNoteValidFlagValues)
        s"record contains a marginalNote flag of $validFlagValue" should {
          s"($name) match when processFlags is true" taggedAs Tag("enabled") in {
            when(mockAuditConnector.sendEvent(any())(any(), any())).thenReturn(Future.successful(AuditResult.Success))
            val payload     =
              Payload(reference, "Chris", None, "Jones", altDateOfBirth, BirthRegisterCountry.ENGLAND)
            val resultMatch =
              testMatchingService.performMatch(payload, List(marginalNote(validFlagValue)), MatchingType.FULL)
            resultMatch.matched                shouldBe true
            resultMatch.firstNamesMatched      shouldBe Good()
            resultMatch.additionalNamesMatched shouldBe Good()
            resultMatch.lastNameMatched        shouldBe Good()
            resultMatch.dateOfBirthMatched     shouldBe Good()
          }
        }

      for (flagValue <- marginalNoteInvalidFlagValues)
        s"record contains a marginalNote flag of $flagValue" should {
          s"($name) not match when processFlags is true" taggedAs Tag("enabled") in {
            when(mockAuditConnector.sendEvent(any())(any(), any())).thenReturn(Future.successful(AuditResult.Success))
            val payload     =
              Payload(reference, "Chris", None, "Jones", altDateOfBirth, BirthRegisterCountry.ENGLAND)
            val resultMatch =
              testMatchingService.performMatch(payload, List(marginalNote(flagValue)), MatchingType.FULL)
            resultMatch.matched                shouldBe false
            resultMatch.firstNamesMatched      shouldBe Good()
            resultMatch.additionalNamesMatched shouldBe Good()
            resultMatch.lastNameMatched        shouldBe Good()
            resultMatch.dateOfBirthMatched     shouldBe Good()
          }

          s"($name) match when processFlags is false" taggedAs Tag("disabled") in {
            when(mockAuditConnector.sendEvent(any())(any(), any())).thenReturn(Future.successful(AuditResult.Success))
            val payload     =
              Payload(reference, "Chris", None, "Jones", altDateOfBirth, BirthRegisterCountry.ENGLAND)
            val resultMatch =
              testMatchingService.performMatch(payload, List(marginalNote(flagValue)), MatchingType.FULL)
            resultMatch.matched                shouldBe true
            resultMatch.firstNamesMatched      shouldBe Good()
            resultMatch.additionalNamesMatched shouldBe Good()
            resultMatch.lastNameMatched        shouldBe Good()
            resultMatch.dateOfBirthMatched     shouldBe Good()
          }

        }

      "record contains a reRegistered flag" should {
        s"($name) not match when processFlags is true" taggedAs Tag("enabled") in {
          when(mockAuditConnector.sendEvent(any())(any(), any())).thenReturn(Future.successful(AuditResult.Success))
          val payload     =
            Payload(reference, "Chris", None, "Jones", altDateOfBirth, BirthRegisterCountry.ENGLAND)
          val resultMatch = testMatchingService.performMatch(payload, List(reRegistered("Other")), MatchingType.FULL)
          resultMatch.matched                shouldBe false
          resultMatch.firstNamesMatched      shouldBe Good()
          resultMatch.additionalNamesMatched shouldBe Good()
          resultMatch.lastNameMatched        shouldBe Good()
          resultMatch.dateOfBirthMatched     shouldBe Good()
        }

        s"($name) match when processFlags is false" taggedAs Tag("disabled") in {
          when(mockAuditConnector.sendEvent(any())(any(), any())).thenReturn(Future.successful(AuditResult.Success))
          val payload     =
            Payload(reference, "Chris", None, "Jones", altDateOfBirth, BirthRegisterCountry.ENGLAND)
          val resultMatch = testMatchingService.performMatch(payload, List(reRegistered("Other")), MatchingType.FULL)
          resultMatch.matched                shouldBe true
          resultMatch.firstNamesMatched      shouldBe Good()
          resultMatch.additionalNamesMatched shouldBe Good()
          resultMatch.lastNameMatched        shouldBe Good()
          resultMatch.dateOfBirthMatched     shouldBe Good()
        }

      }

    }

    "MatchingService" should {

      s"($name) match when firstName contains special characters" in {
        running(getApp(configIgnoreAdditionalNames)) {
          when(mockAuditConnector.sendEvent(any())(any(), any())).thenReturn(Future.successful(AuditResult.Success))

          val payload     =
            Payload(reference, "Chris-Jame's", None, "Jones", altDateOfBirth, BirthRegisterCountry.ENGLAND)
          val resultMatch =
            testMatchingService.performMatch(payload, List(validRecordSpecialCharactersFirstName), MatchingType.FULL)
          resultMatch.matched shouldBe true
        }
      }

      s"($name) match when lastName contains special characters" in {
        running(getApp(configIgnoreAdditionalNames)) {
          when(mockAuditConnector.sendEvent(any())(any(), any())).thenReturn(Future.successful(AuditResult.Success))

          val payload     =
            Payload(reference, "Chris", None, "Jones--Smith", altDateOfBirth, BirthRegisterCountry.ENGLAND)
          val resultMatch =
            testMatchingService.performMatch(payload, List(validRecordSpecialCharactersLastName), MatchingType.FULL)
          resultMatch.matched shouldBe true
        }
      }

      s"($name) match when firstName contains space" in {
        running(getApp(configIgnoreAdditionalNames)) {
          when(mockAuditConnector.sendEvent(any())(any(), any())).thenReturn(Future.successful(AuditResult.Success))

          val payload     =
            Payload(reference, "Chris James", None, "Jones", altDateOfBirth, BirthRegisterCountry.ENGLAND)
          val resultMatch =
            testMatchingService.performMatch(payload, List(validRecordFirstNameSpace), MatchingType.FULL)
          resultMatch.matched shouldBe true
        }
      }

      s"($name) match when lastName contains space" in {
        running(getApp(configIgnoreAdditionalNames)) {
          when(mockAuditConnector.sendEvent(any())(any(), any())).thenReturn(Future.successful(AuditResult.Success))

          val payload     =
            Payload(reference, "Chris", None, "Jones Smith", altDateOfBirth, BirthRegisterCountry.ENGLAND)
          val resultMatch = testMatchingService.performMatch(payload, List(validRecordLastNameSpace), MatchingType.FULL)
          resultMatch.matched shouldBe true
        }
      }

      s"($name) match when lastName from record contains multiple spaces between names" in {
        running(getApp(configIgnoreAdditionalNames)) {
          when(mockAuditConnector.sendEvent(any())(any(), any())).thenReturn(Future.successful(AuditResult.Success))

          val payload     =
            Payload(reference, "Chris", None, "Jones  Smith", altDateOfBirth, BirthRegisterCountry.ENGLAND)
          val resultMatch = testMatchingService.performMatch(payload, List(validRecordLastNameSpace), MatchingType.FULL)
          resultMatch.matched shouldBe true
        }
      }

      s"($name) match when lastName from payload contains multiple spaces between names and includes space at beginning and end of string" in {
        running(getApp(configIgnoreAdditionalNames)) {
          when(mockAuditConnector.sendEvent(any())(any(), any())).thenReturn(Future.successful(AuditResult.Success))

          val payload     = Payload(
            reference,
            "Chris",
            None,
            "  Jones  Smith  ",
            altDateOfBirth,
            BirthRegisterCountry.ENGLAND
          )
          val resultMatch = testMatchingService.performMatch(payload, List(validRecordLastNameSpace), MatchingType.FULL)
          resultMatch.matched shouldBe true
        }
      }

      s"($name) match when lastName from payload contains multiple spaces between names" in {
        running(getApp(configIgnoreAdditionalNames)) {
          when(mockAuditConnector.sendEvent(any())(any(), any())).thenReturn(Future.successful(AuditResult.Success))

          val payload     =
            Payload(reference, "Chris", None, "Jones Smith", altDateOfBirth, BirthRegisterCountry.ENGLAND)
          val resultMatch =
            testMatchingService.performMatch(payload, List(validRecordLastNameMultipleSpace), MatchingType.FULL)
          resultMatch.matched shouldBe true
        }
      }

      s"($name) match when lastName from record contains multiple spaces between names and includes space at beginning and end of string" in {
        running(getApp(configIgnoreAdditionalNames)) {
          when(mockAuditConnector.sendEvent(any())(any(), any())).thenReturn(Future.successful(AuditResult.Success))

          val payload     =
            Payload(reference, "Chris", None, "Jones Smith", altDateOfBirth, BirthRegisterCountry.ENGLAND)
          val resultMatch = testMatchingService.performMatch(
            payload,
            List(validRecordLastNameMultipleSpaceBeginningTrailing),
            MatchingType.FULL
          )
          resultMatch.matched shouldBe true
        }
      }

      s"($name) match when firstName contains UTF-8 characters" in {
        running(getApp(configIgnoreAdditionalNames)) {
          when(mockAuditConnector.sendEvent(any())(any(), any())).thenReturn(Future.successful(AuditResult.Success))

          val payload     =
            Payload(reference, "Chrîs", None, "Jones", altDateOfBirth, BirthRegisterCountry.ENGLAND)
          val resultMatch = testMatchingService.performMatch(payload, List(validRecordUTF8FirstName), MatchingType.FULL)
          resultMatch.matched shouldBe true
        }
      }

      s"($name) match when lastName contains UTF-8 characters" in {
        running(getApp(configIgnoreAdditionalNames)) {
          when(mockAuditConnector.sendEvent(any())(any(), any())).thenReturn(Future.successful(AuditResult.Success))

          val payload     =
            Payload(reference, "Chris", None, "Jonéş", altDateOfBirth, BirthRegisterCountry.ENGLAND)
          val resultMatch = testMatchingService.performMatch(payload, List(validRecordUTF8LastName), MatchingType.FULL)
          resultMatch.matched shouldBe true
        }
      }

      s"($name) match for exact match on firstName and lastName and dateOfBirth on both input and record" in {
        running(getApp(configIgnoreAdditionalNames)) {
          when(mockAuditConnector.sendEvent(any())(any(), any())).thenReturn(Future.successful(AuditResult.Success))

          val payload     =
            Payload(reference, "Chris", None, "Jones", altDateOfBirth, BirthRegisterCountry.ENGLAND)
          val resultMatch = testMatchingService.performMatch(payload, List(validRecord), MatchingType.FULL)
          resultMatch.matched shouldBe true
        }
      }

      s"($name) match when case is different for firstName, lastName on input" in {
        running(getApp(configIgnoreAdditionalNames)) {
          when(mockAuditConnector.sendEvent(any())(any(), any())).thenReturn(Future.successful(AuditResult.Success))

          val payload     =
            Payload(reference, "chRis", None, "joNes", altDateOfBirth, BirthRegisterCountry.ENGLAND)
          val resultMatch = testMatchingService.performMatch(payload, List(validRecord), MatchingType.FULL)
          resultMatch.matched shouldBe true
        }
      }

      s"($name) match when case is different for firstName, lastName on record" in {
        running(getApp(configIgnoreAdditionalNames)) {
          when(mockAuditConnector.sendEvent(any())(any(), any())).thenReturn(Future.successful(AuditResult.Success))

          val payload     =
            Payload(reference, "Chris", None, "Jones", altDateOfBirth, BirthRegisterCountry.ENGLAND)
          val resultMatch = testMatchingService.performMatch(payload, List(wrongCaseValidRecord), MatchingType.FULL)
          resultMatch.matched shouldBe true
        }
      }

      s"($name) match when case is uppercase for firstName, lastName on input" in {
        running(getApp(configIgnoreAdditionalNames)) {
          when(mockAuditConnector.sendEvent(any())(any(), any())).thenReturn(Future.successful(AuditResult.Success))

          val payload     =
            Payload(reference, "CHRIS", None, "JONES", altDateOfBirth, BirthRegisterCountry.ENGLAND)
          val resultMatch = testMatchingService.performMatch(payload, List(validRecord), MatchingType.FULL)
          resultMatch.matched shouldBe true
        }
      }

      s"($name) match when case is uppercase for firstName, lastName on record" in {
        running(getApp(configIgnoreAdditionalNames)) {
          when(mockAuditConnector.sendEvent(any())(any(), any())).thenReturn(Future.successful(AuditResult.Success))

          val payload     =
            Payload(reference, "CHRIS", None, "JONES", altDateOfBirth, BirthRegisterCountry.ENGLAND)
          val resultMatch = testMatchingService.performMatch(payload, List(validRecordUppercase), MatchingType.FULL)
          resultMatch.matched shouldBe true
        }
      }

      s"($name) match when case is different for firstName on input" in {
        running(getApp(configIgnoreAdditionalNames)) {
          when(mockAuditConnector.sendEvent(any())(any(), any())).thenReturn(Future.successful(AuditResult.Success))

          val payload     =
            Payload(reference, "chRis", None, "Jones", altDateOfBirth, BirthRegisterCountry.ENGLAND)
          val resultMatch = testMatchingService.performMatch(payload, List(validRecord), MatchingType.FULL)
          resultMatch.matched shouldBe true
        }
      }

      s"($name) match when case is different for firstName on record" in {
        running(getApp(configIgnoreAdditionalNames)) {
          when(mockAuditConnector.sendEvent(any())(any(), any())).thenReturn(Future.successful(AuditResult.Success))

          val payload     =
            Payload(reference, "Chris", None, "Jones", altDateOfBirth, BirthRegisterCountry.ENGLAND)
          val resultMatch =
            testMatchingService.performMatch(payload, List(wrongCaseFirstNameValidRecord), MatchingType.FULL)
          resultMatch.matched shouldBe true
        }
      }

      s"($name) match when case is different for lastName on input" in {
        running(getApp(configIgnoreAdditionalNames)) {
          when(mockAuditConnector.sendEvent(any())(any(), any())).thenReturn(Future.successful(AuditResult.Success))

          val payload     =
            Payload(reference, "Chris", None, "joNES", altDateOfBirth, BirthRegisterCountry.ENGLAND)
          val resultMatch = testMatchingService.performMatch(payload, List(validRecord), MatchingType.FULL)
          resultMatch.matched shouldBe true
        }
      }

      s"($name) match when case is different for lastName on record" in {
        running(getApp(configIgnoreAdditionalNames)) {
          when(mockAuditConnector.sendEvent(any())(any(), any())).thenReturn(Future.successful(AuditResult.Success))

          val payload     =
            Payload(reference, "Chris", None, "Jones", altDateOfBirth, BirthRegisterCountry.ENGLAND)
          val resultMatch =
            testMatchingService.performMatch(payload, List(wrongCaseLastNameValidRecord), MatchingType.FULL)
          resultMatch.matched shouldBe true
        }
      }

      s"($name) not match when firstName and lastName are different on the input" in {
        running(getApp(configIgnoreAdditionalNames)) {
          when(mockAuditConnector.sendEvent(any())(any(), any())).thenReturn(Future.successful(AuditResult.Success))

          val payload     =
            Payload(reference, "Christopher", None, "Jones", altDateOfBirth, BirthRegisterCountry.ENGLAND)
          val resultMatch = testMatchingService.performMatch(payload, List(validRecord), MatchingType.FULL)
          resultMatch.matched shouldBe false
        }
      }

      s"($name) not match when firstName and lastName are different on the record" in {
        running(getApp(configIgnoreAdditionalNames)) {
          when(mockAuditConnector.sendEvent(any())(any(), any())).thenReturn(Future.successful(AuditResult.Success))

          val payload     =
            Payload(reference, "Chris", None, "Jones", altDateOfBirth, BirthRegisterCountry.ENGLAND)
          val resultMatch = testMatchingService.performMatch(payload, List(invalidRecord), MatchingType.FULL)
          resultMatch.matched shouldBe false
        }
      }

      s"($name) not match when firstName is different on input" in {
        running(getApp(configIgnoreAdditionalNames)) {
          when(mockAuditConnector.sendEvent(any())(any(), any())).thenReturn(Future.successful(AuditResult.Success))

          val payload     =
            Payload(reference, "Christopher", None, "Jones", altDateOfBirth, BirthRegisterCountry.ENGLAND)
          val resultMatch = testMatchingService.performMatch(payload, List(validRecord), MatchingType.FULL)
          resultMatch.matched shouldBe false
        }
      }

      s"($name) not match when firstName is different on record" in {
        running(getApp(configIgnoreAdditionalNames)) {
          when(mockAuditConnector.sendEvent(any())(any(), any())).thenReturn(Future.successful(AuditResult.Success))

          val payload     =
            Payload(reference, "Christopher", None, "Jones", altDateOfBirth, BirthRegisterCountry.ENGLAND)
          val resultMatch =
            testMatchingService.performMatch(payload, List(firstNameNotMatchedRecord), MatchingType.FULL)
          resultMatch.matched shouldBe false
        }
      }

      s"($name) not match when lastName is different on input" in {
        running(getApp(configIgnoreAdditionalNames)) {
          when(mockAuditConnector.sendEvent(any())(any(), any())).thenReturn(Future.successful(AuditResult.Success))

          val payload     =
            Payload(reference, "Chris", None, "Jone", altDateOfBirth, BirthRegisterCountry.ENGLAND)
          val resultMatch = testMatchingService.performMatch(payload, List(validRecord), MatchingType.FULL)
          resultMatch.matched shouldBe false
        }
      }

      s"($name) not match when lastName is different on record" in {
        running(getApp(configIgnoreAdditionalNames)) {
          when(mockAuditConnector.sendEvent(any())(any(), any())).thenReturn(Future.successful(AuditResult.Success))

          val payload     =
            Payload(reference, "Chris", None, "Jones", altDateOfBirth, BirthRegisterCountry.ENGLAND)
          val resultMatch = testMatchingService.performMatch(payload, List(lastNameNotMatchRecord), MatchingType.FULL)
          resultMatch.matched shouldBe false
        }
      }

      s"($name) not match when dateOfBirth is different on input" in {
        running(getApp(configIgnoreAdditionalNames)) {
          when(mockAuditConnector.sendEvent(any())(any(), any())).thenReturn(Future.successful(AuditResult.Success))

          val payload     =
            Payload(reference, "Chris", None, "Jones", LocalDate.of(2012, 2, 15), BirthRegisterCountry.ENGLAND)
          val resultMatch = testMatchingService.performMatch(payload, List(validRecord), MatchingType.FULL)
          resultMatch.matched shouldBe false
        }
      }

      s"($name) not match when dateOfBirth is different on record" in {
        running(getApp(configIgnoreAdditionalNames)) {
          when(mockAuditConnector.sendEvent(any())(any(), any())).thenReturn(Future.successful(AuditResult.Success))

          val payload     =
            Payload(reference, "Chris", None, "Jones", LocalDate.of(2012, 2, 15), BirthRegisterCountry.ENGLAND)
          val resultMatch = testMatchingService.performMatch(payload, List(dobNotMatchRecord), MatchingType.FULL)
          resultMatch.matched shouldBe false
        }
      }
    }
  }

}
