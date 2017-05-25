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
import uk.gov.hmrc.brm.models.matching.MatchingResult
import uk.gov.hmrc.brm.models.response.Record
import uk.gov.hmrc.brm.services.parser.NameParser.{Names, _}

/**
  * Created by mew on 24/05/2017.
  */
object PartialMatching extends MatchingAlgorithm {

  /**
    * We don't consider middle names here?
    * @return
    */

  private def lastNames()(implicit payload: Payload, record: Record) = {
    if (BrmConfig.matchLastName) {
      stringMatch(Some(payload.lastName), Some(record.child.lastName))
    } else {
      Good()
    }
  }

  private def forenames()(implicit payload: Payload, record: Record) = {
    if (BrmConfig.matchFirstName) {
      val namesOnRecord : Names = parseNames(payload, record)
      stringMatch(Some(payload.firstNames), Some(namesOnRecord.firstNames))
    } else {
      Good()
    }
  }

  private def dateOfBirth()(implicit payload: Payload, record: Record) = {
    if (BrmConfig.matchDateOfBirth) {
      dateMatch(Some(payload.dateOfBirth), record.child.dateOfBirth)
    } else {
      Good()
    }
  }

  override def matchFunction: PartialFunction[(Payload, Record), MatchingResult] = {
    case (payload, record) =>
      implicit val p = payload
      implicit val r = record

      val (f, l, d) : (Match, Match, Match) = (forenames(), lastNames(), dateOfBirth())
      val result = f and l and d

      MatchingResult(f, l, d, result)
  }
}
