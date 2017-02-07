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

import play.api.data.validation.ValidationError
import play.api.libs.json.JsPath
import uk.gov.hmrc.brm.config.MicroserviceGlobal
import uk.gov.hmrc.brm.models.brm.Payload
import uk.gov.hmrc.brm.models.matching.ResultMatch
import uk.gov.hmrc.brm.models.response.Record
import uk.gov.hmrc.brm.utils.CommonUtil.{DetailsRequest, ReferenceRequest}
import uk.gov.hmrc.brm.utils.{BrmLogger, CommonUtil}
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
abstract class AuditEvent(
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

/**
  * EnglandAndWalesAuditEvent
  * Responsible for auditing when we find records on GRO
  * @param result map of key value results
  * @param path endpoint path
  * @param hc implicit headerCarrier
  */
final class EnglandAndWalesAuditEvent(result : Map[String, String], path: String)(implicit hc: HeaderCarrier)
  extends AuditEvent(auditType = "BRM-GROEnglandAndWales-Results", detail =  result, transactionName = "brm-england-and-wales-match", path)

/**
  * ScotlandAuditEvent
  * Responsible for auditing when we find records on NRS
  * @param result map of key value results
  * @param path endpoint path
  * @param hc implicit headerCarrier
  */
final class ScotlandAuditEvent(result : Map[String, String], path: String)(implicit hc: HeaderCarrier)
  extends AuditEvent(auditType = "BRM-NRSScotland-Results", detail = result, transactionName = "brm-scotland-match", path)

/**
  * NorthernIrelandAuditEvent
  * Responsible for auditing when we find records on GRO-NI
  * @param result map of key value results
  * @param path endpoint path
  * @param hc implicit headerCarrier
  */
final class NorthernIrelandAuditEvent(result : Map[String, String], path: String)(implicit hc: HeaderCarrier)
  extends AuditEvent(auditType = "BRM-GRONorthernIreland-Results", detail = result, transactionName = "brm-northern-ireland-match", path)

/**
  * OtherCountryAuditEvent
  * @param result map of key value results
  * @param hc implicit headerCarrier
  */
final class OtherCountryAuditEvent(result : Map[String, String])(implicit hc: HeaderCarrier)
  extends AuditEvent(auditType = "BRM-Other-Results", detail = result, transactionName = "brm-other-match", "birth-registration-matching/match")

/**
  * EventRecordFound
  * Audit event for when a record is found on any of the downstream APIs
  * @param result map of key value results
  * @param path endpoint path
  * @param hc implicit headerCarrier
  */
final class RecordFound(result : Map[String, String], path: String)(implicit hc: HeaderCarrier)
  extends AuditEvent(auditType = "BRM-EventRecord-Found", detail = result, transactionName = "brm-event-record-found", path)


// TODO NEW
/**
  * MatchingEvent
  * Audit event for the result of MatchingService, how did the matching perform
  * @param result map of key value results
  * @param hc implicit headerCarrier
  */
final class MatchingEvent(result: Map[String, String], path : String)
                          (implicit hc : HeaderCarrier) extends AuditEvent("BRM-Matching-Results", detail = result, transactionName = "brm-match", path)

trait BRMAudit {

  import scala.concurrent.ExecutionContext.Implicits.global

  protected val connector : AuditConnector

  def event(event: AuditEvent) : Future[AuditResult] = {
    connector.sendEvent(event) map {
      success =>
        BrmLogger.info("BRMAudit", s"event", "event successfully audited")
        success
    } recover {
      case e @ AuditResult.Failure(msg, _) =>
        BrmLogger.warn(s"BRMAudit", s"event", s"event failed to audit")
        e
    }
  }

  def auditWhereBirthRegistered(error: Seq[(JsPath, Seq[ValidationError])])(implicit hc:HeaderCarrier) = {
    def logEvent(key: String, error: Seq[(JsPath, Seq[ValidationError])])(implicit hc:HeaderCarrier)= {
      val validationError = error.filter(_._1.toString().contains(key))
      val errors = validationError.map(x => {
        x._2.headOption.map(_.message)
      })

      errors match {
        case head :: tail =>
          val message = head.getOrElse("")
          if(message.contains("value:")){
              val index = message.lastIndexOf(":") + 1
              val input = message.slice(index, message.length)
              val result: Map[String, String] = Map("match" -> "false", "country" -> input)
              val audit = new OtherCountryAuditEvent(result)
              event(audit)
          }
        case Nil =>
      }
    }

    logEvent(Payload.whereBirthRegistered, error)
  }

  private def logEventRecordFound(implicit hc: HeaderCarrier,  path: String, hasMultipleRecords : Boolean) = {
    val result : Map[String, String] = Map("recordFound" -> "true", "multiple"-> s"$hasMultipleRecords")
    val audit = new RecordFound(result, path)(hc)
    event(audit)
  }

//  def auditRequest(auditEvent: AuditEvent, records: List[Record], path: String, hc: HeaderCarrier) = {
//    BRMAudit.event(auditEvent)
//    if (records.nonEmpty) BRMAudit.logEventRecordFound(hc, path, records.length > 1)
//  }

  def auditMatchResult(input : Payload, result : ResultMatch, records : List[Record])(implicit hc : HeaderCarrier) = {
    CommonUtil.getOperationType(input) match {
      case DetailsRequest() =>
        event(new MatchingEvent(result.audit, "birth-registration-matching/match/details"))
      case ReferenceRequest() =>
        event(new MatchingEvent(result.audit, "birth-registration-matching/match/reference"))
    }
  }
}

object BRMAudit extends BRMAudit {
  override val connector = MicroserviceGlobal.auditConnector
}
