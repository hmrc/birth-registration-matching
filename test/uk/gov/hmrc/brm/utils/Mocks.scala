/*
 * Copyright 2022 HM Revenue & Customs
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

package uk.gov.hmrc.brm.utils

import org.scalatestplus.mockito.MockitoSugar
import org.slf4j.Logger
import play.api.libs.json.JsValue
import play.api.mvc.{Headers, Request}
import play.api.test.Helpers.stubControllerComponents
import uk.gov.hmrc.brm.audit.{WhereBirthRegisteredAudit, _}
import uk.gov.hmrc.brm.config.BrmConfig
import uk.gov.hmrc.brm.connectors.{BirthConnector, GROConnector, GRONIConnector, NRSConnector}
import uk.gov.hmrc.brm.controllers.BirthEventsController
import uk.gov.hmrc.brm.filters.Filters
import uk.gov.hmrc.brm.implicits.{AuditFactory, MetricsFactory}
import uk.gov.hmrc.brm.metrics._
import uk.gov.hmrc.brm.models.brm.Payload
import uk.gov.hmrc.brm.services.LookupService
import uk.gov.hmrc.brm.services.matching.{FullMatching, MatchingService, PartialMatching}
import uk.gov.hmrc.http.HttpClient
import uk.gov.hmrc.play.audit.http.connector.AuditConnector

/**
  * Created by adamconder on 24/02/2017.
  */
object Mocks extends MockitoSugar {

  val mockConnector: BirthConnector      = mock[BirthConnector]
  val mockAuditConnector: AuditConnector = mock[AuditConnector]
  val mockHttp: HttpClient               = mock[HttpClient]
  val mockLookupService: LookupService   = mock[LookupService]
  val mockLogger: Logger                 = mock[org.slf4j.Logger]
  val mockBrmLogger: BRMLogger           = mock[BRMLogger]
  val mockRequest: Request[JsValue]      = mock[Request[JsValue]]
  val headers: Headers                   = mock[Headers]

  val mockHeaderValidator: HeaderValidator = mock[HeaderValidator]
  val metrics: BRMMetrics                   = mock[BRMMetrics]
  val mockAuditor: BRMAudit                 = mock[BRMAudit]
  val mockAuditFactory: AuditFactory        = mock[AuditFactory]
  val mockMetricsFactory: MetricsFactory    = mock[MetricsFactory]
  val mockAPIAuditor: BRMDownstreamAPIAudit = mock[BRMDownstreamAPIAudit]
  val mockKeyGen: KeyGenerator              = mock[KeyGenerator]
  val mockConfig: BrmConfig                 = mock[BrmConfig]
  val mockMatchingAudit: MatchingAudit      = mock[MatchingAudit]
  val mockFullMatching: FullMatching        = mock[FullMatching]
  val mockPartialMatching: PartialMatching  = mock[PartialMatching]

  val mockGroConnector: GROConnector = mock[GROConnector]
  val mockNrsConnector: NRSConnector = mock[NRSConnector]
  val mockGroniConnector: GRONIConnector = mock[GRONIConnector]

  val mockTransactionAuditor: TransactionAuditor = mock[TransactionAuditor]
  val mockRecordParser: RecordParser             = mock[RecordParser]
  val mockMetrics: BRMMetrics                    = mock[BRMMetrics]
  val mockMatchMetrics: MatchCountMetric         = mock[MatchCountMetric]
  val mockNoMatchMetrics: NoMatchCountMetric     = mock[NoMatchCountMetric]
  val mockMatchingservice: MatchingService       = mock[MatchingService]
  val mockCommonUtil: CommonUtil                 = mock[CommonUtil]
  val mockFilters: Filters                       = mock[Filters]

