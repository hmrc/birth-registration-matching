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

package uk.gov.hmrc.brm.utils

import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.OneAppPerSuite
import play.api.libs.json.{JsValue, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.play.audit.http.connector.AuditResult
import uk.gov.hmrc.play.http.HttpResponse
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.Future

class HeaderValidatorSpec extends UnitSpec with OneAppPerSuite with MockitoSugar {

  import uk.gov.hmrc.brm.utils.Mocks._

  val groJsonResponseObject = JsonUtils.getJsonFromFile("gro", "500035710")

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

  def httpResponse(js: JsValue) = HttpResponse.apply(200: Int, Some(js))

  "validateAccept" should {
    "return response code 200 for valid headers" in {
      val request = FakeRequest("POST", "/api/v0/events/birth")
        .withHeaders((ACCEPT, "application/vnd.hmrc.1.0+json"), ("Audit-Source", "DFS"))
        .withBody(payload)

      when(mockConnector.getReference(Matchers.any())(Matchers.any())).thenReturn(Future.successful(httpResponse(groJsonResponseObject)))
      when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))

      val result = await(MockController.post().apply(request))
      status(result) shouldBe OK
    }

    "return response code 406 for invalid content-type in Accept header" in {
      val request = FakeRequest("POST", "/api/v0/events/birth")
        .withHeaders((ACCEPT, "application/vnd.hmrc.1.0+xml"), ("Audit-Source", "DFS"))
        .withBody(payload)

      when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))

      val result = await(MockController.post().apply(request))
      status(result) shouldBe NOT_ACCEPTABLE
      bodyOf(result).toString shouldBe MockErrorResponses.INVALID_CONTENT_TYPE.json
    }

    "return response code 406 for invalid version in Accept header" in {
      val request = FakeRequest("POST", "/api/v0/events/birth")
        .withHeaders((ACCEPT, "application/vnd.hmrc.1+json"), ("Audit-Source", "DFS"))
        .withBody(payload)

      when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))

      val result = await(MockController.post().apply(request))
      status(result) shouldBe NOT_ACCEPTABLE
      bodyOf(result).toString shouldBe MockErrorResponses.INVALID_ACCEPT_HEADER.json
    }

    "return response code 406 for excluded Accept header" in {
      val request = FakeRequest("POST", "/api/v0/events/birth")
        .withHeaders(("Audit-Source", "DFS"))
        .withBody(payload)

      when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))

      val result = await(MockController.post().apply(request))
      status(result) shouldBe NOT_ACCEPTABLE
      bodyOf(result).toString shouldBe MockErrorResponses.INVALID_ACCEPT_HEADER.json
    }

    "return response code 401 for excluded Audit-Source value" in {
      val request = FakeRequest("POST", "/api/v0/events/birth")
        .withHeaders((ACCEPT, "application/vnd.hmrc.1.0+json"), ("Audit-Source", ""))
        .withBody(payload)

      when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))

      val result = await(MockController.post().apply(request))
      status(result) shouldBe UNAUTHORIZED
      bodyOf(result).toString shouldBe MockErrorResponses.INVALID_AUDITSOURCE.json
    }

    "return response code 401 for excluded Audit-Source header" in {
      val request = FakeRequest("POST", "/api/v0/events/birth")
        .withHeaders((ACCEPT, "application/vnd.hmrc.1.0+json"))
        .withBody(payload)

      when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))

      val result = await(MockController.post().apply(request))
      status(result) shouldBe UNAUTHORIZED
      bodyOf(result).toString shouldBe MockErrorResponses.INVALID_AUDITSOURCE.json
    }

    "return response code 406 for excluded Audit-Source and Accept values" in {
      val request = FakeRequest("POST", "/api/v0/events/birth")
        .withHeaders(("Audit-Source", ""), (ACCEPT, ""))
        .withBody(payload)

      when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))

      val result = await(MockController.post().apply(request))
      status(result) shouldBe NOT_ACCEPTABLE
      bodyOf(result).toString shouldBe MockErrorResponses.INVALID_ACCEPT_HEADER.json
    }

  }

}
