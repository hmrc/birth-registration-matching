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

package uk.gov.hmrc.brm.models.matching

import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeApplication
import play.api.test.Helpers._
import uk.gov.hmrc.brm.{BRMFakeApplication, BaseConfig}
import uk.gov.hmrc.brm.services.matching.{Bad, Good}
import uk.gov.hmrc.brm.services.parser.NameParser.Names
import uk.gov.hmrc.play.test.UnitSpec

class MatchingResultSpec extends UnitSpec with BRMFakeApplication {

  /*val ignoreAdditionalNamesEnabled: Map[String, _] = BaseConfig.config ++ Map(
    "microservice.services.birth-registration-matching.matching.ignoreAdditionalNames" -> true
  )*/

  val ignoreAdditionalNamesDisabled: Map[String, _] = BaseConfig.config ++ Map(
    "microservice.services.birth-registration-matching.matching.ignoreAdditionalNames" -> false
  )
  def getApp(config: Map[String, _]) = GuiceApplicationBuilder(disabled = Seq(classOf[com.kenshoo.play.metrics.PlayModule])).configure(config).build()


  "MatchingResult" should {

    "have default noMatch result" in {
      running(FakeApplication(additionalConfiguration = ignoreAdditionalNamesDisabled)) {
        MatchingResult.noMatch shouldBe MatchingResult(Bad(), Bad(), Bad(), Bad(), Names(Nil, Nil, Nil))
      }
    }

    "audit if not matched" in {
      running(FakeApplication(additionalConfiguration = ignoreAdditionalNamesDisabled)) {
        MatchingResult(Good(), Bad(), Bad(), Bad(), Names(Nil, Nil, Nil)).matched shouldBe false
        MatchingResult(Good(), Bad(), Bad(), Bad(), Names(Nil, Nil, Nil)).audit shouldBe Map(
          "match" -> "false",
          "matchFirstName" -> "true",
          "matchAdditionalNames" -> "false",
          "matchLastName" -> "false",
          "matchDateOfBirth" -> "false"
        )
      }
    }

    "audit if matched" in {
      running(FakeApplication(additionalConfiguration = ignoreAdditionalNamesDisabled)) {
        MatchingResult(Good(), Good(), Good(), Good(), Names(Nil, Nil, Nil)).matched shouldBe true
        MatchingResult(Good(), Good(), Good(), Good(), Names(Nil, Nil, Nil)).audit shouldBe Map(
          "match" -> "true",
          "matchFirstName" -> "true",
          "matchAdditionalNames" -> "true",
          "matchLastName" -> "true",
          "matchDateOfBirth" -> "true"
        )
      }
    }

    "cache Names without additionalNames" in {
      running(FakeApplication(additionalConfiguration = ignoreAdditionalNamesDisabled)) {
        val result = MatchingResult(Good(), Good(), Good(), Good(), Names(List("Adam"), Nil, List("Smith")))
        result.matched shouldBe true
        result.audit shouldBe Map(
          "match" -> "true",
          "matchFirstName" -> "true",
          "matchAdditionalNames" -> "true",
          "matchLastName" -> "true",
          "matchDateOfBirth" -> "true"
        )
      }
    }

    "cache Names with additionalNames" in {
      running(FakeApplication(additionalConfiguration = ignoreAdditionalNamesDisabled)) {
        val result = MatchingResult(Good(), Good(), Good(), Good(), Names(List("Adam"), List("Test"), List("Smith")))
        result.matched shouldBe true
        result.audit shouldBe Map(
          "match" -> "true",
          "matchFirstName" -> "true",
          "matchAdditionalNames" -> "true",
          "matchLastName" -> "true",
          "matchDateOfBirth" -> "true"
        )
      }
    }

    "cache Names with additionalNames where did not match additionalNames" in {
      running(FakeApplication(additionalConfiguration = ignoreAdditionalNamesDisabled)) {
        val result = MatchingResult(Good(), Bad(), Good(), Good(), Names(List("Adam"), List("Test"), List("Smith")))
        result.matched shouldBe false
        result.audit shouldBe Map(
          "match" -> "false",
          "matchFirstName" -> "true",
          "matchAdditionalNames" -> "false",
          "matchLastName" -> "true",
          "matchDateOfBirth" -> "true"
        )
      }
    }

  }

}
