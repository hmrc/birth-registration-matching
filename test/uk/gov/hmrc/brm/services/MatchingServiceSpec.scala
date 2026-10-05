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

import org.mockito.ArgumentMatchers.*
import org.mockito.Mockito.*
import org.scalatest.*
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers.*
import uk.gov.hmrc.brm.models.brm.Payload
import uk.gov.hmrc.brm.services.matching.*
import uk.gov.hmrc.brm.utils.FlagsHelper.*
import uk.gov.hmrc.brm.utils.Mocks.*
import uk.gov.hmrc.brm.utils.TestHelper.*
import uk.gov.hmrc.brm.utils.{BirthRegisterCountry, MatchingType}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditResult

import java.time.LocalDate
import scala.concurrent.Future

class MatchingServiceSpec
    extends AnyWordSpecLike with Matchers with OptionValues with MockitoSugar with GuiceOneAppPerTest {

  given hc: HeaderCarrier             = HeaderCarrier()
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
    "microservice.services.birth-registration-matching.matching.ignoreAdditionalNames"                   -> false,
    "microservice.services.birth-registration-matching.features.flags.process"                           -> true,
    "microservice.services.birth-registration-matching.features.gro.flags.potentiallyFictitious.process" -> true,
    "microservice.services.birth-registration-matching.features.gro.flags.blocked.process"               -> true,
    "microservice.services.birth-registration-matching.features.gro.flags.correction.process"            -> true,
    "microservice.services.birth-registration-matching.features.gro.flags.cancelled.process"             -> true,
    "microservice.services.birth-registration-matching.features.gro.flags.marginalNote.process"          -> true,
    "microservice.services.birth-registration-matching.features.gro.flags.reregistration.process"        -> true
  )

  def switchDisabled: Map[String, _] = Map(
    "microservice.services.birth-registration-matching.matching.ignoreAdditionalNames" -> false,
    "microservice.services.birth-registration-matching.features.flags.process"         -> false
  )

  override def newAppForTest(testData: TestData): Application = {
    val config = if (testData.tags.contains("disabled")) {
      switchDisabled
    } else {
      switchEnabled
    }
    new GuiceApplicationBuilder()
      .configure(config)
      .build()
  }

  def getApp(config: Map[String, _]): Application = GuiceApplicationBuilder(
    disabled = Seq(classOf[com.codahale.metrics.MetricRegistry])
  )
    .configure(config)
    .build()

  private val marginalNoteInvalidFlagValues = List("Other", "Re-registered", "Court order in place")
  private val marginalNoteValidFlagValues   = List("Court order revoked", "None")

