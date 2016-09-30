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

import uk.gov.hmrc.brm.models.brm.Payload
import uk.gov.hmrc.brm.models.response.gro.GroResponse
import uk.gov.hmrc.brm.models.matching.ResultMatch
import uk.gov.hmrc.brm.utils.MatchingType


/**
  * Created by manish.wadhwani on 28/09/16.
  */
trait MatchingService {
  def performMatch(input: Payload, response: GroResponse, matchingType: MatchingType.Value): ResultMatch = {

    val algorithm = matchingType match {
      case MatchingType.FULL => FullMatching
    }

    val result = algorithm.performMatch(input, response)
    result.audit //for audit purpose
    result
    /*match {
      case Good() => ResultMatch(true)
      case Bad() => ResultMatch(false)
    }*/
    result

  }

}


object MatchingService extends MatchingService
