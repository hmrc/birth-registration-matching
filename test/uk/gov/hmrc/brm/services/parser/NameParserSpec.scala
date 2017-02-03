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

    /*
      If left is less or equal then drop elements from right
      If left is greater then don't drop elements from right
     */

    "filter right hand side list when left has less elements" in {
      val left = List("Adam", "David", "Charles")
      val right = List("Adam", "David", "Charles", "Edward")

      // filter the list on the right (record) with the number of occurences in the left
      val names = left filter right
      names should not be Nil
      names shouldBe List("Adam", "David", "Charles")
    }

    "filter right hand side list when left has equal elements" in {
      val left = List("Adam", "David", "Charles", "Edward")
      val right = List("Adam", "David", "Charles", "Edward")

      // filter the list on the right (record) with the number of occurences in the left
      val names = left filter right
      names should not be Nil
      names shouldBe List("Adam", "David", "Charles", "Edward")
    }

    "not filter right hand side list when left has more elements and return right" in {
      val left = List("Adam", "David", "Charles", "Edward")
      val right = List("Adam", "David", "Charles")

      // filter the list on the right (record) with the number of occurences in the left
      val names = left filter right
      names should not be Nil
      names shouldBe List("Adam", "David", "Charles")
    }

    "not filter when left and right have zero items" in {
      val left = Nil
      val right = Nil

      // filter the list on the right (record) with the number of occurences in the left
      val names = left filter right
      names shouldBe Nil
    }

    "not filter when right has zero items" in {
      val left = List("Adam", "David")
      val right = Nil

      val names = left filter right
      names shouldBe Nil
    }

    "not filter when left has zero items" in {
      val left = Nil
      val right = List("Adam", "David")

      val names = left filter right
      names shouldBe List("Adam", "David")
    }

    "Nil should build up the names into a string" in {
      val list = Nil
      list.listToString shouldBe ""
    }

    "List(adam, david) should build up the names into a string" in {
      val list = List("Adam", "David")
      list.listToString shouldBe "Adam David"
    }

    "List(adam, david, smith, test) should build up the names into a string" in {
      val list = List("Adam", "David", "Smith", "Test")
      list.listToString shouldBe "Adam David Smith Test"
    }

  }

}
