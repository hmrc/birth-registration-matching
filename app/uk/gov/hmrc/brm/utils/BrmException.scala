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

trait BRMException extends Controller {

  val CLASS_NAME: String = "BrmException"
  val METHOD_NAME: String = "handleException"

  private def logException(method: String, body: String, statusCode: Int)(implicit payload: Payload) = {
    MetricsFactory.getMetrics().status(statusCode)
    statusCode match {
      case Exception5xx() => error(CLASS_NAME, METHOD_NAME, BrmExceptionMessage.message(method, body, statusCode))
      case Exception4xx() => info(CLASS_NAME, METHOD_NAME, BrmExceptionMessage.message(method, body, statusCode))
      case _ => info(CLASS_NAME, METHOD_NAME, BrmExceptionMessage.message(method, body, statusCode))
    }
  }

  def notFoundPF(method: String)(implicit payload: Payload) : PartialFunction[Throwable, Result] = {
    case Upstream4xxResponse(body, NOT_FOUND, _, _) =>
      logException(method, body, NOT_FOUND)
      Ok(Json.toJson(BirthResponseBuilder.withNoMatch()))
  }

  def forbiddenPF(method: String)(implicit payload: Payload) : PartialFunction[Throwable, Result] = {
    case Upstream4xxResponse(body, FORBIDDEN, _, _) =>
      logException(method, body, FORBIDDEN)
      Ok(Json.toJson(BirthResponseBuilder.withNoMatch()))
  }

  def badRequestPF(method: String)(implicit payload: Payload) : PartialFunction[Throwable, Result] = {
    case Upstream4xxResponse(body, BAD_REQUEST, _, _) =>
      logException(method, body, BAD_REQUEST)
      BadRequest
  }

  def badGatewayPF(method: String)(implicit payload: Payload) : PartialFunction[Throwable, Result] = {
    case e: BadGatewayException if payload.whereBirthRegistered == BirthRegisterCountry.SCOTLAND  =>
      logException(method, s"BadGatewayException: ${e.getMessage}", BAD_GATEWAY)
      logException(method, s"Response body: ${ErrorResponse.DES_CONNECTION_DOWN}", BAD_GATEWAY)
      ServiceUnavailable(ErrorResponse.DES_CONNECTION_DOWN)
    case Upstream5xxResponse(body, BAD_GATEWAY, _) =>
      logException(method, body, BAD_GATEWAY)
      BadGateway
  }

  def gatewayTimeoutPF(method: String)(implicit payload: Payload) : PartialFunction[Throwable, Result] = {
    case Upstream5xxResponse(body, GATEWAY_TIMEOUT, _) =>
      logException(method, body, GATEWAY_TIMEOUT)
      GatewayTimeout
  }

  def upstreamErrorPF(method: String)(implicit payload: Payload) : PartialFunction[Throwable, Result] = {
    case e: Upstream5xxResponse if e.message.contains("GRO_CONNECTION_DOWN") =>
      logException(method, s"ServiceUnavailable: ${e.getMessage}", SERVICE_UNAVAILABLE)
      logException(method, s"Response body: ${ErrorResponse.GRO_CONNECTION_DOWN}", SERVICE_UNAVAILABLE)
      ServiceUnavailable(ErrorResponse.GRO_CONNECTION_DOWN)
    case e: Upstream5xxResponse if e.message.contains("SERVICE_UNAVAILABLE") =>
      logException(method, s"ServiceUnavailable: ${e.getMessage}", SERVICE_UNAVAILABLE)
      logException(method, s"Response body: ${ErrorResponse.NRS_CONNECTION_DOWN}", SERVICE_UNAVAILABLE)
      ServiceUnavailable(ErrorResponse.NRS_CONNECTION_DOWN)
    case Upstream5xxResponse(body, upstreamCode, _) =>
      logException(method, s"$body, upstream: $upstreamCode", INTERNAL_SERVER_ERROR)
      InternalServerError
  }

  def badRequestExceptionPF(method: String)(implicit payload: Payload) : PartialFunction[Throwable, Result] = {
    case e: BadRequestException if e.message.contains("INVALID_HEADER") =>
      logException(method, s"BadRequestException from INVALID_HEADER converted into InternalServerError: ${e.getMessage}", INTERNAL_SERVER_ERROR)
      InternalServerError
    case e: BadRequestException =>
      logException(method, s"BadRequestException: ${e.getMessage}", BAD_REQUEST)
      BadRequest
  }

  def notImplementedExceptionPF(method: String)(implicit payload: Payload) : PartialFunction[Throwable, Result] = {
    case e: NotImplementedException =>
      logException(method, s"NotImplementedException: ${e.getMessage}", OK)
      Ok(Json.toJson(BirthResponseBuilder.withNoMatch()))
  }

  def notFoundExceptionPF(method: String)(implicit payload: Payload) : PartialFunction[Throwable, Result] = {
    case e: NotFoundException =>
      logException(method, s"NotFound: ${e.getMessage}", NOT_FOUND)
      Ok(Json.toJson(BirthResponseBuilder.withNoMatch()))
  }

  def exceptionPF(method: String)(implicit payload: Payload) : PartialFunction[Throwable, Result] = {
    case e: Throwable =>
      logException(method, s"InternalServerError: $e", INTERNAL_SERVER_ERROR)
      InternalServerError
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
