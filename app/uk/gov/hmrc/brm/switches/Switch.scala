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

package uk.gov.hmrc.brm.switches

import uk.gov.hmrc.brm.config.BrmConfig

/** Created by mew on 12/05/2017.
  */

trait SwitchException {

  final case class MatchingConfigurationException(switch: String)
      extends RuntimeException(s"birth-registration-matching.matching.$switch configuration not found")

  final case class FeatureSwitchException(switch: String)
      extends RuntimeException(s"birth-registration-matching.features.$switch.enabled configuration not found")

  final def exception(key: String) = throw FeatureSwitchException(key)
}

trait Switch extends SwitchException {
  val config: BrmConfig
  val name: String
  final def isEnabled: Boolean =
    config.conf.getConfBool(s"birth-registration-matching.features.$name.enabled", exception(name))
}

trait SwitchValue extends SwitchException {
  val config: BrmConfig
  val name: String
  final def value: String =
    config.conf.getConfString(s"birth-registration-matching.features.$name.value", exception(name))
}
