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

import org.joda.time.LocalDate
import play.api.libs.functional.syntax._
import play.api.libs.json.Writes._
import play.api.libs.json._
import uk.gov.hmrc.brm.utils.BRMFormat

/**
  * Created by chrisianson on 27/07/16.
  */

case class Payload(
                  reference: Option[String] = None,
                  forename: String,
                  surname: String,
                  dateOfBirth: LocalDate
                  )

object Payload extends BRMFormat {

  implicit val PayloadWrites: Writes[Payload] = (
      (JsPath \ "reference").write[Option[String]] and
      (JsPath \ "forename").write[String] and
      (JsPath \ "surname").write[String] and
      (JsPath \ "dateOfBirth").write[LocalDate](jodaLocalDateWrites(datePattern))
    )(unlift(Payload.unapply))

  implicit val requestFormat: Reads[Payload] = (
    (JsPath \ "reference").readNullable[String] and
    (JsPath \ "forename").read[String] and
    (JsPath \ "surname").read[String] and
    (JsPath \ "dateOfBirth").read[LocalDate])(Payload.apply _)
}