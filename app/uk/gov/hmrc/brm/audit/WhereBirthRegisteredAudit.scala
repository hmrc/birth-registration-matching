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
import uk.gov.hmrc.play.audit.http.connector.{AuditConnector, AuditResult}
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future

/**
  * Created by adamconder on 09/02/2017.
  */
@Singleton
class WhereBirthRegisteredAudit(connector: AuditConnector = MicroserviceGlobal.auditConnector) extends BRMAudit(connector) {

  /**
    * OtherCountryAuditEvent
    * @param result map of key value results
    * @param hc implicit headerCarrier
    */
  final private class OtherCountryAuditEvent(result : Map[String, String])(implicit hc: HeaderCarrier)
    extends AuditEvent(auditType = "BRM-Other-Results", detail = result, transactionName = "brm-other-match", "birth-registration-matching/match")

  def audit(result : Map[String, String], payload: Option[Payload])(implicit hc : HeaderCarrier) : Future[AuditResult] = {
    event(new OtherCountryAuditEvent(result))
  }

}
