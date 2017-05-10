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

package uk.gov.hmrc.brm.controllers

import org.joda.time.LocalDate
import org.scalatest.TestData
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.OneAppPerTest
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import play.api.test.Helpers._
import uk.gov.hmrc.brm.models.brm.Payload
import uk.gov.hmrc.brm.utils.{BaseUnitSpec, BirthRegisterCountry}
import uk.gov.hmrc.brm.utils.Mocks._
import uk.gov.hmrc.play.test.UnitSpec

class BirthEventsControllerAdditionalNameSwitchSpec extends UnitSpec with OneAppPerTest with MockitoSugar with BaseUnitSpec {

  import uk.gov.hmrc.brm.utils.TestHelper._

  val config: Map[String, _] = Map(
    "microservice.services.birth-registration-matching.features.additionalNames.ignore.enabled" -> false
  )

  override def newAppForTest(testData: TestData) = new GuiceApplicationBuilder().configure(
    config
  ).build()

  "validating match when feature to ignore additional name is false." should {

    "return matched value of true request has additional names and record has same value" in {
      //request has additional name in it
      mockReferenceResponse(groResponseWithAdditionalName)
      //TODO once api ready change it.
      val payload = Json.toJson(Payload(Some("500035710"), "Adam Test", "SMITH", new LocalDate("2009-07-01"),
                    BirthRegisterCountry.ENGLAND))
      val result = makeRequest(payload)
      checkResponse(result,OK,  true)
    }

    "return matched value of false request has additional names and record does not have middle name in it." in {
      mockReferenceResponse(groResponseWithoutAdditionalName)
      val payload = Json.toJson(Payload(Some("500035711"), "Adam", "SMITH", new LocalDate("2009-07-01"),
        BirthRegisterCountry.ENGLAND))
      val result = makeRequest(payload)
      checkResponse(result,OK, true)
    }

  }

  def makeRequest(jsonRequest :JsValue):Result = {
    val request = postRequest(jsonRequest)
    val result = await(MockController.post().apply(request))
    result
  }
}
