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

package uk.gov.hmrc.brm.models.matching

import uk.gov.hmrc.brm.services.{Bad, Good, Match}

/**
  * Created by user on 28/09/16.
  */
case class ResultMatch(firstNameMatch: Match,
                       lastNameMatch: Match,
                       dobMatch: Match) {


  def isMatch: Boolean = {
    val resultMatch = firstNameMatch and lastNameMatch and dobMatch

    getBoolean(resultMatch)
  }


  def audit: Map[String, String] = {
    Map(
      "match"-> String.valueOf(isMatch),
      "firstName" -> String.valueOf(getBoolean(firstNameMatch))
    )
  }

  private def getBoolean(matchResult: Match): Boolean = {
    matchResult match {
      case Good() => true
      case Bad() => false
    }
  }
}

