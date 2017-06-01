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

import org.joda.time.DateTime
import uk.gov.hmrc.brm.config.BrmConfig

object CommonUtil {

  def forenames(firstName: String, additionalName: Option[String]): String = {
    val forenames = if(BrmConfig.ignoreAdditionalNames) {
      NameFormat(firstName)
    } else {
      s"${NameFormat(firstName)} ${NameFormat(additionalName.getOrElse(""))}".trim
    }
    NameFormat(forenames)
  }

  //log the time diff in milliseconds.
  def logTime(startTime:Long): Unit ={
    def diffInMillis = DateTime.now.getMillis-startTime
    BRMLogger.info(s"CommonUtil", "sendRequest", s"time in milliseconds for making request: ${diffInMillis}")
  }

}
