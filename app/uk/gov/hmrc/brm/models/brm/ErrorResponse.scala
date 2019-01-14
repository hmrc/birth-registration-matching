/*
 * Copyright 2019 HM Revenue & Customs
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
import play.api.data.validation.ValidationError
import play.api.libs.json.{JsPath, JsValue, Json}
import play.api.i18n.MessagesApi

trait ErrorResponse {

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
}
