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

import play.api.mvc.Result
import uk.gov.hmrc.brm.audit.{BRMAudit, MatchingAudit, TransactionAuditor}
import uk.gov.hmrc.brm.models.brm.Payload
import uk.gov.hmrc.brm.models.matching.ResultMatch
import uk.gov.hmrc.brm.services.Bad
import uk.gov.hmrc.brm.utils.BrmException
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.microservice.controller.BaseController

trait BRMBaseController extends BaseController with BrmException {

  lazy val contentType: String = "application/json; charset=utf-8"
  lazy val headers: (String, String)  = (ACCEPT, "application/vnd.hmrc.1.0+json")

  protected val transactionAuditor : TransactionAuditor
  protected val matchingAuditor : MatchingAudit

  def respond(response: Result): Result = {
    response
      .as(contentType)
      .withHeaders(headers)
  }

  def handleException(method: String)(implicit payload: Payload, auditor: BRMAudit, hc: HeaderCarrier): PartialFunction[Throwable, Result] = {
    case t =>
      val allPfs = Seq(
        notFoundPF(method),
        badRequestPF(method),
        badGatewayPF(method),
        gatewayTimeoutPF(method),
        upstreamErrorPF(method),
        badRequestExceptionPF(method),
        notImplementedExceptionPF(method),
        notFoundExceptionPF(method),
        forbiddenPF(method),
        exceptionPF(method)).reduce(_ orElse _)

      // audit the transaction when there was an exception with default arguments
      auditTransaction()

      respond(allPfs.apply(t))
  }

  private def auditTransaction()(implicit payload: Payload,
                                   auditor: BRMAudit,
                                   hc: HeaderCarrier): Unit = {

    val matchResult = ResultMatch(Bad(), Bad(), Bad(), Bad())

    // audit matching result
    matchingAuditor.audit(matchResult.audit, Some(payload))

    // MetricsFactory auditor
    auditor.audit(auditor.recordFoundAndMatchToMap(Nil, matchResult), Some(payload))

    // audit transaction
    transactionAuditor.audit(transactionAuditor.transactionToMap(payload, Nil, matchResult), Some(payload))
  }

}
