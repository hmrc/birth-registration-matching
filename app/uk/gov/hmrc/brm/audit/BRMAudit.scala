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

package uk.gov.hmrc.brm.audit

import uk.gov.hmrc.brm.config.BrmConfig
import uk.gov.hmrc.brm.models.brm.Payload
import uk.gov.hmrc.brm.models.matching.MatchingResult
import uk.gov.hmrc.brm.models.response.Record
import uk.gov.hmrc.brm.utils.{BRMLogger, KeyGenerator}
import uk.gov.hmrc.play.audit.AuditExtensions._
import uk.gov.hmrc.play.audit.http.connector.{AuditConnector, AuditResult}
import uk.gov.hmrc.play.audit.model.DataEvent

import scala.annotation.tailrec
import scala.concurrent.Future
import uk.gov.hmrc.http.HeaderCarrier
import scala.concurrent.ExecutionContext

/** AuditEvent - Abstract class for auditing events
  * @param auditType type of audit event, given a unique identifier to search on
  * @param detail map of results
  * @param transactionName name of the transaction
  * @param path endpoint path
  * @param hc implicit headerCarrier
  */

private abstract class AuditEvent(
  auditType: String,
  detail: Map[String, String],
  transactionName: String,
  path: String = "N/A"
)(implicit hc: HeaderCarrier)
    extends DataEvent(
      auditSource = "brm",
      auditType = auditType,
      detail = detail,
      tags = hc.toAuditTags(transactionName, path)
    )

abstract class BRMAudit(connector: AuditConnector)(implicit val ec: ExecutionContext) {

  val keyGen: KeyGenerator
  val logger: BRMLogger

  def audit(result: Map[String, String], payload: Option[Payload] = None)(implicit
    hc: HeaderCarrier
  ): Future[AuditResult]

  protected def event(event: AuditEvent): Future[AuditResult] = {
    // get unique key
    val uniqueKey    = Map("brmKey" -> keyGen.getKey())
    val eventWithKey = event.copy(detail = event.detail ++ uniqueKey)

    connector.sendEvent(eventWithKey) map { success =>
      logger.debug(super.getClass.getCanonicalName, s"event", "event successfully audited")
      success
    } recover { case e @ AuditResult.Failure(msg, _) =>
      logger.error(super.getClass.getSimpleName, s"event", s"event failed to audit, msg: $msg")
      e
    }
  }

}

abstract class BRMDownstreamAPIAudit(connector: AuditConnector)(implicit ec: ExecutionContext)
    extends BRMAudit(connector) {

  implicit val config: BrmConfig
  implicit val logger: BRMLogger

  def transaction(payload: Payload, records: List[Record], result: MatchingResult)(implicit
    hc: HeaderCarrier
  ): Future[AuditResult] = {
    // did the search return results
    val searchSummary = searchResults(records)

    // did the payload and record match
    val matchResults = result.audit

    // audit individual record details
    val recordDetails = listToMap(records, payload, Record.audit)

    // audit application feature switches
    val featuresStatus = config.audit(Some(payload))

    // audit payload
    val payloadDetails = payload.audit

    val toAudit = featuresStatus ++
      payloadDetails ++
      recordDetails ++
      searchSummary ++
      matchResults

    audit(toAudit, Some(payload))
  }

  private def searchResults(x: List[Record]): Map[String, String] =
    Map(
      "recordFound"     -> x.nonEmpty.toString,
      "multipleRecords" -> { x.length > 1 }.toString,
      "birthsPerSearch" -> x.length.toString
    )

  private def listToMap[A, B](record: List[A], p: B, f: (A, B, Int) => Map[String, String]): Map[String, String] = {
    @tailrec
    def build(c: Int, r: List[A], m: Map[String, String]): Map[String, String] = r match {
      case Nil       => m
      case h :: tail =>
        val getMap = f(h, p, c)
        build(c + 1, tail, m ++ getMap)
    }

    build(1, record, Map.empty)
  }

}
