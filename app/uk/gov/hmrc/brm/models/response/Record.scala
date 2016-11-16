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

package uk.gov.hmrc.brm.models.response

import play.api.libs.functional.syntax._
import play.api.libs.json._
import uk.gov.hmrc.brm.models.response.gro.{Child, Status}

case class Record(child: Child, status: Option[Status] = None)

object Record {

  implicit val implicitReads: Reads[Record] = (
    JsPath.read[Child] and
      (JsPath \ "status").readNullable[Status]
    )(Record.apply _)
}

//case class Records(records : List[Record])
//
//object Records {
//  implicit val reads = __.read[List[Record]].map(f => Records(f))
//}

//abstract class Record[C, S](child : C, status: Option[S])
//
//case class Response[C, S](
//                   records : List[Record[C, S]]
//                   )

// json.validate[Response[Record]