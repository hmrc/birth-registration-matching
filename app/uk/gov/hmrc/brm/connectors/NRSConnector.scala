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
import uk.gov.hmrc.brm.config.{BrmConfig, WSHttp}
import uk.gov.hmrc.brm.models.brm.Payload
import uk.gov.hmrc.brm.utils.{KeyGenerator, NameFormat}
import uk.gov.hmrc.play.http.HttpPost
import uk.gov.hmrc.brm.utils.CommonConstant._
import uk.gov.hmrc.brm.utils.DateUtil._

import scala.concurrent.Future

/**
  * Created by adamconder on 07/02/2017.
  */
@Singleton
class NRSConnector(var httpPost: HttpPost = WSHttp, auditor: ScotlandAudit = new ScotlandAudit()) extends BirthConnector {

  override val serviceUrl = baseUrl("des")
  private val baseUri = "national-records/births"
  private val detailsUri = s"$serviceUrl/$baseUri"
  private val referenceUri = s"$serviceUrl/$baseUri"
  private val DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS"


  override def headers =
    Seq(
      QUERY_ID_HEADER -> KeyGenerator.getKey(),
      CONTENT_TYPE -> CONTENT_TYPE_JSON,
      ENVIRONMENT_HEADER -> BrmConfig.desEnv,
      TOKEN_HEADER -> s"Bearer ${BrmConfig.desToken}",
      DATETIME_HEADER -> getCurrentDateString(DATE_FORMAT)
   )

  override val referenceBody: PartialFunction[Payload, (String, JsValue)] = {
    case Payload(Some(brn), fName, aName, lName, dob, _) =>

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
    case Payload(None, fName, aName, lName, dob, _) =>
      (detailsUri, Json.parse(
        s"""
           |{
           | "$JSON_FIRSTNAME_PATH" : "${firstName(fName,aName)}",
           | "$JSON_LASTNAME_PATH" : "${NameFormat(lName)}",
           | "$JSON_DATEOFBIRTH_PATH" : "$dob"
           |}
         """.stripMargin))
  }


  //add additional name to firstname while sending to respective service.
  protected def firstName(firstName: String, additionalName: Option[String]): String = {
    val forenames = BrmConfig.ignoreAdditionalName match {
      case true => NameFormat(firstName)
      case false => NameFormat(firstName).concat(" ").concat(NameFormat(additionalName.getOrElse(""))).trim
    }

    NameFormat(forenames)
  }
}
