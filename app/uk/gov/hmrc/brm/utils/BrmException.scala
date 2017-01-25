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
import uk.gov.hmrc.brm.models.brm.Payload
import uk.gov.hmrc.play.http._
import uk.gov.hmrc.brm.utils.BrmLogger._

trait BrmException extends Controller {

  val CLASS_NAME: String = "BrmException"

  val METHOD_NAME: String = "handleException"

  def notFoundPF(message: String)(implicit payload: Payload) : PartialFunction[Throwable, Result] = {
    case Upstream4xxResponse(m, NOT_FOUND, _, _) =>
      logException(Some(message), Some("notFound"), NOT_FOUND)
      Ok(Json.toJson(BirthResponseBuilder.withNoMatch()))
  }

  def badRequestPF(message: String)(implicit payload: Payload) : PartialFunction[Throwable, Result] = {
    case Upstream4xxResponse(m, BAD_REQUEST, _, _) =>
      logException(Some(message), Some("BadRequest"), BAD_REQUEST)
      BadRequest(message)
  }

  def badGatewayPF(message: String)(implicit payload: Payload) : PartialFunction[Throwable, Result] = {
    case Upstream5xxResponse(m, BAD_GATEWAY, _) =>
      logException(Some(message), Some("BadGateway"), BAD_GATEWAY)
      BadGateway(message)
  }

  def gatewayTimeoutPF(message: String)(implicit payload: Payload) : PartialFunction[Throwable, Result] = {
    case Upstream5xxResponse(m, GATEWAY_TIMEOUT, _) =>
      logException(Some(message), Some("GatewayTimeout"), GATEWAY_TIMEOUT)
      GatewayTimeout(message)
  }

  def upstreamErrorPF(message: String)(implicit payload: Payload) : PartialFunction[Throwable, Result] = {
    case Upstream5xxResponse(m, upstreamCode, _) =>
      logException(Some(message), Some(s"InternalServerError: code: $upstreamCode"), INTERNAL_SERVER_ERROR)
      InternalServerError
  }

  def badRequestExceptionPF(message: String)(implicit payload: Payload) : PartialFunction[Throwable, Result] = {
    case e: BadRequestException =>
      logException(Some(message), Some(s"BadRequestException: ${e.getMessage}"), BAD_REQUEST)
      BadRequest(e.getMessage)
  }

  def notImplementedExceptionPF(message: String)(implicit payload: Payload) : PartialFunction[Throwable, Result] = {
    case e: NotImplementedException =>
      logException(Some(message), Some(s"NotImplementedException: ${e.getMessage}"), OK)
      Ok(Json.toJson(BirthResponseBuilder.withNoMatch()))
  }

  private def logException(message: Option[String] = None, status: Option[String] = None, statusCode: Int)(implicit payload: Payload) = {
    MetricsFactory.getMetrics().status(statusCode)
    statusCode match {
      case Exception5xx() => error(CLASS_NAME, METHOD_NAME, BrmExceptionMessage.message(message, status, statusCode))
      case Exception4xx() => info(CLASS_NAME, METHOD_NAME, BrmExceptionMessage.message(message, status, statusCode))
      case _ => info(CLASS_NAME, METHOD_NAME, BrmExceptionMessage.message(message, status, statusCode))
    }
  }

  def notFoundExceptionPF(message: String)(implicit payload: Payload) : PartialFunction[Throwable, Result] = {
    case e: NotFoundException =>
      logException(Some(message), Some(s"NotFound: ${e.getMessage}"), NOT_FOUND)
      Ok(Json.toJson(BirthResponseBuilder.withNoMatch()))
  }

  def exceptionPF(message: String)(implicit payload: Payload) : PartialFunction[Throwable, Result] = {
    case e: Exception =>
      logException(Some(message), Some(s"InternalServerError: message: $e"), INTERNAL_SERVER_ERROR)
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
  def message(message: Option[String] = None, status: Option[String] = None, statusCode: Int): String
}

object BrmExceptionMessage extends ExceptionMessage {
  def message(message: Option[String] = None, status: Option[String] = None, statusCode: Int): String = {
    s"[respond] ${status.get}: ${message.get}"
  }
}
