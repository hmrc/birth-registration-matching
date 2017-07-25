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

package uk.gov.hmrc.brm.audit

import org.joda.time.LocalDate
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import org.specs2.mock.mockito.ArgumentCapture
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers._
import uk.gov.hmrc.brm.models.brm.Payload
import uk.gov.hmrc.brm.models.matching.MatchingResult
import uk.gov.hmrc.brm.models.response.gro.GROStatus
import uk.gov.hmrc.brm.models.response.nrs.NRSStatus
import uk.gov.hmrc.brm.models.response.{Child, Record}
import uk.gov.hmrc.brm.utils.{BaseUnitSpec, BirthRegisterCountry}
import uk.gov.hmrc.play.audit.http.connector.AuditResult
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.Future

/**
  * Created by adamconder on 15/02/2017.
  */
class TransactionAuditorSpec extends UnitSpec with MockitoSugar with BaseUnitSpec {

  import uk.gov.hmrc.brm.utils.Mocks._

  val auditor = auditorFixtures.transactionAudit

  val ignoreAdditionalNamesEnabled: Map[String, _] = Map(
    "microservice.services.birth-registration-matching.features.logFlags.enabled" -> true,
    "microservice.services.birth-registration-matching.matching.ignoreAdditionalNames" -> true
  )

  val ignoreAdditionalNamesDisabled: Map[String, _] = Map(
    "microservice.services.birth-registration-matching.features.logFlags.enabled" -> true,
    "microservice.services.birth-registration-matching.matching.ignoreAdditionalNames" -> false
  )

  def getApp(config: Map[String, _]) = GuiceApplicationBuilder()
    .configure(config)
    .build()


  implicit val hc = HeaderCarrier()