//  val full: FullMatching = new FullMatching(mockConfig)

  private def testMatchingService: MatchingService = app.injector.instanceOf[MatchingService]

  references.foreach { reference =>
    val name = reference match {
      case Some(_) => "with reference"
      case None    => "without reference"
    }

    val altDateOfBirth: LocalDate = LocalDate.of(2012, 2, 16)

    "MatchingService.performMatch" when {

      "record contains a fictitious birth" should {
        s"($name) not match when processFlags is true" taggedAs Tag("enabled") in {
          when(mockAuditConnector.sendEvent(any())(any(), any())).thenReturn(Future.successful(AuditResult.Success))
          val payload =
            Payload(reference, "Chris", None, "Jones", altDateOfBirth, BirthRegisterCountry.ENGLAND)

          val service     = testMatchingService
          val resultMatch = service.performMatch(payload, List(flaggedFictitiousBirth), MatchingType.FULL)
          resultMatch.matched                shouldBe false
          resultMatch.firstNamesMatched      shouldBe Good()
          resultMatch.additionalNamesMatched shouldBe Good()
          resultMatch.lastNameMatched        shouldBe Good()
          resultMatch.dateOfBirthMatched     shouldBe Good()
        }

        s"($name) match when processFlags is false" taggedAs Tag("disabled") in {
          when(mockAuditConnector.sendEvent(any())(any(), any())).thenReturn(Future.successful(AuditResult.Success))
          val payload =
            Payload(reference, "Chris", None, "Jones", altDateOfBirth, BirthRegisterCountry.ENGLAND)

          val service     = testMatchingService
          val resultMatch = service.performMatch(payload, List(flaggedFictitiousBirth), MatchingType.FULL)
          resultMatch.matched                shouldBe false
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
          resultMatch.matched                shouldBe false
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
          resultMatch.matched                shouldBe false
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
            resultMatch.matched                shouldBe false
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
          resultMatch.matched                shouldBe false
          resultMatch.firstNamesMatched      shouldBe Good()
          resultMatch.additionalNamesMatched shouldBe Good()
          resultMatch.lastNameMatched        shouldBe Good()
          resultMatch.dateOfBirthMatched     shouldBe Good()
        }

      }

    }

    "MatchingService" should {

      s"($name) match when firstName contains special characters" in
        running(getApp(configIgnoreAdditionalNames)) {
          when(mockAuditConnector.sendEvent(any())(any(), any())).thenReturn(Future.successful(AuditResult.Success))

          val payload     =
            Payload(reference, "Chris-Jame's", None, "Jones", altDateOfBirth, BirthRegisterCountry.ENGLAND)
          val resultMatch =
            testMatchingService.performMatch(payload, List(validRecordSpecialCharactersFirstName), MatchingType.FULL)
          resultMatch.matched shouldBe true
        }

      s"($name) match when lastName contains special characters" in
        running(getApp(configIgnoreAdditionalNames)) {
          when(mockAuditConnector.sendEvent(any())(any(), any())).thenReturn(Future.successful(AuditResult.Success))

          val payload     =
            Payload(reference, "Chris", None, "Jones--Smith", altDateOfBirth, BirthRegisterCountry.ENGLAND)
          val resultMatch =
            testMatchingService.performMatch(payload, List(validRecordSpecialCharactersLastName), MatchingType.FULL)
          resultMatch.matched shouldBe true
        }

      s"($name) match when firstName contains space" in
        running(getApp(configIgnoreAdditionalNames)) {
          when(mockAuditConnector.sendEvent(any())(any(), any())).thenReturn(Future.successful(AuditResult.Success))

          val payload     =
            Payload(reference, "Chris James", None, "Jones", altDateOfBirth, BirthRegisterCountry.ENGLAND)
          val resultMatch =
            testMatchingService.performMatch(payload, List(validRecordFirstNameSpace), MatchingType.FULL)
          resultMatch.matched shouldBe true
        }

      s"($name) match when lastName contains space" in
        running(getApp(configIgnoreAdditionalNames)) {
          when(mockAuditConnector.sendEvent(any())(any(), any())).thenReturn(Future.successful(AuditResult.Success))

          val payload     =
            Payload(reference, "Chris", None, "Jones Smith", altDateOfBirth, BirthRegisterCountry.ENGLAND)
          val resultMatch = testMatchingService.performMatch(payload, List(validRecordLastNameSpace), MatchingType.FULL)
          resultMatch.matched shouldBe true
        }

      s"($name) match when lastName from record contains multiple spaces between names" in
        running(getApp(configIgnoreAdditionalNames)) {
          when(mockAuditConnector.sendEvent(any())(any(), any())).thenReturn(Future.successful(AuditResult.Success))

          val payload     =
            Payload(reference, "Chris", None, "Jones  Smith", altDateOfBirth, BirthRegisterCountry.ENGLAND)
          val resultMatch = testMatchingService.performMatch(payload, List(validRecordLastNameSpace), MatchingType.FULL)
          resultMatch.matched shouldBe true
        }

      s"($name) match when lastName from payload contains multiple spaces between names and includes space at beginning and end of string" in
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

      s"($name) match when lastName from payload contains multiple spaces between names" in
        running(getApp(configIgnoreAdditionalNames)) {
          when(mockAuditConnector.sendEvent(any())(any(), any())).thenReturn(Future.successful(AuditResult.Success))

          val payload     =
            Payload(reference, "Chris", None, "Jones Smith", altDateOfBirth, BirthRegisterCountry.ENGLAND)
          val resultMatch =
            testMatchingService.performMatch(payload, List(validRecordLastNameMultipleSpace), MatchingType.FULL)
          resultMatch.matched shouldBe true
        }

      s"($name) match when lastName from record contains multiple spaces between names and includes space at beginning and end of string" in
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

      s"($name) match when firstName contains UTF-8 characters" in
        running(getApp(configIgnoreAdditionalNames)) {
          when(mockAuditConnector.sendEvent(any())(any(), any())).thenReturn(Future.successful(AuditResult.Success))

          val payload     =
            Payload(reference, "Chrîs", None, "Jones", altDateOfBirth, BirthRegisterCountry.ENGLAND)
          val resultMatch = testMatchingService.performMatch(payload, List(validRecordUTF8FirstName), MatchingType.FULL)
          resultMatch.matched shouldBe true
        }

      s"($name) match when lastName contains UTF-8 characters" in
        running(getApp(configIgnoreAdditionalNames)) {
          when(mockAuditConnector.sendEvent(any())(any(), any())).thenReturn(Future.successful(AuditResult.Success))

          val payload     =
            Payload(reference, "Chris", None, "Jonéş", altDateOfBirth, BirthRegisterCountry.ENGLAND)
          val resultMatch = testMatchingService.performMatch(payload, List(validRecordUTF8LastName), MatchingType.FULL)
          resultMatch.matched shouldBe true
        }

      s"($name) match for exact match on firstName and lastName and dateOfBirth on both input and record" in
        running(getApp(configIgnoreAdditionalNames)) {
          when(mockAuditConnector.sendEvent(any())(any(), any())).thenReturn(Future.successful(AuditResult.Success))

          val payload     =
            Payload(reference, "Chris", None, "Jones", altDateOfBirth, BirthRegisterCountry.ENGLAND)
          val resultMatch = testMatchingService.performMatch(payload, List(validRecord), MatchingType.FULL)
          resultMatch.matched shouldBe true
        }

      s"($name) match when case is different for firstName, lastName on input" in
        running(getApp(configIgnoreAdditionalNames)) {
          when(mockAuditConnector.sendEvent(any())(any(), any())).thenReturn(Future.successful(AuditResult.Success))

          val payload     =
            Payload(reference, "chRis", None, "joNes", altDateOfBirth, BirthRegisterCountry.ENGLAND)
          val resultMatch = testMatchingService.performMatch(payload, List(validRecord), MatchingType.FULL)
          resultMatch.matched shouldBe true
        }

      s"($name) match when case is different for firstName, lastName on record" in
        running(getApp(configIgnoreAdditionalNames)) {
          when(mockAuditConnector.sendEvent(any())(any(), any())).thenReturn(Future.successful(AuditResult.Success))

          val payload     =
            Payload(reference, "Chris", None, "Jones", altDateOfBirth, BirthRegisterCountry.ENGLAND)
          val resultMatch = testMatchingService.performMatch(payload, List(wrongCaseValidRecord), MatchingType.FULL)
          resultMatch.matched shouldBe true
        }

      s"($name) match when case is uppercase for firstName, lastName on input" in
        running(getApp(configIgnoreAdditionalNames)) {
          when(mockAuditConnector.sendEvent(any())(any(), any())).thenReturn(Future.successful(AuditResult.Success))

          val payload     =
            Payload(reference, "CHRIS", None, "JONES", altDateOfBirth, BirthRegisterCountry.ENGLAND)
          val resultMatch = testMatchingService.performMatch(payload, List(validRecord), MatchingType.FULL)
          resultMatch.matched shouldBe true
        }

      s"($name) match when case is uppercase for firstName, lastName on record" in
        running(getApp(configIgnoreAdditionalNames)) {
          when(mockAuditConnector.sendEvent(any())(any(), any())).thenReturn(Future.successful(AuditResult.Success))

          val payload     =
            Payload(reference, "CHRIS", None, "JONES", altDateOfBirth, BirthRegisterCountry.ENGLAND)
          val resultMatch = testMatchingService.performMatch(payload, List(validRecordUppercase), MatchingType.FULL)
          resultMatch.matched shouldBe true
        }

      s"($name) match when case is different for firstName on input" in
        running(getApp(configIgnoreAdditionalNames)) {
          when(mockAuditConnector.sendEvent(any())(any(), any())).thenReturn(Future.successful(AuditResult.Success))

          val payload     =
            Payload(reference, "chRis", None, "Jones", altDateOfBirth, BirthRegisterCountry.ENGLAND)
          val resultMatch = testMatchingService.performMatch(payload, List(validRecord), MatchingType.FULL)
          resultMatch.matched shouldBe true
        }

      s"($name) match when case is different for firstName on record" in
        running(getApp(configIgnoreAdditionalNames)) {
          when(mockAuditConnector.sendEvent(any())(any(), any())).thenReturn(Future.successful(AuditResult.Success))

          val payload     =
            Payload(reference, "Chris", None, "Jones", altDateOfBirth, BirthRegisterCountry.ENGLAND)
          val resultMatch =
            testMatchingService.performMatch(payload, List(wrongCaseFirstNameValidRecord), MatchingType.FULL)
          resultMatch.matched shouldBe true
        }

      s"($name) match when case is different for lastName on input" in
        running(getApp(configIgnoreAdditionalNames)) {
          when(mockAuditConnector.sendEvent(any())(any(), any())).thenReturn(Future.successful(AuditResult.Success))

          val payload     =
            Payload(reference, "Chris", None, "joNES", altDateOfBirth, BirthRegisterCountry.ENGLAND)
          val resultMatch = testMatchingService.performMatch(payload, List(validRecord), MatchingType.FULL)
          resultMatch.matched shouldBe true
        }

      s"($name) match when case is different for lastName on record" in
        running(getApp(configIgnoreAdditionalNames)) {
          when(mockAuditConnector.sendEvent(any())(any(), any())).thenReturn(Future.successful(AuditResult.Success))

          val payload     =
            Payload(reference, "Chris", None, "Jones", altDateOfBirth, BirthRegisterCountry.ENGLAND)
          val resultMatch =
            testMatchingService.performMatch(payload, List(wrongCaseLastNameValidRecord), MatchingType.FULL)
          resultMatch.matched shouldBe true
        }

      s"($name) not match when firstName and lastName are different on the input" in
        running(getApp(configIgnoreAdditionalNames)) {
          when(mockAuditConnector.sendEvent(any())(any(), any())).thenReturn(Future.successful(AuditResult.Success))

          val payload     =
            Payload(reference, "Christopher", None, "Jones", altDateOfBirth, BirthRegisterCountry.ENGLAND)
          val resultMatch = testMatchingService.performMatch(payload, List(validRecord), MatchingType.FULL)
          resultMatch.matched shouldBe false
        }

      s"($name) not match when firstName and lastName are different on the record" in
        running(getApp(configIgnoreAdditionalNames)) {
          when(mockAuditConnector.sendEvent(any())(any(), any())).thenReturn(Future.successful(AuditResult.Success))

          val payload     =
            Payload(reference, "Chris", None, "Jones", altDateOfBirth, BirthRegisterCountry.ENGLAND)
          val resultMatch = testMatchingService.performMatch(payload, List(invalidRecord), MatchingType.FULL)
          resultMatch.matched shouldBe false
        }

      s"($name) not match when firstName is different on input" in
        running(getApp(configIgnoreAdditionalNames)) {
          when(mockAuditConnector.sendEvent(any())(any(), any())).thenReturn(Future.successful(AuditResult.Success))

          val payload     =
            Payload(reference, "Christopher", None, "Jones", altDateOfBirth, BirthRegisterCountry.ENGLAND)
          val resultMatch = testMatchingService.performMatch(payload, List(validRecord), MatchingType.FULL)
          resultMatch.matched shouldBe false
        }

      s"($name) not match when firstName is different on record" in
        running(getApp(configIgnoreAdditionalNames)) {
          when(mockAuditConnector.sendEvent(any())(any(), any())).thenReturn(Future.successful(AuditResult.Success))

          val payload     =
            Payload(reference, "Christopher", None, "Jones", altDateOfBirth, BirthRegisterCountry.ENGLAND)
          val resultMatch =
            testMatchingService.performMatch(payload, List(firstNameNotMatchedRecord), MatchingType.FULL)
          resultMatch.matched shouldBe false
        }

      s"($name) not match when lastName is different on input" in
        running(getApp(configIgnoreAdditionalNames)) {
          when(mockAuditConnector.sendEvent(any())(any(), any())).thenReturn(Future.successful(AuditResult.Success))

          val payload     =
            Payload(reference, "Chris", None, "Jone", altDateOfBirth, BirthRegisterCountry.ENGLAND)
          val resultMatch = testMatchingService.performMatch(payload, List(validRecord), MatchingType.FULL)
          resultMatch.matched shouldBe false
        }

      s"($name) not match when lastName is different on record" in
        running(getApp(configIgnoreAdditionalNames)) {
          when(mockAuditConnector.sendEvent(any())(any(), any())).thenReturn(Future.successful(AuditResult.Success))

          val payload     =
            Payload(reference, "Chris", None, "Jones", altDateOfBirth, BirthRegisterCountry.ENGLAND)
          val resultMatch = testMatchingService.performMatch(payload, List(lastNameNotMatchRecord), MatchingType.FULL)
          resultMatch.matched shouldBe false
        }

      s"($name) not match when dateOfBirth is different on input" in
        running(getApp(configIgnoreAdditionalNames)) {
          when(mockAuditConnector.sendEvent(any())(any(), any())).thenReturn(Future.successful(AuditResult.Success))

          val payload     =
            Payload(reference, "Chris", None, "Jones", LocalDate.of(2012, 2, 15), BirthRegisterCountry.ENGLAND)
          val resultMatch = testMatchingService.performMatch(payload, List(validRecord), MatchingType.FULL)
          resultMatch.matched shouldBe false
        }

      s"($name) not match when dateOfBirth is different on record" in
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
