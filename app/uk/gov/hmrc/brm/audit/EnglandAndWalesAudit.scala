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
import uk.gov.hmrc.brm.config.MicroserviceGlobal
import uk.gov.hmrc.brm.models.brm.Payload
import uk.gov.hmrc.brm.utils.CommonUtil
import uk.gov.hmrc.brm.utils.CommonUtil.{DetailsRequest, ReferenceRequest}
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future

/**
  * Created by adamconder on 08/02/2017.
  */
@Singleton
class EnglandAndWalesAudit(connector : AuditConnector = MicroserviceGlobal.auditConnector) extends BRMAudit(connector) {

  /**
    * EnglandAndWalesAuditEvent
    * Responsible for auditing when we find records on GRO
    * @param result map of key value results
    * @param path endpoint path
    * @param hc implicit headerCarrier
    */
  final private class EnglandAndWalesAuditEvent(result : Map[String, String], path: String)(implicit hc: HeaderCarrier)
    extends AuditEvent(auditType = "BRM-GROEnglandAndWales-Results", detail =  result, transactionName = "brm-england-and-wales-match", path)

  def audit(result : Map[String, String], payload: Option[Payload])(implicit hc : HeaderCarrier) = {
    payload match {
      case Some(p) =>
        CommonUtil.getOperationType(p) match {
          case DetailsRequest() =>
            event(new EnglandAndWalesAuditEvent(result, "gro-details"))
          case ReferenceRequest() =>
            event(new EnglandAndWalesAuditEvent(result, "gro-reference"))
        }
      case _ =>
        Future.failed(new IllegalArgumentException("[EnglandAndWalesAudit] payload argument not specified"))
    }
  }

}
