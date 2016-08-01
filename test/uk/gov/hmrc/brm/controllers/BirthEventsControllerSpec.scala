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

import com.fasterxml.jackson.annotation.JsonValue
import org.mockito.Matchers
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._
import play.api.Logger
import play.api.libs.json.{JsBoolean, JsString, JsValue, Json}
import play.api.mvc.Action
import play.api.test.FakeRequest
import uk.gov.hmrc.brm.connectors.BirthConnector
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import play.api.test.Helpers._
import uk.gov.hmrc.brm.utils.JsonUtils
import play.api.http.Status
import uk.gov.hmrc.play.http.Upstream5xxResponse

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
       | "dateOfBirth" : "1990-02-16"
       |}
    """.stripMargin)

  val userNoMatchExcludingReferenceValue = Json.parse(
    s"""
       |{
       | "forename" : "Chris",
       | "surname" : "Jones",
       | "dateOfBirth" : "1990-02-16",
       | "reference" : ""
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
       | "reference" : ""
       |}
    """.stripMargin)

  val userNoMatchIncludingReferenceNumber = Json.parse(
    s"""
       |{
       | "forename" : "Chris",
       | "surname" : "Jones",
       | "dateOfBirth" : "1990-02-16",
       | "reference" : "123456789"
       |}
    """.stripMargin)

  val userMatchExcludingReferenceNumber = Json.parse(
    s"""
       |{
       | "forename" : "Adam TEST",
       | "surname" : "SMITH",
       | "dateOfBirth" : "1990-02-16",
       | "reference" : ""
       |}
    """.stripMargin)

  val userMatchIncludingReferenceNumber = Json.parse(
    s"""
       |{
       | "forename" : "Adam TEST",
       | "surname" : "SMITH",
       | "dateOfBirth" : "1990-02-16",
       | "reference" : "500035710"
       |}
    """.stripMargin)

  val userNoMatchExcludingDateOfBirthKey = Json.parse(
    s"""
       |{
       | "forename" : "Adam TEST",
       | "surname" : "SMITH",
       | "reference" : "500035710"
       |}
    """.stripMargin)

  val userNoMatchExcludingDateOfBirthValue = Json.parse(
    s"""
       |{
       | "forename" : "Adam TEST",
       | "surname" : "SMITH",
       | "dateOfBirth" : "",
       | "reference" : "500035710"
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

  def postRequest(v: JsValue) : FakeRequest[JsValue] = FakeRequest("POST", "/api/v0/events/birth")
    .withHeaders(("Content-type", "application/json"))
    .withBody(v)

  val mockConnector = mock[BirthConnector]

  object MockController extends BirthEventsController {
    override val GROConnector = mockConnector
  }

  "initialising" should {
    "wire up dependencies correctly" in {
      BirthEventsController.GROConnector shouldBe a[BirthConnector]
    }
  }

  "POST /birth-registration-matching-proxy/match" should {

    "return 200 with application/json type" in {
      when(MockController.GROConnector.getReference(Matchers.any())(Matchers.any())).thenReturn(Future.successful(groJsonResponseObject))
      val request = postRequest(userNoMatchExcludingReferenceNumber)
      val result = MockController.post().apply(request)
      status(result) shouldBe 200
      contentType(result).get shouldBe "application/json"
    }

    "return JSON response of false on unsuccessful detail match" in {
      when(MockController.GROConnector.getReference(Matchers.any())(Matchers.any())).thenReturn(Future.successful(groJsonResponseObject))
      val request = postRequest(userNoMatchExcludingReferenceNumber)
      val result = MockController.post().apply(request)
      (contentAsJson(result) \ "validated").as[Boolean] shouldBe false
    }

    "return JSON response of true on successful detail match" in {
      when(MockController.GROConnector.getReference(Matchers.any())(Matchers.any())).thenReturn(Future.successful(groJsonResponseObject))
      val request = postRequest(userMatchExcludingReferenceNumber)
      val result = MockController.post().apply(request)
      (contentAsJson(result) \ "validated").as[Boolean] shouldBe true
    }

    "return JSON response of false on unsuccessful reference number match" in {
      when(MockController.GROConnector.getReference(Matchers.any())(Matchers.any())).thenReturn(Future.successful(groJsonResponseObject))
      val request = postRequest(userNoMatchIncludingReferenceNumber)
      val result = MockController.post().apply(request)
      (contentAsJson(result) \ "validated").as[Boolean] shouldBe false
    }

    "return JSON response of true on successful reference number match" in {
      when(MockController.GROConnector.getReference(Matchers.any())(Matchers.any())).thenReturn(Future.successful(groJsonResponseObject))
      val request = postRequest(userMatchIncludingReferenceNumber)
      val result = MockController.post().apply(request)
      (contentAsJson(result) \ "validated").as[Boolean] shouldBe true
    }

    "return response code 200 if request contains missing reference key" in {
      when(MockController.GROConnector.getReference(Matchers.any())(Matchers.any())).thenReturn(Future.successful(groJsonResponseObject))
      val request = postRequest(userNoMatchExcludingReferenceKey)
      val result = MockController.post().apply(request)
      status(result) shouldBe 200
      contentType(result).get shouldBe "application/json"
    }

    "return response code 200 if request contains missing reference value" in {
      when(MockController.GROConnector.getReference(Matchers.any())(Matchers.any())).thenReturn(Future.successful(groJsonResponseObject))
      val request = postRequest(userNoMatchExcludingReferenceValue)
      val result = MockController.post().apply(request)
      status(result) shouldBe 200
      contentType(result).get shouldBe "application/json"
    }

    "return response code 400 if request contains missing dateOfBirth key" in {
      when(MockController.GROConnector.getReference(Matchers.any())(Matchers.any())).thenReturn(Future.successful(groJsonResponseObject))
      val request = postRequest(userNoMatchExcludingDateOfBirthKey)
      val result = MockController.post().apply(request)
      status(result) shouldBe 400
    }

    "return response code 400 if request contains missing dateOfBirth value" in {
      when(MockController.GROConnector.getReference(Matchers.any())(Matchers.any())).thenReturn(Future.successful(groJsonResponseObject))
      val request = postRequest(userNoMatchExcludingDateOfBirthValue)
      val result = MockController.post().apply(request)
      status(result) shouldBe 400
    }

    "return response code 400 if request contains missing firstname key" in {
      when(MockController.GROConnector.getReference(Matchers.any())(Matchers.any())).thenReturn(Future.successful(groJsonResponseObject))
      val request = postRequest(userNoMatchExcludingFirstNameKey)
      val result = MockController.post().apply(request)
      status(result) shouldBe 400
    }

    "return response code 400 if request contains missing firstname value" in {
      when(MockController.GROConnector.getReference(Matchers.any())(Matchers.any())).thenReturn(Future.successful(groJsonResponseObject))
      val request = postRequest(userNoMatchExcludingForenameValue)
      val result = MockController.post().apply(request)
      status(result) shouldBe 400
    }

    "return response code 400 if request contains missing surname key" in {
      when(MockController.GROConnector.getReference(Matchers.any())(Matchers.any())).thenReturn(Future.successful(groJsonResponseObject))
      val request = postRequest(userNoMatchExcludingSurnameKey)
      val result = MockController.post().apply(request)
      status(result) shouldBe 400
    }

    "return response code 400 if request contains missing surname value" in {
      when(MockController.GROConnector.getReference(Matchers.any())(Matchers.any())).thenReturn(Future.successful(groJsonResponseObject))
      val request = postRequest(userNoMatchExcludingSurnameValue)
      val result = MockController.post().apply(request)
      status(result) shouldBe 400
    }

    "return response code 500 when API is down" in {
      when(MockController.GROConnector.getReference(Matchers.any())(Matchers.any())).thenReturn(Future.failed(new Upstream5xxResponse("", 500, 500)))
      val request = postRequest(userMatchExcludingReferenceNumber)
      val result = MockController.post().apply(request)
      status(result) shouldBe 500
    }
  }
}
