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

package uk.gov.hmrc.brm.controllers

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfter
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers._
import uk.gov.hmrc.brm.config.BrmConfig
import uk.gov.hmrc.brm.models.matching.BirthMatchResponse
import uk.gov.hmrc.brm.utils.Mocks._
import uk.gov.hmrc.brm.utils.{BaseUnitSpec, HeaderValidator, MockErrorResponses}
import uk.gov.hmrc.play.audit.http.connector.AuditResult

import scala.concurrent.Future

class BirthEventsControllerValidationLengthSpec extends BaseUnitSpec with BeforeAndAfter {

  import uk.gov.hmrc.brm.utils.TestHelper._

  val config: Map[String, _] = Map(
    "microservice.services.birth-registration-matching.validation.maxNameLength" -> 250
  )

  val testController: BirthEventsController = new BirthEventsController(
    mockLookupService,
    auditorFixtures.whereBirthRegisteredAudit,
    MockAuditFactory,
    app.injector.instanceOf[BrmConfig],
    auditorFixtures.transactionAudit,
    auditorFixtures.errorAudit,
    auditorFixtures.matchingAudit,
    app.injector.instanceOf[HeaderValidator],
    stubControllerComponents(),
    mockCommonUtil,
    mockBrmLogger,
    mockMetricsFactory,
    mockFilters,
    mockEngWalesMetric,
    mockIreMetric,
    mockScotMetric,
    mockInvalidMetric
  )

  override lazy val app: Application = new GuiceApplicationBuilder()
    .configure(config)
    .build()

  before {
    reset(mockGroConnector)
  }

  "validating max length change" should {

    "return OK if firstName < 250 characters" in {
      when(mockLookupService.lookup()(any(), any(), any(), any()))
        .thenReturn(Future.successful(BirthMatchResponse()))

      when(mockFilters.process(any()))
        .thenReturn(List())

      when(mockAuditor.audit(any(), any())(any()))
        .thenReturn(Future.successful(AuditResult.Success))

      when(mockMetricsFactory.getMetrics()(any()))
        .thenReturn(mockEngWalesMetric)
      mockAuditSuccess
      mockReferenceResponse(groJsonResponseObject)
      val request = postRequest(firstNameWithMoreThan100Characters)
      val result  = testController.post().apply(request).futureValue
      checkResponse(result, OK, matchResponse = false)
    }

    "return BAD_REQUEST if firstName > 250 characters" in {
      val request = postRequest(firstNameWithMoreThan250Characters)
      val result  = testController.post().apply(request).futureValue
      checkResponse(result, BAD_REQUEST, MockErrorResponses.INVALID_FIRSTNAME.json)
      verify(mockGroConnector, never).getReference(any())(any(), any())
    }

    "return BAD_REQUEST if lastName > 250 characters" in {
      val request = postRequest(lastNameWithMoreThan250Characters)
      val result  = testController.post().apply(request).futureValue
      checkResponse(result, BAD_REQUEST, MockErrorResponses.INVALID_LASTNAME.json)
      verify(mockGroConnector, never).getReference(any())(any(), any())
    }

  }

}
