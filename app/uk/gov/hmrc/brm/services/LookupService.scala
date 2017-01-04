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

package uk.gov.hmrc.brm.services

import play.api.libs.json.JsValue
import uk.gov.hmrc.brm.audit.BRMAudit
import uk.gov.hmrc.brm.config.BrmConfig
import uk.gov.hmrc.brm.connectors.{BirthConnector, GROEnglandConnector, NirsConnector, NrsConnector}
import uk.gov.hmrc.brm.metrics._
import uk.gov.hmrc.brm.models.brm.Payload
import uk.gov.hmrc.brm.models.response.Record
import uk.gov.hmrc.brm.utils.BrmLogger._
import uk.gov.hmrc.brm.utils.{BirthRegisterCountry, BirthResponseBuilder, MatchingType}
import uk.gov.hmrc.play.http._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

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

  private[LookupService] def parseRecords(json: JsValue)(implicit hc : HeaderCarrier) : List[Record] = {
    val records = json.validate[List[Record]].fold(
      error => {
        info(CLASS_NAME, "parseRecords()", s"Failed to validate as[List[Record]]")
        json.validate[Record].fold(
          e => {
            info(CLASS_NAME, "parseRecords()", s"Failed to validate as[Record]")
            List()
          },
          r => {
            BRMAudit.logEventRecordFound(hc)
            info(CLASS_NAME, "parseRecords()", s"Successfully validated as[Record]")
            List(r)
          }
        )
      },
      success => {
        BRMAudit.logEventRecordFound(hc)
        info(CLASS_NAME, "parseRecords()", s"Successfully validated as[List[Record]]")
        success
      }
    )

    if (records.isEmpty) warn(CLASS_NAME, "parseRecords()", s"Failed to parse response as[List[Record]] and as[Record]")
    records
  }

  def lookup()(implicit hc: HeaderCarrier, payload: Payload, metrics: BRMMetrics) = {
    getRecord(hc, payload, metrics).map {
      response =>

        info(CLASS_NAME, "lookup()", s"response received ${getConnector().getClass.getCanonicalName}")

        /**
          * Should be:
          * response.validate[T]
          * response.validate[List[Record]] so this is going to be a List[Record]
          * then in matching service this takes in Payload and List[Record] and @tailrec these records to match
          *
          * Future:
          * Later on we can make it validate[List[Record[C, S]] where C is the Child Type and S is the Status Type
          * i.e. the implicit reads from GROChild and GROStatus / NRSChild NRSStatus
          */

        val isMatch = matchingService.performMatch(payload, parseRecords(response.json), getMatchingType).isMatch
        if(isMatch) {
          MatchMetrics.matchCount()
          BirthResponseBuilder.getResponse(isMatch)
        } else {
          MatchMetrics.noMatchCount()
          BirthResponseBuilder.withNoMatch()
        }
    }
  }

  private[LookupService] def getRecord(implicit hc: HeaderCarrier, payload: Payload, metrics: BRMMetrics): Future[HttpResponse] = {
    val allPartials = Seq(noReferenceNumberPF, referenceNumberIncludedPF).reduce(_ orElse _)
    val start = metrics.startTimer()
    // return the correct PF to execute based on the payload
    val httpResponse = allPartials.apply(payload)
    metrics.endTimer(start)
    httpResponse
  }

  private[LookupService] def noReferenceNumberPF(implicit hc: HeaderCarrier, payload: Payload): PartialFunction[Payload, Future[HttpResponse]] = {
    case payload@Payload(None, firstName, lastName, dateOfBirth, whereBirthRegistered) =>
      info(CLASS_NAME, "lookup()", s"reference number not provided, search by details")
      getConnector()(payload).getChildDetails(payload)
  }

  private[LookupService] def referenceNumberIncludedPF(implicit hc: HeaderCarrier, payload: Payload): PartialFunction[Payload, Future[HttpResponse]] = {
    case payload@Payload(Some(birthReferenceNumber), _, _, _, _) =>
      info(CLASS_NAME, "lookup()", s"reference number provided, search by reference")
      getConnector()(payload).getReference(payload)
  }

  private[LookupService] def getMatchingType : MatchingType.Value = {
    val fullMatch = BrmConfig.matchFirstName && BrmConfig.matchLastName && BrmConfig.matchDateOfBirth
    info(CLASS_NAME, "getMatchType()", s"isFullMatching: $fullMatch configuration")
    if (fullMatch) MatchingType.FULL else MatchingType.PARTIAL
  }

}
