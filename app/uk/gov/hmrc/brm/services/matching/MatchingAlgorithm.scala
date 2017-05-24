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

import org.joda.time.LocalDate
import uk.gov.hmrc.brm.config.BrmConfig
import uk.gov.hmrc.brm.models.brm.Payload
import uk.gov.hmrc.brm.models.matching.MatchingResult
import uk.gov.hmrc.brm.models.response.Record
import uk.gov.hmrc.brm.services.parser.NameParser._
import uk.gov.hmrc.brm.utils.CommonUtil.forenames

import scala.annotation.tailrec

sealed trait NameParser {

  private[NameParser] def ignoreAdditionalNames : Boolean = BrmConfig.ignoreAdditionalNames

  protected def parseNamesOnRecord(payload: Payload, record: Record) : String =
  {
    if(ignoreAdditionalNames) {
      // return the X number of names from the record for what was provided on the input
      // if I receive 3 names on the input, take 3 names from the record
      // if I give you more names than on the record then return what is on the record
      // if I give you less names than on the record, take the number of names from the record that was on input
      val right = record.child.forenames.names
      val left = payload.firstName.names
      val names = left filter right
      names.listToString
    } else {
      // take all names on the record
      record.child.forenames.names.listToString
    }
  }

}

sealed trait MatchingIterator {
  this: MatchingAlgorithm =>

  private val noMatch = MatchingResult(Bad(), Bad(), Bad(), Bad())

  def performMatch(payload: Payload, records: List[Record], matchOnMultiple: Boolean): MatchingResult = {
    @tailrec
    def matchHelper(records: List[Record], result: MatchingResult, f: ((Payload, Record)) => MatchingResult): MatchingResult = {
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

}

trait MatchingAlgorithm extends MatchingIterator with NameParser {

  protected[MatchingAlgorithm] def matchFunction: PartialFunction[(Payload, Record), MatchingResult]

  /**
    * TODO remove this from MatchingAlgorithm
    */
  protected[MatchingAlgorithm] def matchForenames(payload: Payload, record: Record) : Match = {
    //add additionalNames to firstName based on feature toggle value
    val inputNames =  forenames(payload.firstName, payload.additionalNames).names.listToString
    val parsedNames = parseNamesOnRecord(payload, record)

    stringMatch(Some(inputNames), Some(parsedNames))
  }

  protected[MatchingAlgorithm] def stringMatch(input: Option[String], record: Option[String]): Match =
    matching[String](input, record, _ equalsIgnoreCase _)

  protected[MatchingAlgorithm] def dateMatch(input: Option[LocalDate], record: Option[LocalDate]): Match =
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

}
