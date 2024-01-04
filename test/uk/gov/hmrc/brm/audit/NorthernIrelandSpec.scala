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

package uk.gov.hmrc.brm.audit

import org.joda.time.LocalDate
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import uk.gov.hmrc.brm.models.brm.Payload
import uk.gov.hmrc.brm.utils.BirthRegisterCountry
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.{AuditConnector, AuditResult}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatest.OptionValues

import scala.concurrent.Future

/** Created by adamconder on 09/02/2017.
  */
class NorthernIrelandSpec
    extends AnyWordSpecLike
    with Matchers
    with OptionValues
    with MockitoSugar
    with GuiceOneAppPerSuite
    with ScalaFutures {

  import uk.gov.hmrc.brm.utils.Mocks._

  val connector: AuditConnector     = mockAuditConnector
  val auditor: NorthernIrelandAudit = auditorFixtures.northernIrelandAudit

  implicit val hc: HeaderCarrier = HeaderCarrier()

  "NorthernIrelandAudit" should {

    "audit requests when using reference number" in {
      val payload = Payload(Some("123456789"), "Adam", None, "Test", LocalDate.now(), BirthRegisterCountry.ENGLAND)
      val event   = Map("match" -> "true")

      when(connector.sendEvent(any())(any(), any())).thenReturn(Future.successful(AuditResult.Success))
      val result = auditor.audit(event, Some(payload)).futureValue
      result shouldBe AuditResult.Success
    }

    "audit requests when using child's details" in {
      val payload = Payload(None, "Adam", None, "Test", LocalDate.now(), BirthRegisterCountry.ENGLAND)
      val event   = Map("match" -> "true")

      when(connector.sendEvent(any())(any(), any())).thenReturn(Future.successful(AuditResult.Success))

      val result = auditor.audit(event, Some(payload)).futureValue
      result shouldBe AuditResult.Success
    }

    "throw Illegal argument exception when no payload is provided" in {
      val event = Map("match" -> "true")
      assert(auditor.audit(event, None).failed.futureValue.isInstanceOf[IllegalArgumentException])
    }

  }

}
