/*
 * Copyright 2016 HM Revenue & Customs
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

import org.joda.time.LocalDate
import play.api.http.HeaderNames
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Controller, Request, Result}
import uk.gov.hmrc.brm.config.BrmConfig
import uk.gov.hmrc.brm.implicits.Implicits._
import uk.gov.hmrc.brm.models.brm.Payload
import uk.gov.hmrc.brm.utils.BrmLogger._
import uk.gov.hmrc.play.http._

import scala.util.matching.Regex
import scala.util.matching.Regex.Match

trait ControllerUtil {
  self: Controller =>

  val CLASS_NAME : String = "ControllerUtil"

  def handleException(method: String)(implicit payload: Payload): PartialFunction[Throwable, Result] = {
    case Upstream4xxResponse(message, NOT_FOUND, _, _) =>
      getMetrics().connectorStatus(NOT_FOUND)
      warn(CLASS_NAME, "handleException", s"[$method] NotFound: $message.")
      respond(Ok(Json.toJson(BirthResponseBuilder.withNoMatch())))
    case Upstream4xxResponse(message, BAD_REQUEST, _, _) =>
      getMetrics().connectorStatus(BAD_REQUEST)
      warn(CLASS_NAME, "handleException", s"[$method] BadRequest: $message.")
      respond(BadRequest(message))
    case Upstream5xxResponse(message, BAD_GATEWAY, _) =>
      getMetrics().connectorStatus(BAD_GATEWAY)
      error(CLASS_NAME, "handleException",s"[$method] BadGateway: $message")
      respond(BadGateway(message))
    case Upstream5xxResponse(message, GATEWAY_TIMEOUT, _) =>
      getMetrics().connectorStatus(GATEWAY_TIMEOUT)
      error(CLASS_NAME, "handleException",s"[BirthEventsController][Connector][$method] GatewayTimeout: $message")
      respond(GatewayTimeout(message))
    case Upstream5xxResponse(message, upstreamCode, _) =>
      getMetrics().connectorStatus(INTERNAL_SERVER_ERROR)
      error(CLASS_NAME, "handleException", s"[$method] InternalServerError: code: $upstreamCode message: $message")
      respond(InternalServerError)
    case e: BadRequestException =>
      getMetrics().connectorStatus(BAD_REQUEST)
      warn(CLASS_NAME, "handleException",s"[$method] BadRequestException: ${e.getMessage}")
      respond(BadRequest(e.getMessage))
    case e: NotImplementedException =>
      getMetrics().connectorStatus(OK)
      info(CLASS_NAME, "handleException", s"[BirthEventsController][handleException][$method] NotImplementedException: ${e.getMessage}")
      respond(Ok(Json.toJson(BirthResponseBuilder.withNoMatch())))
    case e: NotFoundException =>
      getMetrics().connectorStatus(NOT_FOUND)
      warn(CLASS_NAME, "handleException",s"[$method] NotFound: ${e.getMessage}")
      respond(Ok(Json.toJson(BirthResponseBuilder.withNoMatch())))
    case e: Exception =>
      error(CLASS_NAME, "handleException", s"[$method] InternalServerError: message: $e")
      respond(InternalServerError)
  }

  def respond(response: Result) = {
    response
      .as("application/json; charset=utf-8")
      .withHeaders((ACCEPT, "application/vnd.hmrc.1.0+json"))
  }


}


object CommonUtil {
  val CLASS_NAME : String = "CommonUtil"
  val versionKey: String = "version"
  val matchHeader: String => Option[Match] =
    new Regex( """^application/vnd[.]{1}hmrc[.]{1}(.*?)[+]{1}(.*)$""", versionKey, "contenttype") findFirstMatchIn _


  def getApiVersion(request: Request[JsValue]): String = {
    val accept = request.headers.get(HeaderNames.ACCEPT)
    val apiVersion = accept.flatMap(
      a =>
        matchHeader(a.toLowerCase) map (
          res => res.group(versionKey)
          )
    ) getOrElse ""
    apiVersion
  }

  /**
    * validate date of birth is after or equal to config date.
    * @param d
    * @return true if valid date.
    */
  def validateDob(d: LocalDate): Boolean = {
    BrmConfig.validateDobForGro match {
      case true =>
        val validDate = new LocalDate(BrmConfig.minimumDateValueForGroValidation)
        d.isAfter(validDate) || d.isEqual(validDate)
      case false =>
        true
    }
  }



}
