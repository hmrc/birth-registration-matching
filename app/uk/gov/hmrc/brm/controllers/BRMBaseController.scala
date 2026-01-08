/*
 * Copyright 2026 HM Revenue & Customs
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

import play.api.libs.json.JsValue
import play.api.mvc.{ControllerComponents, Request, Result}
import uk.gov.hmrc.brm.audit.{BRMDownstreamAPIAudit, MatchingAudit, TransactionAuditor}
import uk.gov.hmrc.brm.models.brm.Payload
import uk.gov.hmrc.brm.models.matching.MatchingResult
import uk.gov.hmrc.brm.utils.{BRMException, CommonUtil, HeaderValidator}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

abstract class BRMBaseController(cc: ControllerComponents) extends BackendController(cc) with BRMException {

  val commonUtils: CommonUtil
  lazy val contentType: String       = "application/json; charset=utf-8"
  lazy val headers: (String, String) = (ACCEPT, "application/vnd.hmrc.1.0+json")

  protected val transactionAuditor: TransactionAuditor
  protected val matchingAuditor: MatchingAudit
  protected val headerValidator: HeaderValidator

  def respond(response: Result): Result =
    response
      .as(contentType)
      .withHeaders(headers)

  def handleException(method: String, startTime: Long)(implicit
    payload: Payload,
    auditor: BRMDownstreamAPIAudit,
    hc: HeaderCarrier,
    request: Request[JsValue]
  ): PartialFunction[Throwable, Result] = { case t =>
    val allPfs = Seq(
      groProxyDownPF(method),
      gatewayTimeoutPF(method),
      desConnectionDownPF(method),
      desInvalidHeadersBadRequestPF(method),
      groConnectionDownPF(method),
      nrsConnectionDownPF(method),
      badRequestExceptionPF(method),
      notImplementedExceptionPF(method),
      notFoundExceptionPF(method),
      forbiddenUpstreamPF(method),
      exceptionPF(method)
    ).reduce(_ orElse _)

    // audit the transaction when there was an exception with default arguments
    auditTransaction()
    commonUtils.logTime(startTime)

    respond(allPfs.apply(t))
  }

  protected def auditTransaction()(implicit
    payload: Payload,
    downstream: BRMDownstreamAPIAudit,
    hc: HeaderCarrier
  ): Unit = {

    val matchResult = MatchingResult.noMatch

    // audit matching result
    matchingAuditor.audit(matchResult.audit, Some(payload))

    downstream.transaction(payload, Nil, matchResult)
    transactionAuditor.transaction(payload, Nil, matchResult)
  }

}
