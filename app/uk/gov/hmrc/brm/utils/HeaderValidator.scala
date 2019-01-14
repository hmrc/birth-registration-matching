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

import play.api.http.HeaderNames
import play.api.mvc.{ActionBuilder, Request, Result, Results}
import uk.gov.hmrc.brm.metrics.{APIVersionMetrics, AuditSourceMetrics}
import uk.gov.hmrc.brm.models.brm._

import scala.concurrent.Future
import scala.util.matching.Regex
import scala.util.matching.Regex.Match

private object HeaderNames extends HeaderNames {
  val AUDIT_SOURCE = "Audit-Source"
}

object HeaderValidator extends HeaderValidator {

  override val validVersions = List("1.0")
  override val groupNames = Seq("version", "contenttype")
  override val regEx = """^application/vnd[.]{1}hmrc[.]{1}(.*?)[+]{1}(.*)$"""

  override val matchAuditSource = new Regex("""^(.*)$""", "auditsource") findFirstMatchIn _
  override val matchHeader = new Regex(regEx, groupNames: _*) findFirstMatchIn _

  def validateVersion = {
    case x : String =>
      APIVersionMetrics(x).count()
      validVersions.contains(x)
  }

  def validateAuditSource = {
    case x : String =>
      AuditSourceMetrics(x).count()
      !x.isEmpty
  }

  def validateContentType = {
    _ == validContentType
  }

  override def validateAccept() = new ActionBuilder[Request] {
    def invokeBlock[A](request: Request[A], block: (Request[A]) => Future[Result]) =
      (validateValue(request, HeaderNames.ACCEPT, "version", validateVersion, matchHeader),
        validateValue(request, HeaderNames.AUDIT_SOURCE, "auditsource", validateAuditSource, matchAuditSource),
        validateValue(request, HeaderNames.ACCEPT, "contenttype", validateContentType, matchHeader))
      match {
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

trait HeaderValidator extends Results {

  val validContentType: String = "json"
  val versionKey: String = "version"

  val validVersions: List[String]
  val groupNames: Seq[String]
  val regEx: String

  val matchAuditSource: String => Option[Match]
  val matchHeader: String => Option[Match]

  def validateVersion: PartialFunction[String, Boolean]
  def validateAuditSource: PartialFunction[String, Boolean]
  def validateContentType: String => Boolean

  def getApiVersion[A](request: Request[A]): String = {
    val accept = request.headers.get(HeaderNames.ACCEPT)
    accept.flatMap(
      a =>
        matchHeader(a.toLowerCase) map (
            res => res.group(versionKey)
          )
    ) getOrElse ""
  }

  def validateValue[A](
                        request: Request[A],
                        headerKey: String,
                        groupName: String,
                        searchF: String => Boolean,
                        matchF: String => Option[Match]): Boolean = {

    request.headers.get(headerKey).flatMap(
      a => {
        matchF(a.toLowerCase) map (
          res => {
              val response = res.group(groupName)
              searchF(response)
            }
          )
      }
    ) getOrElse false

  }

  def validateAccept(): ActionBuilder[Request]

}
