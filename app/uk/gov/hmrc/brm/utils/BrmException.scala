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

import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Request, Result}
import play.api.mvc.Results._
import play.api.http.{Status => StatusCodes}
import uk.gov.hmrc.brm.implicits.MetricsFactory
import uk.gov.hmrc.brm.models.brm.{ErrorResponse, Payload}
import uk.gov.hmrc.brm.utils.BirthRegisterCountry._
import uk.gov.hmrc.http._

trait BRMException extends StatusCodes {

  val metrics: MetricsFactory
  val logger: BRMLogger

  val CLASS_NAME: String  = "BRMException"
  val METHOD_NAME: String = "handleException"

  private def logException(method: String, body: String, statusCode: Int)(implicit
    payload: Payload,
    request: Request[JsValue]
  ) = {
    metrics.getMetrics().status(statusCode)
    statusCode match {
      case Exception5xx() =>
        logger.error(
          CLASS_NAME,
          METHOD_NAME,
          BrmExceptionMessage.message(method, statusCode, request.headers.get("X-Request-ID"))
        )
      case Exception4xx() =>
        logger.warn(
          CLASS_NAME,
          METHOD_NAME,
          BrmExceptionMessage.message(method, statusCode, request.headers.get("X-Request-ID"))
        )
      case _              =>
        logger.info(
          CLASS_NAME,
          METHOD_NAME,
          BrmExceptionMessage.message(method, statusCode, request.headers.get("X-Request-ID"))
        )
    }
  }

  private def InternalServerErrorException(
    method: String,
    e: Throwable,
    upstreamCode: Int = INTERNAL_SERVER_ERROR
  )(implicit payload: Payload, request: Request[JsValue]): Status = {
    logException(method, s"[InternalServerError]: ${e.getMessage}", upstreamCode)
    InternalServerError
  }

  private def serviceUnavailable(method: String, cause: String, e: Exception, body: String)(implicit
    payload: Payload,
    request: Request[JsValue]
  ) = {
    logException(method, s"[ServiceUnavailable] [$cause]: ${e.getMessage}", SERVICE_UNAVAILABLE)
    ServiceUnavailable(body)
  }

  private def respondNoMatch() =
    Ok(Json.toJson(BirthResponseBuilder.withNoMatch()))

  /** Map on e : Exceptions NOT Upstream4xx as HttpVerbs converts these into Exceptions that are thrown instead of
    * returning the Upstream4xx / Upstream5xx exceptions HttpVerbs converts:
    *
    *   - Upstream 400 => BadRequestException
    *   - Upstream 404 => NotFoundException
    *   - ConnectException => BadGatewayException
    *   - TimeoutException => GatewayTimeoutException
    *
    * Everything else is left as a Upstream4xxException / Upstream5xxException with the response body and upstream code
    */

  // Exceptions

  def groProxyDownPF(
    method: String
  )(implicit payload: Payload, request: Request[JsValue]): PartialFunction[Throwable, Result] = {
    case e: BadGatewayException if payload.whereBirthRegistered == ENGLAND || payload.whereBirthRegistered == WALES =>
      serviceUnavailable(method, "GRO down", e, ErrorResponse.GRO_CONNECTION_DOWN)
    case e @ UpstreamErrorResponse(_, BAD_GATEWAY, _, _)
        if payload.whereBirthRegistered == ENGLAND || payload.whereBirthRegistered == WALES =>
      serviceUnavailable(method, "GRO down", e, ErrorResponse.GRO_CONNECTION_DOWN)
  }

  def desConnectionDownPF(
    method: String
  )(implicit payload: Payload, request: Request[JsValue]): PartialFunction[Throwable, Result] = {
    case e: BadGatewayException if payload.whereBirthRegistered == SCOTLAND                          =>
      serviceUnavailable(method, "DES down", e, ErrorResponse.DES_CONNECTION_DOWN)
    case e @ UpstreamErrorResponse(_, BAD_GATEWAY, _, _) if payload.whereBirthRegistered == SCOTLAND =>
      serviceUnavailable(method, "DES down", e, ErrorResponse.DES_CONNECTION_DOWN)
  }

