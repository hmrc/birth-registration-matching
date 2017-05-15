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

import uk.gov.hmrc.brm.config.BrmConfig.RequestType
import uk.gov.hmrc.brm.filters._
import uk.gov.hmrc.brm.models.brm.Payload
import uk.gov.hmrc.brm.utils.{BRMLogger, BirthRegisterCountry}

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

  private val groFilters = List(GROFilter, GROReferenceFilter, GRODetailsFilter)
  private val nrsFilters = Nil
  private val groniFilters = Nil

  private val baseFilters = List(DateOfBirthFilter)

  private def shouldProcess(filter: Filter, payload: Payload) = {

  }

  private def getFilters(payload: Payload) : List[Filter] = {
    val filters = payload.whereBirthRegistered match {
      case BirthRegisterCountry.ENGLAND | BirthRegisterCountry.ENGLAND =>
        groFilters
      case BirthRegisterCountry.SCOTLAND =>
        nrsFilters
      case BirthRegisterCountry.NORTHERN_IRELAND =>
        groniFilters
    }
    val baseWithFilters = baseFilters ::: filters

    BRMLogger.info("Filters", "getFilters", s"processing the following filters: $baseWithFilters")

    baseWithFilters
  }

  def process(payload : Payload) : FilterResult = {

    /**
      * TODO need to also consider whereBirthRegistered for the correct filters
      * @param uncheckedFilters
      * @param failedFilters
      * @return
      */

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
            // check if the filter is correct for request type
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