  "RequestsAndResultsAudit" should {

    "audit request and result when child's reference number used" in {
      running(getApp(ignoreAdditionalNamesDisabled)) {
        val child = Record(Child(
          500035710: Int,
          "John",
          "Smith",
          Some(new LocalDate("2009-06-30"))))
        val localDate = new LocalDate("2017-02-17")
        val payload = Payload(Some("123456789"), "Adam", None, "Test", localDate, BirthRegisterCountry.ENGLAND)

        val argumentCapture = new ArgumentCapture[AuditEvent]
        when(mockAuditConnector.sendEvent(argumentCapture.capture)(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
        val result = await(auditor.transaction(payload, List(child), MatchingResult.noMatch))
        result shouldBe AuditResult.Success

        argumentCapture.value.detail("payload.birthReferenceNumber").contains("123456789")
        argumentCapture.value.detail("payload.firstName") shouldBe "Adam"
        argumentCapture.value.detail("payload.lastName") shouldBe "Test"
        argumentCapture.value.detail("payload.dateOfBirth") shouldBe "2017-02-17"
        argumentCapture.value.detail("payload.whereBirthRegistered") shouldBe "england"
      }
    }

    "audit request and result when child's details used" in {
      running(getApp(ignoreAdditionalNamesDisabled)) {
        val localDate = new LocalDate("2017-02-17")
        val payload = Payload(None, "Adam", None, "Test", localDate, BirthRegisterCountry.ENGLAND)
        val argumentCapture = new ArgumentCapture[AuditEvent]

        when(mockAuditConnector.sendEvent(argumentCapture.capture)(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
        val result = await(auditor.transaction(payload, Nil, MatchingResult.noMatch))
        result shouldBe AuditResult.Success

        argumentCapture.value.detail("payload.birthReferenceNumber") shouldBe "No Birth Reference Number"
        argumentCapture.value.detail("payload.firstName") shouldBe "Adam"
        argumentCapture.value.detail("payload.lastName") shouldBe "Test"
        argumentCapture.value.detail("payload.dateOfBirth") shouldBe "2017-02-17"
        argumentCapture.value.detail("payload.whereBirthRegistered") shouldBe "england"
      }
    }

    "throw Illegal argument exception when no payload is provided" in {
      running(getApp(ignoreAdditionalNamesDisabled)) {
        val event = Map("match" -> "true")
        intercept[IllegalArgumentException] {
          await(auditor.audit(event, None))
        }
      }
    }

  }

  "records audit" should {

    "not return word counts for no records found" in {
      running(getApp(ignoreAdditionalNamesDisabled)) {

        val localDate = new LocalDate("2017-02-17")
        val payload = Payload(Some("123456789"), "Adam", None, "Test", localDate, BirthRegisterCountry.ENGLAND)
        val argumentCapture = new ArgumentCapture[AuditEvent]
        when(mockAuditConnector.sendEvent(argumentCapture.capture)(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
        auditor.transaction(payload, Nil, MatchingResult.noMatch)

        argumentCapture.value.detail.contains("records.record1.numberOfForenames") shouldBe false
        argumentCapture.value.detail.contains("records.record1.numberOfAdditionalNames") shouldBe false
        argumentCapture.value.detail.contains("records.record1.numberOfLastnames") shouldBe false
      }
    }

    "return word count as 0 when a single record is passed with empty name values" in {
      running(getApp(ignoreAdditionalNamesDisabled)) {

        val child = Record(Child(
          500035710: Int,
          "",
          "",
          Some(new LocalDate("2009-06-30"))))
        val localDate = new LocalDate("2017-02-17")
        val payload = Payload(Some("123456789"), "Adam", None, "Test", localDate, BirthRegisterCountry.ENGLAND)

        val argumentCapture = new ArgumentCapture[AuditEvent]
        when(mockAuditConnector.sendEvent(argumentCapture.capture)(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
        auditor.transaction(payload, List(child), MatchingResult.noMatch)

        argumentCapture.value.detail("records.record1.numberOfForenames") shouldBe "0"
        argumentCapture.value.detail("records.record1.numberOfAdditionalNames") shouldBe "0"
        argumentCapture.value.detail("records.record1.numberOfLastnames") shouldBe "0"
      }
    }

    "return correct values for word count when a single record is passed" in {
      running(getApp(ignoreAdditionalNamesDisabled)) {

        val child = Record(Child(
          500035710: Int,
          "Adam TEST",
          "SMITH",
          Some(new LocalDate("2009-06-30"))))
        val localDate = new LocalDate("2009-06-30")
        val payload = Payload(Some("500035710"), "Adam TEST", None, "SMITH", localDate, BirthRegisterCountry.ENGLAND)

        val argumentCapture = new ArgumentCapture[AuditEvent]
        when(mockAuditConnector.sendEvent(argumentCapture.capture)(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
        auditor.transaction(payload, List(child), MatchingResult.noMatch)

        argumentCapture.value.detail("records.record1.numberOfForenames") shouldBe "2"
        argumentCapture.value.detail("records.record1.numberOfAdditionalNames") shouldBe "0"
        argumentCapture.value.detail("records.record1.numberOfLastnames") shouldBe "1"
      }
    }

    "return correct values for word count when a single record is passed having additional names" in {
      running(getApp(ignoreAdditionalNamesDisabled)) {

        val child = Record(Child(
          500035710: Int,
          "Adam TEST",
          "SMITH",
          Some(new LocalDate("2009-06-30"))))
        val localDate = new LocalDate("2009-06-30")
        val payload = Payload(Some("500035710"), "Adam ", Some("test"), "SMITH", localDate, BirthRegisterCountry.ENGLAND)

        val argumentCapture = new ArgumentCapture[AuditEvent]
        when(mockAuditConnector.sendEvent(argumentCapture.capture)(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
        auditor.transaction(payload, List(child), MatchingResult.noMatch)

        argumentCapture.value.detail("records.record1.numberOfForenames") shouldBe "1"
        argumentCapture.value.detail("records.record1.numberOfAdditionalNames") shouldBe "1"
        argumentCapture.value.detail("records.record1.numberOfLastnames") shouldBe "1"
      }
    }

    "return correct values for word count when multiple records are passed" in {
      running(getApp(ignoreAdditionalNamesDisabled)) {

        val child = Record(Child(
          500035710: Int,
          "Adam TEST",
          "SMITH",
          Some(new LocalDate("2009-06-30"))))

        val child2 = Record(Child(
          599935710: Int,
          "Adam",
          "SMITH",
          Some(new LocalDate("2009-06-30"))))
        val localDate = new LocalDate("2017-02-17")
        val payload = Payload(Some("123456789"), "Adam TEST", None, "Test", localDate, BirthRegisterCountry.ENGLAND)

        val argumentCapture = new ArgumentCapture[AuditEvent]
        when(mockAuditConnector.sendEvent(argumentCapture.capture)(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
        auditor.transaction(payload, List(child, child2), MatchingResult.noMatch)

        argumentCapture.value.detail("records.record1.numberOfForenames") shouldBe "2"
        argumentCapture.value.detail("records.record1.numberOfAdditionalNames") shouldBe "0"
        argumentCapture.value.detail("records.record1.numberOfLastnames") shouldBe "1"
        argumentCapture.value.detail("records.record2.numberOfForenames") shouldBe "1"
        argumentCapture.value.detail("records.record2.numberOfLastnames") shouldBe "1"
      }
    }

    "return correct values for word count when multiple records are passed when ignoreAdditionalName is true" in {
      running(getApp(ignoreAdditionalNamesEnabled)) {

        val child = Record(Child(
          500035710: Int,
          "Adam TEST",
          "SMITH",
          Some(new LocalDate("2009-06-30"))))

        val child2 = Record(Child(
          599935710: Int,
          "Adam",
          "SMITH",
          Some(new LocalDate("2009-06-30"))))
        val localDate = new LocalDate("2017-02-17")
        val payload = Payload(Some("123456789"), "Adam TEST", None, "Test", localDate, BirthRegisterCountry.ENGLAND)

        val argumentCapture = new ArgumentCapture[AuditEvent]
        when(mockAuditConnector.sendEvent(argumentCapture.capture)(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
        auditor.transaction(payload, List(child, child2), MatchingResult.noMatch)

        argumentCapture.value.detail("records.record1.numberOfForenames") shouldBe "2"
        argumentCapture.value.detail("records.record1.numberOfAdditionalNames") shouldBe "0"
        argumentCapture.value.detail("records.record1.numberOfLastnames") shouldBe "1"
        argumentCapture.value.detail("records.record2.numberOfForenames") shouldBe "1"
        argumentCapture.value.detail("records.record2.numberOfLastnames") shouldBe "1"
      }
    }
  }

  "records audit for character count " should {

    "not get audited when no record return from upstream service" in {
      running(getApp(ignoreAdditionalNamesDisabled)) {
        val localDate = new LocalDate("2017-02-17")
        val payload = Payload(Some("123456789"), "Adam", None, "Test", localDate, BirthRegisterCountry.ENGLAND)

        val argumentCapture = new ArgumentCapture[AuditEvent]
        when(mockAuditConnector.sendEvent(argumentCapture.capture)(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
        auditor.transaction(payload, Nil, MatchingResult.noMatch)

        argumentCapture.value.detail.contains("records.record1.numberOfCharactersInFirstName") shouldBe false
        argumentCapture.value.detail.contains("records.record1.numberOfCharactersInLastName") shouldBe false
        argumentCapture.value.detail.contains("records.record1.numberOfCharactersInAdditionalName") shouldBe false
      }
    }

    "return correct values when a single record is passed" in {
      running(getApp(ignoreAdditionalNamesDisabled)) {
        val child = Record(Child(
          500035710: Int,
          "Adam TEST",
          "SMITH",
          Some(new LocalDate("2009-06-30"))))
        val localDate = new LocalDate("2017-02-17")
        val payload = Payload(Some("123456789"), "Adam", Some("test"), "Test", localDate, BirthRegisterCountry.ENGLAND)

        val argumentCapture = new ArgumentCapture[AuditEvent]
        when(mockAuditConnector.sendEvent(argumentCapture.capture)(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
        auditor.transaction(payload, List(child), MatchingResult.noMatch)

        argumentCapture.value.detail("records.record1.numberOfCharactersInFirstName") shouldBe "4"
        argumentCapture.value.detail("records.record1.numberOfCharactersInLastName") shouldBe "5"
        argumentCapture.value.detail("records.record1.numberOfCharactersInAdditionalName") shouldBe "4"
      }
    }

    "return correct values when a single record is passed when additional name is not considered." in {
      running(getApp(ignoreAdditionalNamesEnabled)) {
        val child = Record(Child(
          500035710: Int,
          "Adam TEST",
          "SMITH",
          Some(new LocalDate("2009-06-30"))))
        val localDate = new LocalDate("2017-02-17")
        val payload = Payload(Some("123456789"), "Adam", Some("test"), "Test", localDate, BirthRegisterCountry.ENGLAND)

        val argumentCapture = new ArgumentCapture[AuditEvent]
        when(mockAuditConnector.sendEvent(argumentCapture.capture)(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
        auditor.transaction(payload, List(child), MatchingResult.noMatch)

        argumentCapture.value.detail("records.record1.numberOfCharactersInFirstName") shouldBe "4"
        argumentCapture.value.detail("records.record1.numberOfCharactersInLastName") shouldBe "5"
        argumentCapture.value.detail("records.record1.numberOfCharactersInAdditionalName") shouldBe "0"
      }
    }

    "return correct values when a multiple records are passed" in {
      running(getApp(ignoreAdditionalNamesDisabled)) {
        val child1 = Record(Child(
          500035710: Int,
          "Adam TEST",
          "SMITH",
          Some(new LocalDate("2009-06-30"))))
        val child2 = Record(Child(
          599935710: Int,
          "Adam TEST Test",
          "SMITH",
          Some(new LocalDate("2009-08-30"))))

        val localDate = new LocalDate("2017-02-17")
        val payload = Payload(Some("123456789"), "Adam ", Some("TEST"), "Test", localDate, BirthRegisterCountry.ENGLAND)

        val argumentCapture = new ArgumentCapture[AuditEvent]
        when(mockAuditConnector.sendEvent(argumentCapture.capture)(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
        auditor.transaction(payload, List(child1, child2), MatchingResult.noMatch)

        argumentCapture.value.detail("records.record1.numberOfCharactersInFirstName") shouldBe "4"
        argumentCapture.value.detail("records.record1.numberOfCharactersInLastName") shouldBe "5"
        argumentCapture.value.detail("records.record1.numberOfCharactersInAdditionalName") shouldBe "4"
        argumentCapture.value.detail("records.record2.numberOfCharactersInFirstName") shouldBe "4"
        argumentCapture.value.detail("records.record2.numberOfCharactersInLastName") shouldBe "5"
        argumentCapture.value.detail("records.record2.numberOfCharactersInAdditionalName") shouldBe "9"
      }
    }

    "return correct values when a multiple records are passed when additional name is not considered." in {
      running(getApp(ignoreAdditionalNamesEnabled)) {
        val child1 = Record(Child(
          500035710: Int,
          "Adam TEST",
          "SMITH",
          Some(new LocalDate("2009-06-30"))))
        val child2 = Record(Child(
          599935710: Int,
          "Adam TEST Test",
          "SMITH",
          Some(new LocalDate("2009-08-30"))))

        val localDate = new LocalDate("2017-02-17")
        val payload = Payload(Some("123456789"), "Adam ", Some("TEST"), "Test", localDate, BirthRegisterCountry.ENGLAND)

        val argumentCapture = new ArgumentCapture[AuditEvent]
        when(mockAuditConnector.sendEvent(argumentCapture.capture)(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
        auditor.transaction(payload, List(child1, child2), MatchingResult.noMatch)

        argumentCapture.value.detail("records.record1.numberOfCharactersInFirstName") shouldBe "4"
        argumentCapture.value.detail("records.record1.numberOfCharactersInLastName") shouldBe "5"
        argumentCapture.value.detail("records.record1.numberOfCharactersInAdditionalName") shouldBe "0"
        argumentCapture.value.detail("records.record2.numberOfCharactersInFirstName") shouldBe "4"
        argumentCapture.value.detail("records.record2.numberOfCharactersInLastName") shouldBe "5"
        argumentCapture.value.detail("records.record2.numberOfCharactersInAdditionalName") shouldBe "0"
      }
    }

  }

  "record audit for flags" when {

    "record has flags for GRO" should {

      "return a Map() of flags" in {
        running(getApp(ignoreAdditionalNamesDisabled)) {
          val child1 = Record(Child(500035710: Int, "Adam TEST", "SMITH",
            Some(new LocalDate("2009-06-30"))),
            status = Some(
              GROStatus(
                potentiallyFictitiousBirth = true,
                correction = Some("Correction"),
                cancelled = true,
                blockedRegistration = true,
                marginalNote = Some("RCE"),
                reRegistered = Some("Re-registered")
              )
            )
          )
          val localDate = new LocalDate("2017-02-17")
          val payload = Payload(Some("123456789"), "Adam", None, "Test", localDate, BirthRegisterCountry.ENGLAND)


          val argumentCapture = new ArgumentCapture[AuditEvent]
          when(mockAuditConnector.sendEvent(argumentCapture.capture)(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
          auditor.transaction(payload, List(child1), MatchingResult.noMatch)

          argumentCapture.value.detail("records.record1.flags.potentiallyFictitiousBirth") shouldBe "true"
          argumentCapture.value.detail("records.record1.flags.correction") shouldBe "Correction on record"
          argumentCapture.value.detail("records.record1.flags.cancelled") shouldBe "true"
          argumentCapture.value.detail("records.record1.flags.blockedRegistration") shouldBe "true"
          argumentCapture.value.detail("records.record1.flags.marginalNote") shouldBe "Marginal note on record"
          argumentCapture.value.detail("records.record1.flags.reRegistered") shouldBe "Re-registration on record"
        }
      }

      "return a Map() of flags where flag has reason and none" in {
        running(getApp(ignoreAdditionalNamesDisabled)) {
          val child1 = Record(Child(500035710: Int, "Adam TEST", "SMITH",
            Some(new LocalDate("2009-06-30"))),
            status = Some(
              GROStatus(
                potentiallyFictitiousBirth = true,
                correction = Some("Correction None"),
                cancelled = true,
                blockedRegistration = true,
                marginalNote = Some("RCE"),
                reRegistered = Some("Re-registered")
              )
            )
          )
          val localDate = new LocalDate("2017-02-17")
          val payload = Payload(Some("123456789"), "Adam", None, "Test", localDate, BirthRegisterCountry.ENGLAND)


          val argumentCapture = new ArgumentCapture[AuditEvent]
          when(mockAuditConnector.sendEvent(argumentCapture.capture)(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
          auditor.transaction(payload, List(child1), MatchingResult.noMatch)

          argumentCapture.value.detail("records.record1.flags.potentiallyFictitiousBirth") shouldBe "true"
          argumentCapture.value.detail("records.record1.flags.correction") shouldBe "Correction on record"
          argumentCapture.value.detail("records.record1.flags.cancelled") shouldBe "true"
          argumentCapture.value.detail("records.record1.flags.blockedRegistration") shouldBe "true"
          argumentCapture.value.detail("records.record1.flags.marginalNote") shouldBe "Marginal note on record"
          argumentCapture.value.detail("records.record1.flags.reRegistered") shouldBe "Re-registration on record"
        }
      }

      "return a Map() of 'none' flags" in {
        running(getApp(ignoreAdditionalNamesDisabled)) {
          val child1 = Record(Child(500035710: Int, "Adam TEST", "SMITH",
            Some(new LocalDate("2009-06-30"))),
            status = Some(
              GROStatus(
                potentiallyFictitiousBirth = true,
                correction = Some(" None  "),
                cancelled = true,
                blockedRegistration = true,
                marginalNote = Some("  None  "),
                reRegistered = Some(" None.  .none... ")
              )
            )
          )
          val localDate = new LocalDate("2017-02-17")
          val payload = Payload(Some("123456789"), "Adam", None, "Test", localDate, BirthRegisterCountry.ENGLAND)

          val argumentCapture = new ArgumentCapture[AuditEvent]
          when(mockAuditConnector.sendEvent(argumentCapture.capture)(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
          auditor.transaction(payload, List(child1), MatchingResult.noMatch)
          argumentCapture.value.detail("records.record1.flags.potentiallyFictitiousBirth") shouldBe "true"
          argumentCapture.value.detail("records.record1.flags.correction") shouldBe "None"
          argumentCapture.value.detail("records.record1.flags.cancelled") shouldBe "true"
          argumentCapture.value.detail("records.record1.flags.blockedRegistration") shouldBe "true"
          argumentCapture.value.detail("records.record1.flags.marginalNote") shouldBe "None"
          argumentCapture.value.detail("records.record1.flags.reRegistered") shouldBe "Re-registration on record"
        }
      }

    }

    "record has flags for NRS" should {

      "return a Map() of flags" in {
        running(getApp(ignoreAdditionalNamesDisabled)) {
          val child1 = Record(Child(500035710: Int, "Adam TEST", "SMITH",
            Some(new LocalDate("2009-06-30"))),
            status = Some(
              NRSStatus(
                status = 1,
                deathCode = 1
              )
            )
          )
          val localDate = new LocalDate("2017-02-17")
          val payload = Payload(Some("123456789"), "Adam", None, "Test", localDate, BirthRegisterCountry.ENGLAND)

          val argumentCapture = new ArgumentCapture[AuditEvent]
          when(mockAuditConnector.sendEvent(argumentCapture.capture)(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
          auditor.transaction(payload, List(child1), MatchingResult.noMatch)

          argumentCapture.value.detail("records.record1.flags.status") shouldBe "Valid"
          argumentCapture.value.detail("records.record1.flags.deathCode") shouldBe "Potentially deceased"
        }
      }

    }

    "has no status" should {

      "not return status flags" in {
        running(getApp(ignoreAdditionalNamesDisabled)) {
          val child1 = Record(Child(500035710: Int, "Adam TEST", "SMITH",
            Some(new LocalDate("2009-06-30"))),
            status = None
          )
          val localDate = new LocalDate("2017-02-17")
          val payload = Payload(Some("123456789"), "Adam", None, "Test", localDate, BirthRegisterCountry.ENGLAND)

          val argumentCapture = new ArgumentCapture[AuditEvent]
          when(mockAuditConnector.sendEvent(argumentCapture.capture)(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
          auditor.transaction(payload, List(child1), MatchingResult.noMatch)

          argumentCapture.value.detail.contains("records.record1.flags.potentiallyFictitiousBirth") shouldBe false
          argumentCapture.value.detail.contains("records.record1.flags.correction") shouldBe false
          argumentCapture.value.detail.contains("records.record1.flags.cancelled") shouldBe false
          argumentCapture.value.detail.contains("records.record1.flags.blockedRegistration") shouldBe false
          argumentCapture.value.detail.contains("records.record1.flags.marginalNote") shouldBe false
          argumentCapture.value.detail.contains("records.record1.flags.reRegistered") shouldBe false
        }
      }

    }

 }

}
