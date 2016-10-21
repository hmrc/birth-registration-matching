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

package uk.gov.hmrc.brm.services

import uk.gov.hmrc.brm.audit.{BRMAudit, EnglandAndWalesAuditEvent}
import uk.gov.hmrc.brm.models.brm.Payload
import uk.gov.hmrc.brm.models.response.gro.GroResponse
import uk.gov.hmrc.brm.models.matching.ResultMatch
import uk.gov.hmrc.brm.utils.BrmLogger._
import uk.gov.hmrc.brm.utils.MatchingType
import uk.gov.hmrc.play.http.HeaderCarrier

trait MatchingService {
  val CLASS_NAME: String = this.getClass.getCanonicalName

  def performMatch(input: Payload, response: GroResponse, matchingType: MatchingType.Value)(implicit hc: HeaderCarrier) : ResultMatch = {
    info(CLASS_NAME, "MatchingType", s"$matchingType")
    val algorithm = matchingType match {
      case MatchingType.FULL => FullMatching
      case MatchingType.PARTIAL => PartialMatching
    }

    val result = algorithm.performMatch(input, response)
    info(CLASS_NAME, "performMatch", s"${result.audit}")
    val event = new EnglandAndWalesAuditEvent(
      result.audit
    )

    BRMAudit.event(event)
    result
  }
}

object MatchingService extends MatchingService
