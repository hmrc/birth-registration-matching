/*
 * Copyright 2026 HM Revenue & Customs
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
import uk.gov.hmrc.brm.config.BrmConfig
import uk.gov.hmrc.brm.models.brm.{DetailsRequest, Payload, ReferenceRequest}
import uk.gov.hmrc.brm.utils.{BRMLogger, KeyGenerator}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditConnector

import scala.concurrent.{ExecutionContext, Future}

/** Created by adamconder on 08/02/2017.
  */
@Singleton
class MatchingAudit @Inject() (
  connector: AuditConnector,
  val logger: BRMLogger,
  val config: BrmConfig,
  val keyGen: KeyGenerator
)(implicit ec: ExecutionContext)
    extends BRMAudit(connector) {

  /** MatchingEvent Audit event for the result of MatchingService, how did the matching perform
    * @param result
    *   map of key value results
    * @param hc
    *   implicit headerCarrier
    */
  final private class MatchingEvent(result: Map[String, String], path: String)(implicit hc: HeaderCarrier)
      extends AuditEvent("BRM-Matching-Results", detail = result, transactionName = "brm-match", path)

  override def audit(result: Map[String, String], payload: Option[Payload])(implicit hc: HeaderCarrier) = {
    logger.debug("MatchingAudit", "audit", "auditing match event")
    payload match {
      case Some(p) =>
        p.requestType match {
          case DetailsRequest()   =>
            event(new MatchingEvent(result, "birth-registration-matching/match/details"))
          case ReferenceRequest() =>
            event(new MatchingEvent(result, "birth-registration-matching/match/reference"))
        }
      case _       =>
        Future.failed(new IllegalArgumentException("[MatchingAudit] payload argument not specified"))
    }
  }

}
