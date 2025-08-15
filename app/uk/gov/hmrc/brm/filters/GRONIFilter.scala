/*
 * Copyright 2025 HM Revenue & Customs
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
class GRONIFilter @Inject() (conf: BrmConfig) extends Filter(GeneralFilter) {
  class GroniSwitch extends Switch {
    override val config: BrmConfig = conf
    override val name              = "groni"
  }
  val switch            = new GroniSwitch
  override def toString = "GRONIFilter"
}

class GRONIDetailsFilter @Inject() (conf: BrmConfig) extends Filter(DetailsFilter) {
  class GRONIDetailsSwitch extends Switch {
    override val config: BrmConfig = conf
    override val name              = "groni.details"
  }
  val switch            = new GRONIDetailsSwitch
  override def toString = "GRONIDetailsFilter"
}

class GRONIReferenceFilter @Inject() (conf: BrmConfig) extends Filter(ReferenceFilter) {
  class GRONIReferenceSwitch extends Switch {
    override val config: BrmConfig = conf
    override val name              = "groni.reference"
  }
  val switch            = new GRONIReferenceSwitch
  override def toString = "GRONIReferenceFilter"
}
