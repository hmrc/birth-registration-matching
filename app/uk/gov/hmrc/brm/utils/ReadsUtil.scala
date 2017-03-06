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

import org.joda.time.LocalDate
import play.api.libs.json.{JsPath, Reads}
import play.api.libs.json.Reads._
import uk.gov.hmrc.brm.models.response.gro.{Child, Status}
import uk.gov.hmrc.brm.models.response.gro.Child._
import play.api.libs.functional.syntax._
import play.api.libs.json._
import uk.gov.hmrc.brm.models.response.Record

/**
  * Created by user on 06/03/17.
  */
object ReadsUtil {


  val groChildReads : Reads[Child] = (
    (JsPath  \ "systemNumber").read[Int] and
      (JsPath \ "subjects" \ "child" \ "name" \ "givenName").read[String].orElse(Reads.pure("")) and
      (JsPath \ "subjects" \ "child" \ "name" \ "surname").read[String].orElse(Reads.pure("")) and
      (JsPath \ "subjects" \ "child" \ "dateOfBirth").readNullable[LocalDate](jodaLocalDateReads(datePattern)).orElse(Reads.pure(None))
    )(Child.apply _)


  val nrsChildReads : Reads[Child] = (
    (JsPath  \ "id").read[String].map(x => Integer.valueOf(x).intValue()) and
      (JsPath \ "subjects" \ "child" \ "givenName").read[String].orElse(Reads.pure("")) and
      (JsPath \ "subjects" \ "child" \ "surname").read[String].orElse(Reads.pure("")) and
      (JsPath \ "subjects" \ "child" \ "dateOfBirth").readNullable[LocalDate](jodaLocalDateReads(datePattern)).orElse(Reads.pure(None))
    )(Child.apply _)


  val groReadRecords : Reads[Record] = (
    JsPath.read[Child](groChildReads) and
      (JsPath \ "status").readNullable[Status]
    )(Record.apply _)

  val nrsRecordsRead : Reads[Record] = (
    JsPath.read[Child](nrsChildReads) and
      (JsPath \ "status").readNullable[Status]
    )(Record.apply _)





}