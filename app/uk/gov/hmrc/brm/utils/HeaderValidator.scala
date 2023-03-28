/*
 * Copyright 2023 HM Revenue & Customs
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

import play.api.http.{HeaderNames => PlayHeaderNames}

import javax.inject.Inject
import play.api.mvc._
import uk.gov.hmrc.brm.metrics.{APIVersionMetrics, AuditSourceMetrics}
import uk.gov.hmrc.brm.models.brm._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.matching.Regex
import scala.util.matching.Regex.Match

private object HeaderNames extends PlayHeaderNames {
  val AUDIT_SOURCE = "Audit-Source"
}

class HeaderValidator @Inject() (
  apiVersionMetrics: APIVersionMetrics,
  auditSourceMetrics: AuditSourceMetrics,
  keyGenerator: KeyGenerator
) {

  val validContentType: String = "json"
  val versionKey: String       = "version"

  val validVersions = List("1.0")
  val groupNames    = Seq("version", "contenttype")
  val regEx         = """^application/vnd[.]{1}hmrc[.]{1}(.*?)[+]{1}(.*)$"""

  val matchAuditSource: CharSequence => Option[Match] = new Regex("""^(.*)$""", "auditsource") findFirstMatchIn _
  val matchHeader: CharSequence => Option[Match]      = new Regex(regEx, groupNames: _*) findFirstMatchIn _

  def validateVersion(s: String): Boolean = s match {
    case x: String =>
      apiVersionMetrics.count()
      validVersions.contains(x)
  }

  def validateAuditSource(s: String): Boolean = s match {
    case x: String =>
      auditSourceMetrics.count(x)
      x.nonEmpty
  }

  def validateContentType(s: String): Boolean =
    s == validContentType

  def validateValue[A](
    request: Request[A],
    headerKey: String,
    groupName: String,
    searchF: String => Boolean,
    matchF: String => Option[Match]
  ): Boolean =
    request.headers
      .get(headerKey)
      .flatMap { a =>
        matchF(a.toLowerCase) map (res => {
          val response = res.group(groupName)
          searchF(response)
        })
      }
      .getOrElse(false)

  def getApiVersion[A](request: Request[A]): String = {
    val accept = request.headers.get(HeaderNames.ACCEPT)
    accept.flatMap(a => matchHeader(a.toLowerCase) map (res => res.group(versionKey))) getOrElse ""
  }

  def validateAccept(cc: ControllerComponents): ActionBuilder[Request, AnyContent] =
    new ActionBuilder[Request, AnyContent] {
      override val parser: BodyParser[AnyContent]     = cc.parsers.default
      override val executionContext: ExecutionContext = cc.executionContext

      def invokeBlock[A](request: Request[A], block: (Request[A]) => Future[Result]): Future[Result] =
        (
          validateValue(request, HeaderNames.ACCEPT, "version", validateVersion, matchHeader),
          validateValue(request, HeaderNames.AUDIT_SOURCE, "auditsource", validateAuditSource, matchAuditSource),
          validateValue(request, HeaderNames.ACCEPT, "contenttype", validateContentType, matchHeader)
        ) match {
          case (false, _, _) =>
            Future.successful(InvalidAcceptHeader.status)
          case (_, false, _) =>
            Future.successful(InvalidAuditSource.status)
          case (_, _, false) =>
            Future.successful(InvalidContentType.status)
          case (_, _, _)     =>
            keyGenerator.generateAndSetKey(request, getApiVersion(request))
            block(request)
        }
    }
}
