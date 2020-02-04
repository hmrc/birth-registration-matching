/*
 * Copyright 2020 HM Revenue & Customs
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

import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers._
import uk.gov.hmrc.play.test.UnitSpec

class CommonUtilSpec extends UnitSpec {

  lazy val ignoreAdditionalNamesEnabled: Map[String, Boolean] = Map(
    "microservice.services.birth-registration-matching.matching.ignoreAdditionalNames" -> true
  )

  lazy val ignoreAdditionalNamesDisabled: Map[String, Boolean] = Map(
    "microservice.services.birth-registration-matching.matching.ignoreAdditionalNames" -> false
  )

  val ignoreAdditionalNamesOnAppEnabled = GuiceApplicationBuilder()
    .disable[com.kenshoo.play.metrics.PlayModule]
    .configure(ignoreAdditionalNamesEnabled)
    .build()
  val ignoreAdditionalNamesOnAppDisabled = GuiceApplicationBuilder()
    .disable[com.kenshoo.play.metrics.PlayModule]
    .configure(ignoreAdditionalNamesDisabled)
    .build()

  "forenames" should {

    "ignore additionalNames argument if ignoreAdditionalNames is set to true" in running(ignoreAdditionalNamesOnAppEnabled){
      CommonUtil.forenames("John", Some("Jones")) shouldBe "John"
    }

    "return additionalNames argument if ignoreAdditionalNames is set to false" in running(ignoreAdditionalNamesOnAppDisabled) {
      CommonUtil.forenames("John", Some("Jones")) shouldBe "John Jones"
    }

    "return string with trailing and ending space trimmed when ignoreAdditionalNames is set to true" in running(ignoreAdditionalNamesOnAppEnabled){
      CommonUtil.forenames(" John   ", Some("  Jones  Smith  ")) shouldBe "John"
    }

    "return string with trailing and ending space trimmed when ignoreAdditionalNames is set to false" in running(ignoreAdditionalNamesOnAppDisabled) {
      CommonUtil.forenames(" John   ", Some("  Jones Smith  ")) shouldBe "John Jones Smith"
    }

    "convert multiple space separated words to single space separated words" in running(ignoreAdditionalNamesOnAppDisabled) {
      CommonUtil.forenames("  John   ", Some("  Jones  Smith  ")) shouldBe "John Jones Smith"
    }

  }

}
