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

package uk.gov.hmrc.brm.models.matching

import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.brm.services.matching.{Bad, Good}
import uk.gov.hmrc.brm.services.parser.NameParser.Names
import uk.gov.hmrc.brm.utils.BaseUnitSpec

class MatchingResultSpec extends BaseUnitSpec {

  val ignoreAdditionalNamesDisabled: Map[String, _] = Map(
    "microservice.services.birth-registration-matching.matching.ignoreAdditionalNames" -> false
  )

  override lazy val app: Application = GuiceApplicationBuilder()
    .configure(ignoreAdditionalNamesDisabled)
    .build()

  "MatchingResult" should {

    "have default noMatch result" in {
      MatchingResult.noMatch shouldBe MatchingResult(Bad(), Bad(), Bad(), Bad(), Bad(), Names(Nil, Nil, Nil))
    }

    "audit if not matched" in {
      MatchingResult(Bad(), Good(), Bad(), Bad(), Bad(), Names(Nil, Nil, Nil)).matched shouldBe false
      MatchingResult(Bad(), Good(), Bad(), Bad(), Bad(), Names(Nil, Nil, Nil)).audit   shouldBe Map(
        "match"                -> "false",
        "matchFirstName"       -> "true",
        "matchAdditionalNames" -> "false",
        "matchLastName"        -> "false",
        "matchDateOfBirth"     -> "false"
      )
    }

    "audit if matched" in {
      MatchingResult(Good(), Good(), Good(), Good(), Good(), Names(Nil, Nil, Nil)).matched shouldBe true
      MatchingResult(Good(), Good(), Good(), Good(), Good(), Names(Nil, Nil, Nil)).audit   shouldBe Map(
        "match"                -> "true",
        "matchFirstName"       -> "true",
        "matchAdditionalNames" -> "true",
        "matchLastName"        -> "true",
        "matchDateOfBirth"     -> "true"
      )
    }

    "cache Names without additionalNames" in {
      val result = MatchingResult(Good(), Good(), Good(), Good(), Good(), Names(List("Adam"), Nil, List("Smith")))
      result.matched shouldBe true
      result.audit   shouldBe Map(
        "match"                -> "true",
        "matchFirstName"       -> "true",
        "matchAdditionalNames" -> "true",
        "matchLastName"        -> "true",
        "matchDateOfBirth"     -> "true"
      )
    }

    "cache Names with additionalNames" in {
      val result =
        MatchingResult(Good(), Good(), Good(), Good(), Good(), Names(List("Adam"), List("Test"), List("Smith")))
      result.matched shouldBe true
      result.audit   shouldBe Map(
        "match"                -> "true",
        "matchFirstName"       -> "true",
        "matchAdditionalNames" -> "true",
        "matchLastName"        -> "true",
        "matchDateOfBirth"     -> "true"
      )
    }

    "cache Names with additionalNames where did not match additionalNames" in {
      val result =
        MatchingResult(Bad(), Good(), Bad(), Good(), Good(), Names(List("Adam"), List("Test"), List("Smith")))
      result.matched shouldBe false
      result.audit   shouldBe Map(
        "match"                -> "false",
        "matchFirstName"       -> "true",
        "matchAdditionalNames" -> "false",
        "matchLastName"        -> "true",
        "matchDateOfBirth"     -> "true"
      )
    }

  }

}
