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
import uk.gov.hmrc.brm.config.WSHttp
import uk.gov.hmrc.brm.models.brm.Payload
import uk.gov.hmrc.brm.utils.{BRMLogger, KeyGenerator, NameFormat}
import uk.gov.hmrc.play.http.HttpPost

/**
  * Created by adamconder on 07/02/2017.
  */
@Singleton
class GROConnector(var httpPost: HttpPost = WSHttp) extends BirthConnector {

  override val serviceUrl = baseUrl("birth-registration-matching")

  private val baseUri = "birth-registration-matching-proxy"
  private val detailsUri = s"$serviceUrl/$baseUri/match/details"
  private val referenceUri = s"$serviceUrl/$baseUri/match/reference"

  override val headers = Seq(
    BRMLogger.BRM_KEY -> KeyGenerator.getKey(),
    "Content-Type" -> "application/json; charset=utf-8")

  override val referenceBody: PartialFunction[Payload, (String, JsValue)] = {
    case Payload(Some(brn), _, _, _, _) =>
      (referenceUri, Json.parse(
        s"""
           |{
           |  "reference" : "$brn"
           |}
         """.stripMargin))
  }

  override val detailsBody: PartialFunction[Payload, (String, JsValue)] = {
    case Payload(None, f, l, d, _) =>
      (detailsUri, Json.parse(
        s"""
           |{
           | "forenames" : "${NameFormat(f)}",
           | "lastname" : "${NameFormat(l)}",
           | "dateofbirth" : "$d"
           |}
        """.stripMargin))
  }

}
