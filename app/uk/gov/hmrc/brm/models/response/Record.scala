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

package uk.gov.hmrc.brm.models.response

import play.api.libs.functional.syntax._
import play.api.libs.json._
import uk.gov.hmrc.brm.models.response.gro.{Child, Status}

case class Record[C, S](child: C, status: Option[S] = None)

object Record {

  implicit def readRecords[C, S](implicit cRds : Reads[C], sRds : Reads[S]) : Reads[Record[C, S]] = new Reads[Record[C, S]] {

    def reads(json: JsValue): JsResult[Record[C, S]] = new Record[C, S](
      Json.fromJson(cRds),
        (JsPath \ "status").readNullable[Status]
    )(Record.apply _)

  }
}
