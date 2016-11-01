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
    * @param payload
    * @param hc
    * @return
    */
  def lookup()(implicit hc: HeaderCarrier, payload: Payload, metrics: Metrics) = {
    //check if birthReferenceNumber has value
    payload.birthReferenceNumber.fold {
      info(CLASS_NAME, "lookup()", s"reference number not provided - matched: false")
      Future.successful(BirthResponseBuilder.withNoMatch())
    }(
      reference => {
        /**
          * TODO: Return a generic interface BirthResponse which can use Reads/Adapter to map JsValue to case class
          */
        val start = metrics.startTimer()

        getConnector.getReference(reference) map {
          response =>
            metrics.endTimer(start)

            info(CLASS_NAME, "lookup()", s"response received ${getConnector().getClass.getCanonicalName}")
            debug(CLASS_NAME, "lookup()", s"[response] $response")
            debug(CLASS_NAME, "lookup()", s"[payload] $payload")

            response.json.validate[GroResponse].fold(
              error => {
                warn(CLASS_NAME, "lookup()", s"failed to validate json")
                warn(CLASS_NAME, "lookup()", s"returned matched: false")
                BirthResponseBuilder.withNoMatch()
              },
              success => {
                logEvent(hc)
                val isMatch = matchingService.performMatch(payload, success, getMatchingType).isMatch
                info(CLASS_NAME, "lookup()", s"matched: $isMatch")

                if (isMatch) MatchMetrics.matchCount() else MatchMetrics.noMatchCount()

                BirthResponseBuilder.getResponse(isMatch)
              }
            )
        }
      }
    )
  }

  def getMatchingType : MatchingType.Value = {
    val fullMatch = BrmConfig.matchFirstName && BrmConfig.matchLastName && BrmConfig.matchDateOfBirth
    info(CLASS_NAME, "getMatchType()", s"isFullMatching: $fullMatch configuration")
    if (fullMatch) MatchingType.FULL else MatchingType.PARTIAL
  }

  private def logEvent(implicit hc: HeaderCarrier) = {
    var result : Map[String, String] = Map("recordFound" -> "true")
    val event = new EventRecordFound(result)(hc)
    BRMAudit.event(event)
  }
}
