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

package uk.gov.hmrc.brm.switches

/**
  * Created by mew on 15/05/2017.
  */
object GRONISwitch extends Switch {
  override val name = "groni"
}

object GRONIDetailsSwitch extends Switch {
  override val name = "groni.details"
}

object GRONIReferenceSwitch extends Switch {
  override val name = "groni.reference"
}
