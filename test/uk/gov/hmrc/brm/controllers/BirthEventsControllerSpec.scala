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
import org.scalatest.mock.MockitoSugar
import org.scalatest.{BeforeAndAfter, TestData}
import org.scalatestplus.play.OneAppPerTest
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.test.Helpers._
import uk.gov.hmrc.brm.audit.BRMAudit
import uk.gov.hmrc.brm.implicits.Implicits.AuditFactory
import uk.gov.hmrc.brm.models.brm.Payload
import uk.gov.hmrc.brm.services.LookupService
import uk.gov.hmrc.brm.utils.Mocks._
import uk.gov.hmrc.brm.utils.{BaseUnitSpec, BirthRegisterCountry, MockErrorResponses}
import uk.gov.hmrc.play.http._
import uk.gov.hmrc.play.test.UnitSpec

class BirthEventsControllerSpec
    extends UnitSpec
    with MockitoSugar
    with OneAppPerTest
    with BeforeAndAfter with BaseUnitSpec {

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
        mockAuditSuccess
        val request = postRequest(userNoMatchExcludingReferenceValue)
        val result = await(MockController.post().apply(request))
        checkResponse(result,BAD_REQUEST, MockErrorResponses.INVALID_BIRTH_REFERENCE_NUMBER.json)
      }

      "return response code 400 if request contains birthReferenceNumber with invalid characters" in {
        mockAuditSuccess
        val request = postRequest(userNoMatchIncludingInvalidData)
        val result = await(MockController.post().apply(request))
        checkResponse(result,BAD_REQUEST, MockErrorResponses.INVALID_BIRTH_REFERENCE_NUMBER.json)
      }

      for (scenario <- referenceNumberScenario) {
        s"${scenario("description")}" in {
          mockAuditSuccess
          val request = postRequest(userInvalidReference(scenario("country").toString, scenario("referenceNumber").toString))
          val result = await(MockController.post().apply(request))
          var response =  scenario("responseCode")
          checkResponse(result,response.asInstanceOf[Int], MockErrorResponses.INVALID_BIRTH_REFERENCE_NUMBER.json)

        }
      }

    }

    "validate firstName" should {

      "return response code 400 if request contains missing firstName key" in {
        mockAuditSuccess
        val request = postRequest(userNoMatchExcludingFirstNameKey)
        val result = await(MockController.post().apply(request))
        checkResponse(result,BAD_REQUEST, MockErrorResponses.BAD_REQUEST.json)
      }

      "return response code 400 if request contains missing firstName value" in {
        mockAuditSuccess
        val request = postRequest(userNoMatchExcludingfirstNameValue)
        val result = await(MockController.post().apply(request))
        checkResponse(result,BAD_REQUEST, MockErrorResponses.INVALID_FIRSTNAME.json)
      }

      "return response code 400 if request contains special characters in firstName" in {
        mockAuditSuccess
        val request = postRequest(firstNameWithSpecialCharacters)
        val result = await(MockController.post().apply(request))
        checkResponse(result,BAD_REQUEST, MockErrorResponses.INVALID_FIRSTNAME.json)
      }

      "return response code 400 if request contains more than 250 characters in firstName" in {
        mockAuditSuccess
        val request = postRequest(firstNameWithMoreThan250Characters)
        val result = await(MockController.post().apply(request))
        checkResponse(result,BAD_REQUEST, MockErrorResponses.INVALID_FIRSTNAME.json)
      }

    }

    "validate lastName" should {

      "return response code 400 if request contains missing lastName key" in {
        mockAuditSuccess
        val request = postRequest(userNoMatchExcludinglastNameKey)
        val result = await(MockController.post().apply(request))
        checkResponse(result,BAD_REQUEST, MockErrorResponses.BAD_REQUEST.json)
      }

      "return response code 400 if request contains missing lastName value" in {
        mockAuditSuccess

        val request = postRequest(userNoMatchExcludinglastNameValue)
        val result = await(MockController.post().apply(request))

        checkResponse(result,BAD_REQUEST, MockErrorResponses.INVALID_LASTNAME.json)
      }

      "return response code 400 if request contains special character in lastName value" in {
        mockAuditSuccess
        val request = postRequest(lastNameWithSpecialCharacters)
        val result = await(MockController.post().apply(request))

        checkResponse(result,BAD_REQUEST, MockErrorResponses.INVALID_LASTNAME.json)
      }

      "return response code 400 if request contains more than 250 character in lastName value" in {
        mockAuditSuccess
        val request = postRequest(lastNameWithMoreThan250Characters)
        val result = await(MockController.post().apply(request))
        checkResponse(result,BAD_REQUEST, MockErrorResponses.INVALID_LASTNAME.json)
      }

    }

    "validate invalid dateOfBirth" should {

      "return response code 400 if request contains missing dateOfBirth key" in {
        mockAuditSuccess
        val request = postRequest(userNoMatchExcludingDateOfBirthKey)
        val result = await(MockController.post().apply(request))
        checkResponse(result,BAD_REQUEST, MockErrorResponses.BAD_REQUEST.json)
      }

      "return response code 400 if request contains missing dateOfBirth value" in {
        mockAuditSuccess
        val request = postRequest(userNoMatchExcludingDateOfBirthValue)
        val result = await(MockController.post().apply(request))
        checkResponse(result,BAD_REQUEST, MockErrorResponses.INVALID_DATE_OF_BIRTH.json)
      }

      "return response code 400 if request contains invalid dateOfBirth format" in {
        mockAuditSuccess
        val request = postRequest(userInvalidDOBFormat)
        val result = await(MockController.post().apply(request))
        checkResponse(result,BAD_REQUEST, MockErrorResponses.INVALID_DATE_OF_BIRTH.json)

      }

    }

    "validate whereBirthRegistered" should {

      "return 200 if request contains camel case where birth registered" in {
        mockAuditSuccess
        mockReferenceResponse(groJsonResponseObject)
        val request = postRequest(userNoMatchIncludingReferenceNumberCamelCase)
        val result = await(MockController.post().apply(request))
        checkResponse(result,OK, false)
      }

      "return response code 400 if request contains missing whereBirthRegistered key" in {
        mockAuditSuccess
        val request = postRequest(userNoMatchExcludingWhereBirthRegisteredKey)
        val result = await(MockController.post().apply(request))
        checkResponse(result,BAD_REQUEST, MockErrorResponses.BAD_REQUEST.json)
      }

      "return response code 403 if request contains missing whereBirthRegistered value" in {
        mockAuditSuccess
        val request = postRequest(userNoMatchExcludingWhereBirthRegisteredValue)
        val result = await(MockController.post().apply(request))
        checkResponse(result,FORBIDDEN, MockErrorResponses.INVALID_WHERE_BIRTH_REGISTERED.json)
      }

      "return response code 403 if request contains invalid whereBirthRegistered value" in {
        mockAuditSuccess
        val request = postRequest(userInvalidWhereBirthRegistered)
        val result = await(MockController.post().apply(request))
        checkResponse(result,FORBIDDEN, MockErrorResponses.INVALID_WHERE_BIRTH_REGISTERED.json)
      }

    }

    "GRO" when {

      "POST with reference number" should {

        "return JSON response true on successful reference match" in {
          mockAuditSuccess
          mockReferenceResponse(groJsonResponseObject400000001)
          val request = postRequest(user400000001)
          val result = await(MockController.post().apply(request))
          checkResponse(result,OK, true)
        }

        "return JSON response false when date of birth is before 2009-07-01" in {
          mockAuditSuccess
          val request = postRequest(userMatchIncludingReferenceNumber)
          val result = await(MockController.post().apply(request))
          checkResponse(result,OK, false)
        }

        "return JSON response on unsuccessful birthReferenceNumber match" in {
          mockAuditSuccess
          mockReferenceResponse(noJson)
          val request = postRequest(userNoMatchIncludingReferenceNumber)
          val result = await(MockController.post().apply(request))
          checkResponse(result,OK, false)
        }

        "return match false when GRO returns invalid json" in {
          mockAuditSuccess
          mockReferenceResponse(invalidResponse)
          val request = postRequest(userMatchIncludingReferenceNumber)
          val result = await(MockController.post().apply(request))
          checkResponse(result,OK, false)
        }

      }

      "POST by child's details" should {

        "return JSON response on successful child detail match when multiple records are returned" in {
          mockAuditSuccess
          mockDetailsResponse(groJsonResponseObjectMultipleWithMatch)
          val request = postRequest(userMultipleMatchExcludingReferenceKey)
          val result = await(MockController.post().apply(request))
          checkResponse(result,OK, false)
        }

        "return JSON response on unsuccessful child detail match" in {
          mockAuditSuccess
          mockDetailsResponse(Json.parse("[]"))
          val request = postRequest(userNoMatchExcludingReferenceKey)
          val result = await(MockController.post().apply(request))
          checkResponse(result,OK, false)
        }

        "return JSON response on when details contain valid UTF-8 special characters" in {
          mockAuditSuccess
          mockDetailsResponse(Json.parse("[]"))
          val request = postRequest(userNoMatchUTF8SpecialCharacters)
          val result = await(MockController.post().apply(request))
          checkResponse(result,OK, false)
        }

        "return JSON response true on successful child detail match" in {
          mockAuditSuccess

          mockDetailsResponse(groJsonResponseObjectCollection400000001)
          val request = postRequest(user400000001WithoutReferenceNumber)
          val result = await(MockController.post().apply(request))
          checkResponse(result,OK, true)
        }

        "return JSON response false when birth date is before 2009-07-01" in {
          mockAuditSuccess
          mockDetailsResponse(groJsonResponseObjectCollection)
          val request = postRequest(userMatchExcludingReferenceNumberKey)
          val result = await(MockController.post().apply(request))
          checkResponse(result,OK, false)
        }

      }

      "receiving error response from Proxy for reference number" should {

        "return InternalServerError when GRO returns Upstream5xxResponse GATEWAY_TIMEOUT" in {
          mockAuditSuccess
          mockReferenceResponse(Upstream5xxResponse(MockErrorResponses.GATEWAY_TIMEOUT.json, GATEWAY_TIMEOUT, GATEWAY_TIMEOUT))
          val request = postRequest(userNoMatchIncludingReferenceNumber)
          val result = await(MockController.post().apply(request))
          checkResponse(result,INTERNAL_SERVER_ERROR, empty)
        }


        "return InternalServerError when GRO returns 5xx when GatewayTimeout" in {
          mockAuditSuccess
          mockReferenceResponse(new GatewayTimeoutException("502"))

          val request = postRequest(userNoMatchIncludingReferenceNumber)
          val result = await(MockController.post().apply(request))
          checkResponse(result,INTERNAL_SERVER_ERROR, empty)
        }

        "return InternalServerError when GRO returns BadRequestException" in {
          mockAuditSuccess
          mockReferenceResponse(new BadRequestException(""))

          val request = postRequest(userNoMatchIncludingReferenceNumber)
          val result = await(MockController.post().apply(request))
          checkResponse(result,INTERNAL_SERVER_ERROR, empty)
        }

        "return InternalServerError when GRO returns upstream 5xx NOT_IMPLEMENTED" in {
          mockAuditSuccess
          mockReferenceResponse(Upstream5xxResponse(MockErrorResponses.UNKNOWN_ERROR.json, NOT_IMPLEMENTED, NOT_IMPLEMENTED))
          val request = postRequest(userNoMatchIncludingReferenceNumber)
          val result = await(MockController.post().apply(request))
          checkResponse(result,INTERNAL_SERVER_ERROR, empty)
        }


        "return 200 false when GRO returns NotFoundException" in {
          mockAuditSuccess
          mockReferenceResponse(new NotFoundException(""))
          val request = postRequest(userNoMatchIncludingReferenceNumber)
          val result = await(MockController.post().apply(request))
          checkResponse(result,OK, false)
        }

        "return 200 match false when GRO returns Forbidden 418 Teapot body" in {
          mockAuditSuccess
          mockReferenceResponse(Upstream4xxResponse(MockErrorResponses.TEAPOT.json, FORBIDDEN, FORBIDDEN))
          val request = postRequest(userNoMatchIncludingReferenceNumber)
          val result = await(MockController.post().apply(request))
          checkResponse(result,OK, false)
        }

        "return 200 match false when GRO returns Forbidden 'Certificate invalid'" in {
          mockAuditSuccess
          mockReferenceResponse(Upstream4xxResponse(MockErrorResponses.CERTIFICATE_INVALID.json, FORBIDDEN, FORBIDDEN))
          val request = postRequest(userNoMatchIncludingReferenceNumber)
          val result = await(MockController.post().apply(request))
          checkResponse(result,OK, false)
        }

        "return 500 when proxy returns InternalServerError" in {
          mockAuditSuccess
          mockReferenceResponse(new Exception())
          val request = postRequest(userNoMatchIncludingReferenceNumber)
          val result = await(MockController.post().apply(request))
          checkResponse(result,INTERNAL_SERVER_ERROR, empty)
        }

        "return 503 when GRO throws UpstreamInternalServerError" in {
          mockAuditSuccess
          mockReferenceResponse(new Upstream5xxResponse(MockErrorResponses.CONNECTION_DOWN.json, INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR))
          val request = postRequest(userNoMatchIncludingReferenceNumber)
          val result = await(MockController.post().apply(request))
          checkResponse(result,SERVICE_UNAVAILABLE, "GRO_CONNECTION_DOWN","General Registry Office: England and Wales is unavailable")
        }

        "return 503 when GRO returns 503 GRO_CONNECTION_DOWN" in {
          mockAuditSuccess
          mockReferenceResponse(Upstream5xxResponse(MockErrorResponses.CONNECTION_DOWN.json, SERVICE_UNAVAILABLE, SERVICE_UNAVAILABLE))
          val request = postRequest(userNoMatchIncludingReferenceNumber)
          val result = await(MockController.post().apply(request))
          checkResponse(result,SERVICE_UNAVAILABLE, "GRO_CONNECTION_DOWN","General Registry Office: England and Wales is unavailable")
        }


        "return 503 with code GRO_CONNECTION_DOWN when BRMS GRO proxy is down." in {
          mockAuditSuccess
          mockReferenceResponse(new BadGatewayException(""))
          val request = postRequest(userNoMatchIncludingReferenceNumber)
          val result = await(MockController.post().apply(request))
          checkResponse(result,SERVICE_UNAVAILABLE, "GRO_CONNECTION_DOWN","General Registry Office: England and Wales is unavailable")
        }


        "return 503 with code GRO_CONNECTION_DOWN when BRMS GRO proxy is down and returns Upstream5xxResponse BAD_GATEWAY." in {
          mockAuditSuccess
          mockReferenceResponse(new Upstream5xxResponse("", BAD_GATEWAY, BAD_GATEWAY))
          val request = postRequest(userNoMatchIncludingReferenceNumber)
          val result = await(MockController.post().apply(request))
          checkResponse(result,SERVICE_UNAVAILABLE, "GRO_CONNECTION_DOWN","General Registry Office: England and Wales is unavailable")
        }

      }

      "receiving error response from Proxy for details request" should {

        "return 503 with code GRO_CONNECTION_DOWN when gro proxy is down and retuns bad gateway." in {
          mockAuditSuccess
          mockDetailsResponse(new BadGatewayException(""))
          val request = postRequest(userNoMatchExcludingReferenceKey)
          val result = await(MockController.post().apply(request))
          checkResponse(result,SERVICE_UNAVAILABLE, "GRO_CONNECTION_DOWN","General Registry Office: England and Wales is unavailable")
        }



        "return 503 with code GRO_CONNECTION_DOWN when gro proxy is down and retuns bad gateway Upstream5xxResponse." in {
          mockAuditSuccess
          mockDetailsResponse(new Upstream5xxResponse("", BAD_GATEWAY, BAD_GATEWAY))
          val request = postRequest(userNoMatchExcludingReferenceKey)
          val result = await(MockController.post().apply(request))
          checkResponse(result,SERVICE_UNAVAILABLE, "GRO_CONNECTION_DOWN","General Registry Office: England and Wales is unavailable")
        }



        "return InternalServerError when GRO returns 5xx when GatewayTimeout" in {
          mockAuditSuccess
          mockDetailsResponse(new GatewayTimeoutException(""))
          val request = postRequest(userNoMatchExcludingReferenceKey)
          val result = await(MockController.post().apply(request))
          checkResponse(result,INTERNAL_SERVER_ERROR, empty)
        }

        "return InternalServerError when GRO returns BadRequestException" in {
          mockAuditSuccess
          mockDetailsResponse(new BadRequestException(""))
          val request = postRequest(userNoMatchExcludingReferenceKey)
          val result = await(MockController.post().apply(request))
          checkResponse(result,INTERNAL_SERVER_ERROR, empty)
        }

        "return InternalServerError when GRO returns upstream 5xx NOT_IMPLEMENTED" in {
          mockAuditSuccess
          mockDetailsResponse(Upstream5xxResponse(MockErrorResponses.UNKNOWN_ERROR.json, NOT_IMPLEMENTED, NOT_IMPLEMENTED))
          val request = postRequest(userNoMatchExcludingReferenceKey)
          val result = await(MockController.post().apply(request))
          checkResponse(result,INTERNAL_SERVER_ERROR, empty)
        }

     
        "return 200 false when GRO returns NotFoundException" in {
          mockAuditSuccess
          mockDetailsResponse(new NotFoundException(""))
          val request = postRequest(userNoMatchExcludingReferenceKey)
          val result = await(MockController.post().apply(request))
          checkResponse(result,OK, false)
        }

        "return 500 when proxy returns InternalServerError" in {
          mockAuditSuccess
          mockDetailsResponse(new Exception())
          val request = postRequest(userNoMatchExcludingReferenceKey)
          val result = await(MockController.post().apply(request))
          checkResponse(result,INTERNAL_SERVER_ERROR, empty)
        }

        "return 503 when GRO returns upstream InternalServerError" in {
          mockAuditSuccess
          mockDetailsResponse(Upstream5xxResponse(MockErrorResponses.CONNECTION_DOWN.json, INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR))
          val request = postRequest(userNoMatchExcludingReferenceKey)
          val result = await(MockController.post().apply(request))
          checkResponse(result,SERVICE_UNAVAILABLE, "GRO_CONNECTION_DOWN","General Registry Office: England and Wales is unavailable")
        }

        "return 503 when GRO returns 503 GRO_CONNECTION_DOWN" in {
          mockAuditSuccess
          mockDetailsResponse(Upstream5xxResponse(MockErrorResponses.CONNECTION_DOWN.json, SERVICE_UNAVAILABLE, SERVICE_UNAVAILABLE))
          val request = postRequest(userNoMatchExcludingReferenceKey)
          val result = await(MockController.post().apply(request))
          checkResponse(result,SERVICE_UNAVAILABLE, "GRO_CONNECTION_DOWN","General Registry Office: England and Wales is unavailable")
        }

      }

    }

    "NRS" when {

      "POST with reference number" should {

        "return JSON response on successful reference match" in {
          mockAuditSuccess
          mockNrsReferenceResponse(validNrsJsonResponseObject)
          val request = postRequest(userMatchIncludingReferenceNumberKeyForScotland)
          val result = await(MockController.post().apply(request))
          checkResponse(result,OK, true)
        }

        "return JSON response false when date of birth is before 2009-07-01" in {
          mockAuditSuccess
          mockNrsReferenceResponse(nrsRecord20090630)
          val request = postRequest(userDob20090630)
          val result = await(MockController.post().apply(request))
          checkResponse(result,OK, false)
        }

        "return 200 when RCE is present" in {
          mockAuditSuccess
          mockNrsReferenceResponse(validNrsJsonResponseObjectRCE)
          val request = postRequest(userMatchIncludingReferenceNumberKeyForScotland)
          val result = await(MockController.post().apply(request))

          checkResponse(result,OK, false)
        }

        "return 200 matched false when record status is cancelled ie RCE -6" in {
          mockAuditSuccess
          mockNrsReferenceResponse(nrsRecord2017350001)
          val request = postRequest(Json.toJson(nrsRequestPayload2017350001))
          val result = await(MockController.post().apply(request))

          checkResponse(result,OK, false)
        }

        "return 200 response for UTF-8 reference request" in {
          mockAuditSuccess
          mockNrsReferenceResponse(validNrsJsonResponse2017350007)
          val request = postRequest(nrsReferenceRequestWithSpecialCharacters)
          val result = await(MockController.post().apply(request))
          checkResponse(result,OK, true)
        }

        "return JSON response on unsuccessful birthReferenceNumber match" in {
          mockAuditSuccess
          mockNrsReferenceResponse(new Upstream4xxResponse("BIRTH_REGISTRATION_NOT_FOUND", FORBIDDEN, FORBIDDEN))
          val request = postRequest(userNoMatchIncludingReferenceNumber)
          val result = await(MockController.post().apply(request))
          checkResponse(result,OK, false)
        }


        "return 200 false response when first name has special characters for unsuccessful BRN match." in {
          mockAuditSuccess
          mockNrsReferenceResponse(new Upstream4xxResponse("BIRTH_REGISTRATION_NOT_FOUND", FORBIDDEN, FORBIDDEN))
          var payload = Payload(Some("1234567890"), "ÀÁÂÃÄÅÆÇÈÉÊËÌÍ ÏÐÑÒÓÔÕÖ×ØÙÚÛÜÝÞßàáâãäåæçèéêëìíîïðñòóôõö÷øùúûüýþÿÀÁÂÃÄÅÆÇÈÉÊËÌÍ ÏÐÑÒÓÔÕÖ×ØÙÚÛÜÝÞßàáâãäåæçèéêëìíîïðñòóôõö÷øùú111111ÀÁÂÃÄÅÆÇÈÉÊËÌÍ ÏÐÑÒÓÔÕÖ×ØÙÚÛÜÝÞßàáâãäåæçèéêëìíîïðñòóôõö÷øùúûüýþÿÀÁÂÃÄÅÆÇÈÉÊËÌÍ ÏÐÑÒÓÔÕÖ×ØÙÚÛÜÝÞßàáâãäåæçèéêëìíîïðñòóôõö÷ø", "Test", LocalDate.now, BirthRegisterCountry.SCOTLAND)
          val request = postRequest(Json.toJson(payload))
          val result = await(MockController.post().apply(request))
          checkResponse(result,OK, false)
        }

      }

      "POST with child's details" should {

        "return 200 when RCE is present" in {
          mockAuditSuccess
          mockNrsDetailsResponse(validNrsJsonResponseObjectRCE)
          val request = postRequest(userMatchExcludingReferenceNumberKeyForScotland)
          val result = await(MockController.post().apply(request))

          checkResponse(result,OK, false)
        }

        "return 200 response on successful child detail match when multiple records are returned" in {
          mockAuditSuccess
          mockNrsDetailsResponse(nrsResponseWithMultiple)
          val request = postRequest(userMatchExcludingReferenceNumberKeyForScotland)
          val result = await(MockController.post().apply(request))
          checkResponse(result,OK, false)
        }


        "return 200 matched false when record status is cancelled ie RCE -6" in {
          mockAuditSuccess
          mockNrsDetailsResponse(nrsRecord2017350001)
          val request = postRequest(Json.toJson(nrsRequestPayloadWithoutBrn))
          val result = await(MockController.post().apply(request))

          checkResponse(result,OK, false)
        }

        "return 200 response when child details are not found" in {
          mockAuditSuccess
          mockNrsReferenceResponse(new Upstream4xxResponse("BIRTH_REGISTRATION_NOT_FOUND", FORBIDDEN, FORBIDDEN))
          val request = postRequest(userMatchExcludingReferenceNumberKeyForScotland)
          val result = await(MockController.post().apply(request))
          checkResponse(result,OK, false)
        }

        "return 200 false response when child details are not found when first name has special characters." in {
          mockAuditSuccess
          mockNrsReferenceResponse(new Upstream4xxResponse("BIRTH_REGISTRATION_NOT_FOUND", FORBIDDEN, FORBIDDEN))
          var payload = Payload(None, "ÀÁÂÃÄÅÆÇÈÉÊËÌÍ ÏÐÑÒÓÔÕÖ×ØÙÚÛÜÝÞßàáâãäåæçèéêëìíîïðñòóôõö÷øùúûüýþÿÀÁÂÃÄÅÆÇÈÉÊËÌÍ ÏÐÑÒÓÔÕÖ×ØÙÚÛÜÝÞßàáâãäåæçèéêëìíîïðñòóôõö÷øùú111111ÀÁÂÃÄÅÆÇÈÉÊËÌÍ ÏÐÑÒÓÔÕÖ×ØÙÚÛÜÝÞßàáâãäåæçèéêëìíîïðñòóôõö÷øùúûüýþÿÀÁÂÃÄÅÆÇÈÉÊËÌÍ ÏÐÑÒÓÔÕÖ×ØÙÚÛÜÝÞßàáâãäåæçèéêëìíîïðñòóôõö÷ø", "Test", LocalDate.now, BirthRegisterCountry.SCOTLAND)
          val request = postRequest(Json.toJson(payload))
          val result = await(MockController.post().apply(request))
          checkResponse(result,OK, false)
        }

        "return 200 response on when details contain valid UTF-8 special characters" in {
          mockAuditSuccess
          mockNrsDetailsResponse(validNrsJsonResponse2017350007)
          val request = postRequest(nrsRequestWithSpecialCharacters)
          val result = await(MockController.post().apply(request))
          checkResponse(result,OK, true)
        }

        "return 200 response on details match when single record is returned" in {
          mockAuditSuccess
          mockNrsDetailsResponse(validNrsJsonResponseObject)
          val request = postRequest(nrsDetailsRequestWithSingleMatch)
          val result = await(MockController.post().apply(request))
          checkResponse(result,OK, true)
        }

      }

      "receiving error response from NRS" should {


        "return InternalServerError when GRO returns 5xx when GatewayTimeout" in {
          mockAuditSuccess
          mockNrsDetailsResponse(new GatewayTimeoutException(""))
          val request = postRequest(userNoMatchExcludingReferenceKeyScotland)
          val result = await(MockController.post().apply(request))
          checkResponse(result,INTERNAL_SERVER_ERROR, empty)
        }

        "return 500 InternalServerError when NRS returns 400 INVALID_PAYLOAD" in {
          mockAuditSuccess
          mockNrsDetailsResponse(new BadRequestException("INVALID_PAYLOAD"))
          val request = postRequest(userNoMatchExcludingReferenceKeyScotland)
          val result = await(MockController.post().apply(request))
          checkResponse(result,INTERNAL_SERVER_ERROR, empty)
        }

        "return 500 InternalServerError when NRS returns 400 INVALID_HEADER" in {
          mockAuditSuccess
          mockNrsDetailsResponse(new BadRequestException("INVALID_HEADER"))
          val request = postRequest(userNoMatchExcludingReferenceKey)
          val result = await(MockController.post().apply(request))
          checkResponse(result,INTERNAL_SERVER_ERROR, empty)
        }

        "return 400 BadRequest when NRS returns 403 INVALID_DISTRICT_NUMBER" in {
          mockAuditSuccess
          mockNrsDetailsResponse(new Upstream4xxResponse("INVALID_DISTRICT_NUMBER", FORBIDDEN, FORBIDDEN))
          val request = postRequest(userNoMatchExcludingReferenceKey)
          val result = await(MockController.post().apply(request))
          checkResponse(result,OK, false)
        }

        "return 500 InternalServerError when NRS returns 403 QUERY_LENGTH_EXCESSIVE" in {
          mockAuditSuccess
          mockNrsDetailsResponse(new Upstream4xxResponse("QUERY_LENGTH_EXCESSIVE", FORBIDDEN, FORBIDDEN))
          val request = postRequest(userNoMatchExcludingReferenceKey)
          val result = await(MockController.post().apply(request))
          checkResponse(result,OK, false)
        }

        "return 503 when NRS returns 503 Service unavailable" in {
          mockAuditSuccess
          mockNrsDetailsResponse(new Upstream5xxResponse(MockErrorResponses.NRS_CONNECTION_DOWN.json, SERVICE_UNAVAILABLE, SERVICE_UNAVAILABLE))
          val request = postRequest(userNoMatchExcludingReferenceKey)
          val result = await(MockController.post().apply(request))
          checkResponse(result,SERVICE_UNAVAILABLE, "NRS_CONNECTION_DOWN","National Records Scotland: Scotland is unavailable")
        }

        "return 503 when DES returns 502 BAD_GATEWAY" in {
          mockAuditSuccess
          mockNrsDetailsResponse(new BadGatewayException(""))
          val request = postRequest(userNoMatchExcludingReferenceKeyScotland)
          val result = await(MockController.post().apply(request))
          checkResponse(result,SERVICE_UNAVAILABLE, "DES_CONNECTION_DOWN","DES is unavailable")
        }


        "return 503 SERVICE_UNAVAILABLE when DES returns 502 BAD_GATEWAY Upstream5xxResponse" in {
          mockAuditSuccess
          mockNrsDetailsResponse(new Upstream5xxResponse("", BAD_GATEWAY, BAD_GATEWAY))
          val request = postRequest(userNoMatchExcludingReferenceKeyScotland)
          val result = await(MockController.post().apply(request))
          checkResponse(result,SERVICE_UNAVAILABLE, "DES_CONNECTION_DOWN","DES is unavailable")
        }

      }

    }

    "GRO-NI" should {

      "return 200 false if request contains Northern Ireland" in {
        mockAuditSuccess
        mockGroNiReferenceResponse(new NotImplementedException(""))
        val request = postRequest(userWhereBirthRegisteredNI)
        val result = await(MockController.post().apply(request))
        checkResponse(result,OK, false)
      }

      "calls getReference when GRONIFeature is enabled" in {
        mockAuditSuccess
        mockGroNiReferenceResponse(new NotImplementedException(""))
        val request = postRequest(userWhereBirthRegisteredNI)
        val result = await(MockController.post().apply(request))
        checkResponse(result,OK, false)
      }

      "calls getDetails when GRONIFeature is enabled" in {
        mockAuditSuccess
        mockGroNiDetailsResponse(new NotImplementedException(""))
        val request = postRequest(userWhereBirthRegisteredNI)
        val result = await(MockController.post().apply(request))
        checkResponse(result,OK, false)
      }

    }

  }

}
