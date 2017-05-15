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

package uk.gov.hmrc.brm.filters

import org.joda.time.LocalDate
import uk.gov.hmrc.brm.filters.Filter.GeneralFilter
import uk.gov.hmrc.brm.metrics.DateofBirthFeature
import uk.gov.hmrc.brm.models.brm.Payload
import uk.gov.hmrc.brm.switches.{DateOfBirthSwitch, DateOfBirthSwitchValue, FilterResults}

/**
  * Created by mew on 15/05/2017.
  */
object DateOfBirthFilter extends Filter(DateOfBirthSwitch, GeneralFilter) with FilterResults {

  override def process(payload: Payload): Boolean = {
    val isEnabled = super.process(payload)

    if(isEnabled) {
      val config = DateOfBirthSwitchValue.value
      // validate date of birth
      val configDate = LocalDate.parse(config).toDate
      val isValid = !payload.dateOfBirth.toDate.before(configDate)
      if(!isValid){
        DateofBirthFeature.count()
      }
      isValid
    } else {
      // if the check is disabled then skip and continue
      PassedFilters
    }
  }

  override def toString = "DateOfBirthFilter"

}
