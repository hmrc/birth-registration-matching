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
import uk.gov.hmrc.brm.config.BrmConfig
import uk.gov.hmrc.brm.filters.Filter.{DetailsFilter, GeneralFilter, ReferenceFilter}
import uk.gov.hmrc.brm.switches.Switch

/** Created by mew on 19/05/2017.
  */
class NRSFilter @Inject() (conf: BrmConfig) extends Filter(GeneralFilter) {
  class NRSSwitch extends Switch {
    override val config: BrmConfig = conf
    override val name              = "nrs"
  }
  val switch            = new NRSSwitch
  override def toString = "NRSFilter"
}

class NRSDetailsFilter @Inject() (conf: BrmConfig) extends Filter(DetailsFilter) {
  class NRSDetailsSwitch extends Switch {
    override val config: BrmConfig = conf
    override val name              = "nrs.details"
  }
  val switch            = new NRSDetailsSwitch
  override def toString = "NRSDetailsFilter"
}

class NRSReferenceFilter @Inject() (conf: BrmConfig) extends Filter(ReferenceFilter) {
  class NRSReferenceSwitch extends Switch {
    override val config: BrmConfig = conf
    override val name              = "nrs.reference"
  }
  val switch            = new NRSReferenceSwitch
  override def toString = "NRSReferenceFilter"
}
