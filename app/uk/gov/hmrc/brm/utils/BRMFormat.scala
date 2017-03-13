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

package uk.gov.hmrc.brm.utils

import org.joda.time.LocalDate
import play.api.data.validation.ValidationError
import play.api.libs.json.Reads
import play.api.libs.json.Reads.{apply => _, _}
import uk.gov.hmrc.brm.config.BrmConfig
import uk.gov.hmrc.brm.utils.BirthRegisterCountry.BirthRegisterCountry

object BRMFormat extends BRMFormat

trait BRMFormat {

  val datePattern = "yyyy-MM-dd"

  private val invalidNameCharsRegEx =  "[;/\\\\(){}&|]|(<!)|(-->)|(\\n)|(\")".r
  private val validBirthReferenceNumberRegEx = """^[a-zA-Z0-9_-]+$"""
  private val validBirthReferenceNumberGRORegEx = """^[a-zA-Z0-9_-]{9}+$"""
  private val validBirthReferenceNumberNRSRegEx = """^[a-zA-Z0-9_-]{10}+$"""

  private val validationError = ValidationError("")

  def validBirthReferenceNumber(country:  BirthRegisterCountry,  referenceNumber: Option[String]) : Boolean = (country, referenceNumber) match {
    case (BirthRegisterCountry.ENGLAND, Some(_)) => referenceNumber.get.matches(validBirthReferenceNumberGRORegEx)
    case (BirthRegisterCountry.WALES, Some(_)) => referenceNumber.get.matches(validBirthReferenceNumberGRORegEx)
    case (BirthRegisterCountry.SCOTLAND, Some(_)) => referenceNumber.get.matches(validBirthReferenceNumberNRSRegEx)
    case (_, _) => true
  }

  val birthReferenceNumberValidate: Reads[String] =
    Reads.StringReads.filter(validationError)(
      _.matches(validBirthReferenceNumberRegEx)
    )

  val nameValidation: Reads[String] =
    Reads.StringReads.filter(validationError)(
      !invalidNameCharsRegEx.findFirstIn(_).isDefined
    )

  val isAfterDate: Reads[LocalDate] =
    jodaLocalDateReads(datePattern).filter(validationError)(
      _.getYear >= BrmConfig.minimumDateOfBirthYear
    )

}
