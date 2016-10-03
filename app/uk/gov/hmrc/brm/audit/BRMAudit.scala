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

import uk.gov.hmrc.brm.config.MicroserviceGlobal
import uk.gov.hmrc.brm.utils.BrmLogger
import uk.gov.hmrc.play.audit.http.connector.{AuditResult, AuditConnector}
import uk.gov.hmrc.play.audit.model.DataEvent

import scala.concurrent.Future
import scala.util.{Failure, Success}

/**
 * Created by adamconder on 28/09/2016.
 */

abstract class AuditEvent(auditType : String, detail : Map[String, String])
  extends DataEvent(auditSource = "brm", auditType = auditType, detail = detail)


sealed class EnglandAndWalesAuditEvent(result : Map[String, String])
  extends AuditEvent(auditType = "BRM-GROEnglandAndWales-Results", detail =  result)

sealed class ScotlandAuditEvent(result : Map[String, String])
  extends AuditEvent(auditType = "BRM-NRSScotland-Results", detail = result)

sealed class NorthernIrelandAuditEvent(result : Map[String, String])
  extends AuditEvent(auditType = "BRM-GRONorthernIreland-Results", detail = result)


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
        BrmLogger.warn(s"BRMAudit", s"event: ${event.auditType}", s"event failed to audit ${msg}")
        e
    }
  }

}

object BRMAudit extends BRMAudit {
  override val connector = MicroserviceGlobal.auditConnector
}
