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
import uk.gov.hmrc.brm.utils.BirthRegisterCountry
import uk.gov.hmrc.play.audit.http.connector.{AuditConnector, AuditResult}
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.Future

/**
  * Created by adamconder on 15/02/2017.
  */
class RequestsAndResultsAuditSpec extends UnitSpec with MockitoSugar with OneAppPerSuite {

  val connector = mock[AuditConnector]
  val auditor = new RequestsAndResultsAudit(connector)
  implicit val hc = HeaderCarrier()

  "RequestsAndResultsAudit" should {

    // TODO We need to run a FakeApplication() to satisfy BrmConfig.audit()
    // FakeApplication needs to have all feature configs
    // Unittest the BrmAudit.config() in a new spec
    /* unit test this in its own spec:
    //        import uk.gov.hmrc.brm.services.parser.NameParser._
    //        val recordsStats = for ((record, index) <- records.zipWithIndex) yield {
    //          s"records.record$index.numberOfForenames" -> record.child.firstName.names.length
    //          s"records.record$index.numberOfLastnames" -> record.child.lastName.names.length
    //        }
     */
    "audit request and result when child's reference number used" in {
        val localDate = new LocalDate("2017-02-17")
        val payload = Payload(Some("123456789"), "Adam", "Test", localDate, BirthRegisterCountry.ENGLAND)
        val argumentCapture = new ArgumentCapture[AuditEvent]
        val event = Map("match" -> "true")

        when(connector.sendEvent(argumentCapture.capture)(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
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
        val payload = Payload(None, "Adam", "Test", localDate, BirthRegisterCountry.ENGLAND)
        val argumentCapture = new ArgumentCapture[AuditEvent]
        val event = Map("match" -> "true")

        when(connector.sendEvent(argumentCapture.capture)(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
        val result = await(auditor.audit(event, Some(payload)))
        result shouldBe AuditResult.Success
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
}





















