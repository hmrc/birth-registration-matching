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

package uk.gov.hmrc.brm.models.brm

import org.joda.time.LocalDate
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json.Writes._
import play.api.libs.json._
import uk.gov.hmrc.brm.utils.BRMFormat
import uk.gov.hmrc.brm.utils.BirthRegisterCountry.{BirthRegisterCountry, apply => _, _}

case class Payload(
                    birthReferenceNumber: Option[String] = None,
                    firstName: String,
                    lastName: String,
                    dateOfBirth: LocalDate,
                    whereBirthRegistered : BirthRegisterCountry
                  ){
}

object Payload extends BRMFormat {

  val birthReferenceNumber = "birthReferenceNumber"
  val firstName = "firstName"
  val lastName = "lastName"
  val dateOfBirth = "dateOfBirth"
  val whereBirthRegistered = "whereBirthRegistered"

  implicit val PayloadWrites: Writes[Payload] = (
    (JsPath \ birthReferenceNumber).write[Option[String]] and
      (JsPath \ firstName).write[String] and
      (JsPath \ lastName).write[String] and
      (JsPath \ dateOfBirth).write[LocalDate](jodaLocalDateWrites(datePattern)) and
      (JsPath \ whereBirthRegistered).write[BirthRegisterCountry](birthRegisterWrites)

    )(unlift(Payload.unapply))

  implicit val requestFormat: Reads[Payload] = (
    (JsPath \ birthReferenceNumber).readNullable[String](birthReferenceNumberValidate) and
      (JsPath \ firstName).read[String](minLength[String](1)) and
      (JsPath \ lastName).read[String](minLength[String](1)) and
      (JsPath \ dateOfBirth).read[LocalDate](isAfterDate) and
      (JsPath \ whereBirthRegistered).read[BirthRegisterCountry](birthRegisterReads)
    )(Payload.apply _)
}
