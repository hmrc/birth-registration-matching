/*
 * Copyright 2021 HM Revenue & Customs
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

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.mockito.stubbing.OngoingStubbing
import org.scalatest.words.EmptyWord
import org.specs2.mock.mockito.ArgumentCapture
import play.api.http.Status
import play.api.libs.json.JsValue
import play.api.mvc.Result
import play.api.test.Helpers._
import uk.gov.hmrc.brm.connectors.BirthConnector
import uk.gov.hmrc.brm.utils.Mocks._
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

  def mockReferenceResponse(jsonResponse:JsValue): Unit = {
    mockRefResponse(mockGroConnector,jsonResponse)
  }

  def mockReferenceResponse(exception:Exception): OngoingStubbing[Future[HttpResponse]] = {
    mockRefResponse(mockGroConnector, exception)
  }

  private def mockRefResponse(connector : BirthConnector,exception:Exception) = {
    when(connector.getReference(any())(any()))
      .thenReturn(Future.failed(exception))
  }

  private def mockRefResponse(connector: BirthConnector,jsonResponse:JsValue ): Unit ={
    when(connector.getReference(any())(any())).thenReturn(Future.successful(httpResponse(jsonResponse)))
  }

  def mockNrsReferenceResponse(jsonResponse:JsValue): Unit = {
    mockRefResponse(mockNrsConnector,jsonResponse)
  }

  def mockNrsReferenceResponse(exception:Exception): OngoingStubbing[Future[HttpResponse]] = {
    mockRefResponse(mockNrsConnector, exception)
  }

  def mockGroNiReferenceResponse(exception:Exception): OngoingStubbing[Future[HttpResponse]] = {
    mockRefResponse(mockGroniConnector, exception)
  }

  def mockDetailsResponse(jsonResponse:JsValue): OngoingStubbing[Future[HttpResponse]] = {
    when(mockGroConnector.getChildDetails(any())(any())).thenReturn(Future.successful(httpResponse(jsonResponse)))
  }

  def mockNrsDetailsResponse(jsonResponse:JsValue): OngoingStubbing[Future[HttpResponse]] = {
    when(mockNrsConnector.getChildDetails(any())(any())).thenReturn(Future.successful(httpResponse(jsonResponse)))
  }

  def mockDetailsResponse(exception:Exception): OngoingStubbing[Future[HttpResponse]] = {
    when(mockGroConnector.getChildDetails(any())(any()))
      .thenReturn(Future.failed(exception))
  }

  def mockNrsDetailsResponse(exception:Exception): OngoingStubbing[Future[HttpResponse]] = {
    when(mockNrsConnector.getChildDetails(any())(any()))
      .thenReturn(Future.failed(exception))
  }

  def mockGroNiDetailsResponse(exception:Exception): OngoingStubbing[Future[Nothing]] = {
    when(mockGroniConnector.getChildDetails(any())(any()))
      .thenReturn(Future.failed(exception))
  }

  def mockAuditSuccess: OngoingStubbing[Future[AuditResult]] = {
    when(mockAuditConnector.sendEvent(any())(any(), any())).thenReturn(Future.successful(AuditResult.Success))
  }

  def mockAuditFailure: OngoingStubbing[Future[AuditResult]] = {
    when(mockAuditConnector.sendEvent(any())(any(), any())).thenReturn(Future.failed(AuditResult.Failure("")))
  }

  def mockHttpPostResponse(responseStatus:Int = Status.OK, responseJson : scala.Option[play.api.libs.json.JsValue]): ArgumentCapture[JsValue] = {
    val argumentCapture = new ArgumentCapture[JsValue]
    when(mockHttp.POST[JsValue, HttpResponse]( any(), argumentCapture.capture, any())(any(), any(), any(), any()))
      .thenReturn(Future.successful(HttpResponse(responseStatus, responseJson)))
    argumentCapture
  }

  def checkResponse(result: HttpResponse, responseCode:Int): Unit = {
    result shouldBe a[HttpResponse]
    result.status shouldBe responseCode
  }

}