  val mockEngWalesAudit: EnglandAndWalesAudit = mock[EnglandAndWalesAudit]
  val mockEngWalesMetric: EnglandAndWalesBirthRegisteredCountMetrics = mock[EnglandAndWalesBirthRegisteredCountMetrics]
  val mockScotAudit: ScotlandAudit = mock[ScotlandAudit]
  val mockScotMetric: ScotlandBirthRegisteredCountMetrics = mock[ScotlandBirthRegisteredCountMetrics]
  val mockIreAudit: NorthernIrelandAudit = mock[NorthernIrelandAudit]
  val mockIreMetric: NorthernIrelandBirthRegisteredCountMetrics = mock[NorthernIrelandBirthRegisteredCountMetrics]
  val mockInvalidMetric: InvalidBirthRegisteredCountMetrics = mock[InvalidBirthRegisteredCountMetrics]

  val mockWhereBirthRegister: WhereBirthRegisteredAudit = mock[WhereBirthRegisteredAudit]


  object MockBRMLogger extends BRMLogger(mockKeyGen)

  object MockMatchingService extends MatchingService(mockConfig, mockMatchingAudit, mockFullMatching, mockPartialMatching, mockBrmLogger) {
    override val matchOnMultiple: Boolean = true
  }
  class MockMatchingService(partial: PartialMatching) extends MatchingService(mockConfig, mockMatchingAudit, mockFullMatching, partial, mockBrmLogger) {
    override val matchOnMultiple: Boolean = true
  }

  object MockMatchingServiceMatchMultipleFalse extends MatchingService(mockConfig, mockMatchingAudit, mockFullMatching, mockPartialMatching, mockBrmLogger) {
    override val matchOnMultiple: Boolean = false
  }

  object MockLookupService extends LookupService(mockGroConnector,
                                                 mockNrsConnector,
                                                 mockGroniConnector,
                                                 mockMatchingservice,
                                                 mockTransactionAuditor,
                                                 mockBrmLogger,
                                                 mockRecordParser,
                                                 mockMatchMetrics,
                                                 mockNoMatchMetrics)

  object MockAuditFactory extends AuditFactory(mockEngWalesAudit, mockScotAudit, mockIreAudit) {
    override def getAuditor()(implicit payload: Payload): BRMDownstreamAPIAudit = auditorFixtures.englandAndWalesAudit
  }

  object MockController extends BirthEventsController(
    MockLookupService,
    auditorFixtures.whereBirthRegisteredAudit,
    MockAuditFactory,
    mockConfig,
    auditorFixtures.transactionAudit,
    auditorFixtures.matchingAudit,
    mockHeaderValidator,
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

  object MockControllerMockedLookup extends BirthEventsController(
    mockLookupService,
    auditorFixtures.whereBirthRegisteredAudit,
    MockAuditFactory,
    mockConfig,
    auditorFixtures.transactionAudit,
    auditorFixtures.matchingAudit,
    mockHeaderValidator,
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

  def connectorFixtures = {
    new {
      val groConnector = new GROConnector(mockHttp, mockConfig, mockKeyGen, mockBrmLogger, mockCommonUtil)
      val nrsConnector = new NRSConnector(mockHttp, mockConfig, mockCommonUtil, mockKeyGen, mockBrmLogger)
      val groniConnector = new GRONIConnector(
        mockHttp, auditor = new NorthernIrelandAudit(mockAuditConnector, mockConfig, mockKeyGen, mockBrmLogger), mockBrmLogger
      )
    }
  }

  def auditorFixtures = {
    new {
      val whereBirthRegisteredAudit = new WhereBirthRegisteredAudit(mockAuditConnector, mockKeyGen, mockBrmLogger)
      val englandAndWalesAudit = new EnglandAndWalesAudit(mockAuditConnector, mockKeyGen, mockConfig, mockBrmLogger)
      val scotlandAudit = new ScotlandAudit(mockAuditConnector, mockConfig, mockKeyGen, mockBrmLogger)
      val northernIrelandAudit = new NorthernIrelandAudit(mockAuditConnector, mockConfig, mockKeyGen, mockBrmLogger)
      val matchingAudit = new MatchingAudit(mockAuditConnector, mockBrmLogger, mockConfig, mockKeyGen)
      val transactionAudit = new TransactionAuditor(mockAuditConnector, mockKeyGen, mockConfig, mockBrmLogger)
    }
  }

}
