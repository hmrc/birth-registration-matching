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

import org.joda.time.LocalDate
import play.api.http.HeaderNames
import play.api.libs.json.JsValue
import play.api.mvc.{Controller, Request, Result}
import uk.gov.hmrc.brm.config.BrmConfig
import uk.gov.hmrc.brm.models.brm.Payload

import scala.util.matching.Regex
import scala.util.matching.Regex.Match

trait ControllerUtil extends Controller with BrmException {

  lazy val contentType: String = "application/json; charset=utf-8"
  lazy val headers: (String, String)  = (ACCEPT, "application/vnd.hmrc.1.0+json")

  def respond(response: Result): Result = {
    response
      .as(contentType)
      .withHeaders(headers)
  }

  def handleException(method: String)(implicit payload: Payload): PartialFunction[Throwable, Result] = {
    case t =>
      val allPfs = Seq(
        notFoundPF(method),
        badRequestPF(method),
        badGatewayPF(method),
        gatewayTimeoutPF(method),
        upstreamErrorPF(method),
        badRequestExceptionPF(method),
        notImplementedExceptionPF(method),
        notFoundExceptionPF(method),
        exceptionPF(method)).reduce(_ orElse _)

      respond(allPfs.apply(t))
  }
  
}

object CommonUtil extends Controller {

  private val groupNames: Seq[String] = Seq("version", "contenttype")
  private val regEx: String = """^application/vnd[.]{1}hmrc[.]{1}(.*?)[+]{1}(.*)$"""

  val matchHeader: String => Option[Match] = new Regex(
    regEx,
    groupNames: _*) findFirstMatchIn _

  def getApiVersion(request: Request[JsValue]): String = {
    val accept = request.headers.get(HeaderNames.ACCEPT)
    accept.flatMap(
      a =>
        matchHeader(a.toLowerCase) map (
            res => res.group("version")
          )
    ) getOrElse ""
  }

  def validateDob(d: LocalDate): Boolean = {
    BrmConfig.validateDobForGro match {
      case true =>
        val validDate = new LocalDate(BrmConfig.minimumDateValueForGroValidation)
        d.isAfter(validDate) || d.isEqual(validDate)
      case false =>
        true
    }

  }

}
