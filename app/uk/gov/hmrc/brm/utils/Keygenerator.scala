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

import play.api.libs.json.JsValue
import play.api.mvc.Request
import org.joda.time.{DateTime, LocalDate, LocalDateTime}
import org.joda.time.LocalDate._
import org.joda.time.format.{DateTimeFormat, DateTimeFormatter}

/**
  * Created by user on 15/09/16.
  */
object Keygenerator {

  val DATE_FORMAT : String = "yyyyMMdd:HHmmssSS"
  private var keyForRequest : String = null

  def generateKey(request: Request[JsValue]) = {
    var dateTime = new DateTime()
    val formatter = DateTimeFormat.forPattern(DATE_FORMAT);
    val formattedDate : String = formatter.print(dateTime)
    val key = s"${formattedDate}-${request.id}-${request.headers.get("Audit-Source").getOrElse("")}"
    println(key)
    key
  }

  def geKey():String = {
    keyForRequest
  }

  def setKey(key: String ): Unit = {
    keyForRequest = key
  }



}
