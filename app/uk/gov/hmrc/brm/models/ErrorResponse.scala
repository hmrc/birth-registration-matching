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

package uk.gov.hmrc.brm.models

import play.api.i18n.Messages

/**
  * Created by chrisianson on 29/07/16.
  */
trait ErrorResponse {
  def getErrorResponseByErrorCode(errorCode: Int, message: Option[String] = None)
}

object ErrorResponse  extends ErrorResponse {
  def getErrorResponseByErrorCode(errorCode: Int, message: Option[String] = None) = {

    val keys: List[String] = List(
      s"error.code.${errorCode}.code",
      s"error.code.${errorCode}.status",
      s"error.code.${errorCode}.message",
      s"error.code.${errorCode}.title",
      s"error.code.${errorCode}.details",
      s"error.code.${errorCode}.links.about"
    )

    def buildJsonResponse(l: List[String], r: List[String]): List[String] = l match {
      case List(h, t) if(Messages.isDefinedAt(h)) => List(Messages(h))
      case List(h, t) if(!Messages.isDefinedAt(h)) => List("error")
    }
  }
}
