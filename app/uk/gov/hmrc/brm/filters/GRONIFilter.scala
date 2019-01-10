/*
 * Copyright 2019 HM Revenue & Customs
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

import uk.gov.hmrc.brm.filters.Filter.{DetailsFilter, GeneralFilter, ReferenceFilter}
import uk.gov.hmrc.brm.switches.{GRONIDetailsSwitch, GRONIReferenceSwitch, GRONISwitch}

/**
  * Created by mew on 19/05/2017.
  */
object GRONIFilter extends Filter(GRONISwitch, GeneralFilter) {
  override def toString = "GRONIFilter"
}
object GRONIDetailsFilter extends Filter(GRONIDetailsSwitch, DetailsFilter) {
  override def toString = "GRONIDetailsFilter"
}
object GRONIReferenceFilter extends Filter(GRONIReferenceSwitch, ReferenceFilter) {
  override def toString = "GRONIReferenceFilter"
}
