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
import uk.gov.hmrc.brm.utils.BRMLogger

/**
  * Created by adamconder on 02/02/2017.
  */
object NameParser {

  implicit class NameParserImplicit(val s: String) {

    lazy val regex = BrmConfig.ignoreMiddleNamesRegex

    def names: List[String] = {
      val nameArray: Array[String] = s.toLowerCase.trim.split(regex)
      BRMLogger.debug("NameParser", "parse", s"names: ${nameArray.toList}, regex: $regex")

      nameArray.toList
    }

  }

  implicit class FilterList[T](left : List[T]) {

    def filter(right : List[T]) : List[T] = {
      BRMLogger.debug("NameParser", "filter", s"left: $left right: $right")

      if (left.length > right.length || left.isEmpty || right.isEmpty) {
        right
      } else {
        val difference = right.length - left.length
        BRMLogger.debug("NameParser", "parser", s"dropping: $difference")
        val dropped = right.dropRight(difference)
        BRMLogger.debug("NameParser", "parser", s"dropped: $dropped")
        dropped
      }
    }

  }

  implicit class StringListToString(left: List[String]) {

    def listToString : String =
      left.foldLeft("")((x, acc) => s"$x $acc").trim

  }

}
