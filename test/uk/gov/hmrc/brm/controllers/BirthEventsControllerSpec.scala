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

import org.joda.time.LocalDate
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.libs.json.{JsValue, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.brm.connectors.BirthConnector
import uk.gov.hmrc.brm.models.Payload
import uk.gov.hmrc.brm.utils.{BirthRegisterCountry, JsonUtils}
import uk.gov.hmrc.play.http.{Upstream4xxResponse, Upstream5xxResponse}
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

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
    * - Return JSON response of false on unsuccessful reference number match
    * - Return JSON response of true on successful reference number match
    * - Return 200 if request contains missing reference key
    * - Return 200 if request contains missing reference value
    * - Return 400 if request contains missing dateOfBirth key
    * - Return 400 if request contains missing dateOfBirth value
    * - Return 400 if request contains missing firstname key
    * - Return 400 if request contains missing firstname value
    * - Return 400 if request contains missing surname key
    * - Return 400 if request contains missing surname value
      *
      * - Return response code 500 when API is down
      * - Return response code 404 when endpoint has been retired and is no longer in use
      * - Return response code 400 if authentication fails
      * - Return response code 403 if account (service account) is suspended
    **/

  val groJsonResponseObject = JsonUtils.getJsonFromFile("500035710")

  val noJson = Json.parse(
    s"""{
        }
    """.stripMargin)

  val userNoMatchExcludingReferenceKey = Json.parse(
    s"""
       |{
       | "forename" : "Chris",
       | "surname" : "Jones",
       | "dateOfBirth" : "1990-02-16",
       | "whereBirthRegistered" : "england"
       |}
    """.stripMargin)

  val userNoMatchExcludingReferenceValue = Json.parse(
    s"""
       |{
       | "forename" : "Chris",
       | "surname" : "Jones",
       | "dateOfBirth" : "1990-02-16",
       | "reference" : "",
       | "whereBirthRegistered" : "england"
       |}
    """.stripMargin)

  val userNoMatchExcludingFirstNameKey = Json.parse(
    s"""
       |{
       |"surname" : "Jones",
       |"dateOfBirth" : "1980-04-18",
       |"reference" : "123456789"
       |}
     """.stripMargin)

  val userNoMatchExcludingReferenceNumber = Json.parse(
    s"""
       |{
       | "forename" : "Chris",
       | "surname" : "Jones",
       | "dateOfBirth" : "1990-02-16",
       | "reference" : "",
       | "whereBirthRegistered" : "england"
       |}
    """.stripMargin)

  val userNoMatchIncludingReferenceNumber = Json.parse(
    s"""
       |{
       | "forename" : "Chris",
       | "surname" : "Jones",
       | "dateOfBirth" : "1990-02-16",
       | "reference" : "123456789",
       | "whereBirthRegistered" : "england"
       |}
    """.stripMargin)

  val userMatchExcludingReferenceNumber = Json.parse(
    s"""
       |{
       | "forename" : "Adam TEST",
       | "surname" : "SMITH",
       | "dateOfBirth" : "1990-02-16",
       | "reference" : "",
       | "whereBirthRegistered" : "england"
       |}
    """.stripMargin)

  val userMatchIncludingReferenceNumber = Json.parse(
    s"""
       |{
       | "forename" : "Adam TEST",
       | "surname" : "SMITH",
       | "dateOfBirth" : "1990-02-16",
       | "reference" : "500035710",
       | "whereBirthRegistered" : "england"
       |}
    """.stripMargin)

  val userNoMatchExcludingDateOfBirthKey = Json.parse(
    s"""
       |{
       | "forename" : "Adam TEST",
       | "surname" : "SMITH",
       | "reference" : "500035710",
       | "whereBirthRegistered" : "england"
       |}
    """.stripMargin)

  val userNoMatchExcludingDateOfBirthValue = Json.parse(
    s"""
       |{
       | "forename" : "Adam TEST",
       | "surname" : "SMITH",
       | "dateOfBirth" : "",
       | "reference" : "500035710",
       | "whereBirthRegistered" : "england"
       |}
    """.stripMargin)

  val userNoMatchExcludingForenameKey = Json.parse(
    s"""
       |{
       |"surname" : "Smith",
       |"dateOrBirth" : "1997-12-17",
       |"reference" : "123456789"
       |}
     """.stripMargin)

  val userNoMatchExcludingForenameValue = Json.parse(
    s"""
       |{
       |"firstname" : "",
       |"surname" : "Jones",
       |"dateOfBirth" : "1997-11-16",
       |"reference" : "123456789"
       |}
     """.stripMargin)

  val userNoMatchExcludingSurnameKey = Json.parse(
    s"""
       |{
       |"firstname" : "John",
       |"dateOrBirth" : "1997-12-17",
       |"reference" : "123456789"
       |}
     """.stripMargin)

  val userNoMatchExcludingSurnameValue = Json.parse(
    s"""
       |{
       |"firstname" : "John",
       |"surname" : "",
       |"dateOfBirth" : "1997-11-16",
       |"reference" : "123456789"
       |}
     """.stripMargin)


  val userNoMatchExcludingWhereBirthRegisteredKey = Json.parse(
    s"""
       |{
       |"firstname" : "Manish",
       |"surname" : "Varma",
       |"dateOfBirth" : "1997-11-16",
       |"reference" : "123456789"
       |
       |}
     """.stripMargin)

  val userNoMatchExcludingWhereBirthRegisteredValue = Json.parse(
    s"""
       |{
       |"firstname" : "John",
       |"surname" : "Jones",
       |"dateOfBirth" : "1997-11-16",
       |"reference" : "123456789",
       |"whereBirthRegistered" : ""
       |}
     """.stripMargin)

  def postRequest(v: JsValue) : FakeRequest[JsValue] = FakeRequest("POST", "/api/v0/events/birth")
    .withHeaders(("Content-type", "application/json"))
    .withBody(v)

  val mockConnector = mock[BirthConnector]

  object MockController extends BirthEventsController {
    override val Connector = mockConnector
  }

  "initialising" should {
    "wire up dependencies correctly" in {
      BirthEventsController.Connector shouldBe a[BirthConnector]
    }
  }





  "POST /birth-registration-matching-proxy/match" should {


    "return 200 with application/json type" in {
      when(MockController.Connector.getReference(Matchers.any())(Matchers.any())).thenReturn(Future.successful(groJsonResponseObject))
      val request = postRequest(userNoMatchExcludingReferenceNumber)

      val result = MockController.post().apply(request)

      status(result) shouldBe OK
      contentType(result).get shouldBe "application/json"
    }

    "return JSON response of false on unsuccessful detail match" in {
      when(MockController.Connector.getReference(Matchers.any())(Matchers.any())).thenReturn(Future.successful(groJsonResponseObject))
      val request = postRequest(userNoMatchExcludingReferenceNumber)
      val result = MockController.post().apply(request)
      (contentAsJson(result) \ "validated").as[Boolean] shouldBe false
      contentType(result).get shouldBe "application/json"
    }

    "return JSON response of true on successful detail match" in {
      when(MockController.Connector.getReference(Matchers.any())(Matchers.any())).thenReturn(Future.successful(groJsonResponseObject))
      val request = postRequest(userMatchExcludingReferenceNumber)
      val result = MockController.post().apply(request)
      (contentAsJson(result) \ "validated").as[Boolean] shouldBe true
      contentType(result).get shouldBe "application/json"
    }

    "return JSON response of false on unsuccessful reference number match" in {
      when(MockController.Connector.getReference(Matchers.any())(Matchers.any())).thenReturn(Future.successful(groJsonResponseObject))
      val request = postRequest(userNoMatchIncludingReferenceNumber)
      val result = MockController.post().apply(request)
      (contentAsJson(result) \ "validated").as[Boolean] shouldBe false
      contentType(result).get shouldBe "application/json"
    }

    "return JSON response of true on successful reference number match" in {
      when(MockController.Connector.getReference(Matchers.any())(Matchers.any())).thenReturn(Future.successful(groJsonResponseObject))
      val request = postRequest(userMatchIncludingReferenceNumber)
      val result = MockController.post().apply(request)
      (contentAsJson(result) \ "validated").as[Boolean] shouldBe true
      contentType(result).get shouldBe "application/json"
    }

    "return response code 200 if request contains missing reference key" in {
      when(MockController.Connector.getReference(Matchers.any())(Matchers.any())).thenReturn(Future.successful(groJsonResponseObject))
      val request = postRequest(userNoMatchExcludingReferenceKey)
      val result = MockController.post().apply(request)
      status(result) shouldBe OK
      contentType(result).get shouldBe "application/json"
    }

    "return response code 200 if request contains missing reference value" in {
      when(MockController.Connector.getReference(Matchers.any())(Matchers.any())).thenReturn(Future.successful(groJsonResponseObject))
      val request = postRequest(userNoMatchExcludingReferenceValue)
      val result = MockController.post().apply(request)
      status(result) shouldBe OK
      contentType(result).get shouldBe "application/json"
    }

    "return response code 400 if request contains missing dateOfBirth key" in {
      when(MockController.Connector.getReference(Matchers.any())(Matchers.any())).thenReturn(Future.successful(groJsonResponseObject))
      val request = postRequest(userNoMatchExcludingDateOfBirthKey)
      val result = MockController.post().apply(request)
      status(result) shouldBe BAD_REQUEST
      contentType(result).get shouldBe "application/json"
    }

    "return response code 400 if request contains missing dateOfBirth value" in {
      when(MockController.Connector.getReference(Matchers.any())(Matchers.any())).thenReturn(Future.successful(groJsonResponseObject))
      val request = postRequest(userNoMatchExcludingDateOfBirthValue)
      val result = MockController.post().apply(request)
      status(result) shouldBe BAD_REQUEST
      contentType(result).get shouldBe "application/json"
    }

    "return response code 400 if request contains missing firstname key" in {
      when(MockController.Connector.getReference(Matchers.any())(Matchers.any())).thenReturn(Future.successful(groJsonResponseObject))
      val request = postRequest(userNoMatchExcludingFirstNameKey)
      val result = MockController.post().apply(request)
      status(result) shouldBe BAD_REQUEST
      contentType(result).get shouldBe "application/json"
    }

    "return response code 400 if request contains missing firstname value" in {
      when(MockController.Connector.getReference(Matchers.any())(Matchers.any())).thenReturn(Future.successful(groJsonResponseObject))
      val request = postRequest(userNoMatchExcludingForenameValue)
      val result = MockController.post().apply(request)
      status(result) shouldBe BAD_REQUEST
      contentType(result).get shouldBe "application/json"
    }

    "return response code 400 if request contains missing surname key" in {
      when(MockController.Connector.getReference(Matchers.any())(Matchers.any())).thenReturn(Future.successful(groJsonResponseObject))
      val request = postRequest(userNoMatchExcludingSurnameKey)
      val result = MockController.post().apply(request)
      status(result) shouldBe BAD_REQUEST
      contentType(result).get shouldBe "application/json"
    }

    "return response code 400 if request contains missing surname value" in {
      when(MockController.Connector.getReference(Matchers.any())(Matchers.any())).thenReturn(Future.successful(groJsonResponseObject))
      val request = postRequest(userNoMatchExcludingSurnameValue)
      val result = MockController.post().apply(request)
      status(result) shouldBe BAD_REQUEST
      contentType(result).get shouldBe "application/json"
    }

    "return response code 400 if request contains missing whereBirthRegistered key" in {
      when(MockController.Connector.getReference(Matchers.any())(Matchers.any())).thenReturn(Future.successful(groJsonResponseObject))
      val request = postRequest(userNoMatchExcludingWhereBirthRegisteredKey)
      val result = MockController.post().apply(request)
      status(result) shouldBe BAD_REQUEST
      contentType(result).get shouldBe "application/json"
    }

    "return response code 400 if request contains missing whereBirthRegistered value" in {
      when(MockController.Connector.getReference(Matchers.any())(Matchers.any())).thenReturn(Future.successful(groJsonResponseObject))
      val request = postRequest(userNoMatchExcludingWhereBirthRegisteredValue)
      val result = MockController.post().apply(request)
      status(result) shouldBe BAD_REQUEST
      contentType(result).get shouldBe "application/json"
    }

    "return BadRequest when GRO returns 4xx" in {
      when(MockController.Connector.getReference(Matchers.any())(Matchers.any())).thenReturn(Future.failed(new Upstream4xxResponse("", BAD_REQUEST, BAD_REQUEST)))
      val request = postRequest(userMatchExcludingReferenceNumber)
      val result = MockController.post().apply(request)
      status(result) shouldBe BAD_REQUEST
      contentType(result).get shouldBe "application/json"
    }

    "return InternalServerError when GRO returns 5xx" in {
      when(MockController.Connector.getReference(Matchers.any())(Matchers.any())).thenReturn(Future.failed(new Upstream5xxResponse("", INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR)))
      val request = postRequest(userMatchExcludingReferenceNumber)
      val result = MockController.post().apply(request)
      status(result) shouldBe INTERNAL_SERVER_ERROR
      contentType(result).get shouldBe "application/json"
    }
  }
}
