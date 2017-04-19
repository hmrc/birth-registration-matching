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

package uk.gov.hmrc.brm.models.brm

import play.api.Play
import play.api.libs.json.{JsValue, Json}
import play.api.i18n.{MessagesApi}

trait ErrorResponse {
  def getErrorResponseByErrorCode(errorCode: Int, message: Option[String] = None): JsValue
}

object ErrorResponse extends ErrorResponse {

  private def error(code: String, message: String) = Json.parse(
    s"""
       |{
       |  "code": "$code",
       |  "message": "$message"
       |}
     """.stripMargin).toString()

  val GRO_CONNECTION_DOWN = error("GRO_CONNECTION_DOWN", "General Registry Office: England and Wales is unavailable")
  val NRS_CONNECTION_DOWN = error("NRS_CONNECTION_DOWN", "National Records Scotland: Scotland is unavailable")
  val DES_CONNECTION_DOWN = error("DES_CONNECTION_DOWN", "DES is unavailable")

  val messages : MessagesApi = Play.current.injector.instanceOf[MessagesApi]

  def keys(errorCode : Int) = Map(
    "code" -> s"error.code.$errorCode.code",
    "status" -> s"error.code.$errorCode.status",
    "title" -> s"error.code.$errorCode.title",
    "details" -> s"error.code.$errorCode.details",
    "about" -> s"error.code.$errorCode.links.about"
  )

  def invalid = Map(
    "details" ->  s"something is wrong"
  )

  def getErrorResponseByErrorCode(errorCode: Int, message: Option[String] = None): JsValue = {

    def buildJsonResponse(v: String): Option[String] = {
      if (messages.isDefinedAt(v)) {
        Some(messages(v))
      } else {
        None
      }
    }

    val response = keys(errorCode).mapValues(c => buildJsonResponse(c)).collect {
      case (key, Some(value)) => key -> value
      case (key, _) => key -> invalid("details")
    }

    Json.toJson(response)
  }
}
