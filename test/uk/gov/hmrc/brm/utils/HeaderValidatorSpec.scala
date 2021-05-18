/*
 * Copyright 2021 HM Revenue & Customs
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

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.json.{JsValue, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.brm.controllers.BirthEventsController
import uk.gov.hmrc.brm.models.matching.BirthMatchResponse
import uk.gov.hmrc.http.HttpResponse
import uk.gov.hmrc.play.audit.http.connector.AuditResult
import org.scalatest.{Matchers, OptionValues, WordSpecLike}
import play.api.Play.materializer
import scala.concurrent.{ExecutionContext, Future}

class HeaderValidatorSpec extends WordSpecLike with Matchers with OptionValues with GuiceOneAppPerSuite with MockitoSugar with ScalaFutures {

  import uk.gov.hmrc.brm.utils.Mocks._

  val groJsonResponseObject: JsValue = JsonUtils.getJsonFromFile("gro", "500035710")

  val testController = new BirthEventsController (
    mockLookupService,
    auditorFixtures.whereBirthRegisteredAudit,
    MockAuditFactory,
    mockConfig,
    auditorFixtures.transactionAudit,
    auditorFixtures.matchingAudit,
    app.injector.instanceOf[HeaderValidator],
    stubControllerComponents(),
    mockCommonUtil,
    mockBrmLogger,
    mockMetricsFactory,
    mockFilters,
    mockEngWalesMetric,
    mockIreMetric,
    mockScotMetric,
    mockInvalidMetric
  )

  val payload: JsValue = Json.parse(
    s"""
       |{
       | "firstName" : "Chris",
       | "lastName" : "Jones",
       | "dateOfBirth" : "1990-02-16",
       | "birthReferenceNumber" : "500035710",
       | "whereBirthRegistered": "england"
       |}
    """.stripMargin)

  def httpResponse(js: JsValue): HttpResponse = HttpResponse(200: Int, js, Map.empty[String, Seq[String]])

  "validateAccept" should {
    "return response code 200 for valid headers" in {
      val request = FakeRequest("POST", "/api/v0/events/birth")
        .withHeaders((ACCEPT, "application/vnd.hmrc.1.0+json"), ("Audit-Source", "DFS"))
        .withBody(payload)

      when(mockConnector.getReference(any())(any(), any[ExecutionContext])).thenReturn(Future.successful(httpResponse(groJsonResponseObject)))
      when(mockAuditConnector.sendEvent(any())(any(), any())).thenReturn(Future.successful(AuditResult.Success))
      when(mockFilters.process(any()))
        .thenReturn(List())
      when(mockMetricsFactory.getMetrics()(any()))
        .thenReturn(mockScotMetric)
      when(mockLookupService.lookup()(any(), any(), any(), any()))
        .thenReturn(Future.successful(BirthMatchResponse(true)))

      val result = testController.post().apply(request).futureValue
      result.header.status shouldBe OK
    }

    "return response code 406 for invalid content-type in Accept header" in {
      val request = FakeRequest("POST", "/api/v0/events/birth")
        .withHeaders((ACCEPT, "application/vnd.hmrc.1.0+xml"), ("Audit-Source", "DFS"))
        .withBody(payload)

      when(mockAuditConnector.sendEvent(any())(any(), any())).thenReturn(Future.successful(AuditResult.Success))

      val result = testController.post().apply(request).futureValue
      result.header.status shouldBe NOT_ACCEPTABLE
      result.body.consumeData.futureValue.utf8String shouldBe MockErrorResponses.INVALID_CONTENT_TYPE.json
    }

    "return response code 406 for invalid version in Accept header" in {
      val request = FakeRequest("POST", "/api/v0/events/birth")
        .withHeaders((ACCEPT, "application/vnd.hmrc.1+json"), ("Audit-Source", "DFS"))
        .withBody(payload)

      when(mockAuditConnector.sendEvent(any())(any(), any())).thenReturn(Future.successful(AuditResult.Success))

      val result = testController.post().apply(request).futureValue
      result.header.status shouldBe NOT_ACCEPTABLE
      result.body.consumeData.futureValue.utf8String shouldBe MockErrorResponses.INVALID_ACCEPT_HEADER.json
    }

    "return response code 406 for excluded Accept header" in {
      val request = FakeRequest("POST", "/api/v0/events/birth")
        .withHeaders(("Audit-Source", "DFS"))
        .withBody(payload)

      when(mockAuditConnector.sendEvent(any())(any(), any())).thenReturn(Future.successful(AuditResult.Success))

      val result = testController.post().apply(request).futureValue
      result.header.status shouldBe NOT_ACCEPTABLE
      result.body.consumeData.futureValue.utf8String shouldBe MockErrorResponses.INVALID_ACCEPT_HEADER.json
    }

    "return response code 401 for excluded Audit-Source value" in {
      val request = FakeRequest("POST", "/api/v0/events/birth")
        .withHeaders((ACCEPT, "application/vnd.hmrc.1.0+json"), ("Audit-Source", ""))
        .withBody(payload)

      when(mockAuditConnector.sendEvent(any())(any(), any())).thenReturn(Future.successful(AuditResult.Success))

      val result = testController.post().apply(request).futureValue
      result.header.status shouldBe UNAUTHORIZED
      result.body.consumeData.futureValue.utf8String shouldBe MockErrorResponses.INVALID_AUDITSOURCE.json
    }

    "return response code 401 for excluded Audit-Source header" in {
      val request = FakeRequest("POST", "/api/v0/events/birth")
        .withHeaders((ACCEPT, "application/vnd.hmrc.1.0+json"))
        .withBody(payload)

      when(mockAuditConnector.sendEvent(any())(any(), any())).thenReturn(Future.successful(AuditResult.Success))

      val result = testController.post().apply(request).futureValue
      result.header.status shouldBe UNAUTHORIZED
      result.body.consumeData.futureValue.utf8String shouldBe MockErrorResponses.INVALID_AUDITSOURCE.json
    }

    "return response code 406 for excluded Audit-Source and Accept values" in {
      val request = FakeRequest("POST", "/api/v0/events/birth")
        .withHeaders(("Audit-Source", ""), (ACCEPT, ""))
        .withBody(payload)

      when(mockAuditConnector.sendEvent(any())(any(), any())).thenReturn(Future.successful(AuditResult.Success))

      val result = testController.post().apply(request).futureValue
      result.header.status shouldBe NOT_ACCEPTABLE
      result.body.consumeData.futureValue.utf8String shouldBe MockErrorResponses.INVALID_ACCEPT_HEADER.json
    }

  }

}
