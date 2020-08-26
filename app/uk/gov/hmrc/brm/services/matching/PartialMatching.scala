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

package uk.gov.hmrc.brm.services.matching

import javax.inject.Inject
import uk.gov.hmrc.brm.config.BrmConfig
import uk.gov.hmrc.brm.models.brm.Payload
import uk.gov.hmrc.brm.models.matching.MatchingResult
import uk.gov.hmrc.brm.models.response.Record
import uk.gov.hmrc.brm.services.parser.NameParser
import uk.gov.hmrc.brm.services.parser.NameParser.Names

class PartialMatching @Inject()(val config: BrmConfig) extends MatchingAlgorithm {

  private def lastNames()(implicit payload: Payload, record: Record): Match = {
    if (config.matchLastName) {
      stringMatch(Some(payload.lastName), Some(record.child.lastName))
    } else {
      Good()
    }
  }

  private def firstNames(names: Names)(implicit payload: Payload, record: Record): Match = {
    if (config.matchFirstName) {
      stringMatch(Some(payload.firstNames), Some(names.firstNames))
    } else {
      Good()
    }
  }

  private def additionalNames(names: Names)(implicit payload: Payload, record: Record): Match = {
    if (!config.ignoreAdditionalNames) {
      stringMatch(Some(payload.additionalNames), Some(names.additionalNames))
    } else {
      Good()
    }
  }

  private def dateOfBirth()(implicit payload: Payload, record: Record): Match = {
    if (config.matchDateOfBirth) {
      dateMatch(Some(payload.dateOfBirth), record.child.dateOfBirth)
    } else {
      Good()
    }
  }

  override def matchFunction: PartialFunction[(Payload, Record), MatchingResult] = {
    case (payload, record) =>
      implicit val p: Payload = payload
      implicit val r: Record = record

      val namesOnRecord: Names = NameParser.parseNames(payload, record, config.ignoreAdditionalNames)

      val (f, a, l, d) = (firstNames(namesOnRecord), additionalNames(namesOnRecord), lastNames(), dateOfBirth())

      val matched = f and a and l and d

      MatchingResult(matched, f, a, l, d, namesOnRecord)
  }
}
