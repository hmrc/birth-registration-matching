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
import org.scalatest.mock.MockitoSugar
import play.api.libs.json.{JsValue, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.brm.connectors.BirthConnector
import uk.gov.hmrc.brm.services.LookupService
import uk.gov.hmrc.brm.utils.JsonUtils

import uk.gov.hmrc.play.http.{HttpResponse, Upstream4xxResponse, Upstream5xxResponse}
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import uk.gov.hmrc.brm.BRMFakeApplication

import scala.concurrent.Future

/**
  * Created by chrisianson on 26/07/16.
  */
class BirthEventsControllerSpec extends UnitSpec with WithFakeApplication with MockitoSugar {

  /**
    * - Should
    * - Wire up dependencies correctly
    * - Return 200 with application/json type
    * - Return JSON response of false on unsuccessful detail match
    * - Return JSON response of true on successful detail match
    * - Return JSON response of false on unsuccessful birthReferenceNumber  match
    * - Return JSON response of true on successful birthReferenceNumber  match
    * - Return 200 if request contains missing birthReferenceNumber key
    * - Return 200 if request contains missing birthReferenceNumber value
    * - Return 400 if request contains missing dateOfBirth key
    * - Return 400 if request contains missing dateOfBirth value
    * - Return 400 if request contains missing firstName key
    * - Return 400 if request contains missing firstName value
    * - Return 400 if request contains missing lastName key
    * - Return 400 if request contains missing lastName value
    * - Return 400 if request contains missing whereBirthRegistered key
    * - Return 400 if request contains missing whereBirthRegistered value
    * - Return BadRequest when GRO returns 4xx
    * - Return InternalServerError when GRO returns 5xx
    **/

  val groJsonResponseObject = JsonUtils.getJsonFromFile("500035710")

  val invalidResponse = Json.parse(
    """
      |[]
    """.stripMargin)

  val noJson = Json.parse(
    s"""{
        }
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

  def postRequest(v: JsValue) : FakeRequest[JsValue] = FakeRequest("POST", "/api/v0/events/birth")
    .withHeaders((ACCEPT, "application/vnd.hmrc.1.0+json"), ("Audit-Source", "DFS"))
    .withBody(v)

  val mockConnector = mock[BirthConnector]
  object MockLookupService extends LookupService {
    override val groConnector = mockConnector
  }

  object MockController extends BirthEventsController {
    val service = MockLookupService
  }

  def httpResponse(js : JsValue) = HttpResponse.apply(200, Some(js))
  def httpResponse(responseCode : Int ) = HttpResponse.apply(responseCode)

 "initialising" should {
   "wire up dependencies correctly" in {
     BirthEventsController.service shouldBe a[LookupService]
   }
  }



  "POST /birth-registration-matching-proxy/match" should {

    "return 200 with application/json type" in {
      when(mockConnector.getReference(Matchers.any())(Matchers.any())).thenReturn(Future.successful(httpResponse(groJsonResponseObject)))
      val request = postRequest(userNoMatchIncludingReferenceNumber)

      val result = MockController.post().apply(request)
      status(result) shouldBe OK
      contentType(result).get shouldBe "application/json"
      header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
    }

    "return JSON response of false on unsuccessful detail match" in {
      when(mockConnector.getReference(Matchers.any())(Matchers.any())).thenReturn(Future.successful(httpResponse(groJsonResponseObject)))
      val request = postRequest(userNoMatchIncludingReferenceNumber)
      val result = MockController.post().apply(request)
      (contentAsJson(result) \ "validated").as[Boolean] shouldBe false
      contentType(result).get shouldBe "application/json"
      header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
    }

    "return JSON response of true on successful detail match" in {
      when(mockConnector.getReference(Matchers.any())(Matchers.any())).thenReturn(Future.successful(httpResponse(groJsonResponseObject)))
      val request = postRequest(userMatchIncludingReferenceNumber)
      val result = MockController.post().apply(request)
      (contentAsJson(result) \ "validated").as[Boolean] shouldBe true
      contentType(result).get shouldBe "application/json"
      header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
    }

    "return JSON response of false on unsuccessful birthReferenceNumber match" in {
      when(mockConnector.getReference(Matchers.any())(Matchers.any())).thenReturn(Future.successful(httpResponse(noJson)))
      val request = postRequest(userNoMatchIncludingReferenceNumber)
      val result = MockController.post().apply(request)
      (contentAsJson(result) \ "validated").as[Boolean] shouldBe false
      contentType(result).get shouldBe "application/json"
      header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
    }

    "return JSON response of true on successful birthReferenceNumber match" in {
      when(mockConnector.getReference(Matchers.any())(Matchers.any())).thenReturn(Future.successful(httpResponse(groJsonResponseObject)))
      val request = postRequest(userMatchIncludingReferenceNumber)
      val result = MockController.post().apply(request)
      (contentAsJson(result) \ "validated").as[Boolean] shouldBe true
      contentType(result).get shouldBe "application/json"
      header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
    }

    "return response code 200 if request contains missing birthReferenceNumber key" in {
      when(mockConnector.getReference(Matchers.any())(Matchers.any())).thenReturn(Future.successful(httpResponse(groJsonResponseObject)))
      val request = postRequest(userNoMatchExcludingReferenceKey)
      val result = MockController.post().apply(request)
      status(result) shouldBe OK
      contentType(result).get shouldBe "application/json"
      header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
    }

    "return response code 400 if request contains missing birthReferenceNumber value" in {
      when(mockConnector.getReference(Matchers.any())(Matchers.any())).thenReturn(Future.successful(httpResponse(groJsonResponseObject)))
      val request = postRequest(userMatchExcludingReferenceNumber)
      val result = MockController.post().apply(request)

      status(result) shouldBe BAD_REQUEST
      contentType(result).get shouldBe "application/json"
      header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
    }

    "return response code 400 if request contains missing dateOfBirth key" in {
      when(mockConnector.getReference(Matchers.any())(Matchers.any())).thenReturn(Future.successful(httpResponse(groJsonResponseObject)))
      val request = postRequest(userNoMatchExcludingDateOfBirthKey)
      val result = MockController.post().apply(request)

      status(result) shouldBe BAD_REQUEST
      contentType(result).get shouldBe "application/json"
      header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
    }

    "return response code 400 if request contains missing dateOfBirth value" in {
      when(mockConnector.getReference(Matchers.any())(Matchers.any())).thenReturn(Future.successful(httpResponse(groJsonResponseObject)))
      val request = postRequest(userNoMatchExcludingDateOfBirthValue)
      val result = MockController.post().apply(request)
      status(result) shouldBe BAD_REQUEST
      contentType(result).get shouldBe "application/json"
      header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
    }

    "return response code 400 if request contains missing firstname key" in {
      when(mockConnector.getReference(Matchers.any())(Matchers.any())).thenReturn(Future.successful(httpResponse(groJsonResponseObject)))
      val request = postRequest(userNoMatchExcludingFirstNameKey)
      val result = MockController.post().apply(request)
      status(result) shouldBe BAD_REQUEST
      contentType(result).get shouldBe "application/json"
      header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
    }

    "return response code 400 if request contains missing firstname value" in {
      when(mockConnector.getReference(Matchers.any())(Matchers.any())).thenReturn(Future.successful(httpResponse(groJsonResponseObject)))
      val request = postRequest(userNoMatchExcludingfirstNameValue)
      val result = MockController.post().apply(request)
      status(result) shouldBe BAD_REQUEST
      contentType(result).get shouldBe "application/json"
      header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
    }

    "return response code 400 if request contains missing lastName key" in {
      when(mockConnector.getReference(Matchers.any())(Matchers.any())).thenReturn(Future.successful(httpResponse(groJsonResponseObject)))
      val request = postRequest(userNoMatchExcludinglastNameKey)
      val result = MockController.post().apply(request)
      status(result) shouldBe BAD_REQUEST
      contentType(result).get shouldBe "application/json"
      header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
    }

    "return response code 400 if request contains missing lastName value" in {
      when(mockConnector.getReference(Matchers.any())(Matchers.any())).thenReturn(Future.successful(httpResponse(groJsonResponseObject)))
      val request = postRequest(userNoMatchExcludinglastNameValue)
      val result = MockController.post().apply(request)
      status(result) shouldBe BAD_REQUEST
      contentType(result).get shouldBe "application/json"
    }

    "return response code 400 if request contains missing whereBirthRegistered key" in {
      when(mockConnector.getReference(Matchers.any())(Matchers.any())).thenReturn(Future.successful(httpResponse(groJsonResponseObject)))
      val request = postRequest(userNoMatchExcludingWhereBirthRegisteredKey)
      val result = MockController.post().apply(request)
      status(result) shouldBe BAD_REQUEST
      contentType(result).get shouldBe "application/json"
    }

    "return response code 400 if request contains missing whereBirthRegistered value" in {
      when(mockConnector.getReference(Matchers.any())(Matchers.any())).thenReturn(Future.successful(httpResponse(groJsonResponseObject)))
      val request = postRequest(userNoMatchExcludingWhereBirthRegisteredValue)
      val result = MockController.post().apply(request)
      status(result) shouldBe BAD_REQUEST
      contentType(result).get shouldBe "application/json"
      header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
    }

    "return BadRequest when GRO returns 4xx" in {
      when(mockConnector.getReference(Matchers.any())(Matchers.any())).thenReturn(Future.failed(new Upstream4xxResponse("", BAD_REQUEST, BAD_REQUEST)))
      val request = postRequest(userNoMatchIncludingReferenceNumber)
      val result = MockController.post().apply(request)
      status(result) shouldBe BAD_REQUEST
      contentType(result).get shouldBe "application/json"
      header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
    }

    "return InternalServerError when GRO returns 5xx" in {
      when(mockConnector.getReference(Matchers.any())(Matchers.any())).thenReturn(Future.failed(new Upstream5xxResponse("", INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR)))
      val request = postRequest(userNoMatchIncludingReferenceNumber)
      val result = MockController.post().apply(request)
      status(result) shouldBe INTERNAL_SERVER_ERROR
      contentType(result).get shouldBe "application/json"
      header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
    }

    "return InternalServerError when GRO returns invalid json" in {
      when(mockConnector.getReference(Matchers.any())(Matchers.any())).thenReturn(Future.successful(httpResponse(invalidResponse)))
      val request = postRequest(userMatchIncludingReferenceNumber)
      val result = MockController.post().apply(request)
      status(result) shouldBe OK
      (contentAsJson(result) \ "validated").as[Boolean] shouldBe false
      contentType(result).get shouldBe "application/json"
    }

    "return not match when GRO returns NOT FOUND" in {
      when(mockConnector.getReference(Matchers.any())(Matchers.any())).thenReturn(Future.failed(new Upstream4xxResponse("", NOT_FOUND, NOT_FOUND)))
      val request = postRequest(userNoMatchIncludingReferenceNumber)
      val result = MockController.post().apply(request)
      status(result) shouldBe OK
      (contentAsJson(result) \ "validated").as[Boolean] shouldBe false
      contentType(result).get shouldBe "application/json"
    }


    "return not match when GRO returns NOT FOUND response " in {
      when(mockConnector.getReference(Matchers.any())(Matchers.any())).thenReturn(httpResponse(NOT_FOUND))
      val request = postRequest(userNoMatchIncludingReferenceNumber)
      val result = MockController.post().apply(request)
      status(result) shouldBe OK
      (contentAsJson(result) \ "validated").as[Boolean] shouldBe false
      contentType(result).get shouldBe "application/json"
    }

    "return not match when GRO returns UNAUTHORIZED response " in {
      when(mockConnector.getReference(Matchers.any())(Matchers.any())).thenReturn(httpResponse(UNAUTHORIZED))
      val request = postRequest(userNoMatchIncludingReferenceNumber)
      val result = MockController.post().apply(request)
      status(result) shouldBe OK
      (contentAsJson(result) \ "validated").as[Boolean] shouldBe false
      contentType(result).get shouldBe "application/json"
    }

    "return InternalServerError when GRO returns 5xx response" in {
      when(mockConnector.getReference(Matchers.any())(Matchers.any())).thenReturn(httpResponse(INTERNAL_SERVER_ERROR))
      val request = postRequest(userNoMatchIncludingReferenceNumber)
      val result = MockController.post().apply(request)
      status(result) shouldBe INTERNAL_SERVER_ERROR
      contentType(result).get shouldBe "application/json"
      header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
    }

  }
}
