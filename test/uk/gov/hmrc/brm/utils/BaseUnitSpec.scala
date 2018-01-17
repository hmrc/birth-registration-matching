/*
 * Copyright 2018 HM Revenue & Customs
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

package uk.gov.hmrc.brm.utils

import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.words.EmptyWord
import org.specs2.mock.mockito.ArgumentCapture
import play.api.http.Status
import play.api.libs.json.JsValue
import play.api.mvc.Result
import play.api.test.Helpers._
import uk.gov.hmrc.brm.connectors.BirthConnector
import uk.gov.hmrc.brm.utils.Mocks.{MockController, _}
import uk.gov.hmrc.brm.utils.TestHelper._
import uk.gov.hmrc.play.audit.http.connector.AuditResult
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.Future
import uk.gov.hmrc.http.HttpResponse
/**
  * Created by user on 04/05/17.
  */
trait BaseUnitSpec extends UnitSpec  {

  def checkResponse(result: Result, responseStatus:Int , matchResonse:Boolean): Unit = {
    status(result) shouldBe responseStatus
    (contentAsJson(result) \ "matched").as[Boolean] shouldBe matchResonse
    checkHeaders(result)
  }

  def checkResponse(result: Result, responseStatus:Int , responseString:String): Unit = {
    status(result) shouldBe responseStatus
    jsonBodyOf(result).toString() shouldBe responseString
    checkHeaders(result)
  }

  def checkResponse(result: Result, responseStatus:Int , responseBody:EmptyWord): Unit = {
    status(result) shouldBe responseStatus
    bodyOf(result) shouldBe responseBody
    checkHeaders(result)
  }

  def checkResponse(result: Result, responseStatus:Int , code:String, message: String): Unit = {
    status(result) shouldBe responseStatus
    checkHeaders(result)
    (contentAsJson(result) \ "code").as[String] shouldBe code
    (contentAsJson(result) \ "message").as[String] shouldBe message
  }

  private def checkHeaders(result: Result): Unit = {
    contentType(result).get shouldBe "application/json"
    header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
  }

  def mockReferenceResponse(jsonResponse:JsValue) = {
    mockRefResponse(MockController.service.groConnector,jsonResponse)
  }

  def mockReferenceResponse(exception:Exception) = {
    mockRefResponse(MockController.service.groConnector, exception)
  }

  private def mockRefResponse(connector : BirthConnector,exception:Exception) = {
    when(connector.getReference(Matchers.any())(Matchers.any()))
      .thenReturn(Future.failed(exception))
  }

  private def mockRefResponse(connector : BirthConnector,jsonResponse:JsValue ): Unit ={
    when(connector.getReference(Matchers.any())(Matchers.any())).thenReturn(Future.successful(httpResponse(jsonResponse)))
  }

  def mockNrsReferenceResponse(jsonResponse:JsValue) = {
    mockRefResponse(MockController.service.nrsConnector,jsonResponse)
  }

  def mockNrsReferenceResponse(exception:Exception) = {
    mockRefResponse(MockController.service.nrsConnector, exception)
  }

  def mockGroNiReferenceResponse(exception:Exception) = {
    mockRefResponse(MockController.service.groniConnector, exception)
  }

  def mockDetailsResponse(jsonResponse:JsValue) = {
    when(MockController.service.groConnector.getChildDetails(Matchers.any())(Matchers.any())).thenReturn(Future.successful(httpResponse(jsonResponse)))
  }

  def mockNrsDetailsResponse(jsonResponse:JsValue) = {
    when(MockController.service.nrsConnector.getChildDetails(Matchers.any())(Matchers.any())).thenReturn(Future.successful(httpResponse(jsonResponse)))
  }

  def mockDetailsResponse(exception:Exception) = {
    when(MockController.service.groConnector.getChildDetails(Matchers.any())(Matchers.any()))
      .thenReturn(Future.failed(exception))
  }

  def mockNrsDetailsResponse(exception:Exception) = {
    when(MockController.service.nrsConnector.getChildDetails(Matchers.any())(Matchers.any()))
      .thenReturn(Future.failed(exception))
  }

  def mockGroNiDetailsResponse(exception:Exception) = {
    when(MockController.service.groniConnector.getChildDetails(Matchers.any())(Matchers.any()))
      .thenReturn(Future.failed(exception))
  }

  def mockAuditSuccess = {
    when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
  }

  def mockAuditFailure = {
    when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.failed(AuditResult.Failure("")))
  }

  def mockHttpPostResponse(responseStatus:Int = Status.OK, responseJson : scala.Option[play.api.libs.json.JsValue]) = {
    val argumentCapture = new ArgumentCapture[JsValue]
    when(mockHttpPost.POST[JsValue, HttpResponse]( Matchers.any(), argumentCapture.capture, Matchers.any())
      (Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(HttpResponse(responseStatus, responseJson)))
    argumentCapture
  }

  def checkResponse(result: HttpResponse, responseCode:Int): Unit = {
    result shouldBe a[HttpResponse]
    result.status shouldBe responseCode
  }

}