  def desInvalidHeadersBadRequestPF(
    method: String
  )(implicit payload: Payload, request: Request[JsValue]): PartialFunction[Throwable, Result] = {
    case e: BadRequestException if e.message.contains("INVALID_HEADER") =>
      InternalServerErrorException(method, e, BAD_REQUEST)
  }

  def badRequestExceptionPF(
    method: String
  )(implicit payload: Payload, request: Request[JsValue]): PartialFunction[Throwable, Result] = {
    case e: BadRequestException =>
      InternalServerErrorException(method, e, BAD_REQUEST)
  }

  def notImplementedExceptionPF(
    method: String
  )(implicit payload: Payload, request: Request[JsValue]): PartialFunction[Throwable, Result] = {
    case e: NotImplementedException =>
      logException(method, s"[Not implemented]: [${e.getMessage}]", OK)
      respondNoMatch()
  }

  def notFoundExceptionPF(
    method: String
  )(implicit payload: Payload, request: Request[JsValue]): PartialFunction[Throwable, Result] = {
    case e: NotFoundException =>
      logException(method, s"[Not found]: ${e.getMessage}", NOT_FOUND)
      respondNoMatch()
  }

  def exceptionPF(
    method: String
  )(implicit payload: Payload, request: Request[JsValue]): PartialFunction[Throwable, Result] = { case e: Throwable =>
    InternalServerErrorException(method, e)
  }

  // UpstreamResponse(s)

  def forbiddenUpstreamPF(
    method: String
  )(implicit payload: Payload, request: Request[JsValue]): PartialFunction[Throwable, Result] = {
    case UpstreamErrorResponse(body, FORBIDDEN, _, _) =>
      // this is correct as Forbidden 403 does not get converted into an Exception, we get the upstream code
      logException(method, s"[Forbidden / Not found]: [$body]", FORBIDDEN)
      respondNoMatch()
  }

  def gatewayTimeoutPF(
    method: String
  )(implicit payload: Payload, request: Request[JsValue]): PartialFunction[Throwable, Result] = {
    case e @ UpstreamErrorResponse(body, GATEWAY_TIMEOUT, _, _) =>
      logException(method, s"[Gateway timeout]: [$body]", GATEWAY_TIMEOUT)
      InternalServerErrorException(method, e, GATEWAY_TIMEOUT)
  }

  def groConnectionDownPF(
    method: String
  )(implicit payload: Payload, request: Request[JsValue]): PartialFunction[Throwable, Result] = {
    case e @ UpstreamErrorResponse(body, upstream, _, _)
        if Exception5xx.unapply(
          upstream
        ) && (payload.whereBirthRegistered == ENGLAND || payload.whereBirthRegistered == WALES) =>
      logException(method, s"[GRO down]: $body [status]: $upstream", SERVICE_UNAVAILABLE)
      serviceUnavailable(method, "GRO down", e, ErrorResponse.GRO_CONNECTION_DOWN)
  }

  def nrsConnectionDownPF(
    method: String
  )(implicit payload: Payload, request: Request[JsValue]): PartialFunction[Throwable, Result] = {
    case e @ UpstreamErrorResponse(body, upstream, _, _) if payload.whereBirthRegistered == SCOTLAND =>
      logException(method, s"[NRS down]: [$body] [status]: $upstream", SERVICE_UNAVAILABLE)
      serviceUnavailable(method, "[NRS down]", e, ErrorResponse.NRS_CONNECTION_DOWN)
  }

}

private object Exception5xx {
  def unapply(exception: Int): Boolean =
    exception >= 500 && exception < 600
}

private object Exception4xx {
  def unapply(exception: Int): Boolean =
    exception >= 400 && exception < 500
}

object BrmExceptionMessage {
  def message(method: String, statusCode: Int, requestId: Option[String] = None): String =
    s"[respond][$method], status: $statusCode${if (requestId.isDefined) s", requestId: ${requestId.get}" else ""}"
}
