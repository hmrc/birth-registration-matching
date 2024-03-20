/*
 * Copyright 2024 HM Revenue & Customs
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

package uk.gov.hmrc.brm.audit

import com.google.inject.Singleton
import uk.gov.hmrc.brm.models.brm.Payload
import uk.gov.hmrc.brm.utils.{BRMLogger, KeyGenerator}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.{AuditConnector, AuditResult}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ErrorAudit @Inject() (
  connector: AuditConnector,
  val logger: BRMLogger,
  val keyGen: KeyGenerator
)(implicit ec: ExecutionContext)
    extends BRMAudit(connector) {

  final private class PayloadErrorEvent(result: Map[String, String])(implicit hc: HeaderCarrier)
      extends AuditEvent("BRM-Payload-Error", detail = result, transactionName = "brm-payload-error")

  override def audit(result: Map[String, String], payload: Option[Payload])(implicit
    hc: HeaderCarrier
  ): Future[AuditResult] = {
    logger.debug("ErrorAudit", "audit", "auditing error event")
    event(new PayloadErrorEvent(result))
  }
}
