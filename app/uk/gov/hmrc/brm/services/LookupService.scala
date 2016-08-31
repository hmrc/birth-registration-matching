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

package uk.gov.hmrc.brm.services

import play.api.Logger
import play.api.http.Status
import play.api.libs.json.JsObject
import uk.gov.hmrc.brm.connectors.{BirthConnector, GROEnglandConnector}
import uk.gov.hmrc.brm.models.Payload
import uk.gov.hmrc.brm.utils.{BirthRegisterCountry, BirthResponseBuilder}
import uk.gov.hmrc.play.http._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Created by user on 22/08/16.
  */

object LookupService extends LookupService {
  override val groConnector = GROEnglandConnector
}

trait LookupService {

  protected val groConnector: BirthConnector

  private def getConnector(payload: Payload): BirthConnector = {
    payload.whereBirthRegistered match {
      case BirthRegisterCountry.ENGLAND | BirthRegisterCountry.WALES =>
        groConnector
    }
  }

//  private def handleExceptions : PartialFunction[Throwable, _] = {
//    case e @ Status.NOT_FOUND =>
//      Logger.warn(s"[${this.getClass.getName}][NotFound] handleResponse")
//      throw new Upstream4xxResponse(s"[${super.getClass.getName}][NotFound]", e, Status.NOT_FOUND)
//    case e @ Status.BAD_REQUEST =>
//      Logger.warn(s"[${this.getClass.getName}][BadRequest] handleResponse")
//      throw new Upstream4xxResponse(s"[${super.getClass.getName}][BadRequest]", e, Status.BAD_REQUEST)
//    case e @ _ =>
//      Logger.error(s"[${this.getClass.getName}][InternalServerError] handleResponse")
//      throw new Upstream5xxResponse(s"[${super.getClass.getName}][InternalServerError]", e, Status.INTERNAL_SERVER_ERROR)
//  }

  /**
    * connects to groconnector and return match if match input details.
    *
    * @param payload
    * @param hc
    * @return
    */
  def lookup(payload: Payload)(implicit hc: HeaderCarrier) = {
    //check if birthReferenceNumber has value
    payload.birthReferenceNumber.fold(
      Future.successful(BirthResponseBuilder.withNoMatch())
    )(
      reference =>
        if (reference.trim.isEmpty) {
          Logger.debug(s"\n[LookupService][reference isEmpty]\n")
          Future.failed(new BadRequestException("BirthReferenceNumber is empty"))
        } else {
          getConnector(payload).getReference(reference) map {
            response =>
              response.status match {
                case Status.OK =>
                  val json = response.json
                  if (json.validate[JsObject].isError  || json.validate[JsObject].get.keys.isEmpty) {
                    BirthResponseBuilder.withNoMatch()
                  } else {
                    val firstName = (json \ "subjects" \ "child" \ "name" \ "givenName").as[String]
                    val surname = (json \ "subjects" \ "child" \ "name" \ "surname").as[String]

                    val isMatch = firstName.equals(payload.firstName) && surname.equals(payload.lastName)
                    BirthResponseBuilder.getResponse(isMatch)
                  }
                case Status.NOT_FOUND =>
                  BirthResponseBuilder.withNoMatch()
                case Status.UNAUTHORIZED =>
                  BirthResponseBuilder.withNoMatch()
                case _ =>
                  Logger.error(s"[${this.getClass.getName}][InternalServerError] handleResponse - ${response.status}")
                  throw new Upstream5xxResponse(s"[${super.getClass.getName}][InternalServerError]", Status.INTERNAL_SERVER_ERROR, Status.INTERNAL_SERVER_ERROR)
              }
          }
        }
    )
  }
}
