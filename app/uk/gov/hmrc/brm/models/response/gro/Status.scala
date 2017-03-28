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

package uk.gov.hmrc.brm.models.response.gro

import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json.{JsPath, JsValue, Json, Reads}
import uk.gov.hmrc.brm.models.response.StatusInterface

case class Status (
                    potentiallyFictitiousBirth : Boolean = false,
                    correction : Option[String] = None,
                    cancelled : Boolean = false,
                    blockedRegistration : Boolean = false,
                    marginalNote : Option[String] = None,
                    reRegistered : Option[String] = None
                  ) extends StatusInterface {

  override def toJson: JsValue = {
    Json.parse(s"""
       |{
       |  "potentiallyFictitiousBirth": "$potentiallyFictitiousBirth",
       |  "correction": "${correction.getOrElse("")}",
       |  "cancelled": "$cancelled",
       |  "blockedRegistration": "$blockedRegistration",
       |  "marginalNote": "${marginalNote.getOrElse("")}",
       |  "reRegistered": "${reRegistered.getOrElse("")}"
       |}
     """.stripMargin)
  }

}

object Status {

  implicit val childReads : Reads[Status] = (
    (JsPath \ "potentiallyFictitiousBirth").read[Boolean].orElse(Reads.pure(false)) and
      (JsPath \ "correction").readNullable[String] and
      (JsPath \ "cancelled").read[Boolean].orElse(Reads.pure(false)) and
      (JsPath \ "blockedRegistration").read[Boolean].orElse(Reads.pure(false)) and
      (JsPath \ "marginalNote").readNullable[String] and
      (JsPath \ "reRegistered").readNullable[String]
    )(Status.apply _)
}
