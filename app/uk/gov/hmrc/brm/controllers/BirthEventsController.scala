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

import app.Routes

import scala.concurrent.Future

import play.api.Logger
import play.api.libs.json.Json
import play.api.mvc._
import uk.gov.hmrc.brm.connectors.{BirthConnector, GROEnglandAndWalesConnector}
import uk.gov.hmrc.brm.models._
import uk.gov.hmrc.play.http.{HeaderCarrier, Upstream4xxResponse, Upstream5xxResponse}
import uk.gov.hmrc.play.microservice.controller


/**
  * Created by chrisianson on 25/07/16.
  */
object BirthEventsController extends BirthEventsController {
  override val Connector = GROEnglandAndWalesConnector
}

trait BirthEventsController extends controller.BaseController {

  import scala.concurrent.ExecutionContext.Implicits.global

  val Connector : BirthConnector

  private def respond(response : Result) = {
    response.as("application/json")
  }

  object BRMAction extends ActionBuilder[BRMRequest] {
    def invokeBlock[A](request: Request[A], block: (BRMRequest[A] => Future[Result])) : Future[Result] = {

//      val headers = BRMRequest(
//        request = request,
//        brmHeaders = BRMHeaders(
//          apiVersion = request.headers.get(BRMHeaderNames.ApiVersion).map(x => APIVersion(x.toDouble)),
//          auditSource = request.headers.get(BRMHeaderNames.AuditSource).map(AuditSource)
//        )
//      )

      (request.headers.get("Api-Version"), request.headers.get("AuditSource")) match {
        case (Some(version), Some(audit)) =>
          try {
            val brmRequest = BRMRequest(request, BRMHeaders(
              apiVersion = version.toDouble,
              auditSource = audit
            ))
            Logger.info(s"[BRMAction][Received request from]: ${brmRequest.brmHeaders.auditSource}")

//            block(brmRequest)
          } catch {
            case e : Exception => Future.successful(BadRequest("Api-Version is not a number"))
          }
        case (Some(x), _) => Future.successful(BadRequest("Please provide AuditSource"))
        case (_, Some(x)) => Future.successful(BadRequest("Please provide Api-Version"))
        case (_, _) => Future.successful(BadRequest("Please provide Api-Version and AuditSource"))
      }
    }
  }

//  trait BRMFilter extends Filter {
//
//    def apply(next: (RequestHeader) => Future[Result])(rh: RequestHeader) : Future[Result] = {
////      val verb = rh.tags.get(Routes.ROUTE_VERB)
//
//      val headers = BRMHeaders(
//          apiVersion = rh.headers.get(BRMHeaderNames.ApiVersion).map(x => APIVersion(x.toDouble)),
//          auditSource = rh.headers.get(BRMHeaderNames.AuditSource).map(AuditSource)
//      )
//      headers match {
//        case BRMHeaders(Some(version), Some(audit)) =>
//          version.value match {
//            case 1.0 =>
//              // redirect to version 1
//
//              next(rh)
//            case _ =>
//              Future.successful(BadRequest("Please provide Api-Version"))
//          }
//        case BRMHeaders(_, _) =>
//          Future.successful(BadRequest("Please provide Api-Version and AuditSource"))
//      }
//    }
//
//  }

//  def something = BRMAction.async(parse.json) {
//    request =>
//      Future.successful(Ok(""))
//  }

  private def handleException(method: String) : PartialFunction[Throwable, Result] = {
    case e : Upstream4xxResponse =>
      Logger.warn(s"[MatchingController][GROConnector][$method] BadRequest: ${e.message}")
      respond(BadRequest(e.message))
    case e : Upstream5xxResponse =>
      Logger.error(s"[MatchingController][GROConnector][$method] InternalServerError: ${e.message}")
      respond(InternalServerError(e.message))
  }

  def post() = Action.async(parse.json) {
    implicit request =>
      request.body.validate[Payload].fold(
        error => {
          Future.successful(respond(BadRequest("")))
        },
        payload => {
          payload.reference.fold(
            // make request to details stubbed out at the moment
            Future.successful(respond(Ok("no match")))
          )(
            reference =>
              // make request with reference number
              Connector.getReference(reference) map {
                response =>
                  Logger.debug(s"[BirthEventsController][GROConnector][getReference] Success: $response")
                  val firstName = (response \ "subjects" \ "child" \ "name" \ "givenName").as[String]
                  val surname = (response \ "subjects" \ "child" \ "name" \ "surname").as[String]
                  val isMatch = firstName.equals(payload.forename) && surname.equals(payload.surname)
                  val result = Json.parse(
                    s"""
                       |{
                       | "validated" : $isMatch
                       |}
                    """.stripMargin)

                  respond(Ok(result))
              }
          )
        } recover handleException("getReference")
      )
  }
}
