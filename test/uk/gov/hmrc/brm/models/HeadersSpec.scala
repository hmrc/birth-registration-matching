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

package uk.gov.hmrc.brm.models

import play.api.i18n.Messages
import uk.gov.hmrc.play.test.UnitSpec

/**
 * Created by adamconder on 18/08/2016.
 */
class HeadersSpec extends UnitSpec {

  "Headers" should {

    "instantiate an instance of Headers" in {
      val headers = BRMHeaders(
        apiVersion = 1.0,
        auditSource = "dfs"
      )

      headers.apiVersion shouldBe 1.0
      headers.auditSource shouldBe "dfs"
    }

    "throw IllegalArgumentException when invalid Api-Version" in {
      val error = intercept[IllegalArgumentException] {
        BRMHeaders(
          apiVersion = 0.0,
          auditSource = "dfs"
        )
      }
      error.getMessage shouldBe "requirement failed: Accept header application/vnd.hmrc.1.0+json must be 1.0 or greater"
    }

    "throw IllegalArgumentException whe invalid AuditSource" in {
      val error = intercept[IllegalArgumentException] {
        BRMHeaders(
          apiVersion = 1.0,
          auditSource = ""
        )
      }
      error.getMessage shouldBe "requirement failed: Audit-Source must not be empty"
    }

  }

}
