/*
 * Copyright 2016 HM Revenue & Customs
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

import uk.gov.hmrc.play.test.UnitSpec

class FormatSpec extends UnitSpec {

  "NameFormat" should {

    "convert to lowercase" in {
      val input: String = "JAMES"
      NameFormat(input) shouldBe ("james")
    }

    "trim whitespace from the start of a string" in {
      val input: String = " james"
      NameFormat(input) shouldBe ("james")
    }

    "trim whitespace from the end of a string" in {
      val input: String = "james "
      NameFormat(input) shouldBe ("james")
    }

    "trim whitespace from the start and end of a string" in {
      val input: String = " james jones "
      NameFormat(input) shouldBe ("james jones")
    }

    "don't trim whitespace from the middle of a string" in {
      val input: String = "james jones "
      NameFormat(input) shouldBe ("james jones")
    }

    "trim whitespace and convert to lowercase" in {
      val input: String = " JamEs JONES "
      NameFormat(input) shouldBe ("james jones")
    }
  }

  "DateFormat" should {

    "insert leading zeros on month" in {
      val input: String = "2007-1-01"
      DateFormat(input) shouldBe ("2007-01-01")
    }

    "insert leading zeros on day" in {
      val input: String = "2007-01-1"
      DateFormat(input) shouldBe ("2007-01-01")
    }

    "apply no formatting to correctly formatted strings" in {
      val input: String = "2007-12-24"
      DateFormat(input) shouldBe ("2007-12-24")
    }

    "return original string if the string couldn't be formatted" in {
      val input: String = "2007"
      DateFormat(input) shouldBe "2007"
    }
  }

}
