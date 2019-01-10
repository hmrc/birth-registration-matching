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

package uk.gov.hmrc.brm.utils

import play.api.mvc.Request

object KeyGenerator {

  val DATE_FORMAT: String = "yyyyMMdd:HHmmssSS"
  private var keyForRequest: String = ""
  private val AUDITSOURCE_LENGTH = 20

  def generateKey[A](request: Request[A], headerValidator: HeaderValidator = HeaderValidator) = {
    val formattedDate: String = getDateKey
    val apiVersion: String = headerValidator.getApiVersion(request)
    //format is date-requestid-audit source - api version number
    val auditSource = request.headers.get("Audit-Source").getOrElse("")
    val key = s"$formattedDate-${request.id}-${getSubString (auditSource, AUDITSOURCE_LENGTH)}-$apiVersion"
    key
  }

  private def getDateKey: String = {
    DateUtil.getCurrentDateString(DATE_FORMAT)
  }

  def getKey(): String = {
    keyForRequest
  }

  def setKey(key: String): Unit = {
    keyForRequest = key
  }

  def generateAndSetKey[A](request: Request[A]): Unit = {
    val key = generateKey(request)
    setKey(key)
  }

  def getSubString(originalString: String, maxLength : Int) = {
    var  formattedString = originalString
    if(originalString.length > maxLength ) {
       formattedString = originalString.substring(0, maxLength)
    }

    formattedString
  }

}
