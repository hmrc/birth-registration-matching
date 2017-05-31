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
import uk.gov.hmrc.brm.models.brm.{DetailsRequest, Payload, ReferenceRequest}
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future

/**
  * Created by adamconder on 08/02/2017.
  */
@Singleton
class NorthernIrelandAudit(connector : AuditConnector = MicroserviceGlobal.auditConnector) extends BRMAudit(connector) {

  /**
    * NorthernIrelandAuditEvent
    * Responsible for auditing when we find records on GRO-NI
    * @param result map of key value results
    * @param path endpoint path
    * @param hc implicit headerCarrier
    */
  final private class NorthernIrelandAuditEvent(result : Map[String, String], path: String)(implicit hc: HeaderCarrier)
    extends AuditEvent(auditType = "BRM-GRONorthernIreland-Results", detail = result, transactionName = "brm-northern-ireland-match", path)

  def audit(result : Map[String, String], payload: Option[Payload])(implicit hc : HeaderCarrier) = {
    payload match {
      case Some(p) =>
        p.requestType match {
          case DetailsRequest() =>
            event(new NorthernIrelandAuditEvent(result, "gro-ni-details"))
          case ReferenceRequest() =>
            event(new NorthernIrelandAuditEvent(result, "gro-ni-reference"))
        }
      case _ =>
        Future.failed(new IllegalArgumentException("[NorthernIreland] payload argument not specified"))
    }

  }


}
