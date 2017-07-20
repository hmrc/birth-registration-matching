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
import org.scalatest.mock.MockitoSugar
import org.scalatest.{BeforeAndAfterEachTestData, Tag, TestData}
import org.scalatestplus.play.OneAppPerTest
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.JsValue
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.brm.models.matching.BirthMatchResponse
import uk.gov.hmrc.brm.utils.Mocks._
import uk.gov.hmrc.brm.utils.TestHelper._
import uk.gov.hmrc.play.audit.http.connector.AuditResult
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.Future

/**
  * Created by adamconder on 02/12/2016.
  */
class FeatureSwitchSpec extends UnitSpec with OneAppPerTest with MockitoSugar with BeforeAndAfterEachTestData {

  /**
    * Enable both GRO and NRS
    */

  lazy val switchEnabled: Map[String, _] = Map(
    "microservice.services.birth-registration-matching.features.dobValidation.enabled" -> false,
    "microservice.services.birth-registration-matching.features.dobValidation.value" -> "2009-07-01",
    //    Match switch
    "microservice.services.birth-registration-matching.matching.firstName" -> true,
    "microservice.services.birth-registration-matching.matching.lastName" -> true,
    "microservice.services.birth-registration-matching.matching.dateOfBirth" -> true,
    // GRO NRS Switch
    "microservice.services.birth-registration-matching.features.gro.enabled" -> true,
    "microservice.services.birth-registration-matching.features.gro.reference.enabled" -> true,
    "microservice.services.birth-registration-matching.features.gro.details.enabled" -> true,
    "microservice.services.birth-registration-matching.features.nrs.enabled" -> true,
    "microservice.services.birth-registration-matching.features.nrs.reference.enabled" -> true,
    "microservice.services.birth-registration-matching.features.nrs.details.enabled" -> true
  )

  /**
    * Disable both GRO and NRS
    */

  lazy val switchDisabled: Map[String, _] = Map(
    "microservice.services.birth-registration-matching.features.dobValidation.enabled" -> false,
    "microservice.services.birth-registration-matching.features.dobValidation.value" -> "2009-07-01",
    //    Match switch
    "microservice.services.birth-registration-matching.matching.firstName" -> true,
    "microservice.services.birth-registration-matching.matching.lastName" -> true,
    "microservice.services.birth-registration-matching.matching.dateOfBirth" -> true,
    // GRO NRS Switch
    "microservice.services.birth-registration-matching.features.gro.enabled" -> false,
    "microservice.services.birth-registration-matching.features.gro.reference.enabled" -> false,
    "microservice.services.birth-registration-matching.features.gro.details.enabled" -> false,
    "microservice.services.birth-registration-matching.features.nrs.enabled" -> false,
    "microservice.services.birth-registration-matching.features.nrs.reference.enabled" -> false,
    "microservice.services.birth-registration-matching.features.nrs.details.enabled" -> false
  )

  implicit override def newAppForTest(testData: TestData) : Application = {
    val config = if (testData.tags.contains("enabled")) {
      switchEnabled
    } else if (testData.tags.contains("disabled")) {
      switchDisabled
    } else { Map("" -> "") }

    new GuiceApplicationBuilder()
      .disable[com.kenshoo.play.metrics.PlayModule]
      .configure(config)
      .build()
  }

  override protected def beforeEach(testData: TestData): Unit = {
    reset(MockControllerMockedLookup.service)
  }

  def postRequest(v: JsValue): FakeRequest[JsValue] = FakeRequest("POST", "")
    .withHeaders((ACCEPT, "application/vnd.hmrc.1.0+json"), ("Audit-Source", "DFS"))
    .withBody(v)

