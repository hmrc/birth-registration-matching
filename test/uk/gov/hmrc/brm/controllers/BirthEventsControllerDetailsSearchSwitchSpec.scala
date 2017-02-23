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

import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.{Tag, TestData}
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.OneAppPerTest
import play.api.Logger
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.JsValue
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.brm.audit.TransactionAuditor
import uk.gov.hmrc.brm.models.matching.BirthMatchResponse
import uk.gov.hmrc.brm.services.LookupService
import uk.gov.hmrc.brm.utils.TestHelper._
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.Future

/**
  * Created by adamconder on 02/12/2016.
  */
class BirthEventsControllerDetailsSearchSwitchSpec extends UnitSpec with OneAppPerTest with MockitoSugar {

  lazy val switchEnabled: Map[String, _] = Map(
    "microservice.services.birth-registration-matching.validateDobForGro" -> false,
    "microservice.services.birth-registration-matching.matching.firstName" -> true,
    "microservice.services.birth-registration-matching.matching.lastName" -> true,
    "microservice.services.birth-registration-matching.matching.dateOfBirth" -> true,
    "microservice.services.birth-registration-matching.matching.disableSearchByDetails" -> true
  )

  lazy val switchDisabled: Map[String, _] = Map(
    "microservice.services.birth-registration-matching.validateDobForGro" -> false,
    "microservice.services.birth-registration-matching.matching.firstName" -> true,
    "microservice.services.birth-registration-matching.matching.lastName" -> true,
    "microservice.services.birth-registration-matching.matching.dateOfBirth" -> true,
    "microservice.services.birth-registration-matching.matching.disableSearchByDetails" -> false
  )

  override def newAppForTest(testData: TestData) = new GuiceApplicationBuilder().configure {
    if (testData.tags.contains("enabled")) {
      Logger.debug(s"\n\n SWITCH ENABLED \n\n")
      switchEnabled
    } else if (testData.tags.contains("disabled")) {
      Logger.debug(s"\n\n SWITCH DISABLED \n\n")
      switchDisabled
    } else {
      Map("" -> "")
    }
  }.build()

  def postRequest(v: JsValue): FakeRequest[JsValue] = FakeRequest("POST", "/api/v0/events/birth")
    .withHeaders((ACCEPT, "application/vnd.hmrc.1.0+json"), ("Audit-Source", "DFS"))
    .withBody(v)

  object MockControllerMockLookup extends BirthEventsController {
    override val service = mock[LookupService]
  }

  "BirthEventsController" should {

   "not search by child's details when the details switch is enabled and no reference number" taggedAs Tag("enabled") in {
      val request = postRequest(userNoMatchExcludingReferenceKeyScotland)
      val result = await(MockControllerMockLookup.post().apply(request))
      status(result) shouldBe OK
      (contentAsJson(result) \ "matched").as[Boolean] shouldBe false
      header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
      verify(MockControllerMockLookup.service, never()).lookup()(Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any())
    }

    "search by child's details when the details switch is disabled and no reference number" taggedAs Tag("disabled") in {
      when(MockControllerMockLookup.service.lookup()(Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(BirthMatchResponse(true)))

      val request = postRequest(userNoMatchExcludingReferenceKeyScotland)
      val result = await(MockControllerMockLookup.post().apply(request))
      status(result) shouldBe OK
      (contentAsJson(result) \ "matched").as[Boolean] shouldBe true
      header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
      verify(MockControllerMockLookup.service, atLeastOnce()).lookup()(Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any())
    }

    "search by reference number when the details switch is enabled and has reference number" taggedAs Tag("enabled") in {
      when(MockControllerMockLookup.service.lookup()(Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(BirthMatchResponse(true)))

      val request = postRequest(userWhereBirthRegisteredScotland)
      val result = await(MockControllerMockLookup.post().apply(request))
      status(result) shouldBe OK
      (contentAsJson(result) \ "matched").as[Boolean] shouldBe true
      header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
      verify(MockControllerMockLookup.service, atLeastOnce()).lookup()(Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any())
    }

    "search by reference number when the details switch is disabled and has reference number" taggedAs Tag("disabled") in {
      when(MockControllerMockLookup.service.lookup()(Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(BirthMatchResponse(true)))

      val request = postRequest(userWhereBirthRegisteredScotland)
      val result = await(MockControllerMockLookup.post().apply(request))
      status(result) shouldBe OK
      (contentAsJson(result) \ "matched").as[Boolean] shouldBe true
      header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
      verify(MockControllerMockLookup.service, atLeastOnce()).lookup()(Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any())
    }
  }

}
