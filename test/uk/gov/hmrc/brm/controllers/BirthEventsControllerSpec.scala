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

import scala.concurrent.Future

/**
  * Created by chrisianson on 26/07/16.
  */
class BirthEventsControllerSpec extends UnitSpec with WithFakeApplication with MockitoSugar {

  /**
    * - Should
    * - POST /birth-registration-matching-proxy/match should return 200 with application/json type
    * - Return JSON response of false on unsuccessful match
    * - Return valid JSON response on unsuccessful match
    * - Return response code 500 when API is down
    * - Return response code 404 when endpoint has been retired and is no longer in use
    * - Return response code 400 if request contains invalid/missing data
    * - Return response code 400 if authentication fails
    * - Return response code 403 if account (service account) is suspended
    **/

  val groJsonResponseObject = JsonUtils.getJsonFromFile("500035710")

  val userNoMatchExcludingReferenceNumber = Json.parse(
    s"""
       |{
       | "forename" : "Chris",
       | "surname" : "Jones",
       | "dateOfBirth" : "1990-02-16",
       | "reference" : ""
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

    "should return 200 with application/json type" in {
      when(MockController.GROConnector.getReference(Matchers.any())(Matchers.any())).thenReturn(Future.successful(groJsonResponseObject))
      val request = postRequest(userNoMatchExcludingReferenceNumber)
      val result = MockController.post().apply(request)
      status(result) shouldBe 200
      contentType(result).get shouldBe "application/json"
    }

    "should return JSON response of false on unsuccessful match" in {
      when(MockController.GROConnector.getReference(Matchers.any())(Matchers.any())).thenReturn(Future.successful(groJsonResponseObject))
      val request = postRequest(userNoMatchExcludingReferenceNumber)
      val result = MockController.post().apply(request)
      (contentAsJson(result) \ "validated").as[Boolean] shouldBe false
    }

    "should return JSON response of true on successful match" in {
      when(MockController.GROConnector.getReference(Matchers.any())(Matchers.any())).thenReturn(Future.successful(groJsonResponseObject))
      val request = postRequest(userMatchExcludingReferenceNumber)
      val result = MockController.post().apply(request)
      (contentAsJson(result) \ "validated").as[Boolean] shouldBe true
    }
  }
}
