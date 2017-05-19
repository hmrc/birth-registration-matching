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

package uk.gov.hmrc.brm.controllers

import play.api.data.validation.ValidationError
import play.api.libs.json._
import play.api.mvc.{Action, Request, Result}
import uk.gov.hmrc.brm.audit.{BRMAudit, MatchingAudit, TransactionAuditor, WhereBirthRegisteredAudit}
import uk.gov.hmrc.brm.config.BrmConfig
import uk.gov.hmrc.brm.filters.{Filter, Filters}
import uk.gov.hmrc.brm.implicits.Implicits.{AuditFactory, MetricsFactory}
import uk.gov.hmrc.brm.metrics.BRMMetrics
import uk.gov.hmrc.brm.models.brm._
import uk.gov.hmrc.brm.services.LookupService
import uk.gov.hmrc.brm.utils.BRMLogger._
import uk.gov.hmrc.brm.utils.{BirthResponseBuilder, HeaderValidator, _}
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future

object BirthEventsController extends BirthEventsController {
  override val service = LookupService
  override val countryAuditor = new WhereBirthRegisteredAudit()
  override val auditFactory = new AuditFactory()
  override val transactionAuditor = new TransactionAuditor()
  override val matchingAuditor = new MatchingAudit()
  override val headerValidator = HeaderValidator
}

trait BirthEventsController extends BRMBaseController {

  override val CLASS_NAME : String = this.getClass.getCanonicalName
  override val METHOD_NAME: String = "BirthEventsController::post"

  import scala.concurrent.ExecutionContext.Implicits.global

  protected val service: LookupService
  protected val countryAuditor : WhereBirthRegisteredAudit
  protected val auditFactory : AuditFactory

  private def handleInvalidRequest(request : Request[JsValue], errors: Seq[(JsPath, Seq[ValidationError])])(implicit hc : HeaderCarrier) : Future[Result] = {
    countryAuditor.auditCountryInRequest(request.body)
    info(CLASS_NAME, "post()", s"error parsing request body as [Payload]")

    val response = ErrorResponses.getErrorResponseByField(errors)
    info(CLASS_NAME, "post()", s"Response: $response")

    Future.successful(respond(response))
  }

  private def handleInvalidBirthReferenceNumber()(implicit hc : HeaderCarrier) = {
    val response = InvalidBirthReferenceNumber.status
    info(CLASS_NAME, "post()", s"Invalid birth reference number provided.")
    info(CLASS_NAME, "post()", s"Response: $response")
    Future.successful(respond(InvalidBirthReferenceNumber.status))
  }

  private def failedAtFilter(filters : List[Filter])(implicit payload: Payload, hc : HeaderCarrier, audit : BRMAudit) = {
    // audit the request
    auditRequestAndResults()

    info(CLASS_NAME, "post()", s"Request was not processed due to failing filter(s) $filters. " +
      s"Feature switches: ${BrmConfig.audit(Some(payload))}")
    Future.successful(respond(Ok(Json.toJson(BirthResponseBuilder.withNoMatch()))))
  }

  private def traceAndMatchRecord()(implicit payload: Payload, metrics : BRMMetrics, hc : HeaderCarrier, audit : BRMAudit) = {
    info(CLASS_NAME, "post()", s"Request was processed. Feature Switches: ${BrmConfig.audit(Some(payload))}")

    service.lookup() map {
      bm =>
        metrics.status(OK)
        val response = Json.toJson(bm)
        info(CLASS_NAME, "post()", s"Response: $response")
        respond(Ok(response))
    } recover handleException(
      if (payload.birthReferenceNumber.isDefined) "getReference"
      else "getDetails"
    )
  }

  def post() : Action[JsValue] = headerValidator.validateAccept().async(parse.json) {
    implicit request =>
      request.body.validate[Payload].fold(errors => handleInvalidRequest(request, errors),
        implicit payload => {
          if(!BRMFormat.validBirthReferenceNumber(payload.whereBirthRegistered, payload.birthReferenceNumber)) {
            handleInvalidBirthReferenceNumber()
          } else {
            implicit val metrics: BRMMetrics = MetricsFactory.getMetrics()
            implicit val auditor: BRMAudit = auditFactory.getAuditor()

            val processed = Filters.process(payload)

            if (processed.nonEmpty) {
              failedAtFilter(processed)
            } else {
              traceAndMatchRecord()
            }
          }
        }
      )
  }
}
