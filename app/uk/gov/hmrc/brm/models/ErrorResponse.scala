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

package uk.gov.hmrc.brm.models

import play.api.i18n.Messages
import play.api.libs.json.{JsValue, Json}

/**
  * Created by chrisianson on 29/07/16.
  */
trait ErrorResponse {
  def getErrorResponseByErrorCode(errorCode: Int, message: Option[String] = None): JsValue
}

object ErrorResponse  extends ErrorResponse {
  def getErrorResponseByErrorCode(errorCode: Int, message: Option[String] = None): JsValue = {

    val keys = Map(
      "code" -> s"error.code.${errorCode}.code",
      "status" -> s"error.code.${errorCode}.status",
      "title" -> s"error.code.${errorCode}.title",
      "details" -> s"error.code.${errorCode}.details",
      "about" -> s"error.code.${errorCode}.links.about"
    )

    def buildJsonResponse(v: String): String = {
      if (Messages.isDefinedAt(v)) {
        Messages(v)
      } else {
        "oops, this error isn't defined"
      }
    }

    val response = Json.toJson(keys.mapValues(buildJsonResponse))

    response
  }
}
