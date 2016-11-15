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

package uk.gov.hmrc.brm.services

import play.api.libs.json.JsArray
import uk.gov.hmrc.brm.audit.{BRMAudit, EventRecordFound, OtherAuditEvent}
import uk.gov.hmrc.brm.config.BrmConfig
import uk.gov.hmrc.brm.connectors.{BirthConnector, GROEnglandConnector, NirsConnector, NrsConnector}
import uk.gov.hmrc.brm.metrics._
import uk.gov.hmrc.brm.models.brm.Payload
import uk.gov.hmrc.brm.models.response.gro.GroResponse
import uk.gov.hmrc.brm.utils.{BirthRegisterCountry, BirthResponseBuilder, MatchingType}
import uk.gov.hmrc.play.http._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import uk.gov.hmrc.brm.utils.BrmLogger._

object LookupService extends LookupService {
  override val groConnector = GROEnglandConnector
  override val nirsConnector = NirsConnector
  override val nrsConnector = NrsConnector
  override val matchingService = MatchingService
}

trait LookupServiceBinder {
  self: LookupService =>

  protected def getConnector()(implicit payload: Payload): BirthConnector = {
    payload.whereBirthRegistered match {
      case BirthRegisterCountry.ENGLAND | BirthRegisterCountry.WALES =>
        groConnector
      case BirthRegisterCountry.NORTHERN_IRELAND =>
        nirsConnector
      case BirthRegisterCountry.SCOTLAND =>
        nrsConnector
    }
  }

}

trait LookupService extends LookupServiceBinder {

  protected val groConnector: BirthConnector
  protected val nirsConnector: BirthConnector
  protected val nrsConnector: BirthConnector
  protected val matchingService: MatchingService
  val CLASS_NAME: String = this.getClass.getCanonicalName

  /**
    * connects to groconnector and return match if match input details.
    *
    * @param hc
    * @param payload
    * @param metrics
    * @return
    */
  def lookup()(implicit hc: HeaderCarrier, payload: Payload, metrics: BRMMetrics) = {
    getRecord(hc, payload, metrics).map {
      response =>

        info(CLASS_NAME, "lookup()", s"response received ${getConnector().getClass.getCanonicalName}")
        debug(CLASS_NAME, "lookup()", s"[response] ${response.json}")
        debug(CLASS_NAME, "lookup()", s"[payload] $payload")
        //TODO
        val firstRecord  = response.json
       /* val firstRecord  = response.json.asInstanceOf[JsArray].value.head
        debug(CLASS_NAME, "lookup()", s"[firstRecord] $firstRecord")*/
        firstRecord.validate[GroResponse].fold(
          error => {
            warn(CLASS_NAME, "lookup()", s"failed to validate json")
            warn(CLASS_NAME, "lookup()", s"returned matched: false")
            BirthResponseBuilder.withNoMatch()
          },
          success => {
            BRMAudit.logEventRecordFound(hc)
            val isMatch = matchingService.performMatch(payload, success, getMatchingType).isMatch
            info(CLASS_NAME, "lookup()", s"matched: $isMatch")

            if (isMatch) MatchMetrics.matchCount() else MatchMetrics.noMatchCount()

            BirthResponseBuilder.getResponse(isMatch)
          }
        )
    }
  }

  private def getRecord(implicit hc: HeaderCarrier, payload: Payload, metrics: BRMMetrics): Future[HttpResponse] = {
    val allPartials = Seq(noReferenceNumberPF, referenceNumberIncludedPF).reduce(_ orElse _)
    val start = metrics.startTimer()
    val httpResponse = allPartials.apply(payload)
    metrics.endTimer(start)
    httpResponse
  }

  private def noReferenceNumberPF(implicit hc: HeaderCarrier, payload: Payload): PartialFunction[Payload, Future[HttpResponse]] = {
    case payload@Payload(None, firstName, lastName, dateOfBirth, whereBirthRegistered) => {
      info(CLASS_NAME, "lookup()", s"reference number not provided, search by details")
      getConnector()(payload).getChildDetails(payload)
    }
  }

  private def referenceNumberIncludedPF(implicit hc: HeaderCarrier, payload: Payload): PartialFunction[Payload, Future[HttpResponse]] = {
    case payload@Payload(Some(birthReferenceNumber), _, _, _, _) => {
      /**
        * TODO: Return a generic interface BirthResponse which can use Reads/Adapter to map JsValue to case class
        */
      getConnector()(payload).getReference(payload)
    }
  }

  def getMatchingType : MatchingType.Value = {
    val fullMatch = BrmConfig.matchFirstName && BrmConfig.matchLastName && BrmConfig.matchDateOfBirth
    info(CLASS_NAME, "getMatchType()", s"isFullMatching: $fullMatch configuration")
    if (fullMatch) MatchingType.FULL else MatchingType.PARTIAL
  }

}
