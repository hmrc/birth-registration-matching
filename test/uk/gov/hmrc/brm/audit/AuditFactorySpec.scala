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
import uk.gov.hmrc.brm.BRMFakeApplication
import uk.gov.hmrc.brm.implicits.Implicits.AuditFactory
import uk.gov.hmrc.brm.models.brm.Payload
import uk.gov.hmrc.brm.utils.BirthRegisterCountry
import uk.gov.hmrc.brm.utils.Mocks._
import uk.gov.hmrc.play.audit.http.connector.{AuditConnector, AuditResult}
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future

/**
  * Created by adamconder on 09/02/2017.
  */
class AuditFactorySpec extends UnitSpec with MockitoSugar with WithFakeApplication {

  implicit val hc = HeaderCarrier()

  "AuditFactory" should {

    "return EnglandAndWalesAudit for england birth registered request." in {
      implicit val payload = Payload(Some("123456789"), "Adam", None, "Test", LocalDate.now(), BirthRegisterCountry.ENGLAND)
      var auditor = (new AuditFactory()).getAuditor()
      auditor.isInstanceOf[EnglandAndWalesAudit]  shouldBe true
    }

    "return EnglandAndWalesAudit for wales birth registered request." in {
      implicit val payload = Payload(Some("123456789"), "Adam", None, "Test", LocalDate.now(), BirthRegisterCountry.WALES)
      var auditor = (new AuditFactory()).getAuditor()
      auditor.isInstanceOf[EnglandAndWalesAudit] shouldBe true
    }

    "return ScotlandAudit for wales birth registered request." in {
      implicit val payload = Payload(Some("123456789"), "Adam", None, "Test", LocalDate.now(), BirthRegisterCountry.SCOTLAND)
      var auditor = (new AuditFactory()).getAuditor()
      auditor.isInstanceOf[ScotlandAudit] shouldBe true
    }

    "return NorthernIrelandAudit for NORTHERN IRELAND birth registered request." in {
      implicit val payload = Payload(Some("123456789"), "Adam", None, "Test", LocalDate.now(), BirthRegisterCountry.NORTHERN_IRELAND)
      var auditor = (new AuditFactory()).getAuditor()
      auditor.isInstanceOf[NorthernIrelandAudit] shouldBe true
    }

  }

}
