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

import uk.gov.hmrc.brm.models.brm.Payload
import uk.gov.hmrc.brm.models.matching.MatchingResult
import uk.gov.hmrc.brm.models.response.Record
import uk.gov.hmrc.brm.services.parser.NameParser._

/**
  * Created by mew on 24/05/2017.
  */
object FullMatching extends MatchingAlgorithm {

  override def matchFunction: PartialFunction[(Payload, Record), MatchingResult] = {
    case (payload, record) =>
      // Split names on record into firstNames and AdditionalNames
      val namesOnRecord : Names = parseNames(payload, record)
      val firstNamesMatched = stringMatch(Some(payload.firstNames), Some(namesOnRecord.firstNames))

      val additionalNamesMatched = stringMatch(Some(payload.additionalNames), Some(namesOnRecord.additionalNames))

      val lastNameMatched = stringMatch(Some(payload.lastName), Some(record.child.lastName))
      val dateOfBirthMatched = dateMatch(Some(payload.dateOfBirth), record.child.dateOfBirth)

      val result = firstNamesMatched and additionalNamesMatched and lastNameMatched and dateOfBirthMatched

      /**
        * TODO: store result of the additionalNames match method
        * and the split names
        */
      MatchingResult(
        firstNamesMatched and additionalNamesMatched,
        lastNameMatched,
        dateOfBirthMatched,
        result)
  }
}
