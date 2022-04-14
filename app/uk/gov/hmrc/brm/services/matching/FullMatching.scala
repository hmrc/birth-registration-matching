/*
 * Copyright 2022 HM Revenue & Customs
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

class FullMatching @Inject()(val config: BrmConfig) extends MatchingAlgorithm {

  override def matchFunction: PartialFunction[(Payload, Record), MatchingResult] = {
    case (payload, record) =>

      val mp = payload.copy(
        _additionalNames = if(config.ignoreAdditionalNames) None else Some(payload.additionalNames)
      )

      // Split names on record into firstNames and AdditionalNames
      val namesOnRecord: Names = NameParser.parseNames(mp, record, config.ignoreAdditionalNames)

      // Match each property
      val firstNamesMatched = stringMatch(Some(mp.firstNames), Some(namesOnRecord.firstNames))
      val additionalNamesMatched = stringMatch(Some(mp.additionalNames), Some(namesOnRecord.additionalNames))
      val lastNameMatched = stringMatch(Some(mp.lastName), Some(namesOnRecord.lastNames))
      val dateOfBirthMatched = dateMatch(Some(mp.dateOfBirth), record.child.dateOfBirth)

      val matched = firstNamesMatched and additionalNamesMatched and lastNameMatched and dateOfBirthMatched

      MatchingResult(
        matched,
        firstNamesMatched,
        additionalNamesMatched,
        lastNameMatched,
        dateOfBirthMatched,
        namesOnRecord)
  }
}
