/*
 * Copyright 2021 HM Revenue & Customs
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
import org.bouncycastle.jcajce.provider.symmetric.TEA.KeyGen
import uk.gov.hmrc.brm.config.BrmConfig
import uk.gov.hmrc.brm.models.brm.{DetailsRequest, Payload, ReferenceRequest}
import uk.gov.hmrc.brm.utils.{BRMLogger, KeyGenerator}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.{AuditConnector, AuditResult}

import scala.concurrent.Future

/**
  * TODO: should this be deprecated?
  */

@Singleton
class EnglandAndWalesAudit @Inject()(connector: AuditConnector,
                                     val keyGen: KeyGenerator,
                                     val config: BrmConfig,
                                     val logger: BRMLogger) extends BRMDownstreamAPIAudit(connector) {

  /**
    * EnglandAndWalesAuditEvent
    * Responsible for auditing when we find records on GRO
    * @param result map of key value results
    * @param path endpoint path
    * @param hc implicit headerCarrier
    */
  final private class EnglandAndWalesAuditEvent(result : Map[String, String], path: String)(implicit hc: HeaderCarrier)
    extends AuditEvent(auditType = "BRM-GROEnglandAndWales-Results",
      detail =  result,
      transactionName = "brm-england-and-wales-match",
      path)

  override def audit(result : Map[String, String], payload: Option[Payload])(implicit hc : HeaderCarrier): Future[AuditResult] = {
    payload match {
      case Some(p) =>
        p.requestType match {
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
