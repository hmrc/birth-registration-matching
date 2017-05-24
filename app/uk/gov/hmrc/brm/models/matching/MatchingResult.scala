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

package uk.gov.hmrc.brm.models.matching

import uk.gov.hmrc.brm.services.matching.{Match, Good, Bad}

case class MatchingResult(forenamesMatched: Match,
                          lastNameMatched: Match,
                          dobMatched: Match,
                          result: Match) {

  def isMatch: Boolean = {
    getBoolean(result)
  }

  def audit: Map[String, String] = {
    Map(
      s"match" -> s"$isMatch",
      s"matchFirstName" -> s"${getBoolean(forenamesMatched)}",
      s"matchLastName" -> s"${getBoolean(lastNameMatched)}",
      s"matchDateOfBirth" -> s"${getBoolean(dobMatched)}"
    )
  }

  private def getBoolean(matchResult: Match): Boolean = {
    matchResult match {
      case Good() => true
      case Bad() => false
    }
  }
}
