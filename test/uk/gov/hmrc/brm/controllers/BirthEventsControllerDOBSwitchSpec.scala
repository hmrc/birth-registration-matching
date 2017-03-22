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
import org.scalatest.TestData
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.OneAppPerTest
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers._
import uk.gov.hmrc.brm.utils.Mocks._
import uk.gov.hmrc.play.audit.http.connector.AuditResult
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.Future

class BirthEventsControllerDOBSwitchSpec extends UnitSpec with OneAppPerTest with MockitoSugar {

  import uk.gov.hmrc.brm.utils.TestHelper._

  val config: Map[String, _] = Map(
    "microservice.services.birth-registration-matching.features.dobValidation.enabled" -> true,
    "microservice.services.birth-registration-matching.matching.firstName" -> true,
    "microservice.services.birth-registration-matching.matching.lastName" -> true,
    "microservice.services.birth-registration-matching.matching.dateOfBirth" -> false
  )

  override def newAppForTest(testData: TestData) = new GuiceApplicationBuilder().configure(
    config
  ).build()

  "validating date of birth with dobValidation feature" should {

    "return matched value of true when the dateOfBirth is greater than 2009-07-01 and the gro record matches" in {
      when(MockController.service.groConnector.getReference(Matchers.any())(Matchers.any())).thenReturn(Future.successful(httpResponse(groJsonResponseObject20120216)))
      when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))

      val request = postRequest(userValidDOB)
      val result = await(MockController.post().apply(request))
      status(result) shouldBe OK
      (contentAsJson(result) \ "matched").as[Boolean] shouldBe true
      header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
    }

    "return matched value of true when the dateOfBirth is equal to 2009-07-01 and the gro record matches" in {
      when(MockController.service.groConnector.getReference(Matchers.any())(Matchers.any())).thenReturn(Future.successful(httpResponse(groJsonResponseObject20090701)))
      when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))

      val request = postRequest(userValidDOB20090701)
      val result = await(MockController.post().apply(request))
      status(result) shouldBe OK
      (contentAsJson(result) \ "matched").as[Boolean] shouldBe true
      header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
    }

    "return matched value of false when the dateOfBirth is invalid and the gro record matches" in {
      when(MockController.service.groConnector.getReference(Matchers.any())(Matchers.any())).thenReturn(Future.successful(httpResponse(groJsonResponseObject)))
      when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))

      val request = postRequest(userInvalidDOB)
      val result = await(MockController.post().apply(request))
      status(result) shouldBe OK
      (contentAsJson(result) \ "matched").as[Boolean] shouldBe false
      header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
    }

    "return matched value of false when the dateOfBirth is one day earlier than 2009-07-01 and the gro record matches" in {
      when(MockController.service.groConnector.getReference(Matchers.any())(Matchers.any())).thenReturn(Future.successful(httpResponse(groJsonResponseObject20090630)))
      when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))

      val request = postRequest(userValidDOB20090630)
      val result = await(MockController.post().apply(request))
      status(result) shouldBe OK
      (contentAsJson(result) \ "matched").as[Boolean] shouldBe false
      header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
    }

  }

}
