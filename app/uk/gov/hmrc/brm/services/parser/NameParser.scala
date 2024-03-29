/*
 * Copyright 2023 HM Revenue & Customs
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

import uk.gov.hmrc.brm.models.brm.Payload
import uk.gov.hmrc.brm.models.response.Record

/** Created by adamconder on 02/02/2017.
  */
object NameParser {

  /** Cache the names on the record after they have been split into FirstNames and MiddleNames
    * @param _firstNames getter for FirstNames
    * @param _additionalNames getter for AdditionalNames
    */
  case class Names(
    private val _firstNames: List[String],
    private val _additionalNames: List[String] = Nil,
    private val _lastNames: List[String]
  ) {

    def firstNames: String = _firstNames.listToString

    def additionalNames: String = _additionalNames.listToString

    def lastNames: String = _lastNames.listToString

  }

  def parseNames(payload: Payload, record: Record, ignoreAdditionalNames: Boolean = false): Names = {
    val inputLength = payload.firstNames.names.length

    val (firstNames, additionalNames) = record.child.forenames.names.splitAt(inputLength)
    val lastNames                     = record.child.lastName.names

    if (ignoreAdditionalNames) {
      Names(firstNames, Nil, lastNames)
    } else {
      Names(firstNames, additionalNames, lastNames)
    }
  }

  implicit class NameParserImplicit(val s: String) {
    lazy val regex = "\\s+"

    def names: List[String] =
      s.trim.split(regex).toList
  }

  implicit class FilterList[T](left: List[T]) {

    def filter(right: List[T]): List[T] =
      if (left.length > right.length || left.isEmpty || right.isEmpty) {
        right
      } else {
        val difference = right.length - left.length
        val dropped    = right.dropRight(difference)
        dropped
      }

  }

  implicit class StringListToString(left: List[String]) {
    def listToString: String = left.mkString(" ")
  }

}
