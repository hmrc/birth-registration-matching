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

package uk.gov.hmrc.brm.controllers

import play.api.libs.json._
import uk.gov.hmrc.brm.audit.BRMAudit
import uk.gov.hmrc.brm.implicits.Implicits._
import uk.gov.hmrc.brm.metrics.BRMMetrics
import uk.gov.hmrc.brm.models.brm.Payload
import uk.gov.hmrc.brm.services.LookupService
import uk.gov.hmrc.brm.utils.BrmLogger._
import uk.gov.hmrc.brm.utils.{BirthResponseBuilder, HeaderValidator}
import uk.gov.hmrc.brm.utils.CommonUtil._
import uk.gov.hmrc.brm.utils.Keygenerator._
import uk.gov.hmrc.brm.utils._
import uk.gov.hmrc.play.microservice.controller

import scala.concurrent.Future

object BirthEventsController extends BirthEventsController {
  override val service = LookupService
}

trait BirthEventsController extends controller.BaseController with HeaderValidator with ControllerUtil {

  override val CLASS_NAME : String = this.getClass.getCanonicalName

  import scala.concurrent.ExecutionContext.Implicits.global

  protected val service: LookupService

  def post() = validateAccept(acceptHeaderValidationRules).async(parse.json) {
    implicit request =>
      generateAndSetKey(request)
      request.body.validate[Payload].fold(
        error => {
          BRMAudit.auditWhereBirthRegistered(error)
          info(CLASS_NAME, "post()",s" error: $error")
          Future.successful(respond(BadRequest("")))
        },
        payload => {
          implicit val p : Payload = payload
          implicit val metrics : BRMMetrics = getMetrics()

          if (!validateDob(p.dateOfBirth)) {
            // date of birth is before acceptable date
            info(CLASS_NAME, "post()", s"date of birth is before date accepted by GRO, returned match=false")
            Future.successful(respond(Ok(Json.toJson(BirthResponseBuilder.withNoMatch()))))
          } else {
            info(CLASS_NAME, "post()", s"payload and date of birth is valid attempting lookup")
            service.lookup() map {
              bm => {
                getMetrics().connectorStatus(OK)
                info(CLASS_NAME, "post()", s"BirthMatchResponse received")
                info(CLASS_NAME, "post()", s"matched: ${bm.matched}")
                respond(Ok(Json.toJson(bm)))
              }
            } recover handleException("getReference")
          }
        }

      )
  }

}
