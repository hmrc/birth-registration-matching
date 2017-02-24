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

import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.TestData
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.OneAppPerTest
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.JsValue
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.brm.utils.JsonUtils
import uk.gov.hmrc.brm.utils.Mocks._
import uk.gov.hmrc.brm.utils.TestHelper._
import uk.gov.hmrc.play.audit.http.connector.AuditResult
import uk.gov.hmrc.play.http.HttpResponse
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.Future

class BirthEventsControllerValidationLengthSpec extends UnitSpec with OneAppPerTest with MockitoSugar {

  val groJsonResponseObject = JsonUtils.getJsonFromFile("500035710")

  def postRequest(v: JsValue): FakeRequest[JsValue] = FakeRequest("POST", "/api/v0/events/birth")
    .withHeaders((ACCEPT, "application/vnd.hmrc.1.0+json"), ("Audit-Source", "DFS"))
    .withBody(v)

  def httpResponse(js: JsValue) = HttpResponse.apply(200, Some(js))

  def httpResponse(responseCode: Int) = HttpResponse.apply(responseCode)

  val config: Map[String, _] = Map(
    "microservice.services.birth-registration-matching.validation.maxNameLength" -> 100
  )

  override def newAppForTest(testData: TestData) = new GuiceApplicationBuilder().configure(
    config
  ).build()

  "validating max length change" should {

    "return matched value of true when the firstName length  is less than specified value and request is valid" in {
      when(MockController.service.groConnector.getReference(Matchers.any())(Matchers.any())).thenReturn(Future.successful(httpResponse(groJsonResponseObject20120216)))
      when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))

      val request = postRequest(userValidDOB)
      val result = await(MockController.post().apply(request))
      status(result) shouldBe OK
      (contentAsJson(result) \ "matched").as[Boolean] shouldBe true
      header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
    }

    "return BAD REQUEST 400 if request contains more than 100 characters in firstName " in {
      when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))

      val request = postRequest(firstNameWithMoreThan100Characters)
      val result = await(MockController.post().apply(request))
      status(result) shouldBe BAD_REQUEST
      contentType(result).get shouldBe "application/json"
      header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
    }

    "return BAD REQUEST 400 if request contains more than 100 characters in lastname " in {
      when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))

      val request = postRequest(lastNameWithMoreThan100Characters)
      val result = await(MockController.post().apply(request))
      status(result) shouldBe BAD_REQUEST
      contentType(result).get shouldBe "application/json"
      header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
    }

  }

}
