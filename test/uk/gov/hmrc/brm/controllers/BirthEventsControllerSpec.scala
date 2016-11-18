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

package uk.gov.hmrc.brm.controllers

import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfter
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.OneAppPerSuite
import play.api.libs.json.{JsValue, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.brm.connectors.{BirthConnector, NirsConnector, NrsConnector}
import uk.gov.hmrc.brm.services.{LookupService, MatchingService}
import uk.gov.hmrc.brm.utils.TestHelper._
import uk.gov.hmrc.brm.utils.{BRMBaseController, JsonUtils}
import uk.gov.hmrc.play.http._
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.Future

class BirthEventsControllerSpec
  extends UnitSpec
  with MockitoSugar
  with OneAppPerSuite
  with BeforeAndAfter {

  val groJsonResponseObject = JsonUtils.getJsonFromFile("500035710")
  val groJsonResponseObjectCollection = JsonUtils.getJsonFromFile("500035710-array")
  val groJsonResponseObjectMultipleWithMatch = JsonUtils.getJsonFromFile("400000004-multiple-match")
  val groJsonResponseObject20120216 = JsonUtils.getJsonFromFile("2012-02-16")
  val groJsonResponseObject20090701 = JsonUtils.getJsonFromFile("2009-07-01")
  val groJsonResponseObject20090630 = JsonUtils.getJsonFromFile("2009-06-30")

  def postRequest(v: JsValue): FakeRequest[JsValue] = FakeRequest("POST", "/api/v0/events/birth")
    .withHeaders((ACCEPT, "application/vnd.hmrc.1.0+json"), ("Audit-Source", "DFS"))
    .withBody(v)

  val mockConnector = mock[BirthConnector]

  object MockLookupService extends LookupService {
    override val groConnector = mockConnector
    override val nirsConnector = NirsConnector
    override val nrsConnector = NrsConnector
    override val matchingService = MatchingService
  }

  object MockController extends BirthEventsController with BRMBaseController {
    override val service = MockLookupService
  }

  def httpResponse(js: JsValue) = HttpResponse.apply(200, Some(js))
  def httpResponse(responseCode: Int) = HttpResponse.apply(responseCode)

  "BirthEventsController" when {

    "initialising" should {

      "wire up dependencies correctly" in {
        BirthEventsController.service shouldBe a[LookupService]
      }

    }

    "POST /birth-registration-matching-proxy/match NOT INCLUDING reference number" should {
      "return JSON response on unsuccessful child detail match" in {

        when(MockController.service.groConnector.getChildDetails(Matchers.any())(Matchers.any())).thenReturn(Future.successful(httpResponse(Json.parse("[]"))))
        val request = postRequest(userNoMatchExcludingReferenceKey)
        val result = MockController.post().apply(request)
        status(result) shouldBe OK
        contentType(result).get shouldBe "application/json"
        header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
      }

      "return JSON response on successful child detail match" in {
        when(MockController.service.groConnector.getChildDetails(Matchers.any())(Matchers.any())).
          thenReturn(Future.successful(httpResponse(groJsonResponseObjectCollection)))
        val request = postRequest(userMatchExcludingReferenceNumberKey)
        val result = MockController.post().apply(request)
        status(result) shouldBe OK
         contentType(result).get shouldBe "application/json"
         header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
      }

      "return JSON response on successful child detail match when multiple records are returned" in {
        when(MockController.service.groConnector.getChildDetails(Matchers.any())(Matchers.any())).thenReturn(Future.successful(httpResponse(groJsonResponseObjectMultipleWithMatch)))
        val request = postRequest(userMultipleMatchExcludingReferenceKey)
        val result = MockController.post().apply(request)
        status(result) shouldBe OK
        contentType(result).get shouldBe "application/json"
        header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
      }

      "return BadGateway when GRO returns upstream BAD_GATEWAY" in {
        when(MockController.service.groConnector.getChildDetails(Matchers.any())(Matchers.any())).thenReturn(Future.failed(new Upstream5xxResponse("", BAD_GATEWAY, BAD_GATEWAY)))
        val request = postRequest(userNoMatchExcludingReferenceKey)
        val result = MockController.post().apply(request)
        status(result) shouldBe BAD_GATEWAY
        contentType(result).get shouldBe "application/json"
        header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
      }

      "return BadRequest when GRO returns upstream 4xx BadRequest" in {
        when(MockController.service.groConnector.getChildDetails(Matchers.any())(Matchers.any())).thenReturn(Future.failed(new Upstream4xxResponse("", BAD_REQUEST, BAD_REQUEST)))
        val request = postRequest(userNoMatchExcludingReferenceKey)
        val result = await(MockController.post().apply(request))
        status(result) shouldBe BAD_REQUEST
        contentType(result).get shouldBe "application/json"
        header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
      }

      "return GatewayTimeout when GRO returns 5xx when GatewayTimeout" in {
        when(MockController.service.groConnector.getChildDetails(Matchers.any())(Matchers.any())).thenReturn(Future.failed(new Upstream5xxResponse("", GATEWAY_TIMEOUT, GATEWAY_TIMEOUT)))
        val request = postRequest(userNoMatchExcludingReferenceKey)
        val result = MockController.post().apply(request)
        status(result) shouldBe GATEWAY_TIMEOUT
        contentType(result).get shouldBe "application/json"
        header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
      }

      "return BadRequest when GRO returns BadRequestException" in {
        when(MockController.service.groConnector.getChildDetails(Matchers.any())(Matchers.any())).thenReturn(Future.failed(new BadRequestException("")))
        val request = postRequest(userNoMatchExcludingReferenceKey)
        val result = MockController.post().apply(request)
        status(result) shouldBe BAD_REQUEST
        contentType(result).get shouldBe "application/json"
        header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
      }

      "return InternalServerError when GRO returns upstream 5xx NOT_IMPLEMENTED" in {
        when(MockController.service.groConnector.getChildDetails(Matchers.any())(Matchers.any())).thenReturn(Future.failed(new Upstream5xxResponse("", NOT_IMPLEMENTED, NOT_IMPLEMENTED)))
        val request = postRequest(userNoMatchExcludingReferenceKey)
        val result = MockController.post().apply(request)
        status(result) shouldBe INTERNAL_SERVER_ERROR
        contentType(result).get shouldBe "application/json"
        header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
      }

      "return InternalServerError when GRO returns upstream InternalServerError" in {
        when(MockController.service.groConnector.getChildDetails(Matchers.any())(Matchers.any())).thenReturn(Future.failed(new Upstream5xxResponse("", INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR)))
        val request = postRequest(userNoMatchExcludingReferenceKey)
        val result = MockController.post().apply(request)
        status(result) shouldBe INTERNAL_SERVER_ERROR
        contentType(result).get shouldBe "application/json"
        header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
      }

      "return 200 false when GRO returns upstream NOT_FOUND" in {
        when(MockController.service.groConnector.getChildDetails(Matchers.any())(Matchers.any())).thenReturn(Future.failed(new Upstream4xxResponse("", NOT_FOUND, NOT_FOUND)))
        val request = postRequest(userNoMatchExcludingReferenceKey)
        val result = MockController.post().apply(request)
        status(result) shouldBe OK
        contentType(result).get shouldBe "application/json"
        header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
      }

      "return 200 false when GRO returns NotFoundException" in {
        when(MockController.service.groConnector.getChildDetails(Matchers.any())(Matchers.any())).thenReturn(Future.failed(new NotFoundException("")))
        val request = postRequest(userNoMatchExcludingReferenceKey)
        val result = MockController.post().apply(request)
        status(result) shouldBe OK
        contentType(result).get shouldBe "application/json"
        header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
      }

      "return InternalServerError when GRO throws Exception" in {
        when(MockController.service.groConnector.getChildDetails(Matchers.any())(Matchers.any())).thenReturn(Future.failed(new Exception("")))
        val request = postRequest(userNoMatchExcludingReferenceKey)
        val result = MockController.post().apply(request)
        status(result) shouldBe INTERNAL_SERVER_ERROR
        contentType(result).get shouldBe "application/json"
        header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
      }

    }

    "POST /birth-registration-matching-proxy/match INCLUDING reference number" should {

      "return 200 JSON response of true on successful reference match with country in mix case" in {
        when(mockConnector.getReference(Matchers.any())(Matchers.any())).thenReturn(Future.successful(httpResponse(groJsonResponseObject)))
        val request = postRequest(userMatchCountryNameInMixCase)
        val result = MockController.post().apply(request)
        contentType(result).get shouldBe "application/json"
        header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
      }

      "return JSON response on successful reference match" in {
        when(MockController.service.groConnector.getReference(Matchers.any())(Matchers.any())).thenReturn(Future.successful(httpResponse(groJsonResponseObject)))
        val request = postRequest(userMatchIncludingReferenceNumber)
        val result = MockController.post().apply(request)
        status(result) shouldBe OK
        contentType(result).get shouldBe "application/json"
        header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
      }

      "return JSON response on unsuccessful birthReferenceNumber match" in {
        when(MockController.service.groConnector.getReference(Matchers.any())(Matchers.any())).thenReturn(Future.successful(httpResponse(noJson)))
        val request = postRequest(userNoMatchIncludingReferenceNumber)
        val result = MockController.post().apply(request)
        status(result) shouldBe OK
        contentType(result).get shouldBe "application/json"
        header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
      }

      "return response code 200 if request contains missing birthReferenceNumber value" in {
        val request = postRequest(userNoMatchExcludingReferenceValue)
        val result = MockController.post().apply(request)
        status(result) shouldBe BAD_REQUEST
        contentType(result).get shouldBe "application/json"
        header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
      }

      "return match false when GRO returns invalid json" in {
        when(MockController.service.groConnector.getReference(Matchers.any())(Matchers.any())).thenReturn(Future.successful(httpResponse(invalidResponse)))
        val request = postRequest(userMatchIncludingReferenceNumber)
        val result = MockController.post().apply(request)
        status(result) shouldBe OK
        contentType(result).get shouldBe "application/json"
        header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
      }

    }

    "POST valid/invalid reference number" should {

      "return response code 200 if request contains birthReferenceNumber with valid characters that aren't numbers" in {
        when(MockController.service.groConnector.getReference(Matchers.any())(Matchers.any())).thenReturn(Future.successful(httpResponse(groJsonResponseObject)))
        val request = postRequest(userNoMatchIncludingReferenceCharacters)
        val result = MockController.post().apply(request)
        status(result) shouldBe OK
        contentType(result).get shouldBe "application/json"
        header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
        (contentAsJson(result) \ "matched").as[Boolean] shouldBe false
      }

      "return response code 400 if request contains missing birthReferenceNumber value" in {
        val request = postRequest(userNoMatchExcludingReferenceValue)
        val result = MockController.post().apply(request)
        status(result) shouldBe BAD_REQUEST
        contentType(result).get shouldBe "application/json"
        header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
      }

      "return response code 400 if request contains birthReferenceNumber with invalid characters" in {
        val request = postRequest(userNoMatchIncludingInvalidData)
        val result = MockController.post().apply(request)
        status(result) shouldBe BAD_REQUEST
        contentType(result).get shouldBe "application/json"
        header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
      }

    }

    "POST firstName" should {

      "return response code 400 if request contains missing firstname key" in {
        val request = postRequest(userNoMatchExcludingFirstNameKey)
        val result = MockController.post().apply(request)
        status(result) shouldBe BAD_REQUEST
        contentType(result).get shouldBe "application/json"
        header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
      }

      "return response code 400 if request contains missing firstname value" in {
        val request = postRequest(userNoMatchExcludingfirstNameValue)
        val result = MockController.post().apply(request)
        status(result) shouldBe BAD_REQUEST
        contentType(result).get shouldBe "application/json"
        header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
      }

    }

    "POST lastName" should {

      "return response code 400 if request contains missing lastName key" in {
        val request = postRequest(userNoMatchExcludinglastNameKey)
        val result = MockController.post().apply(request)
        status(result) shouldBe BAD_REQUEST
        contentType(result).get shouldBe "application/json"
        header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
      }

      "return response code 400 if request contains missing lastName value" in {
        val request = postRequest(userNoMatchExcludinglastNameValue)
        val result = MockController.post().apply(request)
        status(result) shouldBe BAD_REQUEST
        contentType(result).get shouldBe "application/json"
        header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
      }

    }

    "POST invalid dateOfBirth" should {

      "return response code 400 if request contains missing dateOfBirth key" in {
        val request = postRequest(userNoMatchExcludingDateOfBirthKey)
        val result = MockController.post().apply(request)
        status(result) shouldBe BAD_REQUEST
        contentType(result).get shouldBe "application/json"
        header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
      }

      "return response code 400 if request contains missing dateOfBirth value" in {
        val request = postRequest(userNoMatchExcludingDateOfBirthValue)
        val result = MockController.post().apply(request)
        status(result) shouldBe BAD_REQUEST
        contentType(result).get shouldBe "application/json"
        header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
      }

      "return response code 400 if request contains invalid dateOfBirth format" in {
        val request = postRequest(userInvalidDOBFormat)
        val result = MockController.post().apply(request)
        status(result) shouldBe BAD_REQUEST
        contentType(result).get shouldBe "application/json"
        header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
      }

    }

    "POST whereBirthRegistered" should {

      "return 200 false if request contains Northern Ireland" in {
        val request = postRequest(userWhereBirthRegisteredNI)
        val result = MockController.post().apply(request)
        status(result) shouldBe OK
        (contentAsJson(result) \ "matched").as[Boolean] shouldBe false
        contentType(result).get shouldBe "application/json"
        header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
      }

      "return 200 if request contains camel case where birth registered" in {
        when(MockController.service.groConnector.getReference(Matchers.any())(Matchers.any())).thenReturn(Future.successful(httpResponse(groJsonResponseObject)))
        val request = postRequest(userNoMatchIncludingReferenceNumberCamelCase)
        val result = MockController.post().apply(request)
        status(result) shouldBe OK
        (contentAsJson(result) \ "matched").as[Boolean] shouldBe false
        contentType(result).get shouldBe "application/json"
        header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
      }

      "return 200 false if request contains Scotland" in {
        val request = postRequest(userWhereBirthRegisteredScotland)
        val result = MockController.post().apply(request)
        status(result) shouldBe OK
        (contentAsJson(result) \ "matched").as[Boolean] shouldBe false
        contentType(result).get shouldBe "application/json"
        header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
      }

      "return response code 400 if request contains missing whereBirthRegistered key" in {
        val request = postRequest(userNoMatchExcludingWhereBirthRegisteredKey)
        val result = MockController.post().apply(request)
        status(result) shouldBe BAD_REQUEST
        contentType(result).get shouldBe "application/json"
        header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
      }

      "return response code 400 if request contains missing whereBirthRegistered value" in {
        val request = postRequest(userNoMatchExcludingWhereBirthRegisteredValue)
        val result = MockController.post().apply(request)
        status(result) shouldBe BAD_REQUEST
        contentType(result).get shouldBe "application/json"
        header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
      }

      "return response code 400 if request contains invalid whereBirthRegistered value" in {
        val request = postRequest(userInvalidWhereBirthRegistered)
        val result = MockController.post().apply(request)
        status(result) shouldBe BAD_REQUEST
        contentType(result).get shouldBe "application/json"
        header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
      }

    }

    "receiving error response from GRO" should {

      "return BadGateway when GRO returns upstream BAD_GATEWAY" in {
        when(MockController.service.groConnector.getReference(Matchers.any())(Matchers.any())).thenReturn(Future.failed(new Upstream5xxResponse("", BAD_GATEWAY, BAD_GATEWAY)))
        val request = postRequest(userNoMatchIncludingReferenceNumber)
        val result = MockController.post().apply(request)
        status(result) shouldBe BAD_GATEWAY
        contentType(result).get shouldBe "application/json"
        header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
      }

      "return BadRequest when GRO returns upstream 4xx BadRequest" in {
        when(MockController.service.groConnector.getReference(Matchers.any())(Matchers.any())).thenReturn(Future.failed(new Upstream4xxResponse("", BAD_REQUEST, BAD_REQUEST)))
        val request = postRequest(userNoMatchIncludingReferenceNumber)
        val result = await(MockController.post().apply(request))
        status(result) shouldBe BAD_REQUEST
        contentType(result).get shouldBe "application/json"
        header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
      }

      "return GatewayTimeout when GRO returns 5xx when GatewayTimeout" in {
        when(MockController.service.groConnector.getReference(Matchers.any())(Matchers.any())).thenReturn(Future.failed(new Upstream5xxResponse("", GATEWAY_TIMEOUT, GATEWAY_TIMEOUT)))
        val request = postRequest(userNoMatchIncludingReferenceNumber)
        val result = MockController.post().apply(request)
        status(result) shouldBe GATEWAY_TIMEOUT
        contentType(result).get shouldBe "application/json"
        header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
      }

      "return BadRequest when GRO returns BadRequestException" in {
        when(MockController.service.groConnector.getReference(Matchers.any())(Matchers.any())).thenReturn(Future.failed(new BadRequestException("")))
        val request = postRequest(userNoMatchIncludingReferenceNumber)
        val result = MockController.post().apply(request)
        status(result) shouldBe BAD_REQUEST
        contentType(result).get shouldBe "application/json"
        header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
      }

      "return InternalServerError when GRO returns upstream 5xx NOT_IMPLEMENTED" in {
        when(MockController.service.groConnector.getReference(Matchers.any())(Matchers.any())).thenReturn(Future.failed(new Upstream5xxResponse("", NOT_IMPLEMENTED, NOT_IMPLEMENTED)))
        val request = postRequest(userNoMatchIncludingReferenceNumber)
        val result = MockController.post().apply(request)
        status(result) shouldBe INTERNAL_SERVER_ERROR
        contentType(result).get shouldBe "application/json"
        header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
      }

      "return InternalServerError when GRO returns upstream InternalServerError" in {
        when(MockController.service.groConnector.getReference(Matchers.any())(Matchers.any())).thenReturn(Future.failed(new Upstream5xxResponse("", INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR)))
        val request = postRequest(userNoMatchIncludingReferenceNumber)
        val result = MockController.post().apply(request)
        status(result) shouldBe INTERNAL_SERVER_ERROR
        contentType(result).get shouldBe "application/json"
        header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
      }

      "return 200 false when GRO returns upstream NOT_FOUND" in {
        when(MockController.service.groConnector.getReference(Matchers.any())(Matchers.any())).thenReturn(Future.failed(new Upstream4xxResponse("", NOT_FOUND, NOT_FOUND)))
        val request = postRequest(userNoMatchIncludingReferenceNumber)
        val result = MockController.post().apply(request)
        status(result) shouldBe OK
        (contentAsJson(result) \ "matched").as[Boolean] shouldBe false
        contentType(result).get shouldBe "application/json"
        header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
      }

      "return 200 false when GRO returns NotFoundException" in {
        when(MockController.service.groConnector.getReference(Matchers.any())(Matchers.any())).thenReturn(Future.failed(new NotFoundException("")))
        val request = postRequest(userNoMatchIncludingReferenceNumber)
        val result = MockController.post().apply(request)
        status(result) shouldBe OK
        (contentAsJson(result) \ "matched").as[Boolean] shouldBe false
        contentType(result).get shouldBe "application/json"
        header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
      }

      "return InternalServerError when GRO throws Exception" in {
        when(MockController.service.groConnector.getReference(Matchers.any())(Matchers.any())).thenReturn(Future.failed(new Exception("")))
        val request = postRequest(userNoMatchIncludingReferenceNumber)
        val result = MockController.post().apply(request)
        status(result) shouldBe INTERNAL_SERVER_ERROR
        contentType(result).get shouldBe "application/json"
        header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
      }

    }

  }

}