  "Feature switches" when {

    "enabled for GRO" should {

      "search by child's details when the details switch is enabled and no reference number" taggedAs Tag("enabled") in {
        when(MockControllerMockedLookup.service.lookup()(Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(BirthMatchResponse(true)))
        when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))

        val request = postRequest(userNoMatchIncludingReferenceNumber)
        val result = await(MockControllerMockedLookup.post().apply(request))
        status(result) shouldBe OK
        (contentAsJson(result) \ "matched").as[Boolean] shouldBe true
        header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
        verify(MockControllerMockedLookup.service, atLeastOnce()).lookup()(Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any())
      }

      "search by reference number when the details switch is enabled and has reference number" taggedAs Tag("enabled") in {
        when(MockControllerMockedLookup.service.lookup()(Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(BirthMatchResponse(true)))
        when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))

        val request = postRequest(userNoMatchExcludingReferenceKey)
        val result = await(MockControllerMockedLookup.post().apply(request))
        status(result) shouldBe OK
        (contentAsJson(result) \ "matched").as[Boolean] shouldBe true
        header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
        verify(MockControllerMockedLookup.service, atLeastOnce()).lookup()(Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any())
      }

    }

    "disabled for GRO" should {

      "search by child's details when the details switch is disabled and no reference number" taggedAs Tag("disabled") in {
        when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))

        val request = postRequest(userNoMatchIncludingReferenceNumber)
        val result = await(MockControllerMockedLookup.post().apply(request))
        status(result) shouldBe OK
        (contentAsJson(result) \ "matched").as[Boolean] shouldBe false
        header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
        verify(MockControllerMockedLookup.service, never()).lookup()(Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any())
      }

      "search by reference number when the details switch is disabled and has reference number" taggedAs Tag("disabled") in {
        when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))

        val request = postRequest(userNoMatchExcludingReferenceKey)
        val result = await(MockControllerMockedLookup.post().apply(request))
        status(result) shouldBe OK
        (contentAsJson(result) \ "matched").as[Boolean] shouldBe false
        header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
        verify(MockControllerMockedLookup.service, never()).lookup()(Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any())
      }

    }

    "enabled for NRS" should {

      "search by child's details when the details switch is enabled and no reference number" taggedAs Tag("enabled") in {
        when(MockControllerMockedLookup.service.lookup()(Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(BirthMatchResponse(true)))
        when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))

        val request = postRequest(userNoMatchExcludingReferenceKeyScotland)
        val result = await(MockControllerMockedLookup.post().apply(request))
        status(result) shouldBe OK
        (contentAsJson(result) \ "matched").as[Boolean] shouldBe true
        header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
        verify(MockControllerMockedLookup.service, atLeastOnce()).lookup()(Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any())
      }

      "search by reference number when the details switch is enabled and has reference number" taggedAs Tag("enabled") in {
        when(MockControllerMockedLookup.service.lookup()(Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(BirthMatchResponse(true)))
        when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))

        val request = postRequest(userWhereBirthRegisteredScotland)
        val result = await(MockControllerMockedLookup.post().apply(request))
        status(result) shouldBe OK
        (contentAsJson(result) \ "matched").as[Boolean] shouldBe true
        header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
        verify(MockControllerMockedLookup.service, atLeastOnce()).lookup()(Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any())
      }

    }

    "disabled for NRS" should {

      "search by child's details when the details switch is disabled and no reference number" taggedAs Tag("disabled") in {
        when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))

        val request = postRequest(userNoMatchExcludingReferenceKeyScotland)
        val result = await(MockControllerMockedLookup.post().apply(request))
        status(result) shouldBe OK
        (contentAsJson(result) \ "matched").as[Boolean] shouldBe false
        header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
        verify(MockControllerMockedLookup.service, never()).lookup()(Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any())
      }

      "search by reference number when the details switch is disabled and has reference number" taggedAs Tag("disabled") in {
        when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))

        val request = postRequest(userWhereBirthRegisteredScotland)
        val result = await(MockControllerMockedLookup.post().apply(request))
        status(result) shouldBe OK
        (contentAsJson(result) \ "matched").as[Boolean] shouldBe false
        header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
        verify(MockControllerMockedLookup.service, never()).lookup()(Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any())
      }

    }




  }

}
