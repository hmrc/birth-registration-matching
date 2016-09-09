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
import uk.gov.hmrc.brm.models.brm.Payload
import uk.gov.hmrc.brm.models.gro.GroResponse
import uk.gov.hmrc.brm.connectors.{BirthConnector, GROEnglandConnector, NirsConnector, NrsConnector}
import uk.gov.hmrc.brm.utils.{BirthRegisterCountry, BirthResponseBuilder}
import uk.gov.hmrc.play.http._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Created by user on 22/08/16.
  */

object LookupService extends LookupService {
  override val groConnector = GROEnglandConnector
  override val nirsConnector = NirsConnector
  override val nrsConnector = NrsConnector
}

trait LookupService {

  protected val groConnector: BirthConnector
  protected val nirsConnector: BirthConnector
  protected val nrsConnector: BirthConnector

  private def getConnector(payload: Payload): BirthConnector = {
    payload.whereBirthRegistered match {
      case BirthRegisterCountry.ENGLAND | BirthRegisterCountry.WALES =>
        groConnector
      case BirthRegisterCountry.NORTHERN_IRELAND  =>
        nirsConnector
      case BirthRegisterCountry.SCOTLAND  =>
        nrsConnector
    }
 }

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

        /**
         * TODO: Return a generic interface BirthResponse which can use Reads/Adapter to map JsValue to case class
         */
        getConnector(payload).getReference(reference) map {
          response =>
            Logger.debug(s"[LookupService][response] $response")
            Logger.debug(s"[LookupService][payload] $payload")

            response.json.validate[GroResponse].fold(
              error => {
                Logger.warn(s"[LookupService][validate json][failed to validate json]]")
                BirthResponseBuilder.withNoMatch()
              },
              success => {
                val firstName = success.child.firstName
                val lastName = success.child.lastName

                val isMatch = firstName.equalsIgnoreCase(payload.firstName) && lastName.equalsIgnoreCase(payload.lastName)
                BirthResponseBuilder.getResponse(isMatch)
              }
            )
        }
    )
  }
}
