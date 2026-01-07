/*
 * Copyright 2025 HM Revenue & Customs
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

import play.api.http.Status._
import play.api.mvc.Result
import play.api.mvc.Results.{InternalServerError, ServiceUnavailable}

import javax.inject.Inject
import uk.gov.hmrc.brm.audit.{BRMDownstreamAPIAudit, MatchingAudit, TransactionAuditor}
import uk.gov.hmrc.brm.connectors._
import uk.gov.hmrc.brm.implicits.ReadsFactory
import uk.gov.hmrc.brm.metrics._
import uk.gov.hmrc.brm.models.brm.{ErrorResponse, Payload}
import uk.gov.hmrc.brm.models.matching.{BirthMatchResponse, MatchingResult}
import uk.gov.hmrc.brm.models.response.Record
import uk.gov.hmrc.brm.services.matching.MatchingService
import uk.gov.hmrc.brm.utils.BirthRegisterCountry.{ENGLAND, SCOTLAND, WALES}
import uk.gov.hmrc.brm.utils.{BRMLogger, BirthRegisterCountry, BirthResponseBuilder, RecordParser}
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.audit.http.connector.AuditResult

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

class LookupService @Inject() (
  groConnector: GROConnector,
  nrsConnector: NRSConnector,
  groniConnector: GRONIConnector,
  matchingService: MatchingService,
  transactionAuditor: TransactionAuditor,
  logger: BRMLogger,
  matchingAuditor: MatchingAudit,
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
  ): Future[Either[Result, BirthMatchResponse]] =
    getRecord(hc, payload, metrics).flatMap { response =>
      logger.info(CLASS_NAME, "lookup()", s"response received from ${getConnector().getClass.getSimpleName}")

      response.status match {
        case OK =>
          Try(recordParser.parse[Record](response.json, ReadsFactory.getReads())) match {
            case Success(records) =>
              val matchResult = matchingService.performMatch(payload, records, matchingService.getMatchingType)
              audit(records, matchResult)

              if (matchResult.matched) {
                matchMetric.count()
                Future.successful(Right(BirthResponseBuilder.getResponse(matchResult.matched)))
              } else {
                noMatchMetric.count()
                Future.successful(Right(BirthResponseBuilder.withNoMatch()))
              }

            case Failure(e) =>
              audit(Nil, MatchingResult.noMatch, isError = true)
              logger.error(CLASS_NAME, "lookup()", s"Failed to parse response: ${e.getMessage}")
              metrics.status(INTERNAL_SERVER_ERROR)
              Future.successful(Left(InternalServerError))
          }

        case BAD_GATEWAY =>
          audit(Nil, MatchingResult.noMatch, isError = true)
          payload.whereBirthRegistered match {
            case ENGLAND | WALES =>
              logAndMetric(s"[GRO down]: ${response.body}", SERVICE_UNAVAILABLE)
              Future.successful(Left(ServiceUnavailable(ErrorResponse.GRO_CONNECTION_DOWN)))
            case SCOTLAND        =>
              logAndMetric(s"[DES down]: ${response.body}", SERVICE_UNAVAILABLE)
              Future.successful(Left(ServiceUnavailable(ErrorResponse.DES_CONNECTION_DOWN)))
            case _               =>
              logAndMetric(s"[Service down]: ${response.body}", INTERNAL_SERVER_ERROR)
              Future.successful(Left(InternalServerError))
          }

        case GATEWAY_TIMEOUT =>
          audit(Nil, MatchingResult.noMatch, isError = true)
          logAndMetric(s"[Gateway timeout]: [${response.body}]", GATEWAY_TIMEOUT)
          Future.successful(Left(InternalServerError))

        case BAD_REQUEST =>
          audit(Nil, MatchingResult.noMatch, isError = true)
          logAndMetric(s"[Bad request]: ${response.body}", BAD_REQUEST)
          Future.successful(Left(InternalServerError))

        // currently returning right as thats respondNoMatch gave a success response
        case NOT_IMPLEMENTED =>
          audit(Nil, MatchingResult.noMatch, isError = true)
          logAndMetric(s"[Not implemented]: ${response.body}", OK)
          Future.successful(Right(BirthResponseBuilder.withNoMatch()))

        case NOT_FOUND =>
          audit(Nil, MatchingResult.noMatch, isError = true)
          logAndMetric(s"[Not found]: ${response.body}", NOT_FOUND)
          Future.successful(Right(BirthResponseBuilder.withNoMatch()))

        case FORBIDDEN =>
          audit(Nil, MatchingResult.noMatch, isError = true)
          logAndMetric(s"[Forbidden]: ${response.body}", FORBIDDEN)
          Future.successful(Right(BirthResponseBuilder.withNoMatch()))

        case status if status >= 500 && status < 600 =>
          audit(Nil, MatchingResult.noMatch, isError = true)
          payload.whereBirthRegistered match {
            case ENGLAND | WALES =>
              logAndMetric(s"[GRO down]: ${response.body} [status]: $status", SERVICE_UNAVAILABLE)
              Future.successful(Left(ServiceUnavailable(ErrorResponse.GRO_CONNECTION_DOWN)))
            case SCOTLAND        =>
              logAndMetric(s"[NRS down]: ${response.body} [status]: $status", SERVICE_UNAVAILABLE)
              Future.successful(Left(ServiceUnavailable(ErrorResponse.NRS_CONNECTION_DOWN)))
            case _               =>
              logAndMetric(s"[Service down]: ${response.body} [status]: $status", INTERNAL_SERVER_ERROR)
              Future.successful(Left(InternalServerError))
          }

        case status =>
          audit(Nil, MatchingResult.noMatch, isError = true)
          logAndMetric(s"[Unexpected error]: ${response.body} [status]: $status", INTERNAL_SERVER_ERROR)
          Future.successful(Left(InternalServerError))
      }
    }

  private def logAndMetric(message: String, statusCode: Int)(implicit metrics: BRMMetrics): Unit = {
    metrics.status(statusCode)
    statusCode match {
      case s if s >= 500 => logger.error(CLASS_NAME, "lookup()", message)
      case s if s >= 400 => logger.warn(CLASS_NAME, "lookup()", message)
      case _             => logger.info(CLASS_NAME, "lookup()", message)
    }
  }

  private[LookupService] def audit(records: List[Record], matchResult: MatchingResult, isError: Boolean = false)(
    implicit
    payload: Payload,
    hc: HeaderCarrier,
    downstreamAPIAuditor: BRMDownstreamAPIAudit
  ): Future[AuditResult] = {

    /** Audit the response from APIs:
      *   - if a record was found
      *   - if multiple records were found
      *   - how many records were found
      *   - match result
      *   - number of names for each record
      *   - number of characters in each name for each record
      *   - payload details
      */

    if (isError) {
      matchingAuditor.audit(matchResult.audit, Some(payload))
    }

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
