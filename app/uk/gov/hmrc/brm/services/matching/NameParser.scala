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

package uk.gov.hmrc.brm.services.matching

import uk.gov.hmrc.brm.config.BrmConfig
import uk.gov.hmrc.brm.models.brm.Payload
import uk.gov.hmrc.brm.models.response.Record

trait NameParser {

  import uk.gov.hmrc.brm.services.parser.NameParser._

  private[NameParser] def ignoreAdditionalNames : Boolean = BrmConfig.ignoreAdditionalNames

  protected def parseNamesOnRecord(payload: Payload, record: Record) : String =
  {
    if(ignoreAdditionalNames) {
      // return the X number of names from the record for what was provided on the input
      // if I receive 3 names on the input, take 3 names from the record
      // if I give you more names than on the record then return what is on the record
      // if I give you less names than on the record, take the number of names from the record that was on input
      val right = record.child.forenames.names
      val left = payload.firstName.names
      val names = left filter right
      names.listToString
    } else {
      // take all names on the record
      record.child.forenames.names.listToString
    }
  }

}
