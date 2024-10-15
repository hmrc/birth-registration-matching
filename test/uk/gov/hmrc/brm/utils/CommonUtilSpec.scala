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

package uk.gov.hmrc.brm.utils

import org.mockito.Mockito.when
import uk.gov.hmrc.brm.config.BrmConfig

class CommonUtilSpec extends BaseUnitSpec {

  val mockConfig: BrmConfig = mock[BrmConfig]
  val mockLogger: BRMLogger = mock[BRMLogger]

  val commonUtil: CommonUtil = new CommonUtil(
    mockConfig,
    mockLogger
  )

  "forenames" should {

    "ignore additionalNames argument if ignoreAdditionalNames is set to true" in {
      when(mockConfig.ignoreAdditionalNames)
        .thenReturn(true)

      commonUtil.forenames("John", Some("Jones")) shouldBe "John"
    }

    "return additionalNames argument if ignoreAdditionalNames is set to false" in {
      when(mockConfig.ignoreAdditionalNames)
        .thenReturn(false)

      commonUtil.forenames("John", Some("Jones")) shouldBe "John Jones"
    }

    "return string with trailing and ending space trimmed when ignoreAdditionalNames is set to true" in {
      when(mockConfig.ignoreAdditionalNames)
        .thenReturn(true)

      commonUtil.forenames(" John   ", Some("  Jones  Smith  ")) shouldBe "John"
    }

    "return string with trailing and ending space trimmed when ignoreAdditionalNames is set to false" in {
      when(mockConfig.ignoreAdditionalNames)
        .thenReturn(false)

      commonUtil.forenames(" John   ", Some("  Jones Smith  ")) shouldBe "John Jones Smith"
    }

    "convert multiple space separated words to single space separated words" in {
      when(mockConfig.ignoreAdditionalNames)
        .thenReturn(false)

      commonUtil.forenames("  John   ", Some("  Jones  Smith  ")) shouldBe "John Jones Smith"
    }

  }

  "logTime" should {
    "execute without errors" in {
      val startTime = System.currentTimeMillis()

      noException should be thrownBy {
        commonUtil.logTime(startTime)
      }
    }
  }
}
