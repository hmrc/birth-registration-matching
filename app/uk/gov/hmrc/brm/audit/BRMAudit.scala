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
import uk.gov.hmrc.brm.models.matching.MatchingResult
import uk.gov.hmrc.brm.models.response.Record
import uk.gov.hmrc.brm.utils.{BRMLogger, KeyGenerator}
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

abstract class AuditEvent(auditType : String,
                          detail : Map[String, String],
                          transactionName: String,
                          path : String = "N/A")(implicit hc: HeaderCarrier)
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
    // get unique key
    val uniqueKey = Map("brmKey" -> KeyGenerator.getKey())
    val eventWithKey = event.copy(detail = event.detail ++ uniqueKey)

    connector.sendEvent(eventWithKey) map {
      success =>
        BRMLogger.debug(super.getClass.getCanonicalName, s"event", "event successfully audited")
        success
    } recover {
      case e @ AuditResult.Failure(msg, _) =>
        BRMLogger.warn(super.getClass.getCanonicalName, s"event", s"event failed to audit")
        e
    }
  }

  def recordFoundAndMatchToMap(records : List[Record],
                               matchResult : MatchingResult) = {
    Map(
      "recordFound" -> records.nonEmpty.toString,
      "multipleRecords" -> {records.length > 1}.toString,
      "birthsPerSearch" -> records.length.toString
    ) ++ matchResult.audit
  }

}