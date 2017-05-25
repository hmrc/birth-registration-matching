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
import uk.gov.hmrc.brm.models.brm.Payload
import uk.gov.hmrc.brm.models.response.Record
import uk.gov.hmrc.brm.utils.BRMLogger

/**
  * Created by adamconder on 02/02/2017.
  */
object NameParser {

  /**
    * Cache the names on the record after they have been split into FirstNames and MiddleNames
    * @param _firstNames getter for FirstNames
    * @param _additionalNames getter for AdditionalNames
    */
  case class Names(private val _firstNames : List[String],
                   private val _additionalNames : List[String],
                   private val _lastNames : List[String]) {

    def firstNames : String = _firstNames.listToString

    def additionalNames : String = _additionalNames.listToString

    def lastNames : String = _lastNames.listToString

  }

  private[NameParser] def ignoreAdditionalNames : Boolean = BrmConfig.ignoreAdditionalNames

  /**
    * TODO: do we need to do this now?
    * We can now just use the parseNames() method and ignore the Names(_additionalNames) argument when not matching
    * middle names
    * We no longer need to modify the givenName string on the record
    */

  def parseNames(payload: Payload, record: Record) : Names = {
    val inputLength = payload.firstNames.names.length

    val (firstNames, additionalNames) = record.child.forenames.names.splitAt(inputLength)
    val lastNames = record.child.lastName.names

    if (ignoreAdditionalNames) {
      Names(firstNames, Nil, lastNames)
    } else {
      Names(firstNames, additionalNames, lastNames)
    }
  }

  implicit class NameParserImplicit(val s: String) {

    lazy val regex = BrmConfig.ignoreMiddleNamesRegex

    private def toList(x: Array[String], key: String) = {
      BRMLogger.debug("NameParser", "parse", s"$key: ${x.toList}, regex: $regex")
      x.toList
    }

    def names: List[String] = {
      toList(s.trim.split(regex), "names")
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
