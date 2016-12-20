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

package uk.gov.hmrc.brm.utils

import org.joda.time.LocalDate
import play.api.data.validation.ValidationError
import play.api.libs.json.Reads
import play.api.libs.json.Reads.{apply => _, _}
import uk.gov.hmrc.brm.config.BrmConfig

object BRMFormat extends BRMFormat
trait BRMFormat {
  val datePattern = "yyyy-MM-dd"
  val invalidNameCharsRegx = "[;/\\\\(){}]|(<!)|(-->)|(\\n)|(\")".r

  val birthReferenceNumberValidate : Reads[String] =
    Reads.StringReads.filter(ValidationError(""))(
      str => {
        str.matches("""^[a-zA-Z0-9_-]+$""")
      }
    )

  val nameValidation : Reads[String] =
    Reads.StringReads.filter(ValidationError(""))(
      str => {
        //special charactr should not be there and lenth min 1.
        (!invalidNameCharsRegx.findFirstIn(str).isDefined) && str.length > 1
      }
    )

  val isAfterDate : Reads[LocalDate] =
    jodaLocalDateReads(datePattern).filter(ValidationError(""))(
      date => {
        date.getYear >= BrmConfig.minimumDateOfBirthYear
      }
    )

}
