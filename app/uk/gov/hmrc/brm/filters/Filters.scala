/*
 * Copyright 2023 HM Revenue & Customs
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

package uk.gov.hmrc.brm.filters

import javax.inject.Inject
import uk.gov.hmrc.brm.filters.Filter.{DetailsFilter, ReferenceFilter}
import uk.gov.hmrc.brm.models.brm.Payload
import uk.gov.hmrc.brm.utils.{BRMLogger, BirthRegisterCountry}

import scala.annotation.tailrec

class Filters @Inject() (
  logger: BRMLogger,
  dobFilter: DateOfBirthFilter,
  groFilter: GROFilter,
  groniFilter: GRONIFilter,
  groRefFilter: GROReferenceFilter,
  groDetailsFilter: GRODetailsFilter,
  nrsFilter: NRSFilter,
  nrsRefFilter: NRSReferenceFilter,
  nrsDetailsFilter: NRSDetailsFilter,
  groniRefFilter: GRONIReferenceFilter,
  groniDetailsFilter: GRONIDetailsFilter
) {

  private val groFilters   = List(groFilter, groRefFilter, groDetailsFilter)
  private val nrsFilters   = List(nrsFilter, nrsRefFilter, nrsDetailsFilter)
  private val groniFilters = List(groniFilter, groniRefFilter, groniDetailsFilter)

  private val baseFilters = List(dobFilter)

  /**
    * Should the current filter be processed, is the Payload a reference or details request
    * @param filter current Filter being processed
    * @param payload Payload
    */
  def shouldProcessFilter(filter: Filter, payload: Payload): Boolean =
    filter.filterType match {
      case ReferenceFilter =>
        payload.birthReferenceNumber.isDefined
      case DetailsFilter   =>
        payload.birthReferenceNumber.isEmpty
      case _               =>
        // general filter
        true
    }

  def getFilters(payload: Payload): List[Filter] = {
    val filters         = payload.whereBirthRegistered match {
      case BirthRegisterCountry.ENGLAND | BirthRegisterCountry.WALES =>
        groFilters
      case BirthRegisterCountry.SCOTLAND                             =>
        nrsFilters
      case BirthRegisterCountry.NORTHERN_IRELAND                     =>
        groniFilters
    }
    // Filter out registry filters by request type
    val baseWithFilters = baseFilters ::: filters.filter(f => shouldProcessFilter(f, payload))

    logger.info("Filters", "getFilters", s"processing the following filters: $baseWithFilters")

    baseWithFilters
  }

  /**
    * @param payload request transformed into Payload
    * @return List[Filter], list of failed filters
    */
  def process(payload: Payload): List[Filter] = {

    @tailrec
    def filterHelper(uncheckedFilters: List[Filter], failedFilters: List[Filter]): List[Filter] =
      if (failedFilters.nonEmpty && uncheckedFilters.isEmpty) {
        logger.info("Filters", "process", s"Stopping due to failing a Filter, failed: $failedFilters")
        failedFilters
      } else {
        uncheckedFilters match {
          case Nil          => Nil
          case head :: tail =>
            // check if the filter is correct for request type
            val passed = head.process(payload)
            if (passed) {
              filterHelper(tail, failedFilters)
            } else {
              filterHelper(tail, failedFilters ::: List(head))
            }
        }
      }

    // Start with a FailedFilters and check this value at the end
    filterHelper(getFilters(payload), Nil)

  }

}
