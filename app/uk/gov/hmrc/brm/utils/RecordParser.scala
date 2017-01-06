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

import play.api.libs.json.JsValue
import uk.gov.hmrc.brm.audit.BRMAudit
import uk.gov.hmrc.brm.models.response.Record
import uk.gov.hmrc.play.http.HeaderCarrier

/**
  * Created by adamconder on 05/01/2017.
  */

// TODO Make generic, accept type C, S to model
sealed trait ResponseParser {

  import uk.gov.hmrc.brm.utils.BrmLogger._

  def parse(json: JsValue)(implicit hc : HeaderCarrier, manifest: reflect.Manifest[Record]) : List[Record] = {
    val name = manifest.toString()
    val records = json.validate[List[Record]].fold(
      error => {
        info("RecordParser", "parse()", s"Failed to validate as[List[$name]]")
        json.validate[Record].fold(
          e => {
            info("RecordParser", "parse()", s"Failed to validate as[$name]")
            List()
          },
          r => {
            BRMAudit.logEventRecordFound(hc)
            info("RecordParser", "parse()", s"Successfully validated as[$name]")
            List(r)
          }
        )
      },
      success => {
        BRMAudit.logEventRecordFound(hc)
        info("RecordParser", "parse()", s"Successfully validated as[List[$name]]")
        success
      }
    )

    if (records.isEmpty) warn("RecordParser", "parse()", s"Failed to parse response as[List[$name]] and as[$name]")
    records
  }

}

object RecordParser extends ResponseParser
