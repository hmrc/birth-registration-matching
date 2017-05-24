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

package uk.gov.hmrc.brm.utils

import akka.stream.Materializer
import org.scalatest.mock.MockitoSugar
import play.api.Play
import play.api.libs.json.JsValue
import play.api.mvc.{Headers, Request}
import uk.gov.hmrc.brm.audit._
import uk.gov.hmrc.brm.connectors.{BirthConnector, GROConnector, GRONIConnector, NRSConnector}
import uk.gov.hmrc.brm.controllers.BirthEventsController
import uk.gov.hmrc.brm.implicits.Implicits.AuditFactory
import uk.gov.hmrc.brm.metrics.BRMMetrics
import uk.gov.hmrc.brm.models.brm.Payload
import uk.gov.hmrc.brm.services.LookupService
import uk.gov.hmrc.brm.services.matching.MatchingService
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.http.HttpPost

/**
  * Created by adamconder on 24/02/2017.
  */
object Mocks extends MockitoSugar {

  implicit lazy val materializer = Play.current.injector.instanceOf[Materializer]

  val mockConnector = mock[BirthConnector]
  val mockAuditConnector = mock[AuditConnector]
  val mockHttpPost = mock[HttpPost]
  val mockLookupService = mock[LookupService]
  val mockLogger = mock[org.slf4j.Logger]
  val mockRequest = mock[Request[JsValue]]
  val headers = mock[Headers]

  implicit val metrics = mock[BRMMetrics]
  implicit val mockAuditor = mock[BRMAudit]

  object MockBRMLogger extends BRMLogger(mockLogger)

  object MockMatchingService extends MatchingService {
    override val matchOnMultiple: Boolean = true
    override val auditor = auditorFixtures.matchingAudit
  }

  object MockMatchingServiceMatchMultipleFalse extends MatchingService {
    override val matchOnMultiple: Boolean = false
    override val auditor = auditorFixtures.matchingAudit
  }

  object MockLookupService extends LookupService {
    override val groConnector = mockConnector
    override val groniConnector = mockConnector
    override val nrsConnector = mockConnector
    override val matchingService = MockMatchingServiceMatchMultipleFalse
    override val transactionAuditor = auditorFixtures.transactionAudit
  }

  object MockAuditFactory extends AuditFactory {
    override def getAuditor()(implicit payload: Payload): BRMAudit = auditorFixtures.englandAndWalesAudit
  }

  object MockController extends BirthEventsController {
    override val service = MockLookupService
    override val countryAuditor = auditorFixtures.whereBirthRegisteredAudit
    override val auditFactory = MockAuditFactory
    override val transactionAuditor: TransactionAuditor = auditorFixtures.transactionAudit
    override val matchingAuditor: MatchingAudit = auditorFixtures.matchingAudit
    override val headerValidator: HeaderValidator = HeaderValidator
  }

  object MockControllerMockedLookup extends BirthEventsController {
    override val service = mockLookupService
    override val countryAuditor = auditorFixtures.whereBirthRegisteredAudit
    override val auditFactory = MockAuditFactory
    override val transactionAuditor: TransactionAuditor = auditorFixtures.transactionAudit
    override val matchingAuditor: MatchingAudit = auditorFixtures.matchingAudit
    override val headerValidator: HeaderValidator = HeaderValidator
  }

  def connectorFixtures = {
    new {
      val groConnector = new GROConnector(mockHttpPost)
      val nrsConnector = new NRSConnector(mockHttpPost, auditor = new ScotlandAudit(mockAuditConnector))
      val groniConnector = new GRONIConnector(auditor = new NorthernIrelandAudit(mockAuditConnector))
    }
  }

  def auditorFixtures = {
    new {
      val whereBirthRegisteredAudit = new WhereBirthRegisteredAudit(mockAuditConnector)
      val englandAndWalesAudit = new EnglandAndWalesAudit(mockAuditConnector)
      val scotlandAudit = new ScotlandAudit(mockAuditConnector)
      val northernIrelandAudit = new NorthernIrelandAudit(mockAuditConnector)
      val matchingAudit = new MatchingAudit(mockAuditConnector)
  val transactionAudit = new TransactionAuditor(mockAuditConnector)
}
}

}
