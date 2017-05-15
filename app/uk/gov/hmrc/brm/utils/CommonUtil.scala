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
import play.api.mvc.{Controller, Request}
import uk.gov.hmrc.brm.config.BrmConfig
import uk.gov.hmrc.brm.models.brm.Payload

import scala.util.matching.Regex
import scala.util.matching.Regex.Match

object CommonUtil extends Controller {

  private val groupNames: Seq[String] = Seq("version", "contenttype")
  private val regEx: String = """^application/vnd[.]{1}hmrc[.]{1}(.*?)[+]{1}(.*)$"""

  val matchHeader: String => Option[Match] = new Regex(
    regEx,
    groupNames: _*) findFirstMatchIn _

  def getApiVersion[A](request: Request[A]): String = {
    val accept = request.headers.get(HeaderNames.ACCEPT)
    accept.flatMap(
      a =>
        matchHeader(a.toLowerCase) map (
          res => res.group("version")
          )
    ) getOrElse ""
  }

  abstract class RequestType

  case class ReferenceRequest() extends RequestType

  case class DetailsRequest() extends RequestType

  def getOperationType(payload: Payload): RequestType = {
    payload match {
      case input@Payload(None,_, _,  _, _, _) => {
        DetailsRequest()
      }
      case payload@Payload(Some(birthReferenceNumber), _, _,  _, _, _) => {
        ReferenceRequest()
      }
    }
  }


  //add additional name to firstname if ignore middle name is false.
  def forname(firstName: String, additionalName: Option[String]): String = {
    val forenames = BrmConfig.ignoreMiddleNames match {
      case true => NameFormat(firstName)
      case false => NameFormat(firstName).concat(" ").concat(NameFormat(additionalName.getOrElse(""))).trim
    }
    NameFormat(forenames)
  }

}
