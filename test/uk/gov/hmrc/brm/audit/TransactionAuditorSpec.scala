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
import org.scalatestplus.play.OneAppPerSuite
import org.specs2.mock.mockito.ArgumentCapture
import uk.gov.hmrc.brm.models.brm.Payload
import uk.gov.hmrc.brm.models.matching.ResultMatch
import uk.gov.hmrc.brm.models.response.{Child, Record}
import uk.gov.hmrc.brm.models.response.gro.GROStatus
import uk.gov.hmrc.brm.models.response.nrs.NRSStatus
import uk.gov.hmrc.brm.services.Bad
import uk.gov.hmrc.brm.utils.BirthRegisterCountry
import uk.gov.hmrc.play.audit.http.connector.AuditResult
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.Future

/**
  * Created by adamconder on 15/02/2017.
  */
class TransactionAuditorSpec extends UnitSpec with MockitoSugar with OneAppPerSuite {

  import uk.gov.hmrc.brm.utils.Mocks._

  val auditor = auditorFixtures.transactionAudit

  implicit val hc = HeaderCarrier()

  "RequestsAndResultsAudit" should {

    "audit request and result when child's reference number used" in {
      val localDate = new LocalDate("2017-02-17")
      val payload = Payload(Some("123456789"), "Adam", None, "Test", localDate, BirthRegisterCountry.ENGLAND)
      val argumentCapture = new ArgumentCapture[AuditEvent]
      val event = auditor.transactionToMap(payload, Nil, ResultMatch(Bad(), Bad(), Bad(), Bad()))

      when(mockAuditConnector.sendEvent(argumentCapture.capture)(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
      val result = await(auditor.audit(event, Some(payload)))
      result shouldBe AuditResult.Success

      argumentCapture.value.detail("payload.birthReferenceNumber").contains("123456789")
      argumentCapture.value.detail("payload.firstName") shouldBe "Adam"
      argumentCapture.value.detail("payload.lastName") shouldBe "Test"
      argumentCapture.value.detail("payload.dateOfBirth") shouldBe "2017-02-17"
      argumentCapture.value.detail("payload.whereBirthRegistered") shouldBe "england"
    }

    "audit request and result when child's details used" in {
      val localDate = new LocalDate("2017-02-17")
      val payload = Payload(None, "Adam", None, "Test", localDate, BirthRegisterCountry.ENGLAND)
      val argumentCapture = new ArgumentCapture[AuditEvent]
      val event = auditor.transactionToMap(payload, Nil, ResultMatch(Bad(), Bad(), Bad(), Bad()))

      when(mockAuditConnector.sendEvent(argumentCapture.capture)(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
      val result = await(auditor.audit(event, Some(payload)))
      result shouldBe AuditResult.Success

      argumentCapture.value.detail("payload.birthReferenceNumber") shouldBe "No Birth Reference Number"
      argumentCapture.value.detail("payload.firstName") shouldBe "Adam"
      argumentCapture.value.detail("payload.lastName") shouldBe "Test"
      argumentCapture.value.detail("payload.dateOfBirth") shouldBe "2017-02-17"
      argumentCapture.value.detail("payload.whereBirthRegistered") shouldBe "england"
    }

    "throw Illegal argument exception when no payload is provided" in {
      val event = Map("match" -> "true")
      intercept[IllegalArgumentException] {
        await(auditor.audit(event, None))
      }
    }

  }

  "responseWordCount" should {

    "return empty Map when an empty list is sent" in {
      val response = auditor.recordListToMap(Nil, auditor.wordCount)
      response shouldBe a[Map[_,_]]
      response.isEmpty shouldBe true
    }

    "return correct values when a single record is passed with empty name values" in {
      val child = Record(Child(
        500035710: Int,
        "",
        "",
        Some(new LocalDate("2009-06-30"))))

      val response = auditor.recordListToMap(List(child), auditor.wordCount)

      response shouldBe a[Map[_, _]]
      response("records.record1.numberOfForenames") shouldBe "0"
      response("records.record1.numberOfLastnames") shouldBe "0"
    }

    "return correct values when a single record is passed" in {
      val child = Record(Child(
        500035710: Int,
        "Adam TEST",
        "SMITH",
        Some(new LocalDate("2009-06-30"))))

      val response = auditor.recordListToMap(List(child), auditor.wordCount)

      response("records.record1.numberOfForenames") shouldBe "2"
      response("records.record1.numberOfLastnames") shouldBe "1"
    }

    "return correct values when multiple records are passed" in {
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

      val response = auditor.recordListToMap(List(child, child2), auditor.wordCount)

      response("records.record1.numberOfForenames") shouldBe "2"
      response("records.record1.numberOfLastnames") shouldBe "1"
      response("records.record2.numberOfForenames") shouldBe "1"
      response("records.record2.numberOfLastnames") shouldBe "1"
    }
  }

  "responseCharacterCount" should {

    "return empty Map when an empty list is sent" in {
      val response = auditor.recordListToMap(Nil, auditor.characterCount)
      response shouldBe a[Map[_,_]]
      response.isEmpty shouldBe true
    }

    "return correct values when a single record is passed" in {
      val child = Record(Child(
        500035710: Int,
        "Adam TEST",
        "SMITH",
        Some(new LocalDate("2009-06-30"))))

      val response = auditor.recordListToMap(List(child), auditor.characterCount)
      response shouldBe a[Map[_, _]]
      response("records.record1.numberOfCharactersInFirstName") shouldBe "9"
      response("records.record1.numberOfCharactersInLastName") shouldBe "5"
    }

    "return correct values when a multiple records are passed" in {
      val child1 = Record(Child(
        500035710: Int,
        "Adam TEST",
        "SMITH",
        Some(new LocalDate("2009-06-30"))))
      val child2 = Record(Child(
        599935710: Int,
        "Christopher",
        "Andrews",
        Some(new LocalDate("2009-08-30"))))

      val response = auditor.recordListToMap(List(child1, child2), auditor.characterCount)

      response("records.record1.numberOfCharactersInFirstName") shouldBe "9"
      response("records.record1.numberOfCharactersInLastName") shouldBe "5"
      response("records.record2.numberOfCharactersInFirstName") shouldBe "11"
      response("records.record2.numberOfCharactersInLastName") shouldBe "7"
    }

  }

  "flags" when {

    "record has flags for GRO" should {

      "return a Map() of flags" in {
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
        val response = auditor.recordListToMap(List(child1), auditor.flags)
        response("records.record1.flags.potentiallyFictitiousBirth") shouldBe "true"
        response("records.record1.flags.correction") shouldBe "Correction on record"
        response("records.record1.flags.cancelled") shouldBe "true"
        response("records.record1.flags.blockedRegistration") shouldBe "true"
        response("records.record1.flags.marginalNote") shouldBe "Marginal note on record"
        response("records.record1.flags.reRegistered") shouldBe "Re-registration on record"
      }

      "return a Map() of flags where flag has reason and none" in {
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
        val response = auditor.recordListToMap(List(child1), auditor.flags)
        response("records.record1.flags.potentiallyFictitiousBirth") shouldBe "true"
        response("records.record1.flags.correction") shouldBe "Correction on record"
        response("records.record1.flags.cancelled") shouldBe "true"
        response("records.record1.flags.blockedRegistration") shouldBe "true"
        response("records.record1.flags.marginalNote") shouldBe "Marginal note on record"
        response("records.record1.flags.reRegistered") shouldBe "Re-registration on record"
      }

      "return a Map() of 'none' flags" in {
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
        val response = auditor.recordListToMap(List(child1), auditor.flags)
        response("records.record1.flags.potentiallyFictitiousBirth") shouldBe "true"
        response("records.record1.flags.correction") shouldBe "None"
        response("records.record1.flags.cancelled") shouldBe "true"
        response("records.record1.flags.blockedRegistration") shouldBe "true"
        response("records.record1.flags.marginalNote") shouldBe "None"
        response("records.record1.flags.reRegistered") shouldBe "Re-registration on record"
      }

    }

    "record has flags for NRS" should {

      "return a Map() of flags" in {
        val child1 = Record(Child(500035710: Int, "Adam TEST", "SMITH",
          Some(new LocalDate("2009-06-30"))),
          status = Some(
            NRSStatus(
              status = 1,
              deathCode = 1
            )
          )
        )
        val response = auditor.recordListToMap(List(child1), auditor.flags)
        response("records.record1.flags.status") shouldBe "Valid"
        response("records.record1.flags.deathCode") shouldBe "Potentially deceased"
      }

    }

    "has no status" should {

      "return a empty Map()" in {
        val child1 = Record(Child(500035710: Int, "Adam TEST", "SMITH",
          Some(new LocalDate("2009-06-30"))),
          status = None
        )
        val response = auditor.recordListToMap(List(child1), auditor.flags)
        response shouldBe empty
      }

    }

  }
}
