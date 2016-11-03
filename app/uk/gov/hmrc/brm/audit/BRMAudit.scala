/*
 * Copyright 2016 HM Revenue & Customs
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

import play.api.Logger
import play.api.data.validation.ValidationError
import play.api.libs.json.JsPath
import uk.gov.hmrc.brm.config.MicroserviceGlobal
import uk.gov.hmrc.brm.models.brm.Payload
import uk.gov.hmrc.brm.utils.BrmLogger
import uk.gov.hmrc.brm.utils.BrmLogger._
import uk.gov.hmrc.play.audit.AuditExtensions._
import uk.gov.hmrc.play.audit.http.connector.{AuditConnector, AuditResult}
import uk.gov.hmrc.play.audit.model.DataEvent
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future

abstract class AuditEvent(auditType : String, detail : Map[String, String], transactionName: String, path:String ="N/A")(implicit hc: HeaderCarrier)
  extends DataEvent(auditSource = "brm", auditType = auditType, detail = detail, tags = hc.toAuditTags(transactionName, path))

sealed class EnglandAndWalesAuditEvent(result : Map[String, String], path: String = "birth-registration-matching/match")(implicit hc: HeaderCarrier)
  extends AuditEvent(auditType = "BRM-GROEnglandAndWales-Results", detail =  result, transactionName = "brm-england-and-wales-match",path)

sealed class ScotlandAuditEvent(result : Map[String, String], path: String = "birth-registration-matching/match")(implicit hc: HeaderCarrier)
  extends AuditEvent(auditType = "BRM-NRSScotland-Results", detail = result, transactionName = "brm-scotland-match",path)

sealed class NorthernIrelandAuditEvent(result : Map[String, String], path: String = "birth-registration-matching/match")(implicit hc: HeaderCarrier)
  extends AuditEvent(auditType = "BRM-GRONorthernIreland-Results", detail = result, transactionName = "brm-northern-ireland-match",path)

sealed class OtherAuditEvent(result : Map[String, String], path: String = "birth-registration-matching/match")(implicit hc: HeaderCarrier)
  extends AuditEvent(auditType = "BRM-Other-Results", detail = result, transactionName = "brm-other-match",path)

sealed class EventRecordFound(result : Map[String, String], path: String = "GRO/match")(implicit hc: HeaderCarrier)
  extends AuditEvent(auditType = "BRM-EventRecord-Found", detail = result, transactionName = "brm-event-record-found", path)

trait BRMAudit {

  import scala.concurrent.ExecutionContext.Implicits.global

  protected val connector : AuditConnector

  def event(event: AuditEvent) : Future[AuditResult] = {
    connector.sendEvent(event) map {
      success =>
        BrmLogger.info("BRMAudit", s"event: ${event.auditType}", "event successfully audited")
        success
    } recover {
      case e @ AuditResult.Failure(msg, _) =>
        BrmLogger.warn(s"BRMAudit", s"event: ${event.auditType}", s"event failed to audit $msg")
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
          val index = message.lastIndexOf(":") + 1
          val input = message.slice(index, message.length)
          Logger.debug(s"\n\n validation error: $errors input: $input \n\n")

          debug("BRMAudit", "logEvent()", s"Logging event for country $input")
          val result: Map[String, String] = Map("match" -> "false", "country" -> input)
          val audit = new OtherAuditEvent(result)
          event(audit)
        case Nil =>
      }
    }

    logEvent(Payload.whereBirthRegistered, error)
  }

  def logEventRecordFound(implicit hc: HeaderCarrier) = {
    val result : Map[String, String] = Map("recordFound" -> "true")
    val recordEvent = new EventRecordFound(result)(hc)
    event(recordEvent)
  }

}

object BRMAudit extends BRMAudit {
  override val connector = MicroserviceGlobal.auditConnector
}
