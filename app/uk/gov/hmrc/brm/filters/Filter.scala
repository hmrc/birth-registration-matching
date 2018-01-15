/*
 * Copyright 2018 HM Revenue & Customs
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

import uk.gov.hmrc.brm.filters.Filter.FilterType
import uk.gov.hmrc.brm.models.brm.Payload
import uk.gov.hmrc.brm.switches.Switch
import uk.gov.hmrc.play.config.ServicesConfig

/**
  * Created by mew on 15/05/2017.
  */
object Filter {
  abstract class FilterType
  object ReferenceFilter extends FilterType
  object DetailsFilter extends FilterType
  object GeneralFilter extends FilterType
}

abstract class Filter(switch : Switch, val filterType : FilterType) extends ServicesConfig {

  def process(payload : Payload) : Boolean = {
    switch.isEnabled
  }

}
