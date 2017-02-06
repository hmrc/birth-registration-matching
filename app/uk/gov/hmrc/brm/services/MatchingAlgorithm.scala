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

package uk.gov.hmrc.brm.services

import org.joda.time.LocalDate
import uk.gov.hmrc.brm.config.BrmConfig
import uk.gov.hmrc.brm.models.brm.Payload
import uk.gov.hmrc.brm.models.matching.ResultMatch
import uk.gov.hmrc.brm.models.response.Record
import uk.gov.hmrc.brm.services.parser.NameParser._

import scala.annotation.tailrec

trait MatchingAlgorithm {

  private[MatchingAlgorithm] def ignoreMiddleNames : Boolean = BrmConfig.ignoreMiddleNames
  private[MatchingAlgorithm] val noMatch = ResultMatch(Bad(), Bad(), Bad(), Bad())

  protected[MatchingAlgorithm] def matchFunction: PartialFunction[(Payload, Record), ResultMatch]

  def performMatch(payload: Payload, records: List[Record], matchOnMultiple: Boolean): ResultMatch = {
    @tailrec
    def matchHelper(records: List[Record], result: ResultMatch, f: ((Payload, Record)) => ResultMatch): ResultMatch = {
      records match {
        case Nil =>
          // no records returned therefore no match was found
          noMatch
        case head :: Nil =>
          // 1 record returned therefore we will attempt to match
          f(payload, head)
        case head :: tail =>
          // multiple records returned, iterate these until a match is found
          val r = f(payload, head)
          matchHelper(tail, r, f)
      }
    }

    // do we enable matching on multiple records returned
    if (matchOnMultiple) {
      matchHelper(records, noMatch, matchFunction)
    } else {
      records.length match {
        case 1 =>
          // one record returned, attempt to match
          matchHelper(records, noMatch, matchFunction)
        case _ =>
          // more than 1 record was returned therefore it's no match
          noMatch
      }
    }
  }

  protected[MatchingAlgorithm] def matchFirstNames(payload: Payload, record: Record) : Match = {
    val recordNamesFiltered = filterMiddleNames(payload, record)
    val firstNamePayload = payload.firstName.names.listToString

    val firstNames = nameMatch(Some(firstNamePayload), Some(recordNamesFiltered))
    firstNames
  }

  protected[MatchingAlgorithm] def nameMatch(input: Option[String], record: Option[String]): Match =
    matching[String](input, record, _ equalsIgnoreCase _)

  protected[MatchingAlgorithm] def dobMatch(input: Option[LocalDate], record: Option[LocalDate]): Match =
    matching[LocalDate](input, record, _ isEqual _)

  protected[MatchingAlgorithm] def matching[T](input: Option[T], other: Option[T], matchFunction: (T, T) => Boolean): Match = {
    (input, other) match {
      case (Some(x), Some(y)) =>
        if (matchFunction(x, y)) {
          Good()
        } else {
          Bad()
        }
      case _ => Bad()
    }
  }

  private[MatchingAlgorithm] def filterMiddleNames(payload: Payload, record: Record) = {
    ignoreMiddleNames match {
      case true =>
        val right = record.child.firstName.names
        val left = payload.firstName.names
        val names = left filter right
        names.listToString
      case false =>
        record.child.firstName.names.listToString
    }
  }

}


object FullMatching extends MatchingAlgorithm {

  // TODO also call names() on lastName to strip out the spaces from the record
  override def matchFunction: PartialFunction[(Payload, Record), ResultMatch] = {
    case (payload, record) =>
      val firstNames = matchFirstNames(payload, record)
      val lastNames = nameMatch(Some(payload.lastName.names.listToString), Some(record.child.lastName.names.listToString))
      val dates = dobMatch(Some(payload.dateOfBirth), record.child.dateOfBirth)
      val resultMatch = firstNames and lastNames and dates

      ResultMatch(firstNames, lastNames, dates, resultMatch)
  }
}

object PartialMatching extends MatchingAlgorithm {

  override def matchFunction: PartialFunction[(Payload, Record), ResultMatch] = {
    case (payload, record) =>
      val firstNames = if (BrmConfig.matchFirstName) {
        matchFirstNames(payload, record)
      } else {
        Good()
      }

      val lastNames = if (BrmConfig.matchLastName) {
        nameMatch(Some(payload.lastName), Some(record.child.lastName))
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
