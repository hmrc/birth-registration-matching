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

package uk.gov.hmrc.brm.models.response.nrs

import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.brm.models.response.StatusInterface

case class NRSStatus(
  status: Int = 1,
  deathCode: Int = 0
) extends StatusInterface {

  override def toJson: JsValue = {
    Json.parse(s"""
      |{
      | "status": "$status",
      | "deathCode": "$deathCode"
      |}
    """.stripMargin)
  }

  override def flags: Map[String, String] = Map(
    "status" -> s"$statusReason",
    "deathCode" -> s"$deathCodeReason"
  )

  private def statusReason = {
    status match {
      case 1 => "Valid"
      case -4 => "Corrections"
      case -5 => "Incomplete"
      case -6 => "Cancelled"
      case _ => "Unknown"
    }
  }

  private def deathCodeReason = {
    deathCode match {
      case 0 => "Not deceased"
      case _ => "Potentially deceased"
    }
  }
}
