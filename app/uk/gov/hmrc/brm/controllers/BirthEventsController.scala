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
import play.api.libs.json._
import play.api.mvc.{Request, Result}
import uk.gov.hmrc.brm.audit.BRMAudit
import uk.gov.hmrc.brm.config.BrmConfig
import uk.gov.hmrc.brm.implicits.Implicits._
import uk.gov.hmrc.brm.metrics.Metrics
import uk.gov.hmrc.brm.models.brm.Payload
import uk.gov.hmrc.brm.services.LookupService
import uk.gov.hmrc.brm.utils.BrmLogger._
import uk.gov.hmrc.brm.utils.{BirthResponseBuilder, HeaderValidator, Keygenerator}
import uk.gov.hmrc.play.http._
import uk.gov.hmrc.play.microservice.controller

import scala.concurrent.Future

object BirthEventsController extends BirthEventsController {
  override val service = LookupService
}

trait BirthEventsController extends controller.BaseController with HeaderValidator {

  val CLASS_NAME : String = this.getClass.getCanonicalName

  import scala.concurrent.ExecutionContext.Implicits.global

  protected val service: LookupService

  private def respond(response: Result) = {
    response
      .as("application/json; charset=utf-8")
      .withHeaders((ACCEPT, "application/vnd.hmrc.1.0+json"))
  }

  private def handleException(method: String)(implicit payload: Payload): PartialFunction[Throwable, Result] = {
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

  private def validateDob(d: LocalDate): Boolean = {
    BrmConfig.validateDobForGro match {
      case true =>
        val validDate = new LocalDate(BrmConfig.minimumDateValueForGroValidation)
        d.isAfter(validDate) || d.isEqual(validDate)
      case false =>
        true
    }
  }

  def post() = validateAccept(acceptHeaderValidationRules).async(parse.json) {
    implicit request =>
      generateAndSetKey(request)
      request.body.validate[Payload].fold(
        error => {
          BRMAudit.auditWhereBirthRegistered(error)
          info(CLASS_NAME, "post()",s" error: $error")
          Future.successful(respond(BadRequest("")))
        },
        payload => {
          implicit val p : Payload = payload
          implicit val metrics : Metrics = getMetrics()

          if (!validateDob(p.dateOfBirth)) {
            // date of birth is before acceptable date
            info(CLASS_NAME, "post()", s"date of birth is before date accepted by GRO, returned match=false")
            Future.successful(respond(Ok(Json.toJson(BirthResponseBuilder.withNoMatch()))))
          } else {
            info(CLASS_NAME, "post()", s"payload and date of birth is valid attempting lookup")
            service.lookup() map {
              bm => {
                getMetrics().connectorStatus(OK)
                info(CLASS_NAME, "post()", s"BirthMatchResponse received")
                info(CLASS_NAME, "post()", s"matched: ${bm.matched}")
                respond(Ok(Json.toJson(bm)))
              }
            } recover handleException("getReference")
          }
        }

      )
  }


  private def generateAndSetKey(request: Request[JsValue]): Unit = {
    val key = Keygenerator.generateKey(request)
    Keygenerator.setKey(key)
  }

}
