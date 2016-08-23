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

import org.scalatest.Matchers
import uk.gov.hmrc.play.test.UnitSpec

/**
  * Created by chrisianson on 23/08/16.
  */
class HeaderValidatorSpec extends UnitSpec with Matchers with HeaderValidator {

  "acceptHeaderValidationRules" should {

    "return false when argument values are missing" in {
      acceptHeaderValidationRules() shouldBe false
    }

    "return false when contentType is invalid" in {
      acceptHeaderValidationRules(contentType = Some("text/html"), version = Some("1.0"), auditSource = Some("DFS")) shouldBe false
    }

    "return false when version is invalid" in {
      acceptHeaderValidationRules(contentType = Some("application/json"), version = Some("9"), auditSource = Some("DFS")) shouldBe false
    }

    "return false when auditSource is invalid" in {
      acceptHeaderValidationRules(contentType = Some("application/json"), version = Some("1.0"), auditSource = Some("")) shouldBe false
    }

    "return true when contentType, version and auditSource is valid and included" in {
      acceptHeaderValidationRules(contentType = Some("application/json"), version = Some("1.2"), auditSource = Some("DFS")) shouldBe true
    }
  }

}
