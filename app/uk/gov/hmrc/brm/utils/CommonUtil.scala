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

import play.api.http.HeaderNames
import play.api.libs.json.JsValue
import play.api.mvc.Request

import scala.util.matching.Regex
import scala.util.matching.Regex.Match

object CommonUtil {

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


}
