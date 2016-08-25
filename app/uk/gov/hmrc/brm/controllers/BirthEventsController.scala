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

import play.api.Logger
import play.api.libs.json.Json
import play.api.mvc.Result
import uk.gov.hmrc.brm.models.Payload
import uk.gov.hmrc.brm.services.LookupService
import uk.gov.hmrc.brm.utils.{BirthResponseBuilder, HeaderValidator}
import uk.gov.hmrc.play.http.{BadRequestException, Upstream4xxResponse, Upstream5xxResponse}
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
  }

  private def handleException(method: String) : PartialFunction[Throwable, Result] = {
    case e : Upstream4xxResponse if e.reportAs == NOT_FOUND =>
      Logger.warn(s"[BirthEventsController][Connector][$method] BadRequest: ${e.getMessage}")
      respond(Ok(Json.toJson(BirthResponseBuilder.withNoMatch())))
    case e :  Upstream4xxResponse if e.reportAs == BAD_REQUEST  =>
      Logger.warn(s"[BirthEventsController][Connector][$method] BadRequest: ${e.getMessage}")
      respond(BadRequest(e.getMessage))
    case e :  BadRequestException =>
      Logger.warn(s"[BirthEventsController][Connector][$method] BadRequest: ${e.getMessage}")
      respond(BadRequest(e.getMessage))
    case e : Upstream5xxResponse =>
      Logger.error(s"[BirthEventsController][Connector][$method] InternalServerError: ${e.message}")
      respond(InternalServerError(e.message))
  }

  def post() = validateAccept(acceptHeaderValidationRules).async(parse.json) {
     implicit request =>
       request.body.validate[Payload].fold(
         error => {
           Logger.info(s"[BirthEventsController][Connector][getReference] error: $error")
           Future.successful(respond(BadRequest("")))
         },
         payload => {
           Logger.debug(s"[BirthEventsController][Connector][getReference] payload validated.")
           service.lookup(payload) map {
             bm => {
               Logger.debug(s"[BirthEventsController][Connector][getReference] response received.")
               respond(Ok(Json.toJson(bm)))
             }
           }

         }
       ) recover handleException("getReference")
   }
}
