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
import org.specs2.mutable.BeforeAfter
import play.api.libs.json._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.brm.BRMFakeApplication
import uk.gov.hmrc.brm.utils.JsonUtils
import uk.gov.hmrc.play.http.HttpResponse
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import uk.gov.hmrc.brm.connectors.BirthConnector
import uk.gov.hmrc.brm.services.LookupService

import scala.concurrent.Future

/**
  * Created by chrisianson on 26/08/16.
  */
class BirthEventsControllerDobSwitchSpec
  extends UnitSpec
  with MockitoSugar
  with BRMFakeApplication
  with BeforeAndAfter {

  val groJsonResponseObject = JsonUtils.getJsonFromFile("500035710")
  val groJsonResponseObject20090701 = JsonUtils.getJsonFromFile("2009-07-01")
  val groJsonResponseObject20090630 = JsonUtils.getJsonFromFile("2009-06-30")

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

  "POST /birth-registration-matching-proxy/match" should {

    "return validated value of true when the dateOfBirth is greater than 2009-07-01 and the gro record matches" in {
      when(mockConnector.getReference(Matchers.any())(Matchers.any())).thenReturn(Future.successful(httpResponse(groJsonResponseObject)))
      val request = postRequest(userValidDOB)
      val result = MockController.post().apply(request)
      (contentAsJson(result) \ "validated").as[Boolean] shouldBe true
    }

    "return validated value of true when the dateOfBirth is equal to 2009-07-01 and the gro record matches" in {
      when(mockConnector.getReference(Matchers.any())(Matchers.any())).thenReturn(Future.successful(httpResponse(groJsonResponseObject20090701)))
      val request = postRequest(userValidDOB20090701)
      val result = MockController.post().apply(request)
      (contentAsJson(result) \ "validated").as[Boolean] shouldBe true
    }

    "return validated value of false when the dateOfBirth is invalid and the gro record matches" in {

      when(mockConnector.getReference(Matchers.any())(Matchers.any())).thenReturn(Future.successful(httpResponse(groJsonResponseObject)))
      val request = postRequest(userInvalidDOB)
      val result = MockController.post().apply(request)
      (contentAsJson(result) \ "validated").as[Boolean] shouldBe false
    }

    "return validated value of false when the dateOfBirth is one day earlier than 2009-07-01 and the gro record matches" in {
      when(mockConnector.getReference(Matchers.any())(Matchers.any())).thenReturn(Future.successful(httpResponse(groJsonResponseObject20090630)))
      val request = postRequest(userValidDOB20090630)
      val result = await(MockController.post().apply(request))
      (contentAsJson(result) \ "validated").as[Boolean] shouldBe false
    }

  }

}
