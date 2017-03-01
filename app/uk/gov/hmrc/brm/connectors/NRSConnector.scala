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

package uk.gov.hmrc.brm.connectors

import com.google.inject.Singleton

import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.brm.audit.ScotlandAudit
import uk.gov.hmrc.brm.config.WSHttp
import uk.gov.hmrc.brm.models.brm.Payload
import uk.gov.hmrc.brm.utils.{BRMLogger, KeyGenerator, NameFormat}
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpPost, NotImplementedException}
import uk.gov.hmrc.brm.utils.CommonConstant._

import scala.concurrent.Future

/**
  * Created by adamconder on 07/02/2017.
  */
@Singleton
class NRSConnector(var httpPost: HttpPost = WSHttp, auditor: ScotlandAudit = new ScotlandAudit()) extends BirthConnector {

  //override val desBaseUrl = getConfString("birth-registration-matching")
  override val serviceUrl: String = "http://localhost:9007/national-records/births"
  val envHeader = "envHeader"
  val authToken = "authToken"
  //private val  = ""
  private val detailsUri = s"$serviceUrl"
  private val referenceUri = s"$serviceUrl"
  //private val keyValue = KeyGenerator.getKey()

 /* override val headers = Seq(
    BRMLogger.BRM_KEY -> KeyGenerator.getKey(),
    "Content-Type" -> "application/json; charset=utf-8")*/

  override def headers( brmKey: String) =
    Seq(
    QUERY_ID_HEADER -> KeyGenerator.getKey(),
    "Content-Type" -> "application/json; charset=utf-8",
    ENVIRONMENT_HEADER -> envHeader,
    TOKEN_HEADER -> authToken,
    DATETIME_HEADER -> "2017-02-16T10:55:32.001"

  )

  override val referenceBody: PartialFunction[Payload, (String, JsValue)] = {

    case Payload(Some(brn), fName, lName, dob, _) =>

      (referenceUri, Json.parse(
        s"""
           |{
           | "$JSON_ID_PATH" : "$brn",
           | "$JSON_FIRSTNAME_PATH" : "${NameFormat(fName)}",
           | "$JSON_LASTNAME_PATH" : "${NameFormat(lName)}",
           | "$JSON_DATEOFBIRTH_PATH" : "$dob"
           |}
         """.stripMargin))
  }

  override val detailsBody: PartialFunction[Payload, (String, JsValue)] = {
    case Payload(None, fName, lName, dob, _) =>
      (detailsUri, Json.parse(
        s"""
           |{
           | "$JSON_FIRSTNAME_PATH" : "${NameFormat(fName)}",
           | "$JSON_LASTNAME_PATH" : "${NameFormat(lName)}",
           | "$JSON_DATEOFBIRTH_PATH" : "$dob"
           |}
         """.stripMargin))
  }



  /* override def getReference(payload: Payload)(implicit hc: HeaderCarrier) = {
     BRMLogger.debug(s"NRSConnector", "getReference", s"requesting child's record from NRS")

     referenceBody.apply(payload)

     val result: Map[String, String] = Map("match" -> "false")
     auditor.audit(result, Some(payload))

     Future.failed(new NotImplementedException("No getReference method available for NRS connector."))
   }*/

  /*override def getChildDetails(payload: Payload)(implicit hc: HeaderCarrier) = {
    BRMLogger.debug(s"NRSConnector", "getReference", s"requesting child's record from NRS")

    detailsBody.apply(payload)

    val result: Map[String, String] = Map("match" -> "false")
    auditor.audit(result, Some(payload))

    Future.failed(new NotImplementedException("No getChildDetails method available for NRS connector."))
  }*/

}
