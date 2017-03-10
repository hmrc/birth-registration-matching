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
import org.scalatest.BeforeAndAfter
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.brm.BRMFakeApplication
import uk.gov.hmrc.brm.models.brm.Payload
import uk.gov.hmrc.brm.utils.BirthRegisterCountry
import uk.gov.hmrc.brm.utils.Mocks._
import uk.gov.hmrc.play.audit.http.connector.{AuditConnector, AuditResult}
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.Future

/**
  * Created by adamconder on 09/02/2017.
  */
class EnglandAndWalesAuditSpec extends UnitSpec with MockitoSugar with BRMFakeApplication with BeforeAndAfter {

  val connector = mockAuditConnector
  val auditor = auditorFixtures.englandAndWalesAudit

  implicit val hc = HeaderCarrier()

  before {
    reset(connector)
  }

  "EnglandAndWalesAudit" should {

    "audit requests when using reference number" in {
      val payload = Payload(Some("123456789"), "Adam", "Test", LocalDate.now(), BirthRegisterCountry.ENGLAND)
      val event = Map("match" -> "true")

      when(connector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
      val result = await(auditor.audit(event, Some(payload)))
      result shouldBe AuditResult.Success
    }

    "audit requests when using child's details" in {
      when(connector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
      val payload = Payload(None, "Adam", "Test", LocalDate.now(), BirthRegisterCountry.ENGLAND)
      val event = Map("match" -> "true")

      val result = await(auditor.audit(event, Some(payload)))
      result shouldBe AuditResult.Success
    }

    "throw Illegal argument exception when no payload is provided" in {
      val event = Map("match" -> "true")
      intercept[IllegalArgumentException] {
        await(auditor.audit(event, None))
      }
    }

  }

}
