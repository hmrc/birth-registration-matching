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

package uk.gov.hmrc.brm.utils

import play.api.mvc.Result
import uk.gov.hmrc.brm.models.brm.Payload
import uk.gov.hmrc.play.microservice.controller.BaseController

trait BRMBaseController extends BaseController with BrmException {

  lazy val contentType: String = "application/json; charset=utf-8"
  lazy val headers: (String, String)  = (ACCEPT, "application/vnd.hmrc.1.0+json")

  def respond(response: Result): Result = {
    response
      .as(contentType)
      .withHeaders(headers)
  }

  def handleException(method: String)(implicit payload: Payload): PartialFunction[Throwable, Result] = {
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
        exceptionPF(method)).reduce(_ orElse _)

      respond(allPfs.apply(t))
  }

}
