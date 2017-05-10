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

package uk.gov.hmrc.brm.utils

import play.api.libs.json.Json
import play.api.mvc.{Controller, Result}
import uk.gov.hmrc.brm.implicits.Implicits._
import uk.gov.hmrc.brm.models.brm.{ErrorResponse, Payload}
import uk.gov.hmrc.brm.utils.BRMLogger._
import uk.gov.hmrc.play.http._
import uk.gov.hmrc.brm.utils.BirthRegisterCountry._

trait BRMException extends Controller {

  val CLASS_NAME: String = "BRMException"
  val METHOD_NAME: String = "handleException"

  private def logException(method: String, body: String, statusCode: Int)(implicit payload: Payload) = {
    MetricsFactory.getMetrics().status(statusCode)
    statusCode match {
      case Exception5xx() => error(CLASS_NAME, METHOD_NAME, BrmExceptionMessage.message(method, body, statusCode))
      case Exception4xx() => info(CLASS_NAME, METHOD_NAME, BrmExceptionMessage.message(method, body, statusCode))
      case _ => info(CLASS_NAME, METHOD_NAME, BrmExceptionMessage.message(method, body, statusCode))
    }
  }

  private def InternalServerErrorException(method: String, e : Throwable, upstreamCode : Int = INTERNAL_SERVER_ERROR)(implicit payload: Payload) = {
    e.printStackTrace()
    logException(method, s"InternalServerError: ${e.getMessage}", upstreamCode)
    InternalServerError
  }

  private def serviceUnavailable(method : String, cause: String, e : Exception, body: String)(implicit payload: Payload) = {
    logException(method, s"serviceUnavailable - BadGateway $cause: ${e.getMessage}", SERVICE_UNAVAILABLE)
    ServiceUnavailable(body)
  }

  private def respondNoMatch() = {
    Ok(Json.toJson(BirthResponseBuilder.withNoMatch()))
  }

  /**
    * Map on e : Exceptions NOT Upstream4xx as HttpVerbs converts these
    * into Exceptions that are thrown instead of returning the Upstream4xx / Upstream5xx exceptions
    * HttpVerbs converts:
    *
    * - Upstream 400 => BadRequestException
    * - Upstream 404 => NotFoundException
    * - ConnectException => BadGatewayException
    * - TimeoutException => GatewayTimeoutException
    *
    * Everything else is left as a Upstream4xxException / Upstream5xxException with the response body and upstream code
    */

  // Exceptions

  def groProxyDownPF(method: String)(implicit payload: Payload): PartialFunction[Throwable, Result] = {
    case e: BadGatewayException if payload.whereBirthRegistered == ENGLAND  || payload.whereBirthRegistered == WALES =>
      serviceUnavailable(method, "groProxyDownPF", e, ErrorResponse.GRO_CONNECTION_DOWN)
    case e @ Upstream5xxResponse(_, BAD_GATEWAY, _) if payload.whereBirthRegistered == ENGLAND  || payload.whereBirthRegistered == WALES =>
      serviceUnavailable(method, "groProxyDownPF", e, ErrorResponse.GRO_CONNECTION_DOWN)
  }

  def desConnctionDownPF(method: String)(implicit payload: Payload): PartialFunction[Throwable, Result] = {
    case e: BadGatewayException if payload.whereBirthRegistered == SCOTLAND =>
      serviceUnavailable(method, "desConnctionDownPF", e, ErrorResponse.DES_CONNECTION_DOWN)
    case e @ Upstream5xxResponse(_, BAD_GATEWAY, _) if payload.whereBirthRegistered == SCOTLAND =>
      serviceUnavailable(method, "desConnctionDownPF", e, ErrorResponse.DES_CONNECTION_DOWN)
  }

  def desInvalidHeadersBadRequestPF(method: String)(implicit payload: Payload): PartialFunction[Throwable, Result] = {
    case e: BadRequestException if e.message.contains("INVALID_HEADER") =>
      logException(method, s"desInvalidHeadersBadRequestPF - BadRequestException INVALID_HEADER: ${e.getMessage}", INTERNAL_SERVER_ERROR)
      InternalServerErrorException(method, e, BAD_REQUEST)
  }

  def badRequestExceptionPF(method: String)(implicit payload: Payload): PartialFunction[Throwable, Result] = {
    case e: BadRequestException =>
      logException(method, s"BadRequestException: ${e.getMessage}", BAD_REQUEST)
      InternalServerErrorException(method, e, BAD_REQUEST)
  }

  def notImplementedExceptionPF(method: String)(implicit payload: Payload): PartialFunction[Throwable, Result] = {
    case e: NotImplementedException =>
      logException(method, s"NotImplementedException: ${e.getMessage}", OK)
      respondNoMatch()
  }

  def notFoundExceptionPF(method: String)(implicit payload: Payload): PartialFunction[Throwable, Result] = {
    case e: NotFoundException =>
      logException(method, s"NotFoundException: ${e.getMessage}", NOT_FOUND)
      respondNoMatch()
  }

  def exceptionPF(method: String)(implicit payload: Payload): PartialFunction[Throwable, Result] = {
    case e: Throwable =>
      InternalServerErrorException(method, e)
  }

  // UpstreamResponse(s)

  def forbiddenUpstreamPF(method: String)(implicit payload: Payload): PartialFunction[Throwable, Result] = {
    case Upstream4xxResponse(body, FORBIDDEN, _, _) =>
      // this is correct as Forbidden 403 does not get converted into an Exception, we get the upstream code
      logException(method, s"forbiddenUpstreamPF - Upstream4xxResponse: $body", FORBIDDEN)
      respondNoMatch()
  }

  def gatewayTimeoutPF(method: String)(implicit payload: Payload): PartialFunction[Throwable, Result] = {
    case e @ Upstream5xxResponse(body, GATEWAY_TIMEOUT, _) =>
      logException(method, s"gatewayTimeoutPF - Upstream5xxResponse: $body", GATEWAY_TIMEOUT)
      InternalServerErrorException(method, e, GATEWAY_TIMEOUT)
  }

  def groConnctionDownPF(method: String)(implicit payload: Payload): PartialFunction[Throwable, Result] = {
    case e: Upstream5xxResponse if e.message.contains("GRO_CONNECTION_DOWN") =>
      serviceUnavailable(method, "groConnctionDownPF", e, ErrorResponse.GRO_CONNECTION_DOWN)
  }

  def nrsConnctionDownPF(method: String)(implicit payload: Payload): PartialFunction[Throwable, Result] = {
    case e: Upstream5xxResponse if e.message.contains("SERVICE_UNAVAILABLE") =>
      serviceUnavailable(method, "nrsConnctionDownPF", e, ErrorResponse.NRS_CONNECTION_DOWN)
  }

  def upstreamErrorPF(method: String)(implicit payload: Payload): PartialFunction[Throwable, Result] = {
    case e @ Upstream5xxResponse(body, upstream, _) =>
      logException(method, s"upstreamErrorPF - Upstream5xxResponse: $body", upstream)
      InternalServerErrorException(method, e, upstream)
  }

}

private object Exception5xx {
  def unapply(exception: Int): Boolean = {
    exception >= 500 && exception < 600
  }
}

private object Exception4xx {
  def unapply(exception: Int): Boolean = {
    exception >= 400 && exception < 500
  }
}

trait ExceptionMessage {
  def message(message: String, status: String, statusCode: Int): String
}

object BrmExceptionMessage extends ExceptionMessage {
  def message(method: String, body: String, statusCode: Int): String = {
    s"[respond] [method]: $method, [body]: $body, [status]: $statusCode"
  }
}