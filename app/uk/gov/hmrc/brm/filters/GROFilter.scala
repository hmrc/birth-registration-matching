/*
 * Copyright 2020 HM Revenue & Customs
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

import uk.gov.hmrc.brm.filters.Filter._
import uk.gov.hmrc.brm.switches.{GRODetailsSwitch, GROReferenceSwitch, GROSwitch}

/**
  * Created by mew on 15/05/2017.
  */
object GROFilter extends Filter(GROSwitch, GeneralFilter) {
  override def toString = "GROFilter"
}
object GRODetailsFilter extends Filter(GRODetailsSwitch, DetailsFilter) {
  override def toString = "GRODetailsFilter"
}
object GROReferenceFilter extends Filter(GROReferenceSwitch, ReferenceFilter) {
  override def toString = "GROReferenceFilter"
}
