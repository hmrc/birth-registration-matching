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
import org.scalatest.{BeforeAndAfter, TestData}
import org.scalatestplus.play.OneAppPerTest
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers._
import uk.gov.hmrc.brm.utils.Mocks._
import uk.gov.hmrc.play.audit.http.connector.AuditResult
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.Future

class BirthEventsControllerValidationLengthSpec extends UnitSpec with OneAppPerTest with MockitoSugar with BeforeAndAfter {

  import uk.gov.hmrc.brm.utils.TestHelper._

  val config: Map[String, _] = Map(
    "microservice.services.birth-registration-matching.validation.maxNameLength" -> 250
  )

  override def newAppForTest(testData: TestData) = new GuiceApplicationBuilder().configure(
    config
  ).build()

  before {
    reset(MockController.service.groConnector)
  }

  "validating max length change" should {

    "return OK if firstName < 250 characters" in {
      when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
      when(MockController.service.groConnector.getReference(Matchers.any())(Matchers.any())).thenReturn(Future.successful(httpResponse(groJsonResponseObject)))
      val request = postRequest(firstNameWithMoreThan100Characters)
      val result = await(MockController.post().apply(request))
      status(result) shouldBe OK
      contentType(result).get shouldBe "application/json"
      header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
      (contentAsJson(result) \ "matched").as[Boolean] shouldBe false
    }

    "return BAD_REQUEST if firstName > 250 characters" in {
      when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
      val request = postRequest(firstNameWithMoreThan250Characters)
      val result = await(MockController.post().apply(request))
      status(result) shouldBe BAD_REQUEST
      contentType(result).get shouldBe "application/json"
      header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
      bodyOf(result).toString() shouldBe(empty)
      verify(MockController.service.groConnector, never).getReference(Matchers.any())(Matchers.any())
    }

    "return BAD_REQUEST if lastName > 250 characters" in {
      when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
      val request = postRequest(lastNameWithMoreThan250Characters)
      val result = await(MockController.post().apply(request))
      status(result) shouldBe BAD_REQUEST
      contentType(result).get shouldBe "application/json"
      header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
      bodyOf(result).toString() shouldBe(empty)
      verify(MockController.service.groConnector, never).getReference(Matchers.any())(Matchers.any())
    }

  }

}
