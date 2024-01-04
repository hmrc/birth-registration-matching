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
import uk.gov.hmrc.brm.config.BrmConfig
import uk.gov.hmrc.brm.filters.Filter._
import uk.gov.hmrc.brm.switches.Switch

/** Created by mew on 15/05/2017.
  */
class GROFilter @Inject() (conf: BrmConfig) extends Filter(GeneralFilter) {
  class GROSwitch extends Switch {
    override val name              = "gro"
    override val config: BrmConfig = conf
  }
  val switch            = new GROSwitch
  override def toString = "GROFilter"
}

class GRODetailsFilter @Inject() (conf: BrmConfig) extends Filter(DetailsFilter) {
  class GRODetailsSwitch extends Switch {
    override val name              = "gro.details"
    override val config: BrmConfig = conf
  }
  val switch            = new GRODetailsSwitch
  override def toString = "GRODetailsFilter"
}

class GROReferenceFilter @Inject() (conf: BrmConfig) extends Filter(ReferenceFilter) {
  class GROReferenceSwitch extends Switch {
    override val name              = "gro.reference"
    override val config: BrmConfig = conf
  }
  val switch            = new GROReferenceSwitch
  override def toString = "GROReferenceFilter"
}
