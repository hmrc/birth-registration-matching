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

object Trim {
  def apply(v: String): String = {
    v.trim
  }
}

object LeadingZeros {
  def apply(v: String): String = {

    def formatDatePart(v: String): String = {
      val formatDay: String = "%02d".format(v.toInt)
      formatDay
    }

    v.split("-")
      .map(formatDatePart(_))
        .reduceLeft( (e, s) =>  String.format("%s-%s", e, s))
  }
}

object NameFormat {

  def apply(v: String): String = {
    Trim.apply(v)
  }
}

object DateFormat {

  def apply(v: String): String = {
    LeadingZeros.apply(Trim.apply(v))
  }
}
