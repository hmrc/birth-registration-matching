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

import org.joda.time.LocalDate
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import org.scalatest.{BeforeAndAfter, TestData}
import org.scalatestplus.play.OneAppPerTest
import play.api.http.Status
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.test.Helpers.{contentAsJson, _}
import uk.gov.hmrc.brm.audit.BRMAudit
import uk.gov.hmrc.brm.implicits.Implicits.AuditFactory
import uk.gov.hmrc.brm.models.brm.Payload
import uk.gov.hmrc.brm.services.LookupService
import uk.gov.hmrc.brm.utils.{BirthRegisterCountry, MockErrorResponses}
import uk.gov.hmrc.brm.utils.Mocks._
import uk.gov.hmrc.play.audit.http.connector.AuditResult
import uk.gov.hmrc.play.http._
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.Future

class BirthEventsControllerSpec
    extends UnitSpec
    with MockitoSugar
    with OneAppPerTest
    with BeforeAndAfter {

  import uk.gov.hmrc.brm.utils.TestHelper._

  override def newAppForTest(testData: TestData) = new GuiceApplicationBuilder().configure(
    Map(
      "microservice.services.birth-registration-matching.features.groni.enabled" -> true,
      "microservice.services.birth-registration-matching.features.groni.reference.enabled" -> true,
      "microservice.services.birth-registration-matching.features.groni.details.enabled" -> true
    )
  ).build()

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
        val result = await(MockController.post().apply(request))
        status(result) shouldBe BAD_REQUEST
        contentType(result).get shouldBe "application/json"
        header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
        bodyOf(result) shouldBe empty
      }

      "return response code 400 if request contains birthReferenceNumber with invalid characters" in {
        when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
        val request = postRequest(userNoMatchIncludingInvalidData)
        val result = await(MockController.post().apply(request))
        status(result) shouldBe BAD_REQUEST
        contentType(result).get shouldBe "application/json"
        header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
        bodyOf(result) shouldBe empty
      }

      for (scenario <- referenceNumberScenario) {
        s"${scenario("description")}" in {
          when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
          val request = postRequest(userInvalidReference(scenario("country").toString, scenario("referenceNumber").toString))
          val result = await(MockController.post().apply(request))
          status(result) shouldBe scenario("responseCode")
          contentType(result).get shouldBe "application/json"
          header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
          jsonBodyOf(result).toString shouldBe Json.parse(
            s"""
               |{
               |  "code": "INVALID_BIRTH_REFERENCE_NUMBER",
               |  "message": "The birth reference number does not meet the required length"
               |}
             """.stripMargin).toString
        }
      }

    }

    "validate firstName" should {

      "return response code 400 if request contains missing firstName key" in {
        when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
        val request = postRequest(userNoMatchExcludingFirstNameKey)
        val result = await(MockController.post().apply(request))
        status(result) shouldBe BAD_REQUEST
        contentType(result).get shouldBe "application/json"
        header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
        bodyOf(result) shouldBe empty
      }

      "return response code 400 if request contains missing firstName value" in {
        when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
        val request = postRequest(userNoMatchExcludingfirstNameValue)
        val result = await(MockController.post().apply(request))
        status(result) shouldBe BAD_REQUEST
        contentType(result).get shouldBe "application/json"
        header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
        bodyOf(result) shouldBe empty
      }

      "return response code 400 if request contains special characters in firstName" in {
        when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
        val request = postRequest(firstNameWithSpecialCharacters)
        val result = await(MockController.post().apply(request))
        status(result) shouldBe BAD_REQUEST
        contentType(result).get shouldBe "application/json"
        header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
        bodyOf(result) shouldBe empty
      }

      "return response code 400 if request contains more than 250 characters in firstName" in {
        when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
        val request = postRequest(firstNameWithMoreThan250Characters)
        val result = await(MockController.post().apply(request))
        status(result) shouldBe BAD_REQUEST
        contentType(result).get shouldBe "application/json"
        header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
        bodyOf(result) shouldBe empty
      }

    }

    "validate lastName" should {

      "return response code 400 if request contains missing lastName key" in {
        when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
        val request = postRequest(userNoMatchExcludinglastNameKey)
        val result = await(MockController.post().apply(request))
        status(result) shouldBe BAD_REQUEST
        contentType(result).get shouldBe "application/json"
        header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
        bodyOf(result) shouldBe empty
      }

      "return response code 400 if request contains missing lastName value" in {
        when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
        val request = postRequest(userNoMatchExcludinglastNameValue)
        val result = await(MockController.post().apply(request))
        status(result) shouldBe BAD_REQUEST
        contentType(result).get shouldBe "application/json"
        header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
        bodyOf(result) shouldBe empty
      }

      "return response code 400 if request contains special character in lastName value" in {
        when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
        val request = postRequest(lastNameWithSpecialCharacters)
        val result = await(MockController.post().apply(request))
        status(result) shouldBe BAD_REQUEST
        contentType(result).get shouldBe "application/json"
        header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
        bodyOf(result) shouldBe empty
      }

      "return response code 400 if request contains more than 250 character in lastName value" in {
        when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
        val request = postRequest(lastNameWithMoreThan250Characters)
        val result = await(MockController.post().apply(request))
        status(result) shouldBe BAD_REQUEST
        contentType(result).get shouldBe "application/json"
        header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
        bodyOf(result) shouldBe empty
      }

    }

    "validate invalid dateOfBirth" should {

      "return response code 400 if request contains missing dateOfBirth key" in {
        when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
        val request = postRequest(userNoMatchExcludingDateOfBirthKey)
        val result = await(MockController.post().apply(request))
        status(result) shouldBe BAD_REQUEST
        contentType(result).get shouldBe "application/json"
        header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
        bodyOf(result) shouldBe empty
      }

      "return response code 400 if request contains missing dateOfBirth value" in {
        when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
        val request = postRequest(userNoMatchExcludingDateOfBirthValue)
        val result = await(MockController.post().apply(request))
        status(result) shouldBe BAD_REQUEST
        contentType(result).get shouldBe "application/json"
        header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
        bodyOf(result) shouldBe empty
      }

      "return response code 400 if request contains invalid dateOfBirth format" in {
        when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
        val request = postRequest(userInvalidDOBFormat)
        val result = await(MockController.post().apply(request))
        status(result) shouldBe BAD_REQUEST
        contentType(result).get shouldBe "application/json"
        header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
        bodyOf(result) shouldBe empty
      }

    }

    "validate whereBirthRegistered" should {

      "return 200 if request contains camel case where birth registered" in {
        when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
        when(MockController.service.groConnector.getReference(Matchers.any())(Matchers.any())).thenReturn(Future.successful(httpResponse(groJsonResponseObject)))
        val request = postRequest(userNoMatchIncludingReferenceNumberCamelCase)
        val result = await(MockController.post().apply(request))
        status(result) shouldBe OK
        (contentAsJson(result) \ "matched").as[Boolean] shouldBe false
        contentType(result).get shouldBe "application/json"
        header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
      }

      "return response code 400 if request contains missing whereBirthRegistered key" in {
        when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
        val request = postRequest(userNoMatchExcludingWhereBirthRegisteredKey)
        val result = await(MockController.post().apply(request))
        status(result) shouldBe BAD_REQUEST
        contentType(result).get shouldBe "application/json"
        header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
        bodyOf(result) shouldBe empty
      }

      "return response code 400 if request contains missing whereBirthRegistered value" in {
        when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
        val request = postRequest(userNoMatchExcludingWhereBirthRegisteredValue)
        val result = await(MockController.post().apply(request))
        status(result) shouldBe BAD_REQUEST
        contentType(result).get shouldBe "application/json"
        header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
        bodyOf(result) shouldBe empty
      }

      "return response code 400 if request contains invalid whereBirthRegistered value" in {
        when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
        val request = postRequest(userInvalidWhereBirthRegistered)
        val result = await(MockController.post().apply(request))
        status(result) shouldBe BAD_REQUEST
        contentType(result).get shouldBe "application/json"
        header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
        bodyOf(result) shouldBe empty
      }

    }

    "GRO" when {

      "POST with reference number" should {

        "return JSON response true on successful reference match" in {
          when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
          when(MockController.service.groConnector.getReference(Matchers.any())(Matchers.any())).thenReturn(Future.successful(httpResponse(groJsonResponseObject400000001)))
          val request = postRequest(user400000001)
          val result = await(MockController.post().apply(request))
          status(result) shouldBe OK
          contentType(result).get shouldBe "application/json"
          header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
          jsonBodyOf(result).toString().contains("true") shouldBe true
        }

        "return JSON response false when date of birth is before 2009-07-01" in {
          when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
          val request = postRequest(userMatchIncludingReferenceNumber)
          val result = await(MockController.post().apply(request))
          status(result) shouldBe OK
          contentType(result).get shouldBe "application/json"
          header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
          jsonBodyOf(result).toString().contains("true") shouldBe false
        }

        "return JSON response on unsuccessful birthReferenceNumber match" in {
          when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
          when(MockController.service.groConnector.getReference(Matchers.any())(Matchers.any())).thenReturn(Future.successful(httpResponse(noJson)))

          val request = postRequest(userNoMatchIncludingReferenceNumber)
          val result = await(MockController.post().apply(request))
          status(result) shouldBe OK
          contentType(result).get shouldBe "application/json"
          header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
          (contentAsJson(result) \ "matched").as[Boolean] shouldBe false
        }

        "return match false when GRO returns invalid json" in {
          when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
          when(MockController.service.groConnector.getReference(Matchers.any())(Matchers.any())).thenReturn(Future.successful(httpResponse(invalidResponse)))

          val request = postRequest(userMatchIncludingReferenceNumber)
          val result = await(MockController.post().apply(request))
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
          val result = await(MockController.post().apply(request))
          status(result) shouldBe OK
          contentType(result).get shouldBe "application/json"
          header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
          (contentAsJson(result) \ "matched").as[Boolean] shouldBe false
        }

        "return JSON response on unsuccessful child detail match" in {
          when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
          when(MockController.service.groConnector.getChildDetails(Matchers.any())(Matchers.any())).thenReturn(Future.successful(httpResponse(Json.parse("[]"))))

          val request = postRequest(userNoMatchExcludingReferenceKey)
          val result = await(MockController.post().apply(request))
          status(result) shouldBe OK
          contentType(result).get shouldBe "application/json"
          header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
          (contentAsJson(result) \ "matched").as[Boolean] shouldBe false
        }

        "return JSON response on when details contain valid UTF-8 special characters" in {
          when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
          when(MockController.service.groConnector.getChildDetails(Matchers.any())(Matchers.any())).thenReturn(Future.successful(httpResponse(Json.parse("[]"))))

          val request = postRequest(userNoMatchUTF8SpecialCharacters)
          val result = await(MockController.post().apply(request))
          status(result) shouldBe OK
          contentType(result).get shouldBe "application/json"
          header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
          (contentAsJson(result) \ "matched").as[Boolean] shouldBe false
        }

        "return JSON response true on successful child detail match" in {
          when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
          when(MockController.service.groConnector.getChildDetails(Matchers.any())(Matchers.any())).
            thenReturn(Future.successful(httpResponse(groJsonResponseObjectCollection400000001)))

          val request = postRequest(user400000001WithoutReferenceNumber)
          val result = await(MockController.post().apply(request))
          status(result) shouldBe OK
          contentType(result).get shouldBe "application/json"
          header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
          (contentAsJson(result) \ "matched").as[Boolean] shouldBe true
        }

        "return JSON response false when birth date is before 2009-07-01" in {
          when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
          when(MockController.service.groConnector.getChildDetails(Matchers.any())(Matchers.any())).
            thenReturn(Future.successful(httpResponse(groJsonResponseObjectCollection)))

          val request = postRequest(userMatchExcludingReferenceNumberKey)
          val result = await(MockController.post().apply(request))
          status(result) shouldBe OK
          contentType(result).get shouldBe "application/json"
          header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
          (contentAsJson(result) \ "matched").as[Boolean] shouldBe false
        }

      }

      "receiving error response from Proxy for reference number" should {

        "return BadGateway when GRO returns upstream BAD_GATEWAY" in {
          when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
          when(MockController.service.groConnector.getReference(Matchers.any())(Matchers.any()))
            .thenReturn(Future.failed(Upstream5xxResponse(MockErrorResponses.BAD_REQUEST.json, BAD_GATEWAY, BAD_GATEWAY)))

          val request = postRequest(userNoMatchIncludingReferenceNumber)
          val result = await(MockController.post().apply(request))
          status(result) shouldBe BAD_GATEWAY
          contentType(result).get shouldBe "application/json"
          header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
          bodyOf(result) shouldBe empty
        }

        "return BadRequest when GRO returns upstream 4xx BadRequest" in {
          when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
          when(MockController.service.groConnector.getReference(Matchers.any())(Matchers.any()))
            .thenReturn(Future.failed(Upstream4xxResponse(MockErrorResponses.BAD_REQUEST.json, BAD_REQUEST, BAD_REQUEST)))

          val request = postRequest(userNoMatchIncludingReferenceNumber)
          val result = await(MockController.post().apply(request))
          status(result) shouldBe BAD_REQUEST
          contentType(result).get shouldBe "application/json"
          header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
          bodyOf(result) shouldBe empty
        }

        "return GatewayTimeout when GRO returns 5xx when GatewayTimeout" in {
          when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
          when(MockController.service.groConnector.getReference(Matchers.any())(Matchers.any()))
            .thenReturn(Future.failed(Upstream5xxResponse(MockErrorResponses.GATEWAY_TIMEOUT.json, GATEWAY_TIMEOUT, GATEWAY_TIMEOUT)))

          val request = postRequest(userNoMatchIncludingReferenceNumber)
          val result = await(MockController.post().apply(request))
          status(result) shouldBe GATEWAY_TIMEOUT
          contentType(result).get shouldBe "application/json"
          header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
          bodyOf(result) shouldBe empty
        }

        "return BadRequest when GRO returns BadRequestException" in {
          when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
          when(MockController.service.groConnector.getReference(Matchers.any())(Matchers.any()))
            .thenReturn(Future.failed(new BadRequestException("")))

          val request = postRequest(userNoMatchIncludingReferenceNumber)
          val result = await(MockController.post().apply(request))
          status(result) shouldBe BAD_REQUEST
          contentType(result).get shouldBe "application/json"
          header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
          bodyOf(result) shouldBe empty
        }

        "return InternalServerError when GRO returns upstream 5xx NOT_IMPLEMENTED" in {
          when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
          when(MockController.service.groConnector.getReference(Matchers.any())(Matchers.any()))
            .thenReturn(Future.failed(Upstream5xxResponse(MockErrorResponses.UNKNOWN_ERROR.json, NOT_IMPLEMENTED, NOT_IMPLEMENTED)))

          val request = postRequest(userNoMatchIncludingReferenceNumber)
          val result = await(MockController.post().apply(request))
          status(result) shouldBe INTERNAL_SERVER_ERROR
          contentType(result).get shouldBe "application/json"
          header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
          bodyOf(result) shouldBe empty
        }

        "return InternalServerError when GRO returns upstream InternalServerError" in {
          when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
          when(MockController.service.groConnector.getReference(Matchers.any())(Matchers.any()))
            .thenReturn(Future.failed(Upstream5xxResponse(MockErrorResponses.UNKNOWN_ERROR.json, INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR)))

          val request = postRequest(userNoMatchIncludingReferenceNumber)
          val result = await(MockController.post().apply(request))
          status(result) shouldBe INTERNAL_SERVER_ERROR
          contentType(result).get shouldBe "application/json"
          header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
          bodyOf(result) shouldBe empty
        }

        "return 200 false when GRO returns upstream NOT_FOUND" in {
          when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
          when(MockController.service.groConnector.getReference(Matchers.any())(Matchers.any()))
            .thenReturn(Future.failed(Upstream4xxResponse(MockErrorResponses.NOT_FOUND.json, NOT_FOUND, NOT_FOUND)))

          val request = postRequest(userNoMatchIncludingReferenceNumber)
          val result = await(MockController.post().apply(request))
          status(result) shouldBe OK
          (contentAsJson(result) \ "matched").as[Boolean] shouldBe false
          contentType(result).get shouldBe "application/json"
          header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
        }

        "return 200 false when GRO returns NotFoundException" in {
          when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
          when(MockController.service.groConnector.getReference(Matchers.any())(Matchers.any())).thenReturn(Future.failed(new NotFoundException("")))

          val request = postRequest(userNoMatchIncludingReferenceNumber)
          val result = await(MockController.post().apply(request))
          status(result) shouldBe OK
          (contentAsJson(result) \ "matched").as[Boolean] shouldBe false
          contentType(result).get shouldBe "application/json"
          header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
        }

        "return InternalServerError when GRO throws Exception" in {
          when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
          when(MockController.service.groConnector.getReference(Matchers.any())(Matchers.any())).thenReturn(Future.failed(new Exception("")))

          val request = postRequest(userNoMatchIncludingReferenceNumber)
          val result = await(MockController.post().apply(request))
          status(result) shouldBe INTERNAL_SERVER_ERROR
          contentType(result).get shouldBe "application/json"
          header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
          bodyOf(result) shouldBe empty
        }

        "return 200 match false when GRO returns Forbidden 418 Teapot body" in {
          when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
          when(MockController.service.groConnector.getReference(Matchers.any())(Matchers.any()))
            .thenReturn(Future.failed(Upstream4xxResponse(MockErrorResponses.TEAPOT.json, FORBIDDEN, FORBIDDEN)))

          val request = postRequest(userNoMatchIncludingReferenceNumber)
          val result = await(MockController.post().apply(request))
          status(result) shouldBe OK
          contentType(result).get shouldBe "application/json"
          header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
          (contentAsJson(result) \ "matched").as[Boolean] shouldBe false
        }

        "return 200 match false when GRO returns Forbidden 'Certificate invalid'" in {
          when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
          when(MockController.service.groConnector.getReference(Matchers.any())(Matchers.any()))
            .thenReturn(Future.failed(Upstream4xxResponse(MockErrorResponses.CERTIFICATE_INVALID.json, FORBIDDEN, FORBIDDEN)))

          val request = postRequest(userNoMatchIncludingReferenceNumber)
          val result = await(MockController.post().apply(request))
          status(result) shouldBe OK
          contentType(result).get shouldBe "application/json"
          header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
          (contentAsJson(result) \ "matched").as[Boolean] shouldBe false
        }

        "return 503 when GRO returns 503 GRO_CONNECTION_DOWN" in {
          when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
          when(MockController.service.groConnector.getReference(Matchers.any())(Matchers.any()))
            .thenReturn(Future.failed(Upstream5xxResponse(MockErrorResponses.CONNECTION_DOWN.json, SERVICE_UNAVAILABLE, SERVICE_UNAVAILABLE)))

          val request = postRequest(userNoMatchIncludingReferenceNumber)
          val result = await(MockController.post().apply(request))
          status(result) shouldBe SERVICE_UNAVAILABLE
          contentType(result).get shouldBe "application/json"
          header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
          (contentAsJson(result) \ "code").as[String] shouldBe "GRO_CONNECTION_DOWN"
          (contentAsJson(result) \ "message").as[String] shouldBe "Connection to GRO is down"
        }

      }

      "receiving error response from Proxy for details request" should {

        "return BadGateway when GRO returns upstream BAD_GATEWAY" in {
          when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
          when(MockController.service.groConnector.getChildDetails(Matchers.any())(Matchers.any()))
            .thenReturn(Future.failed(Upstream5xxResponse(MockErrorResponses.BAD_REQUEST.json, BAD_GATEWAY, BAD_GATEWAY)))

          val request = postRequest(userNoMatchExcludingReferenceKey)
          val result = await(MockController.post().apply(request))
          status(result) shouldBe BAD_GATEWAY
          contentType(result).get shouldBe "application/json"
          header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
          bodyOf(result) shouldBe empty
        }

        "return BadRequest when GRO returns upstream 4xx BadRequest" in {
          when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
          when(MockController.service.groConnector.getChildDetails(Matchers.any())(Matchers.any()))
            .thenReturn(Future.failed(Upstream4xxResponse(MockErrorResponses.BAD_REQUEST.json, BAD_REQUEST, BAD_REQUEST)))

          val request = postRequest(userNoMatchExcludingReferenceKey)
          val result = await(MockController.post().apply(request))
          status(result) shouldBe BAD_REQUEST
          contentType(result).get shouldBe "application/json"
          header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
          bodyOf(result) shouldBe empty
        }

        "return GatewayTimeout when GRO returns 5xx when GatewayTimeout" in {
          when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
          when(MockController.service.groConnector.getChildDetails(Matchers.any())(Matchers.any()))
            .thenReturn(Future.failed(Upstream5xxResponse(MockErrorResponses.GATEWAY_TIMEOUT.json, GATEWAY_TIMEOUT, GATEWAY_TIMEOUT)))

          val request = postRequest(userNoMatchExcludingReferenceKey)
          val result = await(MockController.post().apply(request))
          status(result) shouldBe GATEWAY_TIMEOUT
          contentType(result).get shouldBe "application/json"
          header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
          bodyOf(result) shouldBe empty
        }

        "return BadRequest when GRO returns BadRequestException" in {
          when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
          when(MockController.service.groConnector.getChildDetails(Matchers.any())(Matchers.any())).thenReturn(Future.failed(new BadRequestException("")))

          val request = postRequest(userNoMatchExcludingReferenceKey)
          val result = await(MockController.post().apply(request))
          status(result) shouldBe BAD_REQUEST
          contentType(result).get shouldBe "application/json"
          header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
          bodyOf(result) shouldBe empty
        }

        "return InternalServerError when GRO returns upstream 5xx NOT_IMPLEMENTED" in {
          when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
          when(MockController.service.groConnector.getChildDetails(Matchers.any())(Matchers.any()))
            .thenReturn(Future.failed(Upstream5xxResponse(MockErrorResponses.UNKNOWN_ERROR.json, NOT_IMPLEMENTED, NOT_IMPLEMENTED)))

          val request = postRequest(userNoMatchExcludingReferenceKey)
          val result = await(MockController.post().apply(request))
          status(result) shouldBe INTERNAL_SERVER_ERROR
          contentType(result).get shouldBe "application/json"
          header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
          bodyOf(result) shouldBe empty
        }

        "return InternalServerError when GRO returns upstream InternalServerError" in {
          when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
          when(MockController.service.groConnector.getChildDetails(Matchers.any())(Matchers.any()))
            .thenReturn(Future.failed(Upstream5xxResponse(MockErrorResponses.UNKNOWN_ERROR.json, INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR)))

          val request = postRequest(userNoMatchExcludingReferenceKey)
          val result = await(MockController.post().apply(request))
          status(result) shouldBe INTERNAL_SERVER_ERROR
          contentType(result).get shouldBe "application/json"
          header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
          bodyOf(result) shouldBe empty
        }

        "return 200 false when GRO returns upstream NOT_FOUND" in {
          when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
          when(MockController.service.groConnector.getChildDetails(Matchers.any())(Matchers.any()))
            .thenReturn(Future.failed(Upstream4xxResponse(MockErrorResponses.NOT_FOUND.json, NOT_FOUND, NOT_FOUND)))

          val request = postRequest(userNoMatchExcludingReferenceKey)
          val result = await(MockController.post().apply(request))
          status(result) shouldBe OK
          contentType(result).get shouldBe "application/json"
          header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
          (contentAsJson(result) \ "matched").as[Boolean] shouldBe false
        }

        "return 200 false when GRO returns NotFoundException" in {
          when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
          when(MockController.service.groConnector.getChildDetails(Matchers.any())(Matchers.any())).thenReturn(Future.failed(new NotFoundException("")))

          val request = postRequest(userNoMatchExcludingReferenceKey)
          val result = await(MockController.post().apply(request))
          status(result) shouldBe OK
          contentType(result).get shouldBe "application/json"
          header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
          (contentAsJson(result) \ "matched").as[Boolean] shouldBe false
        }

        "return InternalServerError when GRO throws Exception" in {
          when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
          when(MockController.service.groConnector.getChildDetails(Matchers.any())(Matchers.any())).thenReturn(Future.failed(new Exception("")))

          val request = postRequest(userNoMatchExcludingReferenceKey)
          val result = await(MockController.post().apply(request))
          status(result) shouldBe INTERNAL_SERVER_ERROR
          contentType(result).get shouldBe "application/json"
          header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
          bodyOf(result) shouldBe empty
        }

        "return 503 when GRO returns 503 GRO_CONNECTION_DOWN" in {
          when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
          when(MockController.service.groConnector.getChildDetails(Matchers.any())(Matchers.any()))
            .thenReturn(Future.failed(Upstream5xxResponse(MockErrorResponses.CONNECTION_DOWN.json, SERVICE_UNAVAILABLE, SERVICE_UNAVAILABLE)))

          val request = postRequest(userNoMatchExcludingReferenceKey)
          val result = await(MockController.post().apply(request))
          status(result) shouldBe SERVICE_UNAVAILABLE
          contentType(result).get shouldBe "application/json"
          header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
          (contentAsJson(result) \ "code").as[String] shouldBe "GRO_CONNECTION_DOWN"
          (contentAsJson(result) \ "message").as[String] shouldBe "Connection to GRO is down"
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

        "return JSON response false when date of birth is before 2009-07-01" in {
          when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
          when(MockController.service.nrsConnector.getReference(Matchers.any())(Matchers.any())).thenReturn(Future.successful(httpResponse(nrsRecord20090630)))
          val request = postRequest(userDob20090630)
          val result = await(MockController.post().apply(request))
          status(result) shouldBe OK
          contentType(result).get shouldBe "application/json"
          header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
          (contentAsJson(result) \ "matched").as[Boolean] shouldBe false
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

        "return 200 matched false when record status is cancelled ie RCE -6" in {
          when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
          when(MockLookupService.nrsConnector.getReference(Matchers.any())(Matchers.any())).thenReturn(Future.successful(HttpResponse(Status.OK, Some(nrsRecord2017350001))))

          val request = postRequest(Json.toJson(nrsRequestPayload2017350001))
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
          val result = await(MockController.post().apply(request))
          status(result) shouldBe OK
          contentType(result).get shouldBe "application/json"
          header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
          (contentAsJson(result) \ "matched").as[Boolean] shouldBe true
        }

        "return JSON response on unsuccessful birthReferenceNumber match" in {
          when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
          when(MockController.service.nrsConnector.getReference(Matchers.any())(Matchers.any())).thenReturn(Future.successful(Future.failed(new Upstream4xxResponse("BIRTH_REGISTRATION_NOT_FOUND", FORBIDDEN, FORBIDDEN))))

          val request = postRequest(userNoMatchIncludingReferenceNumber)
          val result = await(MockController.post().apply(request))
          status(result) shouldBe OK
          contentType(result).get shouldBe "application/json"
          header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
          (contentAsJson(result) \ "matched").as[Boolean] shouldBe false
        }


        "return 200 false response when first name has special characters for unsuccessful BRN match." in {
          when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
          when(MockController.service.nrsConnector.getReference(Matchers.any())(Matchers.any())).thenReturn(Future.successful(Future.failed(new Upstream4xxResponse("BIRTH_REGISTRATION_NOT_FOUND", FORBIDDEN, FORBIDDEN))))

          var payload = Payload(Some("1234567890"), "ÀÁÂÃÄÅÆÇÈÉÊËÌÍ ÏÐÑÒÓÔÕÖ×ØÙÚÛÜÝÞßàáâãäåæçèéêëìíîïðñòóôõö÷øùúûüýþÿÀÁÂÃÄÅÆÇÈÉÊËÌÍ ÏÐÑÒÓÔÕÖ×ØÙÚÛÜÝÞßàáâãäåæçèéêëìíîïðñòóôõö÷øùú111111ÀÁÂÃÄÅÆÇÈÉÊËÌÍ ÏÐÑÒÓÔÕÖ×ØÙÚÛÜÝÞßàáâãäåæçèéêëìíîïðñòóôõö÷øùúûüýþÿÀÁÂÃÄÅÆÇÈÉÊËÌÍ ÏÐÑÒÓÔÕÖ×ØÙÚÛÜÝÞßàáâãäåæçèéêëìíîïðñòóôõö÷ø", "Test", LocalDate.now, BirthRegisterCountry.SCOTLAND)
          val request = postRequest(Json.toJson(payload))
          val result = await(MockController.post().apply(request))
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
          val result = await(MockController.post().apply(request))
          status(result) shouldBe OK
          contentType(result).get shouldBe "application/json"
          header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
          (contentAsJson(result) \ "matched").as[Boolean] shouldBe false
        }


        "return 200 matched false when record status is cancelled ie RCE -6" in {
          when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
          when(MockLookupService.nrsConnector.getReference(Matchers.any())(Matchers.any())).thenReturn(Future.successful(HttpResponse(Status.OK, Some(nrsRecord2017350001))))

          val request = postRequest(Json.toJson(nrsRequestPayloadWithoutBrn))
          val result = await(MockController.post().apply(request))

          status(result) shouldBe OK
          contentType(result).get shouldBe "application/json"
          header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
          (contentAsJson(result) \ "matched").as[Boolean] shouldBe false
        }

        "return 200 response when child details are not found" in {
          when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
          when(MockController.service.nrsConnector.getReference(Matchers.any())(Matchers.any())).thenReturn(Future.successful(Future.failed(new Upstream4xxResponse("BIRTH_REGISTRATION_NOT_FOUND", FORBIDDEN, FORBIDDEN))))

          val request = postRequest(userMatchExcludingReferenceNumberKeyForScotland)
          val result = await(MockController.post().apply(request))
          status(result) shouldBe OK
          contentType(result).get shouldBe "application/json"
          header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
          (contentAsJson(result) \ "matched").as[Boolean] shouldBe false
        }

        "return 200 false response when child details are not found when first name has special characters." in {
          when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
          when(MockController.service.nrsConnector.getReference(Matchers.any())(Matchers.any())).thenReturn(Future.successful(Future.failed(new Upstream4xxResponse("BIRTH_REGISTRATION_NOT_FOUND", FORBIDDEN, FORBIDDEN))))

          var payload = Payload(None, "ÀÁÂÃÄÅÆÇÈÉÊËÌÍ ÏÐÑÒÓÔÕÖ×ØÙÚÛÜÝÞßàáâãäåæçèéêëìíîïðñòóôõö÷øùúûüýþÿÀÁÂÃÄÅÆÇÈÉÊËÌÍ ÏÐÑÒÓÔÕÖ×ØÙÚÛÜÝÞßàáâãäåæçèéêëìíîïðñòóôõö÷øùú111111ÀÁÂÃÄÅÆÇÈÉÊËÌÍ ÏÐÑÒÓÔÕÖ×ØÙÚÛÜÝÞßàáâãäåæçèéêëìíîïðñòóôõö÷øùúûüýþÿÀÁÂÃÄÅÆÇÈÉÊËÌÍ ÏÐÑÒÓÔÕÖ×ØÙÚÛÜÝÞßàáâãäåæçèéêëìíîïðñòóôõö÷ø", "Test", LocalDate.now, BirthRegisterCountry.SCOTLAND)
          val request = postRequest(Json.toJson(payload))
          val result = await(MockController.post().apply(request))
          status(result) shouldBe OK
          contentType(result).get shouldBe "application/json"
          header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
          (contentAsJson(result) \ "matched").as[Boolean] shouldBe false
        }

        "return 200 response on when details contain valid UTF-8 special characters" in {
          when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
          when(MockController.service.nrsConnector.getChildDetails(Matchers.any())(Matchers.any())).thenReturn(Future.successful(httpResponse(OK, validNrsJsonResponse2017350007)))

          val request = postRequest(nrsRequestWithSpecialCharacters)
          val result = await(MockController.post().apply(request))
          status(result) shouldBe OK
          contentType(result).get shouldBe "application/json"
          header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
          (contentAsJson(result) \ "matched").as[Boolean] shouldBe true
        }

        "return 200 response on details match when single record is returned" in {
          when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
          when(MockController.service.nrsConnector.getChildDetails(Matchers.any())(Matchers.any())).thenReturn(Future.successful(httpResponse(OK, validNrsJsonResponseObject)))

          val request = postRequest(nrsDetailsRequestWithSingleMatch)
          val result = await(MockController.post().apply(request))
          status(result) shouldBe OK
          contentType(result).get shouldBe "application/json"
          header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
          (contentAsJson(result) \ "matched").as[Boolean] shouldBe true
        }

      }

      "receiving error response from NRS" should {

        "return BadGateway when NRS returns upstream BAD_GATEWAY" in {
          when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
          when(MockController.service.nrsConnector.getChildDetails(Matchers.any())(Matchers.any())).thenReturn(Future.failed(Upstream5xxResponse("", BAD_GATEWAY, BAD_GATEWAY)))

          val request = postRequest(userNoMatchExcludingReferenceKeyScotland)
          val result = await(MockController.post().apply(request))
          status(result) shouldBe BAD_GATEWAY
          contentType(result).get shouldBe "application/json"
          header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
          bodyOf(result) shouldBe empty
        }

        "return GatewayTimeout when GRO returns 5xx when GatewayTimeout" in {
          when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
          when(MockController.service.nrsConnector.getChildDetails(Matchers.any())(Matchers.any())).thenReturn(Future.failed(Upstream5xxResponse("", GATEWAY_TIMEOUT, GATEWAY_TIMEOUT)))

          val request = postRequest(userNoMatchExcludingReferenceKeyScotland)
          val result = await(MockController.post().apply(request))
          status(result) shouldBe GATEWAY_TIMEOUT
          contentType(result).get shouldBe "application/json"
          header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
          bodyOf(result) shouldBe empty
        }

        "return 400 BadRequest when NRS returns 400 INVALID_PAYLOAD" in {
          when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
          when(MockController.service.nrsConnector.getChildDetails(Matchers.any())(Matchers.any())).thenReturn(Future.failed(new BadRequestException("INVALID_PAYLOAD")))
          val request = postRequest(userNoMatchExcludingReferenceKeyScotland)
          val result = await(MockController.post().apply(request))
          status(result) shouldBe BAD_REQUEST
          contentType(result).get shouldBe "application/json"
          header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
          bodyOf(result) shouldBe empty
        }

        "return 500 InternalServerError when NRS returns 400 INVALID_HEADER" in {
          when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
          when(MockController.service.nrsConnector.getChildDetails(Matchers.any())(Matchers.any())).thenReturn(Future.failed(new BadRequestException("INVALID_HEADER")))
          val request = postRequest(userNoMatchExcludingReferenceKey)
          val result = await(MockController.post().apply(request))
          status(result) shouldBe INTERNAL_SERVER_ERROR
          contentType(result).get shouldBe "application/json"
          header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
          bodyOf(result) shouldBe empty
        }

        "return 400 BadRequest when NRS returns 403 INVALID_DISTRICT_NUMBER" in {
          when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
          when(MockController.service.nrsConnector.getChildDetails(Matchers.any())(Matchers.any())).thenReturn(Future.failed(new Upstream4xxResponse("INVALID_DISTRICT_NUMBER", FORBIDDEN, FORBIDDEN)))
          val request = postRequest(userNoMatchExcludingReferenceKey)
          val result = await(MockController.post().apply(request))
          status(result) shouldBe OK
          contentType(result).get shouldBe "application/json"
          header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
          (contentAsJson(result) \ "matched").as[Boolean] shouldBe false
        }

        "return 500 InternalServerError when NRS returns 403 QUERY_LENGTH_EXCESSIVE" in {
          when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
          when(MockController.service.nrsConnector.getChildDetails(Matchers.any())(Matchers.any())).thenReturn(Future.successful(Future.failed(new Upstream4xxResponse("QUERY_LENGTH_EXCESSIVE", FORBIDDEN, FORBIDDEN))))

          val request = postRequest(userNoMatchExcludingReferenceKey)
          val result = await(MockController.post().apply(request))
          status(result) shouldBe OK
          contentType(result).get shouldBe "application/json"
          header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
          (contentAsJson(result) \ "matched").as[Boolean] shouldBe false
        }

        "return 500 InternalServerError when NRS returns 500" in {
          when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
          when(MockController.service.nrsConnector.getChildDetails(Matchers.any())(Matchers.any())).thenReturn(Future.failed(new Upstream5xxResponse("", INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR)))

          val request = postRequest(userNoMatchExcludingReferenceKey)
          val result = await(MockController.post().apply(request))
          status(result) shouldBe INTERNAL_SERVER_ERROR
          contentType(result).get shouldBe "application/json"
          header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
          bodyOf(result) shouldBe empty
        }

        "return 500 InternalServerError when NRS returns 503 Service unavailable" in {
          when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
          when(MockController.service.nrsConnector.getChildDetails(Matchers.any())(Matchers.any())).thenReturn(Future.failed(new Upstream5xxResponse("", SERVICE_UNAVAILABLE, SERVICE_UNAVAILABLE)))

          val request = postRequest(userNoMatchExcludingReferenceKey)
          val result = await(MockController.post().apply(request))
          status(result) shouldBe INTERNAL_SERVER_ERROR
          contentType(result).get shouldBe "application/json"
          header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
          bodyOf(result) shouldBe empty
        }

      }

    }

    "GRO-NI" should {

      "return 200 false if request contains Northern Ireland" in {
        when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
        when(MockLookupService.groniConnector.getReference(Matchers.any())(Matchers.any())).thenReturn(Future.failed(new NotImplementedException("")))
        val request = postRequest(userWhereBirthRegisteredNI)
        val result = await(MockController.post().apply(request))
        status(result) shouldBe OK
        (contentAsJson(result) \ "matched").as[Boolean] shouldBe false
        contentType(result).get shouldBe "application/json"
        header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
      }

      "calls getReference when GRONIFeature is enabled" in {
        when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
        when(MockLookupService.groniConnector.getReference(Matchers.any())(Matchers.any())).thenReturn(Future.failed(new NotImplementedException("")))
        val request = postRequest(userWhereBirthRegisteredNI)
        val result = await(MockController.post().apply(request))
        status(result) shouldBe OK
        (contentAsJson(result) \ "matched").as[Boolean] shouldBe false
        contentType(result).get shouldBe "application/json"
        header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
      }

      "calls getDetails when GRONIFeature is enabled" in {
        when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
        when(MockLookupService.groniConnector.getChildDetails(Matchers.any())(Matchers.any())).thenReturn(Future.failed(new NotImplementedException("")))
        val request = postRequest(userWhereBirthRegisteredNI)
        val result = await(MockController.post().apply(request))
        status(result) shouldBe OK
        (contentAsJson(result) \ "matched").as[Boolean] shouldBe false
        contentType(result).get shouldBe "application/json"
        header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
      }

    }

  }

}
