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

import com.ning.http.client.Response.ResponseBuilder
import org.apache.http.protocol.ExecutionContext
import play.api.Logger
import play.api.libs.json.{JsObject, JsValue, Json}
import uk.gov.hmrc.brm.connectors.{BirthConnector, GROEnglandConnector}
import uk.gov.hmrc.brm.models.{BirthMatchResponse, Payload}
import uk.gov.hmrc.brm.utils
import uk.gov.hmrc.brm.utils.{BirthRegisterCountry, BirthResponseBuilder}
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpResponse}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Created by user on 22/08/16.
  */

object LookupService extends LookupService {
  override val groConnector = GROEnglandConnector

}

trait LookupService {

  protected val groConnector : BirthConnector


  private def getConnector(payload: Payload) : BirthConnector = {
    payload.whereBirthRegistered match {
      case BirthRegisterCountry.ENGLAND  =>
        groConnector
      case BirthRegisterCountry.WALES  =>
        groConnector
   
    }
  }

  /**
    * connects to groconnector and return match if match input data.
    * @param payload
    * @param hc
    * @return
    */
  def lookup(payload : Payload)(implicit hc : HeaderCarrier) = {

   //check if birthReferenceNumber has value
    payload.birthReferenceNumber.fold(

      Future.successful(BirthResponseBuilder.withNoMatch())

    )(
      reference => {
        reference.trim match {
            //is it empty
          case "" => Future.successful(BirthResponseBuilder.withNoMatch())
          case _ => {
            getConnector(payload).getReference(reference) map {
              response => {
                if (response.validate[JsObject].get.keys.isEmpty) {
                  BirthResponseBuilder.withNoMatch()
                } else {

                  val firstName = (response \ "subjects" \ "child" \ "name" \ "givenName").as[String]
                  val surname = (response \ "subjects" \ "child" \ "name" \ "surname").as[String]
                  val isMatch = firstName.equals(payload.firstName) && surname.equals(payload.lastName)
                  BirthResponseBuilder.getResponse(isMatch)
                }
              }
            }//end of getconnector
          }
        }


      }






    )

  }

}
