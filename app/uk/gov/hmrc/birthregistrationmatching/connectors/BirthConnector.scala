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

package uk.gov.hmrc.birthregistrationmatching.connectors

import org.apache.http.client.methods.HttpPost
import play.api.http.Status
import play.api.libs.json.{Json, JsValue}
import play.api.libs.ws.WSRequest
import uk.gov.hmrc.birthregistrationmatching.config.WSHttp
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http.{Upstream5xxResponse, HttpResponse, HeaderCarrier}
import uk.gov.hmrc.play.http.ws.{WSGet, WSHttp}

import scala.concurrent.Future

trait BirthConnectorConfig extends ServicesConfig {
  protected val serviceUrl : String
  protected val key : String
  protected val username : String
  protected val baseUri : String
  def http : WSHttp
  protected val version : String = "v0"
  lazy val endpoint = s"$serviceUrl/$baseUri"
}

case class GROEnglandAndWales() extends BirthConnectorConfig {
  override val serviceUrl = baseUrl("birth-registration-matching")
  override val key = getConfString("microservice.services.birth-registration-matching.key", "")
  override val username = getConfString("microservice.services.birth-registration-matching.key", "")
  override val baseUri = s"api/$version/events/birth"
  override def http : WSGet = WSHttp
}

case class GRONorthernIreland() extends BirthConnectorConfig {
  override val serviceUrl = baseUrl("des-ni")
  override val key = getConfString("microservice.services.des-ni.key", "")
  override val username = getConfString("microservice.services.des-ni.key", "")
  override val baseUri = ""
  override def http : WSGet = WSHttp
}

case class NRS() extends BirthConnectorConfig {
  override val serviceUrl = baseUrl("nrs")
  override val key = getConfString("microservice.services.nrs.key", "")
  override val username = getConfString("microservice.services.nrs.key", "")
  override val baseUri = ""
  override def http : WSGet = WSHttp
}

//abstract class BirthConnector(config : BirthConnectorConfig)

trait BirthConnector {
  protected val config : BirthConnectorConfig
}

object GROEnglandAndWalesConnector extends BirthConnector {
  override protected  val config : BirthConnectorConfig = GROEnglandAndWales()

  def getReference(ref: String)(implicit hc : HeaderCarrier) : Future[JsValue] = {
    config.http.GET[HttpResponse](config.endpoint) map {
      response =>
        response.status match {
          case Status.OK =>
            response.json.as[JsValue]
          case e =>
            throw new Upstream5xxResponse("something went wrong", e, Status.INTERNAL_SERVER_ERROR)
        }
    }
  }
}

class BirthService(connector: BirthConnector) {

  def getEvent(ref : String) = {
    connector match {
      case c @ GROEnglandAndWalesConnector =>
        c.getReference(ref)
      case _ =>
        Json.toJson("")
    }
  }

}

