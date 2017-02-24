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

import play.api.libs.json._
import uk.gov.hmrc.brm.audit.{BRMAudit, WhereBirthRegisteredAudit}
import uk.gov.hmrc.brm.implicits.Implicits.{AuditFactory, MetricsFactory}
import uk.gov.hmrc.brm.metrics.BRMMetrics
import uk.gov.hmrc.brm.models.brm.Payload
import uk.gov.hmrc.brm.services.LookupService
import uk.gov.hmrc.brm.utils.BrmLogger._
import uk.gov.hmrc.brm.utils.CommonUtil._
import uk.gov.hmrc.brm.utils.Keygenerator._
import uk.gov.hmrc.brm.utils.{BirthResponseBuilder, HeaderValidator, _}

import scala.concurrent.Future
import scala.util.Try

object BirthEventsController extends BirthEventsController {
  override val service = LookupService
}

trait BirthEventsController extends HeaderValidator with BRMBaseController {

  override val CLASS_NAME : String = this.getClass.getCanonicalName
  override val METHOD_NAME: String = "BirthEventsController::post"

  import scala.concurrent.ExecutionContext.Implicits.global

  protected val service: LookupService

  def post() = validateAccept(acceptHeaderValidationRules).async(parse.json) {
    implicit request =>
      generateAndSetKey(request)

      request.body.validate[Payload].fold(
        error => {
          // TODO move this out somewhere else
          request.body.\(Payload.whereBirthRegistered) match {
            case JsDefined(country) =>
              Try(BirthRegisterCountry.withName(country.toString)) recover {
                case e : Exception =>
                  // audit incorrect country
                  new WhereBirthRegisteredAudit().audit(Map("country" -> country.toString), None)
              }
            case _ =>
              // does not exist on request
              new WhereBirthRegisteredAudit().audit(Map("country" -> "no country specified"), None)
          }

          info(CLASS_NAME, "post()", s"error parsing request body as [Payload]")
          Future.successful(respond(BadRequest("")))
        },
        payload => {

          implicit val p : Payload = payload
          implicit val metrics : BRMMetrics = MetricsFactory.getMetrics()
          implicit val auditor : BRMAudit = AuditFactory.getAuditor()

          // Toggle switch for searching by child's name or whether we should validate date of birth
          if (restrictSearchByDateOfBirthBeforeGROStartDate(p.dateOfBirth) || payload.restrictSearchByDetails) {
            // date of birth is before acceptable date
            info(CLASS_NAME, "post()", s"date of birth is before date accepted by GRO, or restricting search by child's details")
            Future.successful(respond(Ok(Json.toJson(BirthResponseBuilder.withNoMatch()))))
          } else {
            info(CLASS_NAME, "post()", s"payload and date of birth is valid attempting lookup")
            service.lookup() map {
              bm => {
                metrics.status(OK)
                info(CLASS_NAME, "post()", s"BirthMatchResponse received")
                info(CLASS_NAME, "post()", s"matched: ${bm.matched}")
                respond(Ok(Json.toJson(bm)))
              }
            } recover handleException(if (payload.birthReferenceNumber.isDefined) "getReference" else "getDetails")
          }
        }
      )

  }

}
