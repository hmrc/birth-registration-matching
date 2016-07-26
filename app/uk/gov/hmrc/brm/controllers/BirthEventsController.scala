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
import play.api.libs.json.{Json, JsValue}
import play.api.mvc.Action
import uk.gov.hmrc.brm.connectors.{GROEnglandAndWalesConnector, BirthConnector}
import uk.gov.hmrc.play.microservice.controller

import scala.concurrent.Future

/**
  * Created by chrisianson on 25/07/16.
  */
object BirthEventsController extends BirthEventsController {
  override val GROConnector = GROEnglandAndWalesConnector
}

trait BirthEventsController extends controller.BaseController {

  import scala.concurrent.ExecutionContext.Implicits.global

  val GROConnector : BirthConnector

  def post() = Action.async(parse.json) {
    implicit request =>
      Logger.debug(s"Request sent to birth events post")
      withJsonBody[JsValue] {
        json =>
          val ref = json.\("reference").as[String]
          val payloadFirstName = json.\("forename").as[String]
          val payloadSurname = json.\("surname").as[String]
          GROConnector.getReference(ref) map {
            response =>
              val firstName = (response \ "subjects" \ "child" \ "name" \ "givenName").as[String]
              val surname = (response \ "subjects" \ "child" \ "name" \ "surname").as[String]

              val isMatch = firstName.equals(payloadFirstName) && surname.equals(payloadSurname)
              Logger.debug(s"firstName: $firstName, surname: $surname")
              val result = Json.parse(
                s"""
                  |{
                  | "validated" : $isMatch

                    |}
                """.stripMargin)
              Ok(result)
          }
      }
  }

}
