/*
 * Copyright 2019 HM Revenue & Customs
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
import uk.gov.hmrc.brm.models.brm.Payload
import uk.gov.hmrc.brm.models.matching.MatchingResult
import uk.gov.hmrc.brm.models.response.Record

trait MatchingAlgorithm extends MatchingIterator {

  protected[MatchingAlgorithm] def matchFunction: PartialFunction[(Payload, Record), MatchingResult]

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
