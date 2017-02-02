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
      val input = "Adam David      Charles       Mary-Ann'é"
      val names : List[String] = input.names

      names.length shouldBe 4
      names.head shouldBe "Adam"
      names(1) shouldBe "David"
      names(2) shouldBe "Charles"
      names(3) shouldBe "Mary-Ann'é"
    }

  }

}
