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

package uk.gov.hmrc.brm.connectors

import play.api.libs.json.{JsValue, Writes}
import uk.gov.hmrc.brm.models.brm.Payload
import uk.gov.hmrc.brm.utils.BRMLogger
import uk.gov.hmrc.http.{HeaderCarrier, HttpPost, HttpReads, HttpResponse}
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._

import scala.concurrent.ExecutionContext


trait BirthConnector extends ServicesConfig {

  private type BRMHeaders = Seq[(String, String)]

  val serviceUrl: String
  var httpPost: HttpPost

  protected def headers: BRMHeaders


  protected val referenceBody: PartialFunction[Payload, (String, JsValue)]
  protected val detailsBody: PartialFunction[Payload, (String, JsValue)]


  /**
    * RequestType, reference or details
    */
  abstract class RequestType

  case class ReferenceRequest() extends RequestType

  case class DetailsRequest() extends RequestType

  case class Request(uri: String, jsonBody: JsValue)

  private def buildRequest(payload: Payload, operation: RequestType): Request = {
    val f = operation match {
      case ReferenceRequest() => referenceBody
      case DetailsRequest() => detailsBody
    }
    val request = f(payload)
    Request(request._1, request._2)
  }

  private def sendRequest(request: Request)(implicit hc: HeaderCarrier, ec: ExecutionContext) = {

    import scala.concurrent.ExecutionContext.Implicits.global

    val newHc = hc.withExtraHeaders(headers: _*)
    val response = httpPost.POST[JsValue, HttpResponse](request.uri, request.jsonBody)(
      wts = Writes.JsValueWrites,
      rds = HttpReads.readRaw,
      hc = newHc,
      ec)


    response.onComplete(r =>
      BRMLogger.debug("BirthConnector", "sendRequest", s"[HttpResponse]: [status] ${r.map(_.status)} [body] ${r.map(_.body)} [headers] ${r.map(_.allHeaders)}")
    )

    response
  }

  def getReference(payload: Payload)(implicit hc: HeaderCarrier) = {
    BRMLogger.info(s"BirthConnector", "getReference", "calling request with reference number")
    val requestData = buildRequest(payload, ReferenceRequest())
    sendRequest(requestData)
  }

  def getChildDetails(payload: Payload)(implicit hc: HeaderCarrier) = {
    BRMLogger.info(s"BirthConnector", "getChildDetails", "calling request with child details")
    val requestData = buildRequest(payload, DetailsRequest())
    sendRequest(requestData)
  }




}
