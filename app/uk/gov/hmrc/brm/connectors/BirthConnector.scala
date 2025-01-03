/*
 * Copyright 2023 HM Revenue & Customs
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

import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.brm.models.brm.Payload
import uk.gov.hmrc.brm.utils.BRMLogger
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps}

import scala.concurrent.{ExecutionContext, Future}

trait BirthConnector {

  private type BRMHeaders = Seq[(String, String)]

  val serviceUrl: String
  val http: HttpClientV2
  val logger: BRMLogger

  protected def headers: BRMHeaders

  protected val referenceBody: PartialFunction[Payload, (String, JsValue)]
  protected val detailsBody: PartialFunction[Payload, (String, JsValue)]

  /** RequestType, reference or details
    */
  sealed trait RequestType
  case object ReferenceRequest extends RequestType
  case object DetailsRequest extends RequestType

  case class Request(uri: String, jsonBody: JsValue)

  private def buildRequest(payload: Payload, operation: RequestType): Request = {
    val f       = operation match {
      case ReferenceRequest => referenceBody
      case DetailsRequest   => detailsBody
    }
    val request = f(payload)
    Request(request._1, request._2)
  }

  private def sendRequest(request: Request)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] = {

    val response = http
      .post(url"${request.uri}")(hc.copy(authorization = None))
      .setHeader(headers: _*)
      .withBody(Json.toJson(request.jsonBody))
      .execute[HttpResponse]

    logger.debug("BirthConnector", "sendRequest", s"[Request]: $request [HeaderCarrier withExtraHeaders]: $headers")

    response.onComplete(r =>
      logger.debug(
        "BirthConnector",
        "sendRequest",
        s"[HttpResponse]: [status] ${r.map(_.status)} [body] ${r.map(_.body)} [headers] ${r.map(_.headers)}"
      )
    )(ec)

    response
  }

  def getReference(payload: Payload)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] = {
    val requestData = buildRequest(payload, ReferenceRequest)
    sendRequest(requestData)
  }

  def getChildDetails(payload: Payload)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] = {
    val requestData = buildRequest(payload, DetailsRequest)
    sendRequest(requestData)

  }
}
