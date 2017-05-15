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

package uk.gov.hmrc.brm.switches

import uk.gov.hmrc.brm.filters._
import uk.gov.hmrc.brm.models.brm.Payload
import uk.gov.hmrc.brm.utils.BRMLogger

import scala.annotation.tailrec

/**
  * Created by mew on 12/05/2017.
  */

trait FilterResults {
  type FilterResult = Boolean
  val PassedFilters : FilterResult = true
  val FailedFilters : FilterResult = false
}

object Filters extends FilterResults {

  private val baseFilters = List(DateOfBirthFilter, GROFilter)
  private val detailsFilters = List(GRODetailsFilter)
  private val referenceFilters = List(GROReferenceFilter)

  def process(payload : Payload) : FilterResult = {

    def getFilters(payload: Payload) : List[Filter] = {
      val filters = payload match {
        case Payload(Some(_), _, _, _, _) => referenceFilters
        case Payload(None, _, _, _, _) => detailsFilters
      }
      val baseWithFilters = baseFilters ::: filters

      BRMLogger.info("Filters", "getFilters", s"processing the following filters: $baseWithFilters")

      baseWithFilters
    }

    @tailrec
    def filterHelper(uncheckedFilters : List[Filter], failedFilters : List[Filter]) : FilterResult = {
      if (failedFilters.nonEmpty) {
        BRMLogger.info("Filters", "process", s"Stopping due to failing a Filter, " +
          s"remaining: $uncheckedFilters, failed: $failedFilters")
        FailedFilters
      } else {
        uncheckedFilters match {
          case Nil => PassedFilters
          case head :: tail =>
            val passed = head.process(payload)
            if (passed) {
              filterHelper(tail, failedFilters ::: Nil)
            } else {
              filterHelper(tail, failedFilters ::: List(head))
            }
        }
      }
    }

    // Start with a FailedFilters and check this value at the end
    filterHelper(getFilters(payload), Nil)

  }

}