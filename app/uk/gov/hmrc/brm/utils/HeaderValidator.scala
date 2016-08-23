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

import play.api.libs.json.Json
import play.api.libs.openid.Errors.BAD_RESPONSE
import play.api.mvc.{ActionBuilder, Request, Result, Results}
import uk.gov.hmrc.brm.models.ErrorResponse

import scala.concurrent.Future
import scala.util.matching.Regex
import scala.util.matching.Regex.Match

/**
  * Created by chrisianson on 23/08/16.
  */
trait HeaderValidator extends Results {

  private val validVersions : List[String] = List("1.0", "1.1", "1.2")

  val validateVersion : String => Boolean = validVersions.contains(_)

  val validateContentType : String => Boolean = _ == "json"

  val validateAuditSource : String => Boolean = !_.isEmpty

  val matchAuditSource : String => Option[Match] = new Regex("""^(.*)$""", "auditsource") findFirstMatchIn _

  val matchHeader : String => Option[Match] = new Regex( """^application/vnd[.]{1}hmrc[.]{1}(.*?)[+]{1}(.*)$""", "version", "contenttype") findFirstMatchIn _

  def acceptHeaderValidationRules(contentType: Option[String] = None, auditSource: Option[String] = None): Boolean = {

    val contentTypeStatus = contentType.flatMap(
      a =>
        matchHeader(a) map(
          res =>
            validateContentType(res.group("contenttype")) &&
              validateVersion(res.group("version"))
          )
    ) getOrElse false

    val auditSourceStatus = auditSource.flatMap(
      a =>
        matchAuditSource(a) map (
          res =>
            validateAuditSource(res.group("auditsource"))
          )
    ) getOrElse false

    contentTypeStatus && auditSourceStatus
  }

  def validateAccept(rules: (Option[String], Option[String]) => Boolean) = new ActionBuilder[Request] {
    def invokeBlock[A](request: Request[A], block: (Request[A]) => Future[Result]) = {
      if (rules(request.headers.get("Content-Type"), request.headers.get("Audit-Source"))) {
        block(request)
      }
      else
      {
        println(ErrorResponse.getErrorResponseByErrorCode(145))

        val errorCode = 145
        Future.successful((BadRequest) (ErrorResponse.getErrorResponseByErrorCode(errorCode)))
      }
    }
  }

}
