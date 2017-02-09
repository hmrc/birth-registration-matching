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

package uk.gov.hmrc.brm.audit

import uk.gov.hmrc.brm.models.brm.Payload
import uk.gov.hmrc.brm.utils.BrmLogger
import uk.gov.hmrc.play.audit.AuditExtensions._
import uk.gov.hmrc.play.audit.http.connector.{AuditConnector, AuditResult}
import uk.gov.hmrc.play.audit.model.DataEvent
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future

/**
  * AuditEvent - Abstract class for auditing events
  * @param auditType type of audit event, given a unique identifier to search on
  * @param detail map of results
  * @param transactionName name of the transaction
  * @param path endpoint path
  * @param hc implicit headerCarrier
  */
protected abstract class AuditEvent(
                           auditType : String,
                           detail : Map[String, String],
                           transactionName: String,
                           path : String = "N/A"
                         )(implicit hc: HeaderCarrier)
  extends DataEvent(
    auditSource = "brm",
    auditType = auditType,
    detail = detail,
    tags = hc.toAuditTags(transactionName, path)
  )

abstract class BRMAudit(connector : AuditConnector) {

  import scala.concurrent.ExecutionContext.Implicits.global

  def audit(result : Map[String, String], payload: Option[Payload] = None)(implicit hc : HeaderCarrier) : Future[AuditResult]

  protected def event(event: AuditEvent) : Future[AuditResult] = {
    connector.sendEvent(event) map {
      success =>
        BrmLogger.info(super.getClass.getCanonicalName, s"event", "event successfully audited")
        success
    } recover {
      case e @ AuditResult.Failure(msg, _) =>
        BrmLogger.warn(super.getClass.getCanonicalName, s"event", s"event failed to audit")
        e
    }
  }

}