/*
 * Copyright 2022 HM Revenue & Customs
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

import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

/**
  * Created by user on 02/03/17.
  */
object DateUtil {


  def getCurrentDateString(DATE_FORMAT : String) = {

    val dateTime = new DateTime()
    val formatter = DateTimeFormat.forPattern(DATE_FORMAT)
    val formattedDate: String = formatter.print(dateTime)
    formattedDate
  }

}
