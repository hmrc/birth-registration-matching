/*
 * Copyright 2021 HM Revenue & Customs
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

import scala.annotation.tailrec


trait MatchingIterator {
  this: MatchingAlgorithm =>

  implicit val config: BrmConfig

  private def noMatch(): MatchingResult = MatchingResult.noMatch

  def performMatch(payload: Payload, records: List[Record], matchOnMultiple: Boolean): MatchingResult = {
    @tailrec
    def matchHelper(records: List[Record], f: ((Payload, Record)) => MatchingResult): MatchingResult = {
      records match {
        case Nil =>
          // no records returned therefore no match was found
          noMatch()
        case head :: Nil =>
          // 1 record returned therefore we will attempt to match
          val matchResult = f(payload, head)
          if(head.isFlagged) {
            matchResult.copy(_matched = Bad())
          } else {
            matchResult
          }
        case head :: tail =>
          // multiple records returned, iterate these until a match is found
          //val r = f(payload, head)
          matchHelper(tail, f)
      }
    }

    if (matchOnMultiple || records.length == 1) {
      // one record returned, attempt to match
      matchHelper(records, matchFunction)
    } else {
      // cannot filter or match
      noMatch()
    }
  }

}
