/*
 * Copyright 2025 HM Revenue & Customs
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

import uk.gov.hmrc.brm.implicits.AuditFactory
import uk.gov.hmrc.brm.models.brm.Payload
import uk.gov.hmrc.brm.utils.{BaseUnitSpec, BirthRegisterCountry}

import java.time.LocalDate

/** Created by adamconder on 09/02/2017.
  */
class AuditFactorySpec extends BaseUnitSpec {

  val mockEngWalesAudit: EnglandAndWalesAudit = mock[EnglandAndWalesAudit]
  val mockScotAudit: ScotlandAudit            = mock[ScotlandAudit]
  val mockIreAudit: NorthernIrelandAudit      = mock[NorthernIrelandAudit]

  val testAuditor = new AuditFactory(
    mockEngWalesAudit,
    mockScotAudit,
    mockIreAudit
  )

  "AuditFactory" should {

    "return EnglandAndWalesAudit for england birth registered request." in {
      implicit val payload: Payload =
        Payload(Some("123456789"), "Adam", None, "Test", LocalDate.now(), BirthRegisterCountry.ENGLAND)

      testAuditor.getAuditor().isInstanceOf[EnglandAndWalesAudit] shouldBe true
    }

    "return EnglandAndWalesAudit for wales birth registered request." in {
      implicit val payload: Payload =
        Payload(Some("123456789"), "Adam", None, "Test", LocalDate.now(), BirthRegisterCountry.WALES)

      testAuditor.getAuditor().isInstanceOf[EnglandAndWalesAudit] shouldBe true
    }

    "return ScotlandAudit for wales birth registered request." in {
      implicit val payload: Payload =
        Payload(Some("123456789"), "Adam", None, "Test", LocalDate.now(), BirthRegisterCountry.SCOTLAND)

      testAuditor.getAuditor().isInstanceOf[ScotlandAudit] shouldBe true
    }

    "return NorthernIrelandAudit for NORTHERN IRELAND birth registered request." in {
      implicit val payload: Payload =
        Payload(Some("123456789"), "Adam", None, "Test", LocalDate.now(), BirthRegisterCountry.NORTHERN_IRELAND)

      testAuditor.getAuditor().isInstanceOf[NorthernIrelandAudit] shouldBe true
    }

  }

}
