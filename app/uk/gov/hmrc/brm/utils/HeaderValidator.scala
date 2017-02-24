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

package uk.gov.hmrc.brm.utils

import play.api.http.HeaderNames
import play.api.mvc.{ActionBuilder, Request, Result, Results}
import uk.gov.hmrc.brm.metrics.{APIVersionMetrics, AuditSourceMetrics}
import uk.gov.hmrc.brm.models.brm.ErrorResponse
import uk.gov.hmrc.brm.utils.CommonUtil._

import scala.concurrent.Future
import scala.util.matching.Regex
import scala.util.matching.Regex.Match

trait HeaderValidator extends Results {

  private val validVersions : List[String] = List("1.0")

  val validateVersion : String => Boolean = validVersions.contains(_)

  val validateContentType : String => Boolean = _ == "json"

  val validateAuditSource : String => Boolean = !_.isEmpty

  val matchAuditSource : String => Option[Match] = new Regex("""^(.*)$""", "auditsource") findFirstMatchIn _

  def acceptHeaderValidationRules(accept: Option[String] = None, auditSource: Option[String] = None): Boolean = {

    val acceptStatus = accept.flatMap(
      a => {
        matchHeader(a.toLowerCase) map (
          res => {
            val version = res.group("version")
            APIVersionMetrics(version).count()

            validateContentType(res.group("contenttype")) &&
              validateVersion(version)
            }
          )
      }
    ) getOrElse false

    val auditSourceStatus = auditSource.flatMap(
      a =>
        matchAuditSource(a) map (
          res => {
            val source = res.group("auditsource")
            AuditSourceMetrics(source).count()
            validateAuditSource(source)
          }
          )
    ) getOrElse false

    acceptStatus && auditSourceStatus
  }

  def validateAccept(rules: (Option[String], Option[String]) => Boolean) = new ActionBuilder[Request] {
    def invokeBlock[A](request: Request[A], block: (Request[A]) => Future[Result]) = {

      if (rules(request.headers.get(HeaderNames.ACCEPT), request.headers.get("Audit-Source"))) {

        // generate unique key
        KeyGenerator.generateAndSetKey(request)

        block(request)
      } else {
        val errorCode = 145
        Future.successful(BadRequest(ErrorResponse.getErrorResponseByErrorCode(errorCode)))
      }
    }
  }

}
