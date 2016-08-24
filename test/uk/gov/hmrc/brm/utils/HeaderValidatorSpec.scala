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

package uk.gov.hmrc.brm.utils

import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.libs.json.Json
import play.api.test.FakeRequest
import uk.gov.hmrc.brm.connectors.BirthConnector
import uk.gov.hmrc.brm.controllers.BirthEventsController
import uk.gov.hmrc.play.test.UnitSpec
import play.api.test.Helpers._

import scala.concurrent.Future

/**
  * Created by chrisianson on 23/08/16.
  */
class HeaderValidatorSpec extends UnitSpec with MockitoSugar with HeaderValidator {

  val mockConnector = mock[BirthConnector]

  object MockController extends BirthEventsController {
    override val Connector = mockConnector
  }

  val groJsonResponseObject = JsonUtils.getJsonFromFile("500035710")

  val userNoMatchExcludingReferenceKey = Json.parse(
    s"""
       |{
       | "forename" : "Chris",
       | "surname" : "Jones",
       | "dateOfBirth" : "1990-02-16"
       |}
    """.stripMargin)

  "acceptHeaderValidationRules" should {

    "return false when argument values are missing" in {
      acceptHeaderValidationRules() shouldBe false
    }

    "return false when contentType is invalid" in {
      acceptHeaderValidationRules(contentType = Some("text/html"), auditSource = Some("DFS")) shouldBe false
    }

    "return false when version is invalid" in {
      acceptHeaderValidationRules(contentType = Some("application/json"), auditSource = Some("DFS")) shouldBe false
    }

    "return false when auditSource is invalid" in {
      acceptHeaderValidationRules(contentType = Some("application/vnd.hmrc.1.0+json"), auditSource = Some("")) shouldBe false
    }

    "return true when contentType and auditSource is valid and included" in {
      acceptHeaderValidationRules(contentType = Some("application/vnd.hmrc.1.0+json"), auditSource = Some("DFS")) shouldBe true
    }
  }

  "validateAccept" should {
    "return response code 200 for valid headers" in {
      val request = FakeRequest("POST", "/api/v0/events/birth")
        .withHeaders((ACCEPT, "application/vnd.hmrc.1.0+json"),("Audit-Source", "DFS"))
        .withBody(userNoMatchExcludingReferenceKey)
      val result = MockController.post().apply(request)
      status(result) shouldBe OK
    }

    "return response code 400 for invalid Accept headers" in {
      val request = FakeRequest("POST", "/api/v0/events/birth")
        .withHeaders((ACCEPT, "application/vnd.hmrc.1.0+xml"), ("Audit-Source", "DFS"))
        .withBody(userNoMatchExcludingReferenceKey)
      val result = MockController.post().apply(request)
      status(result) shouldBe BAD_REQUEST
    }

  }

}
