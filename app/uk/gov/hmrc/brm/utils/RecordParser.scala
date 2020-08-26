/*
 * Copyright 2020 HM Revenue & Customs
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

import javax.inject.Inject
import play.api.libs.json.{JsValue, Reads}
import uk.gov.hmrc.brm.models.response.Record
import uk.gov.hmrc.http.HeaderCarrier

class RecordParser @Inject()(logger: BRMLogger) {

  def parse[T](json: JsValue, reads : (Reads[List[T]], Reads[T]))(implicit hc : HeaderCarrier, manifest: reflect.Manifest[Record]) : List[T] = {
    val name = manifest.toString()

    //read-1 is list reads and reads_2 is single record read.
    val records = json.validate[List[T]](reads._1).fold(
      error => {
        logger.warn("RecordParser", "parse()", s"Failed to validate as[List[$name]] error $error")
        json.validate[T](reads._2).fold(
          e => {
            logger.warn("RecordParser", "parse()", s"Failed to validate as[$name] $e")
            List()
          },
          r => {
            logger.info("RecordParser", "parse()", s"Successfully validated as[$name]")
            List(r)
          }
        )
      },
      success => {
        logger.info("RecordParser", "parse()", s"Successfully validated as[List[$name]]")
        success
      }
    )

    if (records.isEmpty) logger.warn("RecordParser", "parse()", s"Failed to parse response as[List[$name]] and as[$name]")
    records
  }

}
