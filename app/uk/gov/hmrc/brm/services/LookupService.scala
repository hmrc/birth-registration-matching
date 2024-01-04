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

package uk.gov.hmrc.brm.services

import javax.inject.Inject
import uk.gov.hmrc.brm.audit.{BRMDownstreamAPIAudit, TransactionAuditor}
import uk.gov.hmrc.brm.connectors._
import uk.gov.hmrc.brm.implicits.ReadsFactory
import uk.gov.hmrc.brm.metrics._
import uk.gov.hmrc.brm.models.brm.Payload
import uk.gov.hmrc.brm.models.matching.{BirthMatchResponse, MatchingResult}
import uk.gov.hmrc.brm.models.response.Record
import uk.gov.hmrc.brm.services.matching.MatchingService
import uk.gov.hmrc.brm.utils.{BRMLogger, BirthRegisterCountry, BirthResponseBuilder, RecordParser}
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.audit.http.connector.AuditResult

import scala.concurrent.{ExecutionContext, Future}

class LookupService @Inject() (
  groConnector: GROConnector,
  nrsConnector: NRSConnector,
  groniConnector: GRONIConnector,
  matchingService: MatchingService,
  transactionAuditor: TransactionAuditor,
  logger: BRMLogger,
  recordParser: RecordParser,
  matchMetric: MatchCountMetric,
  noMatchMetric: NoMatchCountMetric
)(implicit val executionContext: ExecutionContext) {

  val CLASS_NAME: String = this.getClass.getSimpleName

  def getConnector()(implicit payload: Payload): BirthConnector =
    payload.whereBirthRegistered match {
      case BirthRegisterCountry.ENGLAND | BirthRegisterCountry.WALES => groConnector
      case BirthRegisterCountry.NORTHERN_IRELAND                     => groniConnector
      case BirthRegisterCountry.SCOTLAND                             => nrsConnector
    }

  /** connects to groconnector and return match if match input details.
    *
    * @param hc
    * @param metrics
    * @param payload
    * @return
    */

  def lookup()(implicit
    hc: HeaderCarrier,
    metrics: BRMMetrics,
    payload: Payload,
    auditor: BRMDownstreamAPIAudit
  ): Future[BirthMatchResponse] =
    getRecord(hc, payload, metrics).map { response =>
      logger.info(CLASS_NAME, "lookup()", s"response received from ${getConnector().getClass.getSimpleName}")
      val records     = recordParser.parse[Record](response.json, ReadsFactory.getReads())
      val matchResult = matchingService.performMatch(payload, records, matchingService.getMatchingType)

      audit(records, matchResult)
      if (matchResult.matched) {
        matchMetric.count()
        BirthResponseBuilder.getResponse(matchResult.matched)
      } else {
        noMatchMetric.count()
        BirthResponseBuilder.withNoMatch()
      }
    }

  private[LookupService] def audit(records: List[Record], matchResult: MatchingResult)(implicit
    payload: Payload,
    hc: HeaderCarrier,
    downstreamAPIAuditor: BRMDownstreamAPIAudit
  ): Future[AuditResult] = {

    /** Audit the response from APIs:
      * - if a record was found
      * - if multiple records were found
      * - how many records were found
      * - match result
      * - number of names for each record
      * - number of characters in each name for each record
      * - payload details
      */

    downstreamAPIAuditor.transaction(payload, records, matchResult)
    transactionAuditor.transaction(payload, records, matchResult)
  }

  private[LookupService] def getRecord(implicit
    hc: HeaderCarrier,
    payload: Payload,
    metrics: BRMMetrics
  ): Future[HttpResponse] = {
    val allPartials: PartialFunction[Payload, Future[HttpResponse]] =
      Seq(noReferenceNumberPF, referenceNumberIncludedPF).reduce(_ orElse _)

    val start                              = metrics.startTimer()
    // return the correct PF to execute based on the payload
    val httpResponse: Future[HttpResponse] = allPartials.apply(payload)
    metrics.endTimer(start)
    httpResponse
  }

  private[LookupService] def noReferenceNumberPF(implicit
    hc: HeaderCarrier
  ): PartialFunction[Payload, Future[HttpResponse]] = {
    case payload: Payload if payload.birthReferenceNumber.isEmpty =>
      logger.info(CLASS_NAME, "noReferenceNumberPF", s"reference number not provided, search by details")
      getConnector()(payload).getChildDetails(payload)
  }

  private[LookupService] def referenceNumberIncludedPF(implicit
    hc: HeaderCarrier
  ): PartialFunction[Payload, Future[HttpResponse]] = {
    case payload: Payload if payload.birthReferenceNumber.isDefined =>
      logger.info(CLASS_NAME, "referenceNumberIncludedPF", s"reference number provided, search by reference")
      getConnector()(payload).getReference(payload)
  }

}
