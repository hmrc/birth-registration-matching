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
import play.api.libs.json.{JsValue, Json}
import play.api.test.Helpers._
import play.api.test.{FakeApplication, FakeRequest}
import uk.gov.hmrc.brm.connectors.{BirthConnector, NirsConnector, NrsConnector}
import uk.gov.hmrc.brm.services.LookupService
import uk.gov.hmrc.brm.utils.JsonUtils
import uk.gov.hmrc.play.http.{BadRequestException, HttpResponse, NotFoundException, Upstream4xxResponse, Upstream5xxResponse}
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future

/**
  * Created by chrisianson on 26/07/16.
  */
class BirthEventsControllerSpec
  extends UnitSpec
  with WithFakeApplication
  with MockitoSugar
  with BeforeAndAfter {

  /**
    * - Should
    * - Wire up dependencies correctly
    * - return 200 with application/json type
    * - return JSON response of false on unsuccessful detail match
    * - return JSON response of true on successful detail match
    * - return JSON response of false on unsuccessful birthReferenceNumber match
    * - return JSON response of true on successful birthReferenceNumber match
    * - return response code 200 if request contains missing birthReferenceNumber key
    * - return response code 400 if request contains missing birthReferenceNumber value
    * - return response code 400 if request contains missing dateOfBirth key
    * - return response code 400 if request contains missing dateOfBirth value
    * - return response code 400 if request contains invalid dateOfBirth format
    * - return response code 400 if request contains missing firstName key
    * - return response code 400 if request contains missing firstName value
    * - return response code 400 if request contains missing lastName key
    * - return response code 400 if request contains missing lastName value
    * - return response code 400 if request contains missing whereBirthRegistered key
    * - return response code 400 if request contains missing whereBirthRegistered value
    * - return BadGateway when GRO returns 4xx
    * - return BadRequest when GRO returns 4xx BadRequest
    * - return GatewayTimeout when GRO returns 5xx when timeout
    * - return BadRequest when GRO returns BadRequestException
    * - return InternalServerError when GRO returns 5xx
    * - return match false when GRO returns invalid json
    * - return not match when GRO returns NOT FOUND
    * - return not match when GRO returns NOT FOUND response
    * - return not match when GRO returns UNAUTHORIZED response
    * - return InternalServerError when GRO returns 5xx response
    * - return matched value of true when the dateOfBirth is greater than 2009-07-01 and the gro record matches
    * - return matched value of true when the dateOfBirth is equal to 2009-07-01 and the gro record matches
    * - return matched value of false when the dateOfBirth is invalid and the gro record matches
    * - return matched value of false when the dateOfBirth is one day earlier than 2009-07-01 and the gro record matches
    * */

  val groJsonResponseObject = JsonUtils.getJsonFromFile("500035710")
  val groJsonResponseObject20090701 = JsonUtils.getJsonFromFile("2009-07-01")
  val groJsonResponseObject20090630 = JsonUtils.getJsonFromFile("2009-06-30")

  val invalidResponse = Json.parse(
    """
      |[]
    """.stripMargin)

  val noJson = Json.parse(
    s"""{
        }
    """.stripMargin)

  val userWhereBirthRegisteredNI = Json.parse(
    s"""
       |{
       | "birthReferenceNumber" : "123456789",
       | "firstName" : "Chris",
       | "lastName" : "Jones",
       | "dateOfBirth" : "2012-02-16",
       | "whereBirthRegistered" : "northern ireland"
       |}
    """.stripMargin)

  val userWhereBirthRegisteredScotland = Json.parse(
    s"""
       |{
       | "birthReferenceNumber" : "123456789",
       | "firstName" : "Chris",
       | "lastName" : "Jones",
       | "dateOfBirth" : "2012-02-16",
       | "whereBirthRegistered" : "scotland"
       |}
    """.stripMargin)

  val userNoMatchExcludingReferenceKey = Json.parse(
    s"""
       |{
       | "firstName" : "Chris",
       | "lastName" : "Jones",
       | "dateOfBirth" : "2012-02-16",
       | "whereBirthRegistered" : "england"
       |}
    """.stripMargin)

  val userNoMatchExcludingReferenceValue = Json.parse(
    s"""
       |{
       | "firstName" : "Chris",
       | "lastName" : "Jones",
       | "dateOfBirth" : "2012-02-16",
       | "birthReferenceNumber" : "",
       | "whereBirthRegistered" : "england"
       |}
    """.stripMargin)

  val userNoMatchExcludingFirstNameKey = Json.parse(
    s"""
       |{
       |"lastName" : "Jones",
       |"dateOfBirth" : "2012-04-18",
       |"birthReferenceNumber" : "123456789"
       |}
     """.stripMargin)

  val userNoMatchExcludingReferenceNumber = Json.parse(
    s"""
       |{
       | "firstName" : "Chris",
       | "lastName" : "Jones",
       | "dateOfBirth" : "2012-02-16",
       | "birthReferenceNumber" : "",
       | "whereBirthRegistered" : "england"
       |}
    """.stripMargin)

  val userNoMatchIncludingReferenceNumber = Json.parse(
    s"""
       |{
       | "firstName" : "Chris",
       | "lastName" : "Jones",
       | "dateOfBirth" : "2012-08-03",
       | "birthReferenceNumber" : "123456789",
       | "whereBirthRegistered" : "wales"
       |}
    """.stripMargin)

  val userNoMatchIncludingReferenceNumberCamelCase = Json.parse(
    s"""
       |{
       | "firstName" : "Chris",
       | "lastName" : "Jones",
       | "dateOfBirth" : "2012-08-03",
       | "birthReferenceNumber" : "123456789",
       | "whereBirthRegistered" : "WalEs"
       |}
    """.stripMargin)

  val userNoMatchIncludingReferenceCharacters = Json.parse(
    s"""
       |{
       | "firstName" : "Chris",
       | "lastName" : "Jones",
       | "dateOfBirth" : "2012-08-03",
       | "birthReferenceNumber" : "ab1_-CD263",
       | "whereBirthRegistered" : "wales"
       |}
    """.stripMargin)

  val userNoMatchIncludingInvalidData = Json.parse(
    s"""
       |{
       | "firstName" : "Chris",
       | "lastName" : "Jones",
       | "dateOfBirth" : "2012-08-03",
       | "birthReferenceNumber" : "123*34)",
       | "whereBirthRegistered" : "wales"
       |}
    """.stripMargin)

  val userMatchExcludingReferenceNumber = Json.parse(
    s"""
       |{
       | "firstName" : "Adam TEST",
       | "lastName" : "SMITH",
       | "dateOfBirth" : "2012-02-16",
       | "birthReferenceNumber" : "",
       | "whereBirthRegistered" : "england"
       |}
    """.stripMargin)

  val userMatchIncludingReferenceNumber = Json.parse(
    s"""
       |{
       | "firstName" : "Adam TEST",
       | "lastName" : "SMITH",
       | "dateOfBirth" : "2012-02-16",
       | "birthReferenceNumber" : "500035710",
       | "whereBirthRegistered" : "england"
       |}
    """.stripMargin)

  val userMatchCountryNameInMixCase = Json.parse(
    s"""
       |{
       | "firstName" : "Adam TEST",
       | "lastName" : "SMITH",
       | "dateOfBirth" : "2012-02-16",
       | "birthReferenceNumber" : "500035710",
       | "whereBirthRegistered" : "EngLand"
       |}
    """.stripMargin)

  val userNoMatchExcludingDateOfBirthKey = Json.parse(
    s"""
       |{
       | "firstName" : "Adam TEST",
       | "lastName" : "SMITH",
       | "birthReferenceNumber" : "500035710",
       | "whereBirthRegistered" : "england"
       |}
    """.stripMargin)

  val userNoMatchExcludingDateOfBirthValue = Json.parse(
    s"""
       |{
       | "firstName" : "Adam TEST",
       | "lastName" : "SMITH",
       | "dateOfBirth" : "",
       | "birthReferenceNumber" : "500035710",
       | "whereBirthRegistered" : "england"
       |}
    """.stripMargin)

  val userNoMatchExcludingfirstNameKey = Json.parse(
    s"""
       |{
       |"lastName" : "Smith",
       |"dateOrBirth" : "2012-12-17",
       |"birthReferenceNumber" : "123456789"
       |}
     """.stripMargin)


  val userInvalidWhereBirthRegistered = Json.parse(
    s"""
       |{
       |"firstname" : "Adam TEST",
       |"lastName" : "SMITH",
       |"dateOfBirth" : "2012-11-16",
       |"birthReferenceNumber" : "500035710",
       |"whereBirthRegistered": "fiji"
       |}
     """.stripMargin)

  val userNoMatchExcludingfirstNameValue = Json.parse(
    s"""
       |{
       |"firstname" : "",
       |"lastName" : "Jones",
       |"dateOfBirth" : "2012-11-16",
       |"birthReferenceNumber" : "123456789"
       |}
     """.stripMargin)

  val userNoMatchExcludinglastNameKey = Json.parse(
    s"""
       |{
       |"firstname" : "John",
       |"dateOrBirth" : "2012-12-17",
       |"birthReferenceNumber" : "123456789"
       |}
     """.stripMargin)

  val userNoMatchExcludinglastNameValue = Json.parse(
    s"""
       |{
       |"firstname" : "John",
       |"lastName" : "",
       |"dateOfBirth" : "2012-11-16",
       |"birthReferenceNumber" : "123456789"
       |}
     """.stripMargin)


  val userNoMatchExcludingWhereBirthRegisteredKey = Json.parse(
    s"""
       |{
       |"firstname" : "Manish",
       |"lastName" : "Varma",
       |"dateOfBirth" : "2012-11-16",
       |"birthReferenceNumber" : "123456789"
       |
       |}
     """.stripMargin)

  val userNoMatchExcludingWhereBirthRegisteredValue = Json.parse(
    s"""
       |{
       |"firstname" : "John",
       |"lastName" : "Jones",
       |"dateOfBirth" : "2012-11-16",
       |"birthReferenceNumber" : "123456789",
       |"whereBirthRegistered" : ""
       |}
     """.stripMargin)

  val userInvalidDOB = Json.parse(
    s"""
       |{
       | "firstName" : "Adam TEST",
       | "lastName" : "SMITH",
       | "dateOfBirth" : "2006-02-16",
       | "birthReferenceNumber" : "500035710",
       | "whereBirthRegistered" : "england"
       |}
    """.stripMargin)

  val userInvalidDOBFormat = Json.parse(
    s"""
       |{
       | "firstName" : "Adam TEST",
       | "lastName" : "SMITH",
       | "dateOfBirth" : "1234567890",
       | "birthReferenceNumber" : "500035710",
       | "whereBirthRegistered" : "england"
       |}
    """.stripMargin)

  val userValidDOB = Json.parse(
    s"""
       |{
       | "firstName" : "Adam TEST",
       | "lastName" : "SMITH",
       | "dateOfBirth" : "2012-02-16",
       | "birthReferenceNumber" : "500035710",
       | "whereBirthRegistered" : "england"
       |}
    """.stripMargin)

  val userValidDOB20090701 = Json.parse(
    s"""
       |{
       | "firstName" : "Adam TEST",
       | "lastName" : "SMITH",
       | "dateOfBirth" : "2009-07-01",
       | "birthReferenceNumber" : "500035710",
       | "whereBirthRegistered" : "england"
       |}
    """.stripMargin)

  val userValidDOB20090630 = Json.parse(
    s"""
       |{
       | "firstName" : "Adam TEST",
       | "lastName" : "SMITH",
       | "dateOfBirth" : "2009-06-30",
       | "birthReferenceNumber" : "500035710",
       | "whereBirthRegistered" : "england"
       |}
    """.stripMargin)

  def postRequest(v: JsValue): FakeRequest[JsValue] = FakeRequest("POST", "/api/v0/events/birth")
    .withHeaders((ACCEPT, "application/vnd.hmrc.1.0+json"), ("Audit-Source", "DFS"))
    .withBody(v)

  val mockConnector = mock[BirthConnector]

  object MockLookupService extends LookupService {
    override val groConnector = mockConnector
    override val nirsConnector = NirsConnector
    override val nrsConnector = NrsConnector
  }

  object MockController extends BirthEventsController {
    override val service = MockLookupService
  }

  def httpResponse(js: JsValue) = HttpResponse.apply(200, Some(js))
  def httpResponse(responseCode: Int) = HttpResponse.apply(responseCode)

  var config: Map[String, _] = Map(
    "microservice.services.birth-registration-matching.validateDobForGro" -> true
  )

  "BirthEventsController" when {

    "initialising" should {

      "wire up dependencies correctly" in {
        BirthEventsController.service shouldBe a[LookupService]
      }

    }

    "POST /birth-registration-matching-proxy/match" should {

      "return JSON response of false on unsuccessful detail match" in {
        when(MockController.service.groConnector.getReference(Matchers.any())(Matchers.any())).thenReturn(Future.successful(httpResponse(groJsonResponseObject)))
        val request = postRequest(userNoMatchIncludingReferenceNumber)
        val result = MockController.post().apply(request)
        status(result) shouldBe OK
        (contentAsJson(result) \ "matched").as[Boolean] shouldBe false
        contentType(result).get shouldBe "application/json"
        header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
      }

      "return 200 JSON response of true on successful detail match with country in mix case" in {
        when(mockConnector.getReference(Matchers.any())(Matchers.any())).thenReturn(Future.successful(httpResponse(groJsonResponseObject)))
        val request = postRequest(userMatchCountryNameInMixCase)
        val result = MockController.post().apply(request)
        (contentAsJson(result) \ "matched").as[Boolean] shouldBe true
        contentType(result).get shouldBe "application/json"
        header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
      }

      "return JSON response of true on successful detail match" in {
        when(MockController.service.groConnector.getReference(Matchers.any())(Matchers.any())).thenReturn(Future.successful(httpResponse(groJsonResponseObject)))
        val request = postRequest(userMatchIncludingReferenceNumber)
        val result = MockController.post().apply(request)
        status(result) shouldBe OK
        (contentAsJson(result) \ "matched").as[Boolean] shouldBe true
        contentType(result).get shouldBe "application/json"
        header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
      }

      "return JSON response of false on unsuccessful birthReferenceNumber match" in {
        when(MockController.service.groConnector.getReference(Matchers.any())(Matchers.any())).thenReturn(Future.successful(httpResponse(noJson)))
        val request = postRequest(userNoMatchIncludingReferenceNumber)
        val result = MockController.post().apply(request)
        (contentAsJson(result) \ "matched").as[Boolean] shouldBe false
        status(result) shouldBe OK
        contentType(result).get shouldBe "application/json"
        header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
      }

      "return JSON response of true on successful birthReferenceNumber match" in {
        when(MockController.service.groConnector.getReference(Matchers.any())(Matchers.any())).thenReturn(Future.successful(httpResponse(groJsonResponseObject)))
        val request = postRequest(userMatchIncludingReferenceNumber)
        val result = MockController.post().apply(request)
        (contentAsJson(result) \ "matched").as[Boolean] shouldBe true
        status(result) shouldBe OK
        contentType(result).get shouldBe "application/json"
        header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
      }

      "return response code 200 if request contains missing birthReferenceNumber key" in {
        when(MockController.service.groConnector.getReference(Matchers.any())(Matchers.any())).thenReturn(Future.successful(httpResponse(groJsonResponseObject)))
        val request = postRequest(userNoMatchExcludingReferenceKey)
        val result = MockController.post().apply(request)
        status(result) shouldBe OK
        contentType(result).get shouldBe "application/json"
        header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
        (contentAsJson(result) \ "matched").as[Boolean] shouldBe false
      }

      "return match false when GRO returns invalid json" in {
        when(MockController.service.groConnector.getReference(Matchers.any())(Matchers.any())).thenReturn(Future.successful(httpResponse(invalidResponse)))
        val request = postRequest(userMatchIncludingReferenceNumber)
        val result = MockController.post().apply(request)
        status(result) shouldBe OK
        (contentAsJson(result) \ "matched").as[Boolean] shouldBe false
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
        when(MockController.service.groConnector.getReference(Matchers.any())(Matchers.any())).thenReturn(Future.successful(httpResponse(groJsonResponseObject)))
        val request = postRequest(userMatchExcludingReferenceNumber)
        val result = MockController.post().apply(request)
        status(result) shouldBe BAD_REQUEST
        contentType(result).get shouldBe "application/json"
        header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
      }

      "return response code 400 if request contains birthReferenceNumber with invalid characters" in {
        when(MockController.service.groConnector.getReference(Matchers.any())(Matchers.any())).thenReturn(Future.successful(httpResponse(groJsonResponseObject)))
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
        bodyOf(result) shouldBe empty
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

    "validating date of birth with GRO switch" should {

      "return matched value of true when the dateOfBirth is greater than 2009-07-01 and the gro record matches" in {
        running(FakeApplication(additionalConfiguration = config)) {
          when(MockController.service.groConnector.getReference(Matchers.any())(Matchers.any())).thenReturn(Future.successful(httpResponse(groJsonResponseObject)))
          val request = postRequest(userValidDOB)
          val result = MockController.post().apply(request)
          status(result) shouldBe OK
          (contentAsJson(result) \ "matched").as[Boolean] shouldBe true
          header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
        }
      }

      "return matched value of true when the dateOfBirth is equal to 2009-07-01 and the gro record matches" in {
        running(FakeApplication(additionalConfiguration = config)) {
          when(MockController.service.groConnector.getReference(Matchers.any())(Matchers.any())).thenReturn(Future.successful(httpResponse(groJsonResponseObject20090701)))
          val request = postRequest(userValidDOB20090701)
          val result = MockController.post().apply(request)
          status(result) shouldBe OK
          (contentAsJson(result) \ "matched").as[Boolean] shouldBe true
          header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
        }
      }

      "return matched value of false when the dateOfBirth is invalid and the gro record matches" in {
        running(FakeApplication(additionalConfiguration = config)) {
          when(MockController.service.groConnector.getReference(Matchers.any())(Matchers.any())).thenReturn(Future.successful(httpResponse(groJsonResponseObject)))
          val request = postRequest(userInvalidDOB)
          val result = MockController.post().apply(request)
          status(result) shouldBe OK
          (contentAsJson(result) \ "matched").as[Boolean] shouldBe false
          header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
        }
      }

      "return matched value of false when the dateOfBirth is one day earlier than 2009-07-01 and the gro record matches" in {
        running(FakeApplication(additionalConfiguration = config)) {
          when(MockController.service.groConnector.getReference(Matchers.any())(Matchers.any())).thenReturn(Future.successful(httpResponse(groJsonResponseObject20090630)))
          val request = postRequest(userValidDOB20090630)
          val result = await(MockController.post().apply(request))
          status(result) shouldBe OK
          (contentAsJson(result) \ "matched").as[Boolean] shouldBe false
          header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
        }
      }

    }

  }

}
