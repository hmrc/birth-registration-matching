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

import uk.gov.hmrc.brm.utils.BaseUnitSpec
import uk.gov.hmrc.play.audit.http.connector.AuditResult

/** Created by adamconder on 09/02/2017.
  */
class WhereBirthRegisteredAuditSpec extends BaseUnitSpec {

  import uk.gov.hmrc.brm.utils.Mocks._

  val auditor = auditorFixtures.whereBirthRegisteredAudit

  "WhereBirthRegisteredAudit" should {

    "audit country when an invalid birth country is used" in {
      mockAuditSuccess
      val result = auditor.audit(Map(), None).futureValue
      result shouldBe AuditResult.Success
    }

    "not audit when datastream is down" in {
      mockAuditFailure
      val result = auditor.audit(Map(), None).futureValue
      result shouldBe a[AuditResult.Failure]
    }

  }

}
