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

import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.brm.audit.ScotlandAudit
import uk.gov.hmrc.brm.config.WSHttp
import uk.gov.hmrc.brm.models.brm.Payload
import uk.gov.hmrc.brm.utils.BrmLogger
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpPost, NotImplementedException}

import scala.concurrent.Future

/**
  * Created by adamconder on 07/02/2017.
  */
object NRSConnector extends BirthConnector {

  override val serviceUrl = ""
  override var httpPost: HttpPost = WSHttp
  private val baseUri = ""
  val detailsUri = s"$serviceUrl/$baseUri"
  private val referenceUri = s"$serviceUrl/$baseUri"

  override val headers = Seq()

  override val referenceBody: PartialFunction[Payload, (String, JsValue)] = {
    case Payload(Some(brn), _, _, _, _) =>
      (referenceUri, Json.parse(
        s"""
           |{}
         """.stripMargin))
  }

  override val detailsBody: PartialFunction[Payload, (String, JsValue)] = {
    case Payload(None, f, l, d, _) =>
      (detailsUri, Json.parse(
        s"""
           |{}
         """.stripMargin))
  }

  override def getReference(payload: Payload)(implicit hc: HeaderCarrier) = {
    BrmLogger.debug(s"NRSConnector", "getReference", s"requesting child's record from NRS")

    val result: Map[String, String] = Map("match" -> "false")
    new ScotlandAudit().audit(result, Some(payload))

    Future.failed(new NotImplementedException("No getReference method available for NRS connector."))
  }

  override def getChildDetails(payload: Payload)(implicit hc: HeaderCarrier) = {
    BrmLogger.debug(s"NRSConnector", "getReference", s"requesting child's record from NRS")

    val result: Map[String, String] = Map("match" -> "false")
    new ScotlandAudit().audit(result, Some(payload))

    Future.failed(new NotImplementedException("No getChildDetails method available for NRS connector."))
  }

}
