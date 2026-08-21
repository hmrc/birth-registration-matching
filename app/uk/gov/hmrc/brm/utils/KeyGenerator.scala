/*
 * Copyright 2026 HM Revenue & Customs
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

import javax.inject.{Inject, Singleton}

object KeyGenerator {
  val DateFormat: String         = "yyyyMMdd:HHmmssSS"
  val AuditSourceMaxLen: Int     = 20
  def liveDateSupplier(): String = DateUtil.getCurrentDateString(DateFormat)
}

@Singleton
class KeyGenerator(private val dateSupplier: () => String) {

  @Inject
  def this() = this(() => KeyGenerator.liveDateSupplier())

  private var keyForRequest: String = ""

  def generateKey[A](request: Request[A], apiVersion: String): String = {
    val formattedDate = dateSupplier()
    val auditSource   = request.headers.get("Audit-Source").getOrElse("")
    s"$formattedDate-${request.id}-${getSubString(auditSource, KeyGenerator.AuditSourceMaxLen)}-$apiVersion"
  }

  def getKey(): String =
    keyForRequest

  def setKey(key: String): Unit =
    keyForRequest = key

  def generateAndSetKey[A](request: Request[A], apiVersion: String): Unit =
    setKey(generateKey(request, apiVersion))

  def getSubString(originalString: String, maxLength: Int): String =
    if (originalString.length > maxLength) originalString.substring(0, maxLength) else originalString

}
