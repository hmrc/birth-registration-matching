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

package uk.gov.hmrc.brm.controllers

import org.joda.time.LocalDate
import play.api.Logger
import play.api.libs.json.Json
import play.api.mvc.Result
import uk.gov.hmrc.brm.config.BrmConfig
import uk.gov.hmrc.brm.models.brm.Payload
import uk.gov.hmrc.brm.services.LookupService
import uk.gov.hmrc.brm.utils.{BirthResponseBuilder, HeaderValidator}
import uk.gov.hmrc.play.http._
import uk.gov.hmrc.play.microservice.controller

import scala.concurrent.Future


/**
  * Created by chrisianson on 25/07/16.
  */
object BirthEventsController extends BirthEventsController {

  override val service = LookupService
}

trait BirthEventsController extends controller.BaseController with HeaderValidator {

  import scala.concurrent.ExecutionContext.Implicits.global

  protected val service : LookupService

  private def respond(response : Result) = {
    response
      .as("application/json")
      .withHeaders((ACCEPT, "application/vnd.hmrc.1.0+json"))
      .withHeaders((CONTENT_TYPE, "application/json"))
  }

  private def handleException(method: String) : PartialFunction[Throwable, Result] = {

    case Upstream4xxResponse(message, NOT_FOUND, _, _) =>
      Logger.warn(s"[BirthEventsController][Connector][$method] NotFound: $message")
      respond(Ok(Json.toJson(BirthResponseBuilder.withNoMatch())))
    case Upstream4xxResponse(message, BAD_REQUEST, _, _) =>
      Logger.warn(s"[BirthEventsController][Connector][$method] BadRequest: $message")
      respond(BadRequest(message))
    case Upstream5xxResponse(message, BAD_GATEWAY, _) =>
      Logger.warn(s"[BirthEventsController][Connector][$method] BadGateway: $message")
      respond(BadGateway(message))
    case Upstream5xxResponse(message, GATEWAY_TIMEOUT, _) =>
      Logger.warn(s"[BirthEventsController][Connector][$method] GatewayTimeout: $message")
      respond(GatewayTimeout(message))
    case e :  BadRequestException =>
      Logger.warn(s"[BirthEventsController][Connector][$method] BadRequestException: ${e.getMessage}")
      respond(BadRequest(e.getMessage))
    case e : NotImplementedException  =>
      Logger.warn(s"[BirthEventsController][handleException][$method] NotImplementedException: ${e.getMessage}")
      respond(Ok(Json.toJson(BirthResponseBuilder.withNoMatch())))
    case Upstream5xxResponse(message, upstreamCode, _) =>
      Logger.error(s"[BirthEventsController][Connector][$method] InternalServerError: code: $upstreamCode message: $message")
      respond(InternalServerError)
    case e : NotFoundException =>
      Logger.warn(s"[BirthEventsController][Connector][$method] NotFound: ${e.getMessage}")
      respond(Ok(Json.toJson(BirthResponseBuilder.withNoMatch())))
    case e : Exception =>
      Logger.error(s"[BirthEventsController][Connector][$method] InternalServerError: message: ${e}")
      respond(InternalServerError)
  }



 private def validateDob(d: LocalDate): Boolean = {
    BrmConfig.validateDobForGro match {
      case true =>
        val validDate = new LocalDate("2009-07-01")
        d.isAfter(validDate) || d.isEqual(validDate)
      case false =>
        true
    }
  }

 def post() = validateAccept(acceptHeaderValidationRules).async(parse.json) {
     implicit request =>
       request.body.validate[Payload].fold(
         error => {
           Logger.info(s"[BirthEventsController][Connector][getReference] error: $error")
           Future.successful(respond(BadRequest("")))
         },
         payload => {
          payload match {
             case x: Payload if !validateDob(x.dateOfBirth) =>
               Logger.debug(s"[BirthEventsController][post] validateDob returned false.")
               Future.successful(respond(Ok(Json.toJson(BirthResponseBuilder.withNoMatch()))))
             case _ =>
               Logger.debug(s"[BirthEventsController][Connector][getReference] payload matched.")
               service.lookup(payload) map {
                 bm => {
                   Logger.debug(s"[BirthEventsController][Connector][getReference] response received.")
                   respond(Ok(Json.toJson(bm)))
                 }
               } recover handleException("getReference")
           }
         }
      )
   }

}
