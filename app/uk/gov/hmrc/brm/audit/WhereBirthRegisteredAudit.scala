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

import com.google.inject.Singleton
import javax.inject.Inject
import play.api.libs.json.{JsDefined, JsValue}
import uk.gov.hmrc.brm.models.brm.Payload
import uk.gov.hmrc.brm.utils.{BRMLogger, BirthRegisterCountry, KeyGenerator}
import uk.gov.hmrc.play.audit.http.connector.{AuditConnector, AuditResult}

import scala.concurrent.Future
import scala.util.Try
import uk.gov.hmrc.http.HeaderCarrier

/**
  * Created by adamconder on 09/02/2017.
  */
@Singleton
class WhereBirthRegisteredAudit @Inject() (connector: AuditConnector, val keyGen: KeyGenerator, val logger: BRMLogger)
    extends BRMAudit(connector) {

  /**
    * OtherCountryAuditEvent
    * @param result map of key value results
    * @param hc implicit headerCarrier
    */
  final private class OtherCountryAuditEvent(result: Map[String, String])(implicit hc: HeaderCarrier)
      extends AuditEvent(
        auditType = "BRM-Other-Results",
        detail = result,
        transactionName = "brm-other-match",
        "birth-registration-matching/match"
      )

  override def audit(result: Map[String, String], payload: Option[Payload])(implicit
    hc: HeaderCarrier
  ): Future[AuditResult] =
    event(new OtherCountryAuditEvent(result))

  def auditCountryInRequest(json: JsValue)(implicit hc: HeaderCarrier) =
    json.\(Payload.whereBirthRegistered) match {
      case JsDefined(country) =>
        Try(BirthRegisterCountry.withName(country.toString)) recover { _ =>
          // audit incorrect country
          audit(Map("country" -> country.toString), None)
        }
      case _                  =>
        // does not exist on request
        audit(Map("country" -> "no country specified"), None)
    }

}
