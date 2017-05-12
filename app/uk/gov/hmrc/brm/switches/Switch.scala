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

import uk.gov.hmrc.brm.config.BrmConfig.FeatureSwitchException
import uk.gov.hmrc.play.config.ServicesConfig

/**
  * Created by mew on 12/05/2017.
  */

trait SwitchException {
  def exception(key : String) = throw FeatureSwitchException(key)
}

trait Switch extends ServicesConfig with SwitchException {
  val key : String
  def isEnabled : Boolean = getConfBool(s"birth-registration-matching.matching.features.$key.enabled", exception(key))
}

trait SwitchValue extends ServicesConfig with SwitchException {
  val key : String
  def value : String = getConfString(s"birth-registration-matching.features.$key.value", exception(key))
}

object GROReferenceSwitch extends Switch {
  override val key = "gro.reference"
}
object GRODetailsSwitch extends Switch {
  override val key = "gro.details"
}
object GROSwitch extends Switch {
  override val key = "gro"
}

object DateOfBirthSwitch extends Switch {
  override val key = "dobValidation"
}
object DateOfBirthSwitchValue extends SwitchValue {
  override val key = "dobValidation"
}
