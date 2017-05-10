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
import uk.gov.hmrc.brm.models.brm.Payload
import uk.gov.hmrc.brm.models.matching.ResultMatch
import uk.gov.hmrc.brm.models.response.Record
import uk.gov.hmrc.brm.services.parser.NameParser._
import uk.gov.hmrc.brm.utils.CommonUtil.{DetailsRequest, ReferenceRequest}
import uk.gov.hmrc.brm.utils.{CommonUtil, KeyGenerator}
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

  def audit(result : Map[String, String], payload: Option[Payload])(implicit hc : HeaderCarrier) = {
    payload match {
      case Some(p) =>
        CommonUtil.getOperationType(p) match {
          case DetailsRequest() =>
            event(new RequestsAndResultsAuditEvent(result, "birth-registration-matching/match/details"))
          case ReferenceRequest() =>
            event(new RequestsAndResultsAuditEvent(result, "birth-registration-matching/match/reference"))
        }
      case _ =>
        Future.failed(new IllegalArgumentException("[TransactionAuditor] payload argument not specified"))
    }
  }

  def transactionToMap(payload: Payload,
                   records : List[Record],
                   matchResult : ResultMatch): Map[String, String] = {

    // audit match result and if a record was found
    val matchAudit = recordFoundAndMatchToMap(records, matchResult)

    // audit status on the records
    val auditWordsPerNameOnRecords = recordListToMap(records, wordCount)
    val auditCharactersPerNameOnRecords = recordListToMap(records, characterCount)

    // flags for each record
    val auditFlags = BrmConfig.logFlags match {
      case true => recordListToMap(records, flags)
      case _ => Map().empty
    }

    // audit application feature switches
    val features = BrmConfig.audit(Some(payload))

    // audit payload
    val payloadAudit = payload.audit

    // concat the Map() of all features
    features ++
      payloadAudit ++
      auditWordsPerNameOnRecords ++
      auditCharactersPerNameOnRecords ++
      matchAudit ++
      auditFlags
  }

  def wordCount(r: Record, c: Int): Map[String, String] = {
    Map(
      s"records.record$c.numberOfForenames" -> s"${r.child.firstName.names.count(_.nonEmpty)}",
      s"records.record$c.numberOfLastnames" -> s"${r.child.lastName.names.count(_.nonEmpty)}"
    )
  }

  def characterCount(r: Record, c: Int): Map[String, String] = {
    Map(
      s"records.record$c.numberOfCharactersInFirstName" -> s"${r.child.firstName.names.filter(_.nonEmpty).listToString.length}",
      s"records.record$c.numberOfCharactersInLastName" -> s"${r.child.lastName.names.filter(_.nonEmpty).listToString.length}"
    )
  }

  def flags(r : Record, index: Int) : Map[String, String] = {
    /**
     convert a Map() of flags into a flattened Map() with index associated to each key
     otherwise return empty Map()
     */
    r.status match {
      case Some(s) =>
        val flags = s.flags
        flags.keys.map(k =>
          s"records.record$index.flags.$k" -> flags(k)
        ).toMap
      case None => Map()
    }
  }

  def recordListToMap(record: List[Record], f: (Record, Int) => Map[String, String]): Map[String, String] = {
    @tailrec
    def build(c: Int, r: List[Record], m: Map[String, String]) : Map[String, String] = {
      r match {
        case Nil => m
        case h :: tail =>
          val newMap = f(h, c)
          build(c + 1, tail, m ++ newMap)
      }
    }

    build(1, record, Map.empty)
  }

}
