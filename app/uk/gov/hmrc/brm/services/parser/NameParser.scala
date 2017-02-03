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

package uk.gov.hmrc.brm.services.parser

import uk.gov.hmrc.brm.config.BrmConfig
import uk.gov.hmrc.brm.utils.BrmLogger

/**
  * Created by adamconder on 02/02/2017.
  */
object NameParser {

  implicit class NameParserImplicit(val s: String) {

    lazy val regex = BrmConfig.ignoreMiddleNamesRegex

    def names: List[String] = {
      BrmLogger.debug("NameParser", "parser regex", regex)
      val nameArray: Array[String] = s.toLowerCase.trim.split(regex)
      BrmLogger.debug("NameParser", "parse", s"${nameArray.toList}")

      nameArray.toList
    }

  }

  implicit class FilterList[T](left : List[T]) {

    def filter[T](right : List[T]) : List[T] = {
      Nil
    }

  }

}
