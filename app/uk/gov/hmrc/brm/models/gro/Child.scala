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

package uk.gov.hmrc.brm.models.gro

import org.joda.time.LocalDate
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._
import uk.gov.hmrc.brm.utils.BRMFormat

/**
  * Created by chrisianson on 31/08/16.
  */
case class Child(
                  birthReferenceNumber: Int,
                  firstName: String,
                  lastName: String,
                  dateOfBirth: Option[LocalDate]
                )

object Child extends BRMFormat {

  implicit val childReads : Reads[Child] = (
    (JsPath  \ "systemNumber").read[Int] and
      (JsPath \ "subjects" \ "child" \ "name" \ "givenName").read[String].orElse(Reads.pure("")) and
      (JsPath \ "subjects" \ "child" \ "name" \ "surname").read[String].orElse(Reads.pure("")) and
      (JsPath \ "subjects" \ "child" \ "dateOfBirth").readNullable[LocalDate](jodaLocalDateReads(datePattern)).orElse(Reads.pure(None))
    )(Child.apply _)
}
