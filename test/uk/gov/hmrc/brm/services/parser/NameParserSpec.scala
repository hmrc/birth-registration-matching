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

import uk.gov.hmrc.brm.BRMFakeApplication
import uk.gov.hmrc.brm.services.parser.NameParser._
import uk.gov.hmrc.play.test.UnitSpec

/**
  * Created by adamconder on 02/02/2017.
  */
class NameParserSpec extends UnitSpec with BRMFakeApplication {

  "NameParser" should {

    "split a string into words removing trailing space" in {
      val input = "    Adam David      Charles       Mary-Ann'é"
      val names : List[String] = input.names

      names.length shouldBe 4
      names.head shouldBe "adam"
      names(1) shouldBe "david"
      names(2) shouldBe "charles"
      names(3) shouldBe "mary-ann'é"
    }

    "filter two list of names and remove additional names in the list not in the input" in {
      val left = List("Adam", "David", "charles")
      val right = List("Adam", "David", "Charles", "Edward")

      // filter the list on the right (record) with the number of occurrances in the left
      val names = left filter right
      names should not be Nil
    }

  }

}
