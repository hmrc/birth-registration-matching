/*
 * Copyright 2016 HM Revenue & Customs
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

import org.mockito.Matchers
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.play.audit.http.connector.{AuditResult, AuditConnector}
import uk.gov.hmrc.play.test.{WithFakeApplication, UnitSpec}
import org.mockito.Mockito._

import scala.concurrent.Future

/**
 * Created by adamconder on 28/09/2016.
 */
class BRMAuditSpec extends UnitSpec with MockitoSugar with WithFakeApplication {

  val mockConnector = mock[AuditConnector]
  object MockBRMAudit extends BRMAudit {
    override val connector = mockConnector
  }

  "BRMAudit" should {

    "initialise with correct dependencies" in {
      val audit = BRMAudit
      audit.connector shouldBe a[AuditConnector]
    }

    "audit event for BRMEvent" in {
      val event = new EnglandAndWalesAuditEvent(
        Map(
          "match" -> "true",
          "firstNameMatch" -> "true",
          "lastNameMatch" -> "true",
          "dateOfBirthMatch" -> "false"
        )
      )


      when(mockConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
      val result = await(MockBRMAudit.event(event))
      result shouldBe a[AuditResult]
    }

  }

}
