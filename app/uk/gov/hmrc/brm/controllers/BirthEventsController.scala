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

package uk.gov.hmrc.brm.controllers

import javax.inject.Inject
import java.time._
import play.api.libs.json._
import play.api.mvc.{Action, ControllerComponents, Request, Result}
import uk.gov.hmrc.brm.audit._
import uk.gov.hmrc.brm.config.BrmConfig
import uk.gov.hmrc.brm.filters.{Filter, Filters}
import uk.gov.hmrc.brm.implicits.{AuditFactory, MetricsFactory}
import uk.gov.hmrc.brm.metrics._
import uk.gov.hmrc.brm.models.brm._
import uk.gov.hmrc.brm.models.matching.MatchingResult
import uk.gov.hmrc.brm.services.LookupService
import uk.gov.hmrc.brm.utils.{BRMLogger, BirthResponseBuilder, CommonUtil, HeaderValidator}
import uk.gov.hmrc.http.HeaderCarrier

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

class BirthEventsController @Inject() (
  val service: LookupService,
  countryAuditor: WhereBirthRegisteredAudit,
  auditFactory: AuditFactory,
  config: BrmConfig,
  val transactionAuditor: TransactionAuditor,
  val errorAuditor: ErrorAudit,
  val matchingAuditor: MatchingAudit,
  val headerValidator: HeaderValidator,
  cc: ControllerComponents,
  val commonUtils: CommonUtil,
  val logger: BRMLogger,
  val metrics: MetricsFactory,
  filters: Filters,
  implicit val engAndWalesMetrics: EnglandAndWalesBirthRegisteredCountMetrics,
  implicit val northIreMetrics: NorthernIrelandBirthRegisteredCountMetrics,
  implicit val scotlandMetrics: ScotlandBirthRegisteredCountMetrics,
  implicit val invalidRegMetrics: InvalidBirthRegisteredCountMetrics
)(implicit val ec: ExecutionContext)
    extends BRMBaseController(cc) {

  val CLASS_NAME: String              = this.getClass.getSimpleName
  private val HEADER_X_CORRELATION_ID = "X-Correlation-Id"

  private def handleInvalidRequest(
    request: Request[JsValue],
    errors: scala.collection.Seq[(JsPath, scala.collection.Seq[JsonValidationError])]
  )(implicit hc: HeaderCarrier): Future[Result] = {
    countryAuditor.auditCountryInRequest(request.body)
    errorAuditor.audit(Json.fromJson[Map[String, String]](request.body).getOrElse(Map.empty[String, String]))
    val response = ErrorResponses.getErrorResponseByField(errors)
    logger.warn(CLASS_NAME, "handleInvalidRequest", s"error parsing request body as [Payload]")

    Future.successful(respond(response))
  }

  private def failedAtFilter(filters: List[Filter])(implicit payload: Payload, hc: HeaderCarrier): Future[Result] = {
    // audit the request
    transactionAuditor.transaction(payload, Nil, MatchingResult.noMatch)

    logger.warn(
      CLASS_NAME,
      "failedAtFilter",
      s"Request was not processed due to failing filter(s) $filters. " +
        s"Feature switches: ${config.audit(Some(payload))}"
    )
    Future.successful(respond(Ok(Json.toJson(BirthResponseBuilder.withNoMatch()))))
  }

  private def traceAndMatchRecord()(implicit
    payload: Payload,
    hc: HeaderCarrier,
    metrics: BRMMetrics,
    downstream: BRMDownstreamAPIAudit,
    request: Request[JsValue]
  ): Future[Result] = {
    logger.info(
      CLASS_NAME,
      "traceAndMatchRecord",
      s"Request was processed. Feature Switches: ${config.audit(Some(payload))}"
    )

    val beforeRequestTime = Instant.now().toEpochMilli

    service
      .lookup()(implicitly, metrics, implicitly, implicitly)
      .map {
        case Right(birthMatchResponse) =>
          metrics.status(OK)
          commonUtils.logTime(beforeRequestTime)
          respond(Ok(Json.toJson(birthMatchResponse)))

        case Left(errorResult) =>
          commonUtils.logTime(beforeRequestTime)
          respond(errorResult)
      }
      .recover { case e: Exception =>
        logger.error(CLASS_NAME, "traceAndMatchRecord", s"Unexpected exception: ${e.getMessage}")
        commonUtils.logTime(beforeRequestTime)
        respond(InternalServerError)
      }
  }

  private def getOrCreateCorrelationID(request: Request[_]): String = {
    logger.debug(
      CLASS_NAME,
      "getOrCreateCorrelationID",
      "Checking for Upstream x-correlation-id, returning new id if none."
    )
    request.headers.get(HEADER_X_CORRELATION_ID).getOrElse(UUID.randomUUID().toString)
  }

  def post(): Action[JsValue] = headerValidator.validateAccept(cc).async(parse.json) { implicit request =>
    implicit val hc: HeaderCarrier =
      HeaderCarrier().withExtraHeaders((HEADER_X_CORRELATION_ID, getOrCreateCorrelationID(request)))
    request.body
      .validate[Payload]
      .fold(
        errors => handleInvalidRequest(request, errors),
        implicit payload => {

          implicit val auditor: BRMDownstreamAPIAudit = auditFactory.getAuditor()

          val processed = filters.process(payload)

          if (processed.nonEmpty) {
            failedAtFilter(processed)
          } else {
            val metric: BRMMetrics = metrics.getMetrics()
            traceAndMatchRecord()(implicitly, implicitly, metrics = metric, implicitly, implicitly)
          }
        }
      )
  }

}
