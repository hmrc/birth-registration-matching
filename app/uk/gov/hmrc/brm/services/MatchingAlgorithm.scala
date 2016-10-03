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

import org.joda.time.LocalDate
import uk.gov.hmrc.brm.config.BrmConfig
import uk.gov.hmrc.brm.models.brm.Payload
import uk.gov.hmrc.brm.models.response.gro.GroResponse
import uk.gov.hmrc.brm.models.matching.ResultMatch

/**
  * Created by user on 28/09/16.
  */
trait MatchingAlgorithm {

  def performMatch(payload: Payload, responsePayload: GroResponse): ResultMatch


  protected def firstNamesMatch(brmsFirstname: Option[String], groFirstName: Option[String]): Match =
    matching[String](brmsFirstname, groFirstName, _ equalsIgnoreCase _  )

  protected def lastNameMatch(brmsLastName: Option[String], groLastName: Option[String]): Match =
    matching[String](brmsLastName, groLastName, _ equalsIgnoreCase _ )

  protected def dobMatch(brmsDob: Option[LocalDate], groDob: Option[LocalDate]): Match =
    matching[LocalDate](brmsDob, groDob, _ isEqual _ )

  protected def matching[T](input: Option[T], other: Option[T], matchFunction: (T, T) => Boolean): Match = {
    (input, other) match {
      case (Some(input), Some(other)) =>
        if (matchFunction(input, other)) Good()
        else Bad()
      case _ => Bad()
    }

  }
  
}


object FullMatching extends MatchingAlgorithm {
  def performMatch(payload: Payload, responsePayload: GroResponse): ResultMatch = {
    val firstNames = firstNamesMatch(Some(payload.firstName), Some(responsePayload.child.firstName))
    val lastNames = lastNameMatch(Some(payload.lastName), Some(responsePayload.child.lastName))
    val dates = dobMatch(Some(payload.dateOfBirth), responsePayload.child.dateOfBirth)
    val resultMatch = firstNames and  lastNames and dates
    ResultMatch(firstNames,lastNames,dates,resultMatch )

  }
}

object PartialMatching extends MatchingAlgorithm {
  def performMatch(payload: Payload, responsePayload: GroResponse): ResultMatch = {
    val firstNames = firstNamesMatch(Some(payload.firstName), Some(responsePayload.child.firstName))
    val lastNames = lastNameMatch(Some(payload.lastName), Some(responsePayload.child.lastName))
    val dates = dobMatch(Some(payload.dateOfBirth), responsePayload.child.dateOfBirth)

    getMatchResult(firstNames, lastNames,dates )

   // ResultMatch(firstNames,lastNames,dates)

  }


  private def getMatchResult(firstNames : Match, lastNames : Match,  dates: Match) : ResultMatch = {
    var matchResult : Match = Good()

    if (BrmConfig.matchFirstName)  matchResult = matchResult and  firstNames
    if (BrmConfig.matchLastName) matchResult =  matchResult and  lastNames
    if (BrmConfig.matchDateOfBirth)  matchResult= matchResult and  dates

    ResultMatch(firstNames,lastNames,dates,matchResult )

  }


}

sealed abstract class Match {

  def and(other: Match): Match = (this, other) match {
    case (Good(), Good()) => Good()
    case _ => Bad()
  }

}

case class Good() extends Match

case class Bad() extends Match


