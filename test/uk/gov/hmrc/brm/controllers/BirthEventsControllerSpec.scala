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

package uk.gov.hmrc.brm.controllers

import java.time.LocalDate
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.concurrent.IntegrationPatience
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.test.Helpers._
import uk.gov.hmrc.brm.audit.BRMAudit
import uk.gov.hmrc.brm.config.BrmConfig
import uk.gov.hmrc.brm.models.brm.Payload
import uk.gov.hmrc.brm.models.matching.BirthMatchResponse
import uk.gov.hmrc.brm.utils.Mocks._
import uk.gov.hmrc.brm.utils.{BaseUnitSpec, BirthRegisterCountry, HeaderValidator, MockErrorResponses}
import uk.gov.hmrc.http._
import uk.gov.hmrc.play.audit.http.connector.AuditResult
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatest.OptionValues

import scala.concurrent.Future

class BirthEventsControllerSpec
    extends AnyWordSpecLike
    with Matchers
    with OptionValues
    with MockitoSugar
    with GuiceOneAppPerSuite
    with BaseUnitSpec
    with IntegrationPatience {

  import uk.gov.hmrc.brm.utils.TestHelper._

  private val specialCharacters: String =
    "ÀÁÂÃÄÅÆÇÈÉÊËÌÍ ÏÐÑÒÓÔÕÖ×ØÙÚÛÜÝÞßàáâãäåæçèéêëìíîïðñòóôõö÷øùúûüýþÿÀÁÂÃÄÅÆÇÈÉÊËÌÍ" +
      " ÏÐÑÒÓÔÕÖ×ØÙÚÛÜÝÞßàáâãäåæçèéêëìíîïðñòóôõö÷øùú111111ÀÁÂÃÄÅÆÇÈÉÊËÌÍ ÏÐÑÒÓÔÕÖ×Ø" +
      "ÙÚÛÜÝÞßàáâãäåæçèéêëìíîïðñòóôõö÷øùúûüýþÿÀÁÂÃÄÅÆÇÈÉÊËÌÍ ÏÐÑÒÓÔÕÖ×ØÙÚÛÜÝÞßàáâãä" +
      "åæçèéêëìíîïðñòóôõö÷ø"

  override lazy val app: Application = new GuiceApplicationBuilder()
    .configure(
      Map(
        "microservice.services.birth-registration-matching.features.groni.enabled"           -> true,
        "microservice.services.birth-registration-matching.features.groni.reference.enabled" -> true,
        "microservice.services.birth-registration-matching.features.groni.details.enabled"   -> true,
        "microservice.services.birth-registration-matching.matching.ignoreAdditionalNames"   -> true
      )
    )
    .build()

  val birthEventsController: BirthEventsController = app.injector.instanceOf[BirthEventsController]

  val dateOfBirth: LocalDate = LocalDate.of(2009, 7, 1)

  import scala.concurrent.ExecutionContext.Implicits.global

  val testController: BirthEventsController = new BirthEventsController(
    mockLookupService,
    auditorFixtures.whereBirthRegisteredAudit,
    MockAuditFactory,
    app.injector.instanceOf[BrmConfig],
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

  "BirthEventsController with ignoreAdditionalName true" when {
    "initialising" should {
      "wire up dependencies correctly" in {
        birthEventsController.transactionAuditor shouldBe a[BRMAudit]
        birthEventsController.matchingAuditor    shouldBe a[BRMAudit]
      }
    }

    "validate birth reference number" should {

      "return response code 400 if request contains missing birthReferenceNumber value" in {
        when(mockLookupService.lookup()(any(), any(), any(), any()))
          .thenReturn(Future.successful(BirthMatchResponse(true)))

        when(mockFilters.process(any()))
          .thenReturn(List())

        when(mockAuditor.audit(any(), any())(any()))
          .thenReturn(Future.successful(AuditResult.Success))

        when(mockMetricsFactory.getMetrics()(any()))
          .thenReturn(mockEngWalesMetric)

        mockAuditSuccess
        val request = postRequest(userNoMatchExcludingReferenceValue)
        val result  = testController.post().apply(request).futureValue
        checkResponse(result, BAD_REQUEST, MockErrorResponses.INVALID_BIRTH_REFERENCE_NUMBER.json)
      }

      "return response code 400 if request contains birthReferenceNumber with invalid characters" in {
        mockAuditSuccess
        val request = postRequest(userNoMatchIncludingInvalidData)
        val result  = testController.post().apply(request).futureValue
        checkResponse(result, BAD_REQUEST, MockErrorResponses.INVALID_BIRTH_REFERENCE_NUMBER.json)
      }

      for (scenario <- referenceNumberScenario)
        s"${scenario("description")}" in {
          mockAuditSuccess
          val request  =
            postRequest(userInvalidReference(scenario("country").toString, scenario("referenceNumber").toString))
          val result   = testController.post().apply(request).futureValue
          val response = scenario("responseCode")
          checkResponse(result, response.asInstanceOf[Int], MockErrorResponses.INVALID_BIRTH_REFERENCE_NUMBER.json)

        }

    }

    "validate firstName" should {

      "return response code 400 if request contains missing firstName key" in {
        mockAuditSuccess
        val request = postRequest(userNoMatchExcludingFirstNameKey)
        val result  = testController.post().apply(request).futureValue
        checkResponse(result, BAD_REQUEST, MockErrorResponses.BAD_REQUEST.json)
      }

      "return response code 400 if request contains missing firstName value" in {
        mockAuditSuccess
        val request = postRequest(userNoMatchExcludingfirstNameValue)
        val result  = testController.post().apply(request).futureValue
        checkResponse(result, BAD_REQUEST, MockErrorResponses.INVALID_FIRSTNAME.json)
      }

      "return response code 400 if request contains special characters in firstName" in {
        mockAuditSuccess
        val request = postRequest(firstNameWithSpecialCharacters)
        val result  = testController.post().apply(request).futureValue
        checkResponse(result, BAD_REQUEST, MockErrorResponses.INVALID_FIRSTNAME.json)
      }

      "return response code 400 if request contains more than 250 characters in firstName" in {
        mockAuditSuccess
        val request = postRequest(firstNameWithMoreThan250Characters)
        val result  = testController.post().apply(request).futureValue
        checkResponse(result, BAD_REQUEST, MockErrorResponses.INVALID_FIRSTNAME.json)
      }

      "return response code 400 if request contains = characters in firstName" in {
        mockAuditSuccess
        val request = postRequest(firstNameWithEqualsCharacter)
        val result  = testController.post().apply(request).futureValue
        checkResponse(result, BAD_REQUEST, MockErrorResponses.INVALID_FIRSTNAME.json)
      }

      "return response code 400 if request contains + characters in firstName" in {
        mockAuditSuccess
        val request = postRequest(firstNameWithPlusCharacter)
        val result  = testController.post().apply(request).futureValue
        checkResponse(result, BAD_REQUEST, MockErrorResponses.INVALID_FIRSTNAME.json)
      }

      "return response code 400 if request contains @ characters in firstName" in {
        mockAuditSuccess
        val request = postRequest(firstNameWithAtCharacter)
        val result  = testController.post().apply(request).futureValue
        checkResponse(result, BAD_REQUEST, MockErrorResponses.INVALID_FIRSTNAME.json)
      }

      "return response code 400 if request contains \u0000 (NULL) characters in firstName" in {
        mockAuditSuccess
        val request = postRequest(firstNameWithNullCharacter)
        val result  = testController.post().apply(request).futureValue
        checkResponse(result, BAD_REQUEST, MockErrorResponses.INVALID_FIRSTNAME.json)
      }

      "return response code 400 if request contains a single space in firstName" in {
        mockAuditSuccess
        val request = postRequest(firstNameWithASingleSpace)
        val result  = testController.post().apply(request).futureValue
        checkResponse(result, BAD_REQUEST, MockErrorResponses.INVALID_FIRSTNAME.json)
      }

      "return response code 400 if request contains multiple spaces in firstName" in {
        mockAuditSuccess
        val request = postRequest(firstNameWithMultipleSpaces)
        val result  = testController.post().apply(request).futureValue
        checkResponse(result, BAD_REQUEST, MockErrorResponses.INVALID_FIRSTNAME.json)
      }
    }

    "validate additionalNames" should {

      "return response code 400 if request contains additionalNames key but no value" in {
        mockAuditSuccess
        val request = postRequest(additionalNamesKeyNoValue)
        val result  = testController.post().apply(request).futureValue
        checkResponse(result, BAD_REQUEST, MockErrorResponses.INVALID_ADDITIONALNAMES.json)
      }

      "return response code 400 if request contains special characters in additionalNames" in {
        mockAuditSuccess
        val request = postRequest(additionalNameWithSpecialCharacters)
        val result  = testController.post().apply(request).futureValue
        checkResponse(result, BAD_REQUEST, MockErrorResponses.INVALID_ADDITIONALNAMES.json)
      }

      "return response code 400 if request contains more than 250 characters in additionalNames" in {
        mockAuditSuccess
        val request = postRequest(additionalNameWithMoreThan250Characters)
        val result  = testController.post().apply(request).futureValue
        checkResponse(result, BAD_REQUEST, MockErrorResponses.INVALID_ADDITIONALNAMES.json)
      }

      "return response code 400 if request contains a single space in additionalNames" in {
        mockAuditSuccess
        val request = postRequest(additionalNameWithASingleSpace)
        val result  = testController.post().apply(request).futureValue
        checkResponse(result, BAD_REQUEST, MockErrorResponses.INVALID_ADDITIONALNAMES.json)
      }

      "return response code 400 if request contains multiple spaces in additionalNames" in {
        mockAuditSuccess
        val request = postRequest(additionalNameWithMultipleSpaces)
        val result  = testController.post().apply(request).futureValue
        checkResponse(result, BAD_REQUEST, MockErrorResponses.INVALID_ADDITIONALNAMES.json)
      }
    }

    "validate lastName" should {

      "return response code 400 if request contains missing lastName key" in {
        mockAuditSuccess
        val request = postRequest(userNoMatchExcludinglastNameKey)
        val result  = testController.post().apply(request).futureValue
        checkResponse(result, BAD_REQUEST, MockErrorResponses.BAD_REQUEST.json)
      }

      "return response code 400 if request contains missing lastName value" in {
        mockAuditSuccess
        val request = postRequest(userNoMatchExcludinglastNameValue)
        val result  = testController.post().apply(request).futureValue
        checkResponse(result, BAD_REQUEST, MockErrorResponses.INVALID_LASTNAME.json)
      }

      "return response code 400 if request contains special character in lastName value" in {
        mockAuditSuccess
        val request = postRequest(lastNameWithSpecialCharacters)
        val result  = testController.post().apply(request).futureValue
        checkResponse(result, BAD_REQUEST, MockErrorResponses.INVALID_LASTNAME.json)
      }

      "return response code 400 if request contains more than 250 character in lastName value" in {
        mockAuditSuccess
        val request = postRequest(lastNameWithMoreThan250Characters)
        val result  = testController.post().apply(request).futureValue
        checkResponse(result, BAD_REQUEST, MockErrorResponses.INVALID_LASTNAME.json)
      }

      "return response code 400 if request contains a single space in lastName" in {
        mockAuditSuccess
        val request = postRequest(lastNameWithASingleSpace)
        val result  = testController.post().apply(request).futureValue
        checkResponse(result, BAD_REQUEST, MockErrorResponses.INVALID_LASTNAME.json)
      }

      "return response code 400 if request contains multiple spaces in lastName" in {
        mockAuditSuccess
        val request = postRequest(lastNameWithMultipleSpaces)
        val result  = testController.post().apply(request).futureValue
        checkResponse(result, BAD_REQUEST, MockErrorResponses.INVALID_LASTNAME.json)
      }
    }

    "validate invalid dateOfBirth" should {

      "return response code 400 if request contains missing dateOfBirth key" in {
        mockAuditSuccess
        val request = postRequest(userNoMatchExcludingDateOfBirthKey)
        val result  = testController.post().apply(request).futureValue
        checkResponse(result, BAD_REQUEST, MockErrorResponses.BAD_REQUEST.json)
      }

      "return response code 400 if request contains missing dateOfBirth value" in {
        mockAuditSuccess
        val request = postRequest(userNoMatchExcludingDateOfBirthValue)
        val result  = testController.post().apply(request).futureValue
        checkResponse(result, BAD_REQUEST, MockErrorResponses.INVALID_DATE_OF_BIRTH.json)
      }

      "return response code 400 if request contains invalid dateOfBirth format" in {
        mockAuditSuccess
        val request = postRequest(userInvalidDOBFormat)
        val result  = testController.post().apply(request).futureValue
        checkResponse(result, BAD_REQUEST, MockErrorResponses.INVALID_DATE_OF_BIRTH.json)

      }

    }

    "validate whereBirthRegistered" should {

      "return 200 if request contains camel case where birth registered" in {
        when(mockLookupService.lookup()(any(), any(), any(), any()))
          .thenReturn(Future.successful(BirthMatchResponse()))

        mockAuditSuccess
        mockReferenceResponse(groJsonResponseObject)
        val request = postRequest(userNoMatchIncludingReferenceNumberCamelCase)
        val result  = testController.post().apply(request).futureValue
        checkResponse(result, OK, matchResponse = false)
      }

      "return response code 400 if request contains missing whereBirthRegistered key" in {
        mockAuditSuccess
        val request = postRequest(userNoMatchExcludingWhereBirthRegisteredKey)
        val result  = testController.post().apply(request).futureValue
        checkResponse(result, BAD_REQUEST, MockErrorResponses.BAD_REQUEST.json)
      }

      "return response code 403 if request contains missing whereBirthRegistered value" in {
        mockAuditSuccess
        val request = postRequest(userNoMatchExcludingWhereBirthRegisteredValue)
        val result  = testController.post().apply(request).futureValue
        checkResponse(result, FORBIDDEN, MockErrorResponses.INVALID_WHERE_BIRTH_REGISTERED.json)
      }

      "return response code 403 if request contains invalid whereBirthRegistered value" in {
        mockAuditSuccess
        val request = postRequest(userInvalidWhereBirthRegistered)
        val result  = testController.post().apply(request).futureValue
        checkResponse(result, FORBIDDEN, MockErrorResponses.INVALID_WHERE_BIRTH_REGISTERED.json)
      }

    }

    "GRO" when {

      "POST with reference number" should {

        "return JSON response true on successful reference match" in {
          when(mockLookupService.lookup()(any(), any(), any(), any()))
            .thenReturn(Future.successful(BirthMatchResponse(true)))

          mockAuditSuccess
          mockReferenceResponse(groJsonResponseObject400000001)
          val request = postRequest(user400000001)
          val result  = testController.post().apply(request).futureValue
          checkResponse(result, OK, matchResponse = true)
        }

        "return JSON response true and ignore additional name provided in request." in {
          mockAuditSuccess
          mockReferenceResponse(groResponseWithAdditionalName)
          val payload = Json.toJson(
            Payload(
              Some("500035710"),
              "Adam",
              Some("test"),
              "SMITH",
              dateOfBirth,
              BirthRegisterCountry.ENGLAND
            )
          )
          val request = postRequest(payload)
          val result  = testController.post().apply(request).futureValue
          checkResponse(result, OK, matchResponse = true)
        }

        "return JSON response true and ignore additional name provided in request as ignoreAdditionalName is true." in {
          mockAuditSuccess
          mockReferenceResponse(groResponseWithAdditionalName)
          val payload = Json.toJson(
            Payload(
              Some("500035710"),
              "Adam test",
              Some("test"),
              "SMITH",
              dateOfBirth,
              BirthRegisterCountry.ENGLAND
            )
          )
          val request = postRequest(payload)
          val result  = testController.post().apply(request).futureValue
          checkResponse(result, OK, matchResponse = true)
        }

        "return JSON response false when date of birth is before 2009-07-01" in {
          when(mockLookupService.lookup()(any(), any(), any(), any()))
            .thenReturn(Future.successful(BirthMatchResponse()))

          mockAuditSuccess
          val request = postRequest(userMatchIncludingReferenceNumber)
          val result  = testController.post().apply(request).futureValue
          checkResponse(result, OK, matchResponse = false)
        }

        "return JSON response on unsuccessful birthReferenceNumber match" in {
          mockAuditSuccess
          mockReferenceResponse(noJson)
          val request = postRequest(userNoMatchIncludingReferenceNumber)
          val result  = testController.post().apply(request).futureValue
          checkResponse(result, OK, matchResponse = false)
        }

        "return match false when GRO returns invalid json" in {
          mockAuditSuccess
          mockReferenceResponse(invalidResponse)
          val request = postRequest(userMatchIncludingReferenceNumber)
          val result  = testController.post().apply(request).futureValue
          checkResponse(result, OK, matchResponse = false)
        }

      }

      "POST by child's details" should {

        "return JSON response on successful child detail match when multiple records are returned" in {
          mockAuditSuccess
          mockDetailsResponse(groJsonResponseObjectMultipleWithMatch)
          val request = postRequest(userMultipleMatchExcludingReferenceKey)
          val result  = testController.post().apply(request).futureValue
          checkResponse(result, OK, matchResponse = false)
        }

        "return JSON response on unsuccessful child detail match" in {
          mockAuditSuccess
          mockDetailsResponse(Json.parse("[]"))
          val request = postRequest(userNoMatchExcludingReferenceKey)
          val result  = testController.post().apply(request).futureValue
          checkResponse(result, OK, matchResponse = false)
        }

        "return JSON response on when details contain valid UTF-8 special characters" in {
          mockAuditSuccess
          mockDetailsResponse(Json.parse("[]"))
          val request = postRequest(userNoMatchUTF8SpecialCharacters)
          val result  = testController.post().apply(request).futureValue
          checkResponse(result, OK, matchResponse = false)
        }

        "return JSON response true on successful child detail match" in {
          when(mockLookupService.lookup()(any(), any(), any(), any()))
            .thenReturn(Future.successful(BirthMatchResponse(true)))

          mockAuditSuccess
          mockDetailsResponse(groJsonResponseObjectCollection400000001)
          val request = postRequest(user400000001WithoutReferenceNumber)
          val result  = testController.post().apply(request).futureValue
          checkResponse(result, OK, matchResponse = true)
        }

        "return JSON response false when birth date is before 2009-07-01" in {
          when(mockLookupService.lookup()(any(), any(), any(), any()))
            .thenReturn(Future.successful(BirthMatchResponse()))

          mockAuditSuccess
          mockDetailsResponse(groJsonResponseObjectCollection)
          val request = postRequest(userMatchExcludingReferenceNumberKey)
          val result  = testController.post().apply(request).futureValue
          checkResponse(result, OK, matchResponse = false)
        }

      }

      "receiving error response from Proxy for reference number" should {

        "return InternalServerError when GRO returns Upstream5xxResponse GATEWAY_TIMEOUT" in {
          when(mockLookupService.lookup()(any(), any(), any(), any()))
            .thenReturn(Future.failed(UpstreamErrorResponse("503", GATEWAY_TIMEOUT, GATEWAY_TIMEOUT)))

          when(mockMatchingAudit.audit(any(), any())(any()))
            .thenReturn(Future.successful(AuditResult.Success))

          when(mockEngWalesAudit.audit(any(), any())(any()))
            .thenReturn(Future.successful(AuditResult.Success))

          when(mockConfig.audit(any()))
            .thenReturn(Map[String, String]())

          when(mockTransactionAuditor.transaction(any(), any(), any())(any()))
            .thenReturn(Future.successful(AuditResult.Success))

          mockAuditSuccess

          val request = postRequest(userNoMatchIncludingReferenceNumber)
          val result  = testController.post().apply(request).futureValue
          checkResponse(result, INTERNAL_SERVER_ERROR, empty)
        }

        "return InternalServerError when GRO returns 5xx when GatewayTimeout" in {
          when(mockLookupService.lookup()(any(), any(), any(), any()))
            .thenReturn(Future.failed(UpstreamErrorResponse("503", GATEWAY_TIMEOUT, GATEWAY_TIMEOUT)))
          mockAuditSuccess

          val request = postRequest(userNoMatchIncludingReferenceNumber)
          val result  = testController.post().apply(request).futureValue
          checkResponse(result, INTERNAL_SERVER_ERROR, empty)
        }

        "return InternalServerError when GRO returns BadRequestException" in {
          when(mockLookupService.lookup()(any(), any(), any(), any()))
            .thenReturn(Future.failed(new BadRequestException("oops")))
          mockAuditSuccess

          val request = postRequest(userNoMatchIncludingReferenceNumber)
          val result  = testController.post().apply(request).futureValue
          checkResponse(result, INTERNAL_SERVER_ERROR, empty)
        }

        "return ServiceUnavailable when GRO returns upstream 5xx NOT_IMPLEMENTED" in {
          when(mockLookupService.lookup()(any(), any(), any(), any()))
            .thenReturn(Future.failed(UpstreamErrorResponse("501", NOT_IMPLEMENTED, NOT_IMPLEMENTED)))
          mockAuditSuccess

          val request = postRequest(userNoMatchIncludingReferenceNumber)
          val result  = testController.post().apply(request).futureValue
          checkResponse(
            result,
            SERVICE_UNAVAILABLE,
            "GRO_CONNECTION_DOWN",
            "General Registry Office: England and Wales is unavailable"
          )
        }

        "return 200 false when GRO returns NotFoundException" in {
          when(mockLookupService.lookup()(any(), any(), any(), any()))
            .thenReturn(Future.failed(new NotFoundException("oops")))
          mockAuditSuccess

          val request = postRequest(userNoMatchIncludingReferenceNumber)
          val result  = testController.post().apply(request).futureValue
          checkResponse(result, OK, matchResponse = false)
        }

        "return 200 match false when GRO returns Forbidden 418 Teapot body" in {
          when(mockLookupService.lookup()(any(), any(), any(), any()))
            .thenReturn(Future.failed(UpstreamErrorResponse("418", FORBIDDEN, FORBIDDEN)))
          mockAuditSuccess

          val request = postRequest(userNoMatchIncludingReferenceNumber)
          val result  = testController.post().apply(request).futureValue
          checkResponse(result, OK, matchResponse = false)
        }

        "return 200 match false when GRO returns Forbidden 'Certificate invalid'" in {
          when(mockLookupService.lookup()(any(), any(), any(), any()))
            .thenReturn(
              Future.failed(UpstreamErrorResponse(MockErrorResponses.CERTIFICATE_INVALID.json, FORBIDDEN, FORBIDDEN))
            )
          mockAuditSuccess

          val request = postRequest(userNoMatchIncludingReferenceNumber)
          val result  = testController.post().apply(request).futureValue
          checkResponse(result, OK, matchResponse = false)
        }

        "return 500 when proxy returns InternalServerError" in {
          when(mockLookupService.lookup()(any(), any(), any(), any()))
            .thenReturn(Future.failed(new Exception()))
          mockAuditSuccess

          val request = postRequest(userNoMatchIncludingReferenceNumber)
          val result  = testController.post().apply(request).futureValue
          checkResponse(result, INTERNAL_SERVER_ERROR, empty)
        }

        "return 503 when GRO throws UpstreamInternalServerError" in {
          when(mockLookupService.lookup()(any(), any(), any(), any()))
            .thenReturn(Future.failed(UpstreamErrorResponse("502", INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR)))
          mockAuditSuccess
          mockReferenceResponse(
            UpstreamErrorResponse(MockErrorResponses.CONNECTION_DOWN.json, INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR)
          )
          val request = postRequest(userNoMatchIncludingReferenceNumber)
          val result  = testController.post().apply(request).futureValue
          checkResponse(
            result,
            SERVICE_UNAVAILABLE,
            "GRO_CONNECTION_DOWN",
            "General Registry Office: England and Wales is unavailable"
          )
        }

        "return 503 when GRO returns 503 GRO_CONNECTION_DOWN" in {
          when(mockLookupService.lookup()(any(), any(), any(), any()))
            .thenReturn(Future.failed(UpstreamErrorResponse("503", INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR)))
          mockAuditSuccess
          mockReferenceResponse(
            UpstreamErrorResponse(MockErrorResponses.CONNECTION_DOWN.json, SERVICE_UNAVAILABLE, SERVICE_UNAVAILABLE)
          )
          val request = postRequest(userNoMatchIncludingReferenceNumber)
          val result  = testController.post().apply(request).futureValue
          checkResponse(
            result,
            SERVICE_UNAVAILABLE,
            "GRO_CONNECTION_DOWN",
            "General Registry Office: England and Wales is unavailable"
          )
        }

        "return 503 with code GRO_CONNECTION_DOWN when BRMS GRO proxy is down." in {
          mockAuditSuccess
          mockReferenceResponse(new BadGatewayException(""))
          val request = postRequest(userNoMatchIncludingReferenceNumber)
          val result  = testController.post().apply(request).futureValue
          checkResponse(
            result,
            SERVICE_UNAVAILABLE,
            "GRO_CONNECTION_DOWN",
            "General Registry Office: England and Wales is unavailable"
          )
        }

        "return 503 with code GRO_CONNECTION_DOWN when BRMS GRO proxy is down and returns Upstream5xxResponse BAD_GATEWAY." in {
          mockAuditSuccess
          mockReferenceResponse(UpstreamErrorResponse("", BAD_GATEWAY, BAD_GATEWAY))
          val request = postRequest(userNoMatchIncludingReferenceNumber)
          val result  = testController.post().apply(request).futureValue
          checkResponse(
            result,
            SERVICE_UNAVAILABLE,
            "GRO_CONNECTION_DOWN",
            "General Registry Office: England and Wales is unavailable"
          )
        }

      }

      "receiving error response from Proxy for details request" should {

        "return 503 with code GRO_CONNECTION_DOWN when gro proxy is down and retuns bad gateway." in {
          mockAuditSuccess
          mockDetailsResponse(new BadGatewayException(""))
          val request = postRequest(userNoMatchExcludingReferenceKey)
          val result  = testController.post().apply(request).futureValue
          checkResponse(
            result,
            SERVICE_UNAVAILABLE,
            "GRO_CONNECTION_DOWN",
            "General Registry Office: England and Wales is unavailable"
          )
        }

        "return 503 with code GRO_CONNECTION_DOWN when gro proxy is down and retuns bad gateway Upstream5xxResponse." in {
          mockAuditSuccess
          mockDetailsResponse(UpstreamErrorResponse("", BAD_GATEWAY, BAD_GATEWAY))
          val request = postRequest(userNoMatchExcludingReferenceKey)
          val result  = testController.post().apply(request).futureValue
          checkResponse(
            result,
            SERVICE_UNAVAILABLE,
            "GRO_CONNECTION_DOWN",
            "General Registry Office: England and Wales is unavailable"
          )
        }

        "return InternalServerError when GRO returns 5xx when GatewayTimeout" in {
          when(mockLookupService.lookup()(any(), any(), any(), any()))
            .thenReturn(Future.failed(new Exception()))

          mockAuditSuccess

          val request = postRequest(userNoMatchExcludingReferenceKey)
          val result  = testController.post().apply(request).futureValue
          checkResponse(result, INTERNAL_SERVER_ERROR, empty)
        }

        "return InternalServerError when GRO returns BadRequestException" in {
          mockAuditSuccess
          mockDetailsResponse(new BadRequestException(""))
          val request = postRequest(userNoMatchExcludingReferenceKey)
          val result  = testController.post().apply(request).futureValue
          checkResponse(result, INTERNAL_SERVER_ERROR, empty)
        }

        "return ServiceUnavailable when GRO returns upstream 5xx NOT_IMPLEMENTED" in {
          when(mockLookupService.lookup()(any(), any(), any(), any()))
            .thenReturn(
              Future.failed(
                UpstreamErrorResponse(
                  MockErrorResponses.UNKNOWN_ERROR.json,
                  INTERNAL_SERVER_ERROR,
                  INTERNAL_SERVER_ERROR
                )
              )
            )

          mockAuditSuccess

          val request = postRequest(userNoMatchExcludingReferenceKey)
          val result  = testController.post().apply(request).futureValue
          checkResponse(
            result,
            SERVICE_UNAVAILABLE,
            "GRO_CONNECTION_DOWN",
            "General Registry Office: England and Wales is unavailable"
          )
        }

        "return 200 false when GRO returns NotFoundException" in {
          when(mockLookupService.lookup()(any(), any(), any(), any()))
            .thenReturn(Future.failed(new NotFoundException("")))
          mockAuditSuccess

          val request = postRequest(userNoMatchExcludingReferenceKey)
          val result  = testController.post().apply(request).futureValue
          checkResponse(result, OK, matchResponse = false)
        }

        "return 500 when proxy returns InternalServerError" in {
          when(mockLookupService.lookup()(any(), any(), any(), any()))
            .thenReturn(Future.failed(new Exception()))

          mockAuditSuccess

          val request = postRequest(userNoMatchExcludingReferenceKey)
          val result  = testController.post().apply(request).futureValue
          checkResponse(result, INTERNAL_SERVER_ERROR, empty)
        }

        "return 503 when GRO returns upstream InternalServerError" in {
          when(mockLookupService.lookup()(any(), any(), any(), any()))
            .thenReturn(
              Future.failed(
                UpstreamErrorResponse(
                  MockErrorResponses.CONNECTION_DOWN.json,
                  INTERNAL_SERVER_ERROR,
                  INTERNAL_SERVER_ERROR
                )
              )
            )
          mockAuditSuccess

          val request = postRequest(userNoMatchExcludingReferenceKey)
          val result  = testController.post().apply(request).futureValue
          checkResponse(
            result,
            SERVICE_UNAVAILABLE,
            "GRO_CONNECTION_DOWN",
            "General Registry Office: England and Wales is unavailable"
          )
        }

        "return 503 when GRO returns 503 GRO_CONNECTION_DOWN" in {
          mockAuditSuccess
          mockDetailsResponse(
            UpstreamErrorResponse(MockErrorResponses.CONNECTION_DOWN.json, SERVICE_UNAVAILABLE, SERVICE_UNAVAILABLE)
          )
          val request = postRequest(userNoMatchExcludingReferenceKey)
          val result  = testController.post().apply(request).futureValue
          checkResponse(
            result,
            SERVICE_UNAVAILABLE,
            "GRO_CONNECTION_DOWN",
            "General Registry Office: England and Wales is unavailable"
          )
        }

      }

    }

    "NRS" when {

      "POST with reference number" should {

        "return JSON response on successful reference match" in {
          when(mockLookupService.lookup()(any(), any(), any(), any()))
            .thenReturn(Future.successful(BirthMatchResponse(true)))

          mockAuditSuccess
          mockNrsReferenceResponse(validNrsJsonResponseObject)
          val request = postRequest(userMatchIncludingReferenceNumberKeyForScotland)
          val result  = testController.post().apply(request).futureValue
          checkResponse(result, OK, matchResponse = true)
        }

        "return JSON response false when date of birth is before 2009-07-01" in {
          when(mockLookupService.lookup()(any(), any(), any(), any()))
            .thenReturn(Future.successful(BirthMatchResponse()))
          mockAuditSuccess
          mockNrsReferenceResponse(nrsRecord20090630)
          val request = postRequest(userDob20090630)
          val result  = testController.post().apply(request).futureValue
          checkResponse(result, OK, matchResponse = false)
        }

        "return 200 when RCE is present" in {
          mockAuditSuccess
          mockNrsReferenceResponse(validNrsJsonResponseObjectRCE)
          val request = postRequest(userMatchIncludingReferenceNumberKeyForScotland)
          val result  = testController.post().apply(request).futureValue

          checkResponse(result, OK, matchResponse = false)
        }

        "return 200 matched false when record status is cancelled ie RCE -6" in {
          mockAuditSuccess
          mockNrsReferenceResponse(nrsRecord2017350001)
          val request = postRequest(Json.toJson(nrsRequestPayload2017350001))
          val result  = testController.post().apply(request).futureValue

          checkResponse(result, OK, matchResponse = false)
        }

        "return 200 response for UTF-8 reference request" in {
          when(mockLookupService.lookup()(any(), any(), any(), any()))
            .thenReturn(Future.successful(BirthMatchResponse(true)))

          mockAuditSuccess
          mockNrsReferenceResponse(validNrsJsonResponse2017350007)
          val request = postRequest(nrsReferenceRequestWithSpecialCharacters)
          val result  = testController.post().apply(request).futureValue
          checkResponse(result, OK, matchResponse = true)
        }

        "return JSON response on unsuccessful birthReferenceNumber match" in {
          when(mockLookupService.lookup()(any(), any(), any(), any()))
            .thenReturn(Future.successful(BirthMatchResponse()))

          mockAuditSuccess
          mockNrsReferenceResponse(UpstreamErrorResponse("BIRTH_REGISTRATION_NOT_FOUND", FORBIDDEN, FORBIDDEN))
          val request = postRequest(userNoMatchIncludingReferenceNumber)
          val result  = testController.post().apply(request).futureValue
          checkResponse(result, OK, matchResponse = false)
        }

        "return 200 false response when first name has special characters for unsuccessful BRN match." in {
          mockAuditSuccess
          mockNrsReferenceResponse(UpstreamErrorResponse("BIRTH_REGISTRATION_NOT_FOUND", FORBIDDEN, FORBIDDEN))
          val payload =
            Payload(Some("1234567890"), specialCharacters, None, "Test", LocalDate.now, BirthRegisterCountry.SCOTLAND)
          val request = postRequest(Json.toJson(payload))
          val result  = testController.post().apply(request).futureValue
          checkResponse(result, OK, matchResponse = false)
        }

      }

      "POST with child's details" should {

        "return 200 when RCE is present" in {
          mockAuditSuccess
          mockNrsDetailsResponse(validNrsJsonResponseObjectRCE)
          val request = postRequest(userMatchExcludingReferenceNumberKeyForScotland)
          val result  = testController.post().apply(request).futureValue

          checkResponse(result, OK, matchResponse = false)
        }

        "return 200 response on successful child detail match when multiple records are returned" in {
          mockAuditSuccess
          mockNrsDetailsResponse(nrsResponseWithMultiple)
          val request = postRequest(userMatchExcludingReferenceNumberKeyForScotland)
          val result  = testController.post().apply(request).futureValue
          checkResponse(result, OK, matchResponse = false)
        }

        "return 200 matched false when record status is cancelled ie RCE -6" in {
          mockAuditSuccess
          mockNrsDetailsResponse(nrsRecord2017350001)
          val request = postRequest(Json.toJson(nrsRequestPayloadWithoutBrn))
          val result  = testController.post().apply(request).futureValue

          checkResponse(result, OK, matchResponse = false)
        }

        "return 200 response when child details are not found" in {
          mockAuditSuccess
          mockNrsReferenceResponse(UpstreamErrorResponse("BIRTH_REGISTRATION_NOT_FOUND", FORBIDDEN, FORBIDDEN))
          val request = postRequest(userMatchExcludingReferenceNumberKeyForScotland)
          val result  = testController.post().apply(request).futureValue
          checkResponse(result, OK, matchResponse = false)
        }

        "return 200 false response when child details are not found when first name has special characters." in {
          mockAuditSuccess
          mockNrsReferenceResponse(UpstreamErrorResponse("BIRTH_REGISTRATION_NOT_FOUND", FORBIDDEN, FORBIDDEN))
          val payload = Payload(None, specialCharacters, None, "Test", LocalDate.now, BirthRegisterCountry.SCOTLAND)
          val request = postRequest(Json.toJson(payload))
          val result  = testController.post().apply(request).futureValue
          checkResponse(result, OK, matchResponse = false)
        }

        "return 200 response on when details contain valid UTF-8 special characters" in {
          when(mockLookupService.lookup()(any(), any(), any(), any()))
            .thenReturn(Future.successful(BirthMatchResponse(true)))

          mockAuditSuccess
          mockNrsDetailsResponse(validNrsJsonResponse2017350007)
          val request = postRequest(nrsRequestWithSpecialCharacters)
          val result  = testController.post().apply(request).futureValue
          checkResponse(result, OK, matchResponse = true)
        }

        "return 200 response on details match when single record is returned" in {
          mockAuditSuccess
          mockNrsDetailsResponse(validNrsJsonResponseObject)
          val request = postRequest(nrsDetailsRequestWithSingleMatch)
          val result  = testController.post().apply(request).futureValue
          checkResponse(result, OK, matchResponse = true)
        }

      }

      "receiving error response from NRS" should {

        "return InternalServerError when GRO returns 5xx when GatewayTimeout" in {
          when(mockLookupService.lookup()(any(), any(), any(), any()))
            .thenReturn(Future.failed(new GatewayTimeoutException("")))
          mockAuditSuccess

          val request = postRequest(userNoMatchExcludingReferenceKeyScotland)
          val result  = testController.post().apply(request).futureValue
          checkResponse(result, INTERNAL_SERVER_ERROR, empty)
        }

        "return 500 InternalServerError when NRS returns 400 INVALID_PAYLOAD" in {
          when(mockLookupService.lookup()(any(), any(), any(), any()))
            .thenReturn(Future.failed(new BadRequestException("INVALID_PAYLOAD")))
          mockAuditSuccess

          val request = postRequest(userNoMatchExcludingReferenceKeyScotland)
          val result  = testController.post().apply(request).futureValue
          checkResponse(result, INTERNAL_SERVER_ERROR, empty)
        }

        "return 500 InternalServerError when NRS returns 400 INVALID_HEADER" in {
          when(mockLookupService.lookup()(any(), any(), any(), any()))
            .thenReturn(Future.failed(new BadRequestException("INVALID_HEADER")))
          mockAuditSuccess

          val request = postRequest(userNoMatchExcludingReferenceKey)
          val result  = testController.post().apply(request).futureValue
          checkResponse(result, INTERNAL_SERVER_ERROR, empty)
        }

        "return 400 BadRequest when NRS returns 403 INVALID_DISTRICT_NUMBER" in {
          when(mockLookupService.lookup()(any(), any(), any(), any()))
            .thenReturn(Future.failed(UpstreamErrorResponse("INVALID_DISTRICT_NUMBER", FORBIDDEN, FORBIDDEN)))

          mockAuditSuccess

          val request = postRequest(userNoMatchExcludingReferenceKey)
          val result  = testController.post().apply(request).futureValue
          checkResponse(result, OK, matchResponse = false)
        }

        "return 500 InternalServerError when NRS returns 403 QUERY_LENGTH_EXCESSIVE" in {
          when(mockLookupService.lookup()(any(), any(), any(), any()))
            .thenReturn(Future.failed(UpstreamErrorResponse("QUERY_LENGTH_EXCESSIVE", FORBIDDEN, FORBIDDEN)))

          mockAuditSuccess

          val request = postRequest(userNoMatchExcludingReferenceKey)
          val result  = testController.post().apply(request).futureValue
          checkResponse(result, OK, matchResponse = false)
        }

        "return 503 when NRS returns 503 Service unavailable" in {
          when(mockLookupService.lookup()(any(), any(), any(), any()))
            .thenReturn(
              Future.failed(
                UpstreamErrorResponse(
                  MockErrorResponses.NRS_CONNECTION_DOWN.json,
                  SERVICE_UNAVAILABLE,
                  SERVICE_UNAVAILABLE
                )
              )
            )
          mockAuditSuccess

          val request = postRequest(userNoMatchScotlandExcludingReferenceKey)
          val result  = testController.post().apply(request).futureValue
          checkResponse(
            result,
            SERVICE_UNAVAILABLE,
            "NRS_CONNECTION_DOWN",
            "National Records Scotland: Scotland is unavailable"
          )
        }

        "return 503 when DES returns 502 BAD_GATEWAY" in {
          when(mockLookupService.lookup()(any(), any(), any(), any()))
            .thenReturn(Future.failed(new BadGatewayException("")))
          mockAuditSuccess

          val request = postRequest(userNoMatchExcludingReferenceKeyScotland)
          val result  = testController.post().apply(request).futureValue
          checkResponse(result, SERVICE_UNAVAILABLE, "DES_CONNECTION_DOWN", "DES is unavailable")
        }

        "return 503 SERVICE_UNAVAILABLE when DES returns 502 BAD_GATEWAY Upstream5xxResponse" in {
          when(mockLookupService.lookup()(any(), any(), any(), any()))
            .thenReturn(Future.failed(UpstreamErrorResponse("", BAD_GATEWAY, BAD_GATEWAY)))
          mockAuditSuccess

          val request = postRequest(userNoMatchExcludingReferenceKeyScotland)
          val result  = testController.post().apply(request).futureValue
          checkResponse(result, SERVICE_UNAVAILABLE, "DES_CONNECTION_DOWN", "DES is unavailable")
        }

      }

    }

    "GRO-NI" should {

      "return 200 false if request contains Northern Ireland" in {
        when(mockLookupService.lookup()(any(), any(), any(), any()))
          .thenReturn(Future.successful(BirthMatchResponse()))
        mockAuditSuccess
        mockGroNiReferenceResponse(new NotImplementedException(""))
        val request = postRequest(userWhereBirthRegisteredNI)
        val result  = testController.post().apply(request).futureValue
        checkResponse(result, OK, matchResponse = false)
      }

      "calls getReference when GRONIFeature is enabled" in {
        mockAuditSuccess
        mockGroNiReferenceResponse(new NotImplementedException(""))
        val request = postRequest(userWhereBirthRegisteredNI)
        val result  = testController.post().apply(request).futureValue
        checkResponse(result, OK, matchResponse = false)
      }

      "calls getDetails when GRONIFeature is enabled" in {
        mockAuditSuccess
        mockGroNiDetailsResponse(new NotImplementedException(""))
        val request = postRequest(userWhereBirthRegisteredNI)
        val result  = testController.post().apply(request).futureValue
        checkResponse(result, OK, matchResponse = false)
      }

    }

  }

}
