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

package uk.gov.hmrc.brm.switches

import org.scalatest.{BeforeAndAfter, Tag, TestData}
import org.scalatestplus.play.OneAppPerTest
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.play.test.UnitSpec

/**
  * Created by mew on 15/05/2017.
  */
class SwitchSpec extends UnitSpec with BeforeAndAfter with OneAppPerTest {

  object TestSwitch extends Switch {
    override val name = "test"
  }

  def switchDisabled: Map[String, _] = Map(
    "microservice.services.birth-registration-matching.features.test.enabled" -> false
  )

  def switchEnabled: Map[String, _] = Map(
    "microservice.services.birth-registration-matching.features.test.enabled" -> true
  )

  override def newAppForTest(testData: TestData) = GuiceApplicationBuilder(
    disabled = Seq(classOf[com.kenshoo.play.metrics.PlayModule])
  ).configure {
    if (testData.tags.contains("enabled")) {
      switchEnabled
    } else if (testData.tags.contains("disabled")) {
      switchDisabled
    } else {
      throw new Exception("cannot create correct test configuration")
    }
  }.build()

  "Switch" should {

    "load configuration for a feature and return true for isEnabled" taggedAs Tag("enabled") in {
      val switch = TestSwitch
      switch.isEnabled shouldBe true
    }

    "load configuration for a feature and return false for isEnabled" taggedAs Tag("disabled") in {
      val switch = TestSwitch
      switch.isEnabled shouldBe false
    }

  }

}
