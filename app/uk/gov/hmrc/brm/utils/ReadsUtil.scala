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

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json.{JsPath, Reads, _}
import uk.gov.hmrc.brm.models.brm.Payload
import uk.gov.hmrc.brm.models.response.gro.GROStatus
import uk.gov.hmrc.brm.models.response.nrs.NRSStatus
import uk.gov.hmrc.brm.models.response.{Child, Record}

import scala.language.postfixOps
import scala.util.{Success, Try}

/** Created by user on 06/03/17.
  */

object ReadsUtil {

  private val minimumDateOfBirthYear = 1900
  private val validationError: JsonValidationError = JsonValidationError("")

  implicit val validLocalDateReads: Reads[LocalDate] = Reads[LocalDate] {
    case JsString(str) =>
      Try(LocalDate.parse(str, DateTimeFormatter.ofPattern(Payload.datePattern))) match {
        case Success(date: LocalDate) if date.getYear >= minimumDateOfBirthYear =>
          JsSuccess(date)
        case _ => JsError(validationError)
      }
    case _ => JsError(validationError)
  }

  val nrsChildReads: Reads[Child] = (
    (JsPath \ "id").read[String].map(x => Integer.valueOf(x).intValue()) and
      (JsPath \ "subjects" \ "child" \ "firstName").read[String].orElse(Reads.pure("")) and
      (JsPath \ "subjects" \ "child" \ "lastName").read[String].orElse(Reads.pure("")) and
      (JsPath \ "subjects" \ "child" \ "dateOfBirth").readNullable[LocalDate](validLocalDateReads).orElse(Reads.pure(None))
  )(Child.apply _)

  val groChildReads: Reads[Child] = (
    (JsPath \ "systemNumber").read[Int] and
      (JsPath \ "subjects" \ "child" \ "name" \ "givenName").read[String].orElse(Reads.pure("")) and
      (JsPath \ "subjects" \ "child" \ "name" \ "surname").read[String].orElse(Reads.pure("")) and
      (JsPath \ "subjects" \ "child" \ "dateOfBirth").readNullable[LocalDate](validLocalDateReads).orElse(Reads.pure(None))
  )(Child.apply _)

  val groReadRecord: Reads[Record] = (
    JsPath.read[Child](groChildReads) and
      (JsPath \ "status").readNullable[GROStatus]
  )(Record.apply _)

  val nrsRecordsRead: Reads[Record] = (
    JsPath.read[Child](nrsChildReads) and
      JsPath
        .read(
          (JsPath \ "status").read[Int] and
            (JsPath \ "deathCode").read[Int] tupled
        )
        .map(status => Some(NRSStatus(status._1, status._2)))
  )(Record.apply _)

  val nrsRecordsListRead: Reads[List[Record]] =
    (JsPath \ "births")
      .read[JsArray]
      .map((births: JsArray) => births.value.map(v => v.as[Record](nrsRecordsRead)).toList)

  val groRecordsListRead: Reads[List[Record]] =
    JsPath.read[JsArray].map((births: JsArray) => births.value.map(v => v.as[Record](groReadRecord)).toList)

}
