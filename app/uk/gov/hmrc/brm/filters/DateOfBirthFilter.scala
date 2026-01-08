/*
 * Copyright 2026 HM Revenue & Customs
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
import java.time.LocalDate
import uk.gov.hmrc.brm.config.BrmConfig
import uk.gov.hmrc.brm.filters.Filter.GeneralFilter
import uk.gov.hmrc.brm.metrics.DateofBirthFeatureCountMetric
import uk.gov.hmrc.brm.models.brm.Payload
import uk.gov.hmrc.brm.switches.{Switch, SwitchValue}

/** Created by mew on 15/05/2017.
  */
class DateOfBirthFilter @Inject() (dobCount: DateofBirthFeatureCountMetric, conf: BrmConfig)
    extends Filter(GeneralFilter) {

  class DateOfBirthSwitch extends Switch {
    override val name              = "dobValidation"
    override val config: BrmConfig = conf
  }

  class DateOfBirthSwitchValue extends SwitchValue {
    override val name              = "dobValidation"
    override val config: BrmConfig = conf
  }

  val switch              = new DateOfBirthSwitch
  val switchValue: String = (new DateOfBirthSwitchValue).value

  override def process(payload: Payload): Boolean = {
    val isEnabled = super.process(payload)

    if (isEnabled) {
      val config     = switchValue
      // validate date of birth
      val configDate = LocalDate.parse(config)
      val isValid    = !payload.dateOfBirth.isBefore(configDate)
      if (!isValid) {
        dobCount.count()
      }
      isValid
    } else {
      // if the check is disabled then skip and continue
      true
    }
  }

  override def toString = "DateOfBirthFilter"

}
