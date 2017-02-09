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
import play.api.data.validation.ValidationError
import play.api.libs.json.JsPath
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

  def audit(error: Seq[(JsPath, Seq[ValidationError])])(implicit hc:HeaderCarrier) : Future[AuditResult] = {

    val failure = Future.failed(AuditResult.Failure(""))

    def logEvent(key: String, error: Seq[(JsPath, Seq[ValidationError])])(implicit hc:HeaderCarrier) = {
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
            val event: Map[String, String] = Map("match" -> "false", "country" -> input)
            audit(event)
          } else {
            failure
          }
        case Nil =>
          failure
      }
    }

    logEvent(Payload.whereBirthRegistered, error)
  }

}
