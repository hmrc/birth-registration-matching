/*
 * Copyright 2026 HM Revenue & Customs
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
import org.mockito.Mockito.when
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.JsValue
import play.api.mvc.Result
import play.api.test.Helpers._
import uk.gov.hmrc.brm.config.BrmConfig
import uk.gov.hmrc.brm.models.matching.BirthMatchResponse
import uk.gov.hmrc.brm.utils.Mocks._
import uk.gov.hmrc.brm.utils.{BaseUnitSpec, HeaderValidator}
import uk.gov.hmrc.play.audit.http.connector.AuditResult

import scala.concurrent.Future

class BirthEventsControllerDOBSwitchSpec extends BaseUnitSpec {

  import uk.gov.hmrc.brm.utils.TestHelper._

  val config: Map[String, _] = Map(
    "microservice.services.birth-registration-matching.features.dobValidation.enabled" -> true,
    "microservice.services.birth-registration-matching.matching.firstName"             -> true,
    "microservice.services.birth-registration-matching.matching.lastName"              -> true,
    "microservice.services.birth-registration-matching.matching.dateOfBirth"           -> false
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

  def makeRequest(jsonRequest: JsValue): Result = {
    mockAuditSuccess
    val request = postRequest(jsonRequest)
    val result  = testController.post().apply(request).futureValue
    result
  }

  def makeRealRequest(jsonRequest: JsValue): Result = {
    mockAuditSuccess
    val request = postRequest(jsonRequest)
    val result  = app.injector.instanceOf[BirthEventsController].post().apply(request).futureValue
    result
  }

  "validating date of birth with dobValidation feature" should {

    "return matched value of true when the dateOfBirth is greater than 2009-07-01 and the gro record matches" in {
      when(mockGroConnector.getReference(any())(any(), any()))
        .thenReturn(Future.successful(httpResponse(groJsonResponseObject20120216)))

      when(mockLookupService.lookup()(any(), any(), any(), any()))
        .thenReturn(Future.successful(BirthMatchResponse(true)))

      when(mockFilters.process(any()))
        .thenReturn(List())

      when(mockAuditor.audit(any(), any())(any()))
        .thenReturn(Future.successful(AuditResult.Success))

      when(mockMetricsFactory.getMetrics()(any()))
        .thenReturn(mockEngWalesMetric)

      val result = makeRequest(userValidDOB)
      checkResponse(result, OK, matchResponse = true)
    }

    "return matched value of true when the dateOfBirth is equal to 2009-07-01 and the gro record matches" in {
      mockReferenceResponse(groJsonResponseObject20090701)
      val result = makeRequest(userValidDOB20090701)
      checkResponse(result, OK, matchResponse = true)
    }

    "return matched value of false when the dateOfBirth is invalid and the gro record matches" in {
      mockReferenceResponse(groJsonResponseObject)
      val result = makeRealRequest(userInvalidDOB)
      checkResponse(result, OK, matchResponse = false)
    }

    "return matched value of false when the dateOfBirth is one day earlier than 2009-07-01 and the gro record matches" in {
      mockReferenceResponse(groJsonResponseObject20090630)
      val result = makeRealRequest(userValidDOB20090630)
      checkResponse(result, OK, matchResponse = false)
    }
  }

}
