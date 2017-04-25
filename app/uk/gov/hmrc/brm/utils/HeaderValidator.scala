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
import uk.gov.hmrc.brm.models.brm._
import uk.gov.hmrc.brm.utils.CommonUtil._

import scala.concurrent.Future
import scala.util.matching.Regex
import scala.util.matching.Regex.Match

trait HeaderValidator extends Results {

  private val validVersions: List[String] = List("1.0")

  val validateVersion: String => Boolean = validVersions.contains(_)

  val validateContentType: String => Boolean = _ == "json"

  val validateAuditSource: String => Boolean = !_.isEmpty

  val matchAuditSource: String => Option[Match] = new Regex("""^(.*)$""", "auditsource") findFirstMatchIn _

  private def versionValidation(accept: Option[String] = None): Boolean = {

    accept.flatMap(
      a => {
        matchHeader(a.toLowerCase) map (
            res => {
              val version = res.group("version")
              APIVersionMetrics(version).count()

              validateVersion(version)
            }
          )
      }
    ) getOrElse false
  }

  private def contentTypeValidation(accept: Option[String] = None): Boolean = {

    accept.flatMap(
      a => {
        matchHeader(a.toLowerCase) map (
            res => {
              validateContentType(res.group("contenttype"))
            }
          )
      }
    ) getOrElse false
  }

  private def auditSourceValidation(auditSource: Option[String] = None): Boolean = {

    auditSource.flatMap(
      a =>
        matchAuditSource(a) map (
            res => {
              val source = res.group("auditsource")
              AuditSourceMetrics(source).count()
              validateAuditSource(source)
            }
          )
    ) getOrElse false
  }

  def validateAccept() = new ActionBuilder[Request] {
    def invokeBlock[A](request: Request[A], block: (Request[A]) => Future[Result]) = {
      (
        versionValidation(request.headers.get(HeaderNames.ACCEPT)),
        auditSourceValidation(request.headers.get("Audit-Source")),
        contentTypeValidation(request.headers.get(HeaderNames.ACCEPT))) match {
        case (false, _, _) =>
          Future.successful(InvalidAcceptHeader.status)
        case (_, false, _) =>
          Future.successful(InvalidAuditSource.status)
        case (_, _, false) =>
          Future.successful(InvalidContentType.status)
        case (_, _, _) =>
          KeyGenerator.generateAndSetKey(request)
          block(request)
      }
    }
  }

}
