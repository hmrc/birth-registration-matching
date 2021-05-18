/*
 * Copyright 2021 HM Revenue & Customs
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

package uk.gov.hmrc.brm.filters.flags

import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import uk.gov.hmrc.brm.config.BrmConfig
import uk.gov.hmrc.brm.models.response.gro.FlagSeverity
import uk.gov.hmrc.brm.models.response.nrs.NRSStatus
import org.scalatest.{Matchers, OptionValues, WordSpecLike}

class NRSFlagSeveritySpec extends WordSpecLike with Matchers with OptionValues with MockitoSugar with GuiceOneAppPerSuite {

  val allFlagsGreen: NRSStatus = NRSStatus(status = 1, deathCode = 0)
  val conf: BrmConfig = app.injector.instanceOf[BrmConfig]

  "determineFlagSeverity" should {

    "return FlagSeverity" in {
      allFlagsGreen.determineFlagSeverity shouldBe a[FlagSeverity]
    }

    "return true when all flags are default value" in {
      val groFlags = allFlagsGreen.determineFlagSeverity
      groFlags.canProcessRecord(conf) shouldBe true
    }
  }
}
