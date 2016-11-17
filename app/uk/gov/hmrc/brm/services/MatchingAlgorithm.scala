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
import uk.gov.hmrc.brm.models.matching.ResultMatch
import uk.gov.hmrc.brm.models.response.Record

import scala.annotation.tailrec

trait MatchingAlgorithm {

  private[MatchingAlgorithm] val noMatch = ResultMatch(Bad(), Bad(), Bad(), Bad())

  protected[MatchingAlgorithm] val matchFunction: PartialFunction[(Payload, Record), ResultMatch]

  def performMatch(payload: Payload, records: List[Record], matchOnMultiple: Boolean = false): ResultMatch = {
    @tailrec
    def matchHelper(records: List[Record], result: ResultMatch, f: ((Payload, Record)) => ResultMatch): ResultMatch = {
      records match {
        case Nil => noMatch
        case head :: Nil =>
          f(payload, head)
        case head :: tail =>
          val r = f(payload, head)
          matchHelper(tail, r, f)
      }
    }

    if (matchOnMultiple) {
      matchHelper(records, noMatch, matchFunction)
    }
    else {
      records.length match {
        case 1 => matchHelper(records, noMatch, matchFunction)
        case _ => noMatch
      }
    }
  }

  protected[MatchingAlgorithm] def firstNamesMatch(brmsFirstname: Option[String], groFirstName: Option[String]): Match =
    matching[String](brmsFirstname, groFirstName, _ equalsIgnoreCase _)

  protected[MatchingAlgorithm] def lastNameMatch(brmsLastName: Option[String], groLastName: Option[String]): Match =
    matching[String](brmsLastName, groLastName, _ equalsIgnoreCase _)

  protected[MatchingAlgorithm] def dobMatch(brmsDob: Option[LocalDate], groDob: Option[LocalDate]): Match =
    matching[LocalDate](brmsDob, groDob, _ isEqual _)

  protected[MatchingAlgorithm] def matching[T](input: Option[T], other: Option[T], matchFunction: (T, T) => Boolean): Match = {
    (input, other) match {
      case (Some(x), Some(y)) =>
        if (matchFunction(x, y)) {
          Good()
        }
        else {
          Bad()
        }
      case _ => Bad()
    }
  }

}


object FullMatching extends MatchingAlgorithm {

  override val matchFunction: PartialFunction[(Payload, Record), ResultMatch] = {
    case (payload, record) =>
      val firstNames = firstNamesMatch(Some(payload.firstName), Some(record.child.firstName))
      val lastNames = lastNameMatch(Some(payload.lastName), Some(record.child.lastName))
      val dates = dobMatch(Some(payload.dateOfBirth), record.child.dateOfBirth)
      val resultMatch = firstNames and lastNames and dates

      ResultMatch(firstNames, lastNames, dates, resultMatch)
  }
}

object PartialMatching extends MatchingAlgorithm {

  override val matchFunction: PartialFunction[(Payload, Record), ResultMatch] = {
    case (payload, record) =>

      val firstNames = if (BrmConfig.matchFirstName) {
        firstNamesMatch(Some(payload.firstName), Some(record.child.firstName))
      } else {
        Good()
      }

      val lastNames = if (BrmConfig.matchLastName) {
        lastNameMatch(Some(payload.lastName), Some(record.child.lastName))
      } else {
        Good()
      }

      val dates = if (BrmConfig.matchDateOfBirth) {
        dobMatch(Some(payload.dateOfBirth), record.child.dateOfBirth)
      } else {
        Good()
      }

      val resultMatch = firstNames and lastNames and dates

      ResultMatch(firstNames, lastNames, dates, resultMatch)
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


