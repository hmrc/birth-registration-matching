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

package uk.gov.hmrc.brm.controllers

import java.util.concurrent.TimeUnit

import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfter
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.OneAppPerSuite
import play.api.http.Status
import play.api.libs.json.{JsValue, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsJson, _}
import uk.gov.hmrc.brm.audit.BRMAudit
import uk.gov.hmrc.brm.implicits.Implicits.AuditFactory
import uk.gov.hmrc.brm.services.LookupService
import uk.gov.hmrc.brm.utils.Mocks._
import uk.gov.hmrc.play.audit.http.connector.AuditResult
import uk.gov.hmrc.play.http._
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.Future
import scala.concurrent.duration.Duration

class BirthEventsControllerSpec
  extends UnitSpec
    with MockitoSugar
    with OneAppPerSuite
    with BeforeAndAfter {

  import uk.gov.hmrc.brm.utils.TestHelper._

  import scala.concurrent.ExecutionContext.Implicits.global

  def postRequest(v: JsValue): FakeRequest[JsValue] = FakeRequest("POST", "/birth-registration-matching/match")
    .withHeaders((ACCEPT, "application/vnd.hmrc.1.0+json"), ("Audit-Source", "DFS"))
    .withBody(v)

  def httpResponse(responseCode : Int, js : JsValue) = HttpResponse.apply(responseCode, Some(js))
  def httpResponse(js: JsValue) = HttpResponse.apply(OK, Some(js))
  def httpResponse(responseCode: Int) = HttpResponse.apply(responseCode)

  "BirthEventsController" when {

    "initialising" should {

      "wire up dependencies correctly" in {
        BirthEventsController.service shouldBe a[LookupService]
        BirthEventsController.countryAuditor shouldBe a[BRMAudit]
        BirthEventsController.auditFactory shouldBe a[AuditFactory]
        BirthEventsController.transactionAuditor shouldBe a[BRMAudit]
        BirthEventsController.matchingAuditor shouldBe a[BRMAudit]
      }

    }

    "validate birth reference number" should {

      "return response code 400 if request contains missing birthReferenceNumber value" in {
        when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))

        val request = postRequest(userNoMatchExcludingReferenceValue)
        val result = MockController.post().apply(request)
        status(result) shouldBe BAD_REQUEST
        contentType(result).get shouldBe "application/json"
        header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
      }

      "return response code 400 if request contains birthReferenceNumber with invalid characters" in {
        when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))

        val request = postRequest(userNoMatchIncludingInvalidData)
        val result = MockController.post().apply(request)
        status(result) shouldBe BAD_REQUEST
        contentType(result).get shouldBe "application/json"
        header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
      }

    }

    "validate firstName" should {

      "return response code 400 if request contains missing firstname key" in {
        when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))

        val request = postRequest(userNoMatchExcludingFirstNameKey)
        val result = MockController.post().apply(request)
        status(result) shouldBe BAD_REQUEST
        contentType(result).get shouldBe "application/json"
        header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
      }

      "return response code 400 if request contains missing firstname value" in {
        when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))

        val request = postRequest(userNoMatchExcludingfirstNameValue)
        val result = MockController.post().apply(request)
        status(result) shouldBe BAD_REQUEST
        contentType(result).get shouldBe "application/json"
        header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
      }

      "return response code 400 if request contains special characters in firstName" in {
        when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))

        val request = postRequest(firstNameWithSpecialCharacters)
        val result = MockController.post().apply(request)
        status(result) shouldBe BAD_REQUEST
        contentType(result).get shouldBe "application/json"
        header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
      }

      "return response code 400 if request contains more than 250 characters in firstName" in {
        when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))

        val request = postRequest(firstNameWithMoreThan250Characters)
        val result = MockController.post().apply(request)
        status(result) shouldBe BAD_REQUEST
        contentType(result).get shouldBe "application/json"
        header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
      }

    }

    "validate lastName" should {

      "return response code 400 if request contains missing lastName key" in {
        when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))

        val request = postRequest(userNoMatchExcludinglastNameKey)
        val result = MockController.post().apply(request)
        status(result) shouldBe BAD_REQUEST
        contentType(result).get shouldBe "application/json"
        header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
      }

      "return response code 400 if request contains missing lastName value" in {
        when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))

        val request = postRequest(userNoMatchExcludinglastNameValue)
        val result = MockController.post().apply(request)
        status(result) shouldBe BAD_REQUEST
        contentType(result).get shouldBe "application/json"
        header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
      }

      "return response code 400 if request contains special character in lastname value" in {
        when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))

        val request = postRequest(lastNameWithSpecialCharacters)
        val result = MockController.post().apply(request)
        status(result) shouldBe BAD_REQUEST
        contentType(result).get shouldBe "application/json"
        header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
      }

      "return response code 400 if request contains more than 250 character in lastname value" in {
        when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))

        val request = postRequest(lastNameWithMoreThan250Characters)
        val result = MockController.post().apply(request)
        status(result) shouldBe BAD_REQUEST
        contentType(result).get shouldBe "application/json"
        header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
      }

    }

    "validate invalid dateOfBirth" should {

      "return response code 400 if request contains missing dateOfBirth key" in {
        when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))

        val request = postRequest(userNoMatchExcludingDateOfBirthKey)
        val result = MockController.post().apply(request)
        status(result) shouldBe BAD_REQUEST
        contentType(result).get shouldBe "application/json"
        header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
      }

      "return response code 400 if request contains missing dateOfBirth value" in {
        when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))

        val request = postRequest(userNoMatchExcludingDateOfBirthValue)
        val result = MockController.post().apply(request)
        status(result) shouldBe BAD_REQUEST
        contentType(result).get shouldBe "application/json"
        header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
      }

      "return response code 400 if request contains invalid dateOfBirth format" in {
        when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))

        val request = postRequest(userInvalidDOBFormat)
        val result = MockController.post().apply(request)
        status(result) shouldBe BAD_REQUEST
        contentType(result).get shouldBe "application/json"
        header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
      }

    }

    "validate whereBirthRegistered" should {

      //      TODO feature toggle test?
      //      "return 200 false if request contains Northern Ireland" in {
      //        when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
      //
      //        val request = postRequest(userWhereBirthRegisteredNI)
      //        val result = MockController.post().apply(request)
      //        status(result) shouldBe OK
      //        (contentAsJson(result) \ "matched").as[Boolean] shouldBe false
      //        contentType(result).get shouldBe "application/json"
      //        header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
      //      }

      "return 200 if request contains camel case where birth registered" in {
        when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
        when(MockController.service.groConnector.getReference(Matchers.any())(Matchers.any())).thenReturn(Future.successful(httpResponse(groJsonResponseObject)))

        val request = postRequest(userNoMatchIncludingReferenceNumberCamelCase)
        val result = MockController.post().apply(request)
        status(result) shouldBe OK
        (contentAsJson(result) \ "matched").as[Boolean] shouldBe false
        contentType(result).get shouldBe "application/json"
        header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
      }

      //      TODO feature toggle test?
      //      "return 200 false if request contains Scotland" in {
      //        when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
      //
      //        val request = postRequest(userWhereBirthRegisteredScotland)
      //        val result = MockController.post().apply(request)
      //        status(result) shouldBe OK
      //        (contentAsJson(result) \ "matched").as[Boolean] shouldBe false
      //        contentType(result).get shouldBe "application/json"
      //        header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
      //      }

      "return response code 400 if request contains missing whereBirthRegistered key" in {
        when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))

        val request = postRequest(userNoMatchExcludingWhereBirthRegisteredKey)
        val result = MockController.post().apply(request)
        status(result) shouldBe BAD_REQUEST
        contentType(result).get shouldBe "application/json"
        header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
      }

      "return response code 400 if request contains missing whereBirthRegistered value" in {
        when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))

        val request = postRequest(userNoMatchExcludingWhereBirthRegisteredValue)
        val result = MockController.post().apply(request)
        status(result) shouldBe BAD_REQUEST
        contentType(result).get shouldBe "application/json"
        header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
      }

      "return response code 400 if request contains invalid whereBirthRegistered value" in {
        when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))

        val request = postRequest(userInvalidWhereBirthRegistered)
        val result = MockController.post().apply(request)
        status(result) shouldBe BAD_REQUEST
        contentType(result).get shouldBe "application/json"
        header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
      }

    }

    "GRO" when {

      "POST with reference number" should {

        "return JSON response on successful reference match" in {
          when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
          when(MockController.service.groConnector.getReference(Matchers.any())(Matchers.any())).thenReturn(Future.successful(httpResponse(groJsonResponseObject)))
          val request = postRequest(userMatchIncludingReferenceNumber)
          val result = await(MockController.post().apply(request))
          status(result) shouldBe OK
          contentType(result).get shouldBe "application/json"
          header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
          jsonBodyOf(result).toString().contains("true") shouldBe true
        }

        "return JSON response on unsuccessful birthReferenceNumber match" in {
          when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
          when(MockController.service.groConnector.getReference(Matchers.any())(Matchers.any())).thenReturn(Future.successful(httpResponse(noJson)))

          val request = postRequest(userNoMatchIncludingReferenceNumber)
          val result = MockController.post().apply(request)
          status(result) shouldBe OK
          contentType(result).get shouldBe "application/json"
          header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
          (contentAsJson(result) \ "matched").as[Boolean] shouldBe false
        }

        "return response code 200 if request contains missing birthReferenceNumber value" in {
          when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
          val request = postRequest(userNoMatchExcludingReferenceValue)
          val result = MockController.post().apply(request)
          status(result) shouldBe BAD_REQUEST
          contentType(result).get shouldBe "application/json"
          header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
        }

        "return match false when GRO returns invalid json" in {
          when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
          when(MockController.service.groConnector.getReference(Matchers.any())(Matchers.any())).thenReturn(Future.successful(httpResponse(invalidResponse)))

          val request = postRequest(userMatchIncludingReferenceNumber)
          val result = MockController.post().apply(request)
          status(result) shouldBe OK
          contentType(result).get shouldBe "application/json"
          header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
          (contentAsJson(result) \ "matched").as[Boolean] shouldBe false
        }

      }

      "POST by child's details" should {

        "return JSON response on successful child detail match when multiple records are returned" in {
          when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
          when(MockController.service.groConnector.getChildDetails(Matchers.any())(Matchers.any())).thenReturn(Future.successful(httpResponse(groJsonResponseObjectMultipleWithMatch)))

          val request = postRequest(userMultipleMatchExcludingReferenceKey)
          val result = MockController.post().apply(request)
          status(result) shouldBe OK
          contentType(result).get shouldBe "application/json"
          header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
          (contentAsJson(result) \ "matched").as[Boolean] shouldBe false
        }

        "return JSON response on unsuccessful child detail match" in {
          when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
          when(MockController.service.groConnector.getChildDetails(Matchers.any())(Matchers.any())).thenReturn(Future.successful(httpResponse(Json.parse("[]"))))

          val request = postRequest(userNoMatchExcludingReferenceKey)
          val result = MockController.post().apply(request)
          status(result) shouldBe OK
          contentType(result).get shouldBe "application/json"
          header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
          (contentAsJson(result) \ "matched").as[Boolean] shouldBe false
        }

        "return JSON response on when details contain valid UTF-8 special characters" in {
          when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
          when(MockController.service.groConnector.getChildDetails(Matchers.any())(Matchers.any())).thenReturn(Future.successful(httpResponse(Json.parse("[]"))))

          val request = postRequest(userNoMatchUTF8SpecialCharacters)
          val result = MockController.post().apply(request)
          status(result) shouldBe OK
          contentType(result).get shouldBe "application/json"
          header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
          (contentAsJson(result) \ "matched").as[Boolean] shouldBe false
        }

        "return JSON response on successful child detail match" in {
          when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
          when(MockController.service.groConnector.getChildDetails(Matchers.any())(Matchers.any())).
            thenReturn(Future.successful(httpResponse(groJsonResponseObjectCollection)))

          val request = postRequest(userMatchExcludingReferenceNumberKey)
          val result = MockController.post().apply(request)
          status(result) shouldBe OK
          contentType(result).get shouldBe "application/json"
          header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
          (contentAsJson(result) \ "matched").as[Boolean] shouldBe true
        }

      }

      "receiving error response from Proxy for reference number" should {

        "return BadGateway when GRO returns upstream BAD_GATEWAY" in {
          when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
          when(MockController.service.groConnector.getReference(Matchers.any())(Matchers.any())).thenReturn(Future.failed(new Upstream5xxResponse("", BAD_GATEWAY, BAD_GATEWAY)))

          val request = postRequest(userNoMatchIncludingReferenceNumber)
          val result = MockController.post().apply(request)
          status(result) shouldBe BAD_GATEWAY
          contentType(result).get shouldBe "application/json"
          header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
        }

        "return BadRequest when GRO returns upstream 4xx BadRequest" in {
          when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
          when(MockController.service.groConnector.getReference(Matchers.any())(Matchers.any())).thenReturn(Future.failed(new Upstream4xxResponse("", BAD_REQUEST, BAD_REQUEST)))

          val request = postRequest(userNoMatchIncludingReferenceNumber)
          val result = await(MockController.post().apply(request))
          status(result) shouldBe BAD_REQUEST
          contentType(result).get shouldBe "application/json"
          header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
        }

        "return GatewayTimeout when GRO returns 5xx when GatewayTimeout" in {
          when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
          when(MockController.service.groConnector.getReference(Matchers.any())(Matchers.any())).thenReturn(Future.failed(new Upstream5xxResponse("", GATEWAY_TIMEOUT, GATEWAY_TIMEOUT)))

          val request = postRequest(userNoMatchIncludingReferenceNumber)
          val result = MockController.post().apply(request)
          status(result) shouldBe GATEWAY_TIMEOUT
          contentType(result).get shouldBe "application/json"
          header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
        }

        "return BadRequest when GRO returns BadRequestException" in {
          when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
          when(MockController.service.groConnector.getReference(Matchers.any())(Matchers.any())).thenReturn(Future.failed(new BadRequestException("")))

          val request = postRequest(userNoMatchIncludingReferenceNumber)
          val result = MockController.post().apply(request)
          status(result) shouldBe BAD_REQUEST
          contentType(result).get shouldBe "application/json"
          header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
        }

        "return InternalServerError when GRO returns upstream 5xx NOT_IMPLEMENTED" in {
          when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
          when(MockController.service.groConnector.getReference(Matchers.any())(Matchers.any())).thenReturn(Future.failed(new Upstream5xxResponse("", NOT_IMPLEMENTED, NOT_IMPLEMENTED)))

          val request = postRequest(userNoMatchIncludingReferenceNumber)
          val result = MockController.post().apply(request)
          status(result) shouldBe INTERNAL_SERVER_ERROR
          contentType(result).get shouldBe "application/json"
          header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
        }

        "return InternalServerError when GRO returns upstream InternalServerError" in {
          when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
          when(MockController.service.groConnector.getReference(Matchers.any())(Matchers.any())).thenReturn(Future.failed(new Upstream5xxResponse("", INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR)))

          val request = postRequest(userNoMatchIncludingReferenceNumber)
          val result = MockController.post().apply(request)
          status(result) shouldBe INTERNAL_SERVER_ERROR
          contentType(result).get shouldBe "application/json"
          header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
        }

        "return 200 false when GRO returns upstream NOT_FOUND" in {
          when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
          when(MockController.service.groConnector.getReference(Matchers.any())(Matchers.any())).thenReturn(Future.failed(new Upstream4xxResponse("", NOT_FOUND, NOT_FOUND)))

          val request = postRequest(userNoMatchIncludingReferenceNumber)
          val result = MockController.post().apply(request)
          status(result) shouldBe OK
          (contentAsJson(result) \ "matched").as[Boolean] shouldBe false
          contentType(result).get shouldBe "application/json"
          header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
        }

        "return 200 false when GRO returns NotFoundException" in {
          when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
          when(MockController.service.groConnector.getReference(Matchers.any())(Matchers.any())).thenReturn(Future.failed(new NotFoundException("")))

          val request = postRequest(userNoMatchIncludingReferenceNumber)
          val result = MockController.post().apply(request)
          status(result) shouldBe OK
          (contentAsJson(result) \ "matched").as[Boolean] shouldBe false
          contentType(result).get shouldBe "application/json"
          header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
        }

        "return InternalServerError when GRO throws Exception" in {
          when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
          when(MockController.service.groConnector.getReference(Matchers.any())(Matchers.any())).thenReturn(Future.failed(new Exception("")))

          val request = postRequest(userNoMatchIncludingReferenceNumber)
          val result = MockController.post().apply(request)
          status(result) shouldBe INTERNAL_SERVER_ERROR
          contentType(result).get shouldBe "application/json"
          header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
        }

      }

      "receiving error response from Proxy for details request" should {

        "return BadGateway when GRO returns upstream BAD_GATEWAY" in {
          when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
          when(MockController.service.groConnector.getChildDetails(Matchers.any())(Matchers.any())).thenReturn(Future.failed(new Upstream5xxResponse("", BAD_GATEWAY, BAD_GATEWAY)))

          val request = postRequest(userNoMatchExcludingReferenceKey)
          val result = MockController.post().apply(request)
          status(result) shouldBe BAD_GATEWAY
          contentType(result).get shouldBe "application/json"
          header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
        }

        "return BadRequest when GRO returns upstream 4xx BadRequest" in {
          when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
          when(MockController.service.groConnector.getChildDetails(Matchers.any())(Matchers.any())).thenReturn(Future.failed(new Upstream4xxResponse("", BAD_REQUEST, BAD_REQUEST)))

          val request = postRequest(userNoMatchExcludingReferenceKey)
          val result = await(MockController.post().apply(request))
          status(result) shouldBe BAD_REQUEST
          contentType(result).get shouldBe "application/json"
          header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
        }

        "return GatewayTimeout when GRO returns 5xx when GatewayTimeout" in {
          when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
          when(MockController.service.groConnector.getChildDetails(Matchers.any())(Matchers.any())).thenReturn(Future.failed(new Upstream5xxResponse("", GATEWAY_TIMEOUT, GATEWAY_TIMEOUT)))

          val request = postRequest(userNoMatchExcludingReferenceKey)
          val result = MockController.post().apply(request)
          status(result) shouldBe GATEWAY_TIMEOUT
          contentType(result).get shouldBe "application/json"
          header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
        }

        "return BadRequest when GRO returns BadRequestException" in {
          when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
          when(MockController.service.groConnector.getChildDetails(Matchers.any())(Matchers.any())).thenReturn(Future.failed(new BadRequestException("")))

          val request = postRequest(userNoMatchExcludingReferenceKey)
          val result = MockController.post().apply(request)
          status(result) shouldBe BAD_REQUEST
          contentType(result).get shouldBe "application/json"
          header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
        }

        "return InternalServerError when GRO returns upstream 5xx NOT_IMPLEMENTED" in {
          when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
          when(MockController.service.groConnector.getChildDetails(Matchers.any())(Matchers.any())).thenReturn(Future.failed(new Upstream5xxResponse("", NOT_IMPLEMENTED, NOT_IMPLEMENTED)))

          val request = postRequest(userNoMatchExcludingReferenceKey)
          val result = MockController.post().apply(request)
          status(result) shouldBe INTERNAL_SERVER_ERROR
          contentType(result).get shouldBe "application/json"
          header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
        }

        "return InternalServerError when GRO returns upstream InternalServerError" in {
          when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
          when(MockController.service.groConnector.getChildDetails(Matchers.any())(Matchers.any())).thenReturn(Future.failed(new Upstream5xxResponse("", INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR)))

          val request = postRequest(userNoMatchExcludingReferenceKey)
          val result = MockController.post().apply(request)
          status(result) shouldBe INTERNAL_SERVER_ERROR
          contentType(result).get shouldBe "application/json"
          header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
        }

        "return 200 false when GRO returns upstream NOT_FOUND" in {
          when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
          when(MockController.service.groConnector.getChildDetails(Matchers.any())(Matchers.any())).thenReturn(Future.failed(new Upstream4xxResponse("", NOT_FOUND, NOT_FOUND)))

          val request = postRequest(userNoMatchExcludingReferenceKey)
          val result = MockController.post().apply(request)
          status(result) shouldBe OK
          contentType(result).get shouldBe "application/json"
          header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
        }

        "return 200 false when GRO returns NotFoundException" in {
          when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
          when(MockController.service.groConnector.getChildDetails(Matchers.any())(Matchers.any())).thenReturn(Future.failed(new NotFoundException("")))

          val request = postRequest(userNoMatchExcludingReferenceKey)
          val result = MockController.post().apply(request)
          status(result) shouldBe OK
          contentType(result).get shouldBe "application/json"
          header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
        }

        "return InternalServerError when GRO throws Exception" in {
          when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
          when(MockController.service.groConnector.getChildDetails(Matchers.any())(Matchers.any())).thenReturn(Future.failed(new Exception("")))

          val request = postRequest(userNoMatchExcludingReferenceKey)
          val result = MockController.post().apply(request)
          status(result) shouldBe INTERNAL_SERVER_ERROR
          contentType(result).get shouldBe "application/json"
          header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
        }

      }
    }

    "NRS" when {

      "POST with reference number" should {

        "return JSON response on successful reference match" in {
          when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
          when(MockController.service.nrsConnector.getReference(Matchers.any())(Matchers.any())).thenReturn(Future.successful(httpResponse(validNrsJsonResponseObject)))
          val request = postRequest(userMatchIncludingReferenceNumberKeyForScotland)
          val result = await(MockController.post().apply(request))
          status(result) shouldBe OK
          contentType(result).get shouldBe "application/json"
          header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
          (contentAsJson(result) \ "matched").as[Boolean] shouldBe true
        }

        "return 200 when RCE is present" in {
          when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
          when(MockLookupService.nrsConnector.getReference(Matchers.any())(Matchers.any())).thenReturn(Future.successful(HttpResponse(Status.OK, Some(validNrsJsonResponseObjectRCE))))

          val request = postRequest(userMatchIncludingReferenceNumberKeyForScotland)
          val result = await(MockController.post().apply(request))

          status(result) shouldBe OK
          contentType(result).get shouldBe "application/json"
          header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
          (contentAsJson(result) \ "matched").as[Boolean] shouldBe false
        }

        "return 200 response for UTF-8 reference request" in {
          when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
          when(MockController.service.nrsConnector.getReference(Matchers.any())(Matchers.any())).thenReturn(Future.successful(httpResponse(200, validNrsJsonResponse2017350007)))

          val request = postRequest(nrsReferenceRequestWithSpecialCharacters)
          val result = MockController.post().apply(request)
          status(result) shouldBe OK
          contentType(result).get shouldBe "application/json"
          header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
          (contentAsJson(result) \ "matched").as[Boolean] shouldBe true
        }

        "return JSON response on unsuccessful birthReferenceNumber match" in {
          when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
          when(MockController.service.nrsConnector.getReference(Matchers.any())(Matchers.any())).thenReturn(Future.successful(httpResponse(FORBIDDEN, nrsNoRecordResponse)))

          val request = postRequest(userNoMatchIncludingReferenceNumber)
          val result = MockController.post().apply(request)
          status(result) shouldBe OK
          contentType(result).get shouldBe "application/json"
          header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
          (contentAsJson(result) \ "matched").as[Boolean] shouldBe false
        }

      }

      "POST with child's details" should {

        "return 200 when RCE is present" in {
          when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
          when(MockLookupService.nrsConnector.getChildDetails(Matchers.any())(Matchers.any())).thenReturn(Future.successful(HttpResponse(Status.OK, Some(validNrsJsonResponseObjectRCE))))

          val request = postRequest(userMatchExcludingReferenceNumberKeyForScotland)
          val result = await(MockController.post().apply(request))

          status(result) shouldBe OK
          contentType(result).get shouldBe "application/json"
          header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
          (contentAsJson(result) \ "matched").as[Boolean] shouldBe false
        }

        "return 200 response on successful child detail match when multiple records are returned" in {
          when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
          when(MockController.service.nrsConnector.getChildDetails(Matchers.any())(Matchers.any())).thenReturn(Future.successful(httpResponse(OK, nrsResponseWithMultiple)))

          val request = postRequest(userMatchExcludingReferenceNumberKeyForScotland)
          val result = MockController.post().apply(request)
          status(result) shouldBe OK
          contentType(result).get shouldBe "application/json"
          header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
          (contentAsJson(result) \ "matched").as[Boolean] shouldBe false
        }

        "return 200 response when child details are not found" in {
          when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
          when(MockController.service.nrsConnector.getChildDetails(Matchers.any())(Matchers.any())).thenReturn(Future.successful(httpResponse(FORBIDDEN, nrsNoRecordResponse)))

          val request = postRequest(userMatchExcludingReferenceNumberKeyForScotland)
          val result = MockController.post().apply(request)
          status(result) shouldBe OK
          contentType(result).get shouldBe "application/json"
          header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
          (contentAsJson(result) \ "matched").as[Boolean] shouldBe false
        }

        "return 200 response on when details contain valid UTF-8 special characters" in {
          when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
          when(MockController.service.nrsConnector.getChildDetails(Matchers.any())(Matchers.any())).thenReturn(Future.successful(httpResponse(OK, validNrsJsonResponse2017350007)))

          val request = postRequest(nrsRequestWithSpecialCharacters)
          val result = MockController.post().apply(request)
          status(result) shouldBe OK
          contentType(result).get shouldBe "application/json"
          header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
          (contentAsJson(result) \ "matched").as[Boolean] shouldBe true
        }

        "return 200 response on details match when single record is returned" in {
          when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
          when(MockController.service.nrsConnector.getChildDetails(Matchers.any())(Matchers.any())).thenReturn(Future.successful(httpResponse(OK, validNrsJsonResponseObject)))

          val request = postRequest(nrsDetailsRequestWithSingleMatch)
          val result = MockController.post().apply(request)
          status(result) shouldBe OK
          contentType(result).get shouldBe "application/json"
          header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
          (contentAsJson(result) \ "matched").as[Boolean] shouldBe true
        }

      }

      "receiving error response from NRS" should {

        // TODO WHAT RESPONSE CODES DO WE NEED TO MAP?

      }

    }

    "GRO-NI" when {

    }

  }

}
