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

import com.google.inject.Singleton
import uk.gov.hmrc.brm.config.{BrmConfig, MicroserviceGlobal}
import uk.gov.hmrc.brm.models.brm.{DetailsRequest, Payload, ReferenceRequest}
import uk.gov.hmrc.brm.models.matching.MatchingResult
import uk.gov.hmrc.brm.models.response.Record
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.annotation.tailrec
import scala.concurrent.Future

/**
  * Created by adamconder on 15/02/2017.
  */
@Singleton
class TransactionAuditor(connector : AuditConnector = MicroserviceGlobal.auditConnector) extends BRMAudit(connector) {

  /**
    * Audit event for the result of MatchingService and data submitted to the API
 *
    * @param result map of key value results
    * @param hc implicit headerCarrier
    */
  final private class RequestsAndResultsAuditEvent(result : Map[String, String], path : String)
                                                  (implicit hc : HeaderCarrier)
    extends AuditEvent(
      auditType = "BRM-RequestsAndResults",
      detail = result,
      transactionName = "brm-customer-information-and-results",
      path = path
    )

  def audit(result : Map[String, String], payload: Option[Payload])(implicit hc : HeaderCarrier) = payload match {
      case Some(p) =>
        p.requestType match {
          case DetailsRequest() =>
            event(new RequestsAndResultsAuditEvent(result, "birth-registration-matching/match/details"))
          case ReferenceRequest() =>
            event(new RequestsAndResultsAuditEvent(result, "birth-registration-matching/match/reference"))
        }
      case _ =>
        Future.failed(new IllegalArgumentException("[TransactionAuditor] payload argument not specified"))
  }

  def transactionToMap(payload: Payload,
                   records : List[Record],
                   matchResult : MatchingResult): Map[String, String] = {

    // audit match result and if a record was found
    val matchSummary = matchingSummary(records, matchResult)

    // audit individual record details
    val recordDetails = listToMap(records, payload, Record.audit)

    // audit application feature switches
    val featuresStatus = BrmConfig.audit(Some(payload))

    // audit payload
    val payloadDetails = payload.audit

    featuresStatus ++ payloadDetails ++ recordDetails ++ matchSummary
  }

  private def listToMap[A, B](record: List[A], p: B, f: (A, B, Int) => Map[String, String]): Map[String, String] = {
    @tailrec
    def build(c: Int, r: List[A], m: Map[String, String]) : Map[String, String] = r match {
        case Nil => m
        case h :: tail =>
          val getMap = f(h, p, c)
          build(c + 1, tail, m ++ getMap)
    }

    build(1, record, Map.empty)
  }

}