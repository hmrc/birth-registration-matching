/*
 * Copyright 2016 HM Revenue & Customs
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

package uk.gov.hmrc.brm.config

import uk.gov.hmrc.play.config.ServicesConfig

trait BrmConfig extends ServicesConfig
{
  def validateDobForGro: Boolean = getConfBool("birth-registration-matching.validateDobForGro", defBool = false)
  def minimumDateValueForGroValidation: String = getConfString("birth-registration-matching.validDateForGro", "1900-01-01")

  val defaultDate: Int = 1900

  def minimumDateOfBirthYear: Int = getConfInt("birth-registration-matching.minimumDateOfBirthYear", defaultDate)

  def matchFirstName : Boolean = getConfBool("birth-registration-matching.matching.firstName",  defBool = true)
  def matchLastName : Boolean = getConfBool("birth-registration-matching.matching.lastName",  defBool = true)
  def matchDateOfBirth : Boolean = getConfBool("birth-registration-matching.matching.dateOfBirth",  defBool = true)

  def matchOnMultiple : Boolean = getConfBool("birth-registration-matching.matching.matchOnMultiple", defBool = false)

  def disableSearchByDetails : Boolean = getConfBool("birth-registration-matching.matching.disableSearchByDetails", defBool = false)

}

object BrmConfig extends BrmConfig
