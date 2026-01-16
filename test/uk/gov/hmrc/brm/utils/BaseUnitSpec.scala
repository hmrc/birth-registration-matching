/*
 * Copyright 2025 HM Revenue & Customs
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

import izumi.reflect.Tag
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.mockito.stubbing.OngoingStubbing
import org.scalatest.OptionValues
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.dsl.EmptyWord
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import org.mockito.ArgumentCaptor
import play.api.Play.materializer
import play.api.http.Status
import play.api.inject.Injector
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.libs.ws.BodyWritable
import play.api.mvc.Result
import play.api.test.Helpers._
import uk.gov.hmrc.brm.connectors.BirthConnector
import uk.gov.hmrc.brm.utils.Mocks._
import uk.gov.hmrc.brm.utils.TestHelper._
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads, HttpResponse}
import uk.gov.hmrc.play.audit.http.connector.AuditResult
import java.net.URL
import scala.concurrent.{ExecutionContext, Future}

/** Created by user on 04/05/17.
  */
trait BaseUnitSpec
    extends AnyWordSpecLike
    with GuiceOneAppPerSuite
    with Matchers
    with OptionValues
    with ScalaFutures
    with MockitoSugar {

  lazy val injector: Injector = app.injector

  implicit lazy val hc: HeaderCarrier    = HeaderCarrier()
  implicit lazy val ec: ExecutionContext = injector.instanceOf[ExecutionContext]

  def checkResponse(result: Result, responseStatus: Int, matchResponse: Boolean): Unit = {
    result.header.status                                                                 shouldBe responseStatus
    (Json.parse(result.body.consumeData.futureValue.utf8String) \ "matched").as[Boolean] shouldBe matchResponse
    checkHeaders(result)
  }

  def checkResponse(result: Result, responseStatus: Int, responseString: String): Unit =
    checkResponse(result, responseStatus.toString, responseString)

  def checkResponse(result: Result, responseStatus: String, responseString: String): Unit = {
    result.header.status.toString                                         shouldBe responseStatus
    Json.parse(result.body.consumeData.futureValue.utf8String).toString() shouldBe responseString
    checkHeaders(result)
  }

  def checkResponse(result: Result, responseStatus: Int, responseBody: EmptyWord): Unit = {
    result.header.status                           shouldBe responseStatus
    result.body.consumeData.futureValue.utf8String shouldBe responseBody
    checkHeaders(result)
  }

  def checkResponse(result: Result, responseStatus: Int, code: String, message: String): Unit = {
    result.header.status                                                                shouldBe responseStatus
    checkHeaders(result)
    (Json.parse(result.body.consumeData.futureValue.utf8String) \ "code").as[String]    shouldBe code
    (Json.parse(result.body.consumeData.futureValue.utf8String) \ "message").as[String] shouldBe message
  }

  private def checkHeaders(result: Result): Unit = {
    result.body.contentType.get     should startWith("application/json")
    result.header.headers(ACCEPT) shouldBe "application/vnd.hmrc.1.0+json"
  }

  def mockReferenceResponse(jsonResponse: JsValue): Unit =
    mockRefResponse(mockGroConnector, jsonResponse)

  def mockReferenceResponse(exception: Exception): OngoingStubbing[Future[HttpResponse]] =
    mockRefResponse(mockGroConnector, exception)

  private def mockRefResponse(connector: BirthConnector, exception: Exception) =
    when(connector.getReference(any())(any(), any()))
      .thenReturn(Future.failed(exception))

  private def mockRefResponse(connector: BirthConnector, jsonResponse: JsValue): Unit =
    when(connector.getReference(any())(any(), any())).thenReturn(Future.successful(httpResponse(jsonResponse)))

  def mockNrsReferenceResponse(jsonResponse: JsValue): Unit =
    mockRefResponse(mockNrsConnector, jsonResponse)

  def mockNrsReferenceResponse(exception: Exception): OngoingStubbing[Future[HttpResponse]] =
    mockRefResponse(mockNrsConnector, exception)

  def mockDetailsResponse(jsonResponse: JsValue): OngoingStubbing[Future[HttpResponse]] =
    when(mockGroConnector.getChildDetails(any())(any(), any()))
      .thenReturn(Future.successful(httpResponse(jsonResponse)))

  def mockNrsDetailsResponse(jsonResponse: JsValue): OngoingStubbing[Future[HttpResponse]] =
    when(mockNrsConnector.getChildDetails(any())(any(), any()))
      .thenReturn(Future.successful(httpResponse(jsonResponse)))

  def mockDetailsResponse(exception: Exception): OngoingStubbing[Future[HttpResponse]] =
    when(mockGroConnector.getChildDetails(any())(any(), any()))
      .thenReturn(Future.failed(exception))

  def mockNrsDetailsResponse(exception: Exception): OngoingStubbing[Future[HttpResponse]] =
    when(mockNrsConnector.getChildDetails(any())(any(), any()))
      .thenReturn(Future.failed(exception))

  def mockAuditSuccess: OngoingStubbing[Future[AuditResult]] =
    when(mockAuditConnector.sendEvent(any())(any(), any())).thenReturn(Future.successful(AuditResult.Success))

  def mockAuditFailure: OngoingStubbing[Future[AuditResult]] =
    when(mockAuditConnector.sendEvent(any())(any(), any())).thenReturn(Future.failed(AuditResult.Failure("")))

  def mockHttpPostResponse(
    responseStatus: Int = Status.OK,
    responseJson: Option[JsValue]
  ): ArgumentCaptor[JsValue] = {
    val argumentCapture: ArgumentCaptor[JsValue] = ArgumentCaptor.forClass(classOf[JsValue])

    when(
      mockRequestBuilder
        .withBody(argumentCapture.capture)(any[BodyWritable[JsValue]], any[Tag[JsValue]], any[ExecutionContext])
    )
      .thenReturn(mockRequestBuilder)
    when(mockRequestBuilder.execute[HttpResponse](any[HttpReads[HttpResponse]], any[ExecutionContext])).thenReturn(
      Future.successful(
        HttpResponse(responseStatus, responseJson.getOrElse(JsObject.empty), Map.empty[String, Seq[String]])
      )
    )

    argumentCapture
  }

  when(mockConfig.serviceUrl).thenReturn("http://localhost:6001")
  when(mockConfig.desUrl).thenReturn("http://localhost:6001")
  when(mockHttp.post(any[URL])(any[HeaderCarrier])).thenReturn(mockRequestBuilder)
  when(mockRequestBuilder.setHeader(any())).thenReturn(mockRequestBuilder)

  def checkResponse(result: HttpResponse, responseCode: Int): Unit = {
    result        shouldBe a[HttpResponse]
    result.status shouldBe responseCode
  }

}
