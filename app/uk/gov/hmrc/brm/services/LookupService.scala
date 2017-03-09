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

import uk.gov.hmrc.brm.audit.{BRMAudit, TransactionAuditor}
import uk.gov.hmrc.brm.connectors._
import uk.gov.hmrc.brm.implicits.Implicits.ReadsFactory
import uk.gov.hmrc.brm.metrics._
import uk.gov.hmrc.brm.models.brm.Payload
import uk.gov.hmrc.brm.models.matching.ResultMatch
import uk.gov.hmrc.brm.models.response.Record
import uk.gov.hmrc.brm.utils.BRMLogger._
import uk.gov.hmrc.brm.utils.{BirthRegisterCountry, BirthResponseBuilder,  RecordParser}
import uk.gov.hmrc.play.http._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object LookupService extends LookupService {
  override val groConnector = new GROConnector
  override val nrsConnector = new NRSConnector
  override val groniConnector = new GRONIConnector
  override val matchingService = MatchingService
  override val transactionAuditor = new TransactionAuditor()
}

trait LookupServiceBinder {
  self: LookupService =>

  protected def getConnector()(implicit payload: Payload): BirthConnector = {
    payload.whereBirthRegistered match {
      case BirthRegisterCountry.ENGLAND | BirthRegisterCountry.WALES =>
        groConnector
      case BirthRegisterCountry.NORTHERN_IRELAND =>
        groniConnector
      case BirthRegisterCountry.SCOTLAND =>
        nrsConnector
    }
  }

}

trait LookupService extends LookupServiceBinder {

  protected val groConnector: BirthConnector
  protected val nrsConnector: BirthConnector
  protected val groniConnector: BirthConnector
  protected val matchingService: MatchingService

  protected val transactionAuditor : TransactionAuditor

  val CLASS_NAME: String = this.getClass.getCanonicalName

  /**
    * connects to groconnector and return match if match input details.
    *
    * @param hc
    * @param payload
    * @param metrics
    * @return
    */

  def lookup()(implicit hc: HeaderCarrier,
               payload: Payload,
               metrics: BRMMetrics,
               auditor: BRMAudit) = {
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

        val records = RecordParser.parse[Record](response.json,ReadsFactory.getReads())
        val matchResult = matchingService.performMatch(payload, records, matchingService.getMatchingType)

        audit(records, matchResult)

        if(matchResult.isMatch) {
          MatchCountMetric.count()
          BirthResponseBuilder.getResponse(matchResult.isMatch)
        } else {
          NoMatchCountMetric.count()
          BirthResponseBuilder.withNoMatch()
        }
    }
  }




  private[LookupService] def audit(records : List[Record], matchResult : ResultMatch)
                                  (implicit payload : Payload,
                                   hc: HeaderCarrier,
                                   downstreamAPIAuditor: BRMAudit) = {
    /**
      * Audit the response from APIs:
      * - if a record was found
      * - if multiple records were found
      * - how many records were found
      * - match result
      * - number of names for each record
      * - number of characters in each name for each record
      * - payload details
      */

    val matchAudit = downstreamAPIAuditor.recordFoundAndMatchToMap(records, matchResult)
    val transactionAudit = transactionAuditor.transactionToMap(payload, records, matchResult)

    transactionAuditor.audit(transactionAudit, Some(payload))
    downstreamAPIAuditor.audit(matchAudit, Some(payload))
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

}
