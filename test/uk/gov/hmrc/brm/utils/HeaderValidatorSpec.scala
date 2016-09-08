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
import play.api.libs.json.{JsValue, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.brm.BRMFakeApplication
import uk.gov.hmrc.brm.connectors.BirthConnector
import uk.gov.hmrc.brm.controllers.BirthEventsController
import uk.gov.hmrc.brm.services.LookupService
import uk.gov.hmrc.play.http.HttpResponse
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.Future

/**
  * Created by chrisianson on 23/08/16.
  */
class HeaderValidatorSpec extends UnitSpec with MockitoSugar with HeaderValidator with BRMFakeApplication {

  private val jsonResponse = """{"code":"145","status":"400","details":"The headers you supplied are invalid","title":"Headers invalid","about":"http://http://htmlpreview.github.io/?https://github.com/hmrc/birth-registration-matching/blob/master/api-documents/api.html"}"""

  val mockConnector = mock[BirthConnector]


  object MockLookupService extends LookupService {
    override val groConnector = mockConnector
    override val nirsConnector = mockConnector
    override val nrsConnector = mockConnector
  }

  object MockController extends BirthEventsController {
    override val service = MockLookupService
  }

  val groJsonResponseObject = JsonUtils.getJsonFromFile("500035710")

  val payload = Json.parse(
    s"""
       |{
       | "firstName" : "Chris",
       | "lastName" : "Jones",
       | "dateOfBirth" : "1990-02-16",
       | "birthReferenceNumber" : "500035710",
       | "whereBirthRegistered": "england"
       |}
    """.stripMargin)

  def httpResponse(js : JsValue) = HttpResponse.apply(200, Some(js))

  "acceptHeaderValidationRules" should {

    "return false when argument values are missing" in {
      acceptHeaderValidationRules() shouldBe false
    }

    "return false when Accept header is invalid" in {
      acceptHeaderValidationRules(accept = Some("text/html"), auditSource = Some("DFS")) shouldBe false
    }

    "return false when version is invalid" in {
      acceptHeaderValidationRules(accept = Some("application/json"), auditSource = Some("DFS")) shouldBe false
    }

    "return false when auditSource is invalid" in {
      acceptHeaderValidationRules(accept = Some("application/vnd.hmrc.1.0+json"), auditSource = Some("")) shouldBe false
    }

    "return true when Accept header and auditSource header are valid" in {
      acceptHeaderValidationRules(accept = Some("application/vnd.hmrc.1.0+json"), auditSource = Some("DFS")) shouldBe true
    }

    "return true when Accept header for mixed case and auditSource header are valid " in {
      acceptHeaderValidationRules(accept = Some("application/vNd.HMRC.1.0+jSon"), auditSource = Some("DFS")) shouldBe true
    }

    "return false when Accept header is not valid and auditSource header is valid " in {
      acceptHeaderValidationRules(accept = Some(""), auditSource = Some("DFS")) shouldBe false
    }

    "return false when Accept header has no value and auditSource header is valid " in {
      acceptHeaderValidationRules(accept = None, auditSource = Some("DFS")) shouldBe false
    }
  }

  "validateAccept" should {
    "return response code 200 for valid headers" in {
      val request = FakeRequest("POST", "/api/v0/events/birth")
        .withHeaders((ACCEPT, "application/vnd.hmrc.1.0+json"),("Audit-Source", "DFS"))
        .withBody(payload)
      when(mockConnector.getReference(Matchers.any())(Matchers.any())).thenReturn(Future.successful(httpResponse(groJsonResponseObject)))
      val result = MockController.post().apply(request)
      status(result) shouldBe OK
    }

    "return response code 400 for invalid content-type in Accept header" in {
      running(fakeApplication) {
        val request = FakeRequest("POST", "/api/v0/events/birth")
          .withHeaders((ACCEPT, "application/vnd.hmrc.1.0+xml"), ("Audit-Source", "DFS"))
          .withBody(payload)
        val result = await(MockController.post().apply(request))
        status(result) shouldBe BAD_REQUEST
        bodyOf(result) shouldBe jsonResponse
      }
    }

    "return response code 400 for invalid version in Accept header" in {
      running(fakeApplication) {
        val request = FakeRequest("POST", "/api/v0/events/birth")
          .withHeaders((ACCEPT, "application/vnd.hmrc.1+json"), ("Audit-Source", "DFS"))
          .withBody(payload)
        val result = await(MockController.post().apply(request))
        status(result) shouldBe BAD_REQUEST
        bodyOf(result) shouldBe jsonResponse
      }
    }

    "return response code 400 for excluded Accept header" in {
      running(fakeApplication) {
        val request = FakeRequest("POST", "/api/v0/events/birth")
          .withHeaders(("Audit-Source", "DFS"))
          .withBody(payload)
        val result = await(MockController.post().apply(request))
        status(result) shouldBe BAD_REQUEST
        bodyOf(result) shouldBe jsonResponse
      }
    }

    "return response code 400 for excluded Audit-Source value" in {
      running(fakeApplication) {
        val request = FakeRequest("POST", "/api/v0/events/birth")
          .withHeaders((ACCEPT, "application/vnd.hmrc.1.0+json"), ("Audit-Source", ""))
          .withBody(payload)
        val result = await(MockController.post().apply(request))
        status(result) shouldBe BAD_REQUEST
        bodyOf(result) shouldBe jsonResponse
      }
    }

    "return response code 400 for excluded Audit-Source header" in {
      running(fakeApplication) {
        val request = FakeRequest("POST", "/api/v0/events/birth")
          .withHeaders((ACCEPT, "application/vnd.hmrc.1.0+json"))
          .withBody(payload)
        val result = await(MockController.post().apply(request))
        status(result) shouldBe BAD_REQUEST
        bodyOf(result) shouldBe jsonResponse
      }
    }

  }

}
