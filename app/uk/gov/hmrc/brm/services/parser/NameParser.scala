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

    private def toList(x: Array[String], key: String) = {
      BRMLogger.debug("NameParser", "parse", s"$key: ${x.toList}, regex: $regex")
      x.toList
    }

    def names: List[String] = {
      toList(s.toLowerCase.trim.split(regex), "names")
    }

    def namesOriginalCase: List[String] = {
      toList(s.trim.split(regex), "namesOriginalCase")
    }
    
  }

  implicit class FilterList[T](left : List[T]) {

    def filter(right : List[T]) : List[T] = {

      if (left.length > right.length || left.isEmpty || right.isEmpty) {
        BRMLogger.debug("NameParser", "parser", s"Not dropping any names")
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
    def listToString : String = left.foldLeft("")((x, acc) => s"$x $acc").trim
  }

}
