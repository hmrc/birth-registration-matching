/*
 * Copyright 2023 HM Revenue & Customs
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
import uk.gov.hmrc.brm.audit.MatchingAudit
import uk.gov.hmrc.brm.config.BrmConfig
import uk.gov.hmrc.brm.models.brm.Payload
import uk.gov.hmrc.brm.models.matching.MatchingResult
import uk.gov.hmrc.brm.models.response.Record
import uk.gov.hmrc.brm.utils.{BRMLogger, MatchingType}
import uk.gov.hmrc.http.HeaderCarrier

class MatchingService @Inject() (
  config: BrmConfig,
  auditor: MatchingAudit,
  fullMatching: FullMatching,
  partialMatching: PartialMatching,
  logger: BRMLogger
) {

  val CLASS_NAME: String = this.getClass.getSimpleName

  val matchOnMultiple: Boolean = config.matchOnMultiple

  def performMatch(input: Payload, records: List[Record], matchingType: MatchingType.Value)(implicit
    hc: HeaderCarrier
  ): MatchingResult = {

    logger.info(CLASS_NAME, "MatchingType", s"$matchingType")

    val algorithm = matchingType match {
      case MatchingType.FULL    => fullMatching
      case MatchingType.PARTIAL => partialMatching
      case _                    => fullMatching
    }
    val result    =
      algorithm.performMatch(input, records, matchOnMultiple)

    // audit match result
    auditor.audit(result.audit, Some(input))

    result
  }

  def getMatchingType: MatchingType.Value = {
    val fullMatch = config.matchFirstName && config.matchLastName && config.matchDateOfBirth
    logger.info(CLASS_NAME, "getMatchType()", s"isFullMatching: $fullMatch configuration")
    if (fullMatch) MatchingType.FULL else MatchingType.PARTIAL
  }

}
