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

package uk.gov.hmrc.brm.connectors

import play.api.Logger
import play.api.http.Status
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.brm.config.WSHttp
import uk.gov.hmrc.play.config.ServicesConfig

import uk.gov.hmrc.play.http.ws.{WSPost, WSGet, WSHttp}
import uk.gov.hmrc.play.http._

import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future

trait BirthConnectorConfig extends ServicesConfig {
  protected val serviceUrl : String
//  protected val key : String
  def username : String
  protected val baseUri : String
  def httpPost : WSPost
  def httpGet : WSGet
  protected val version : String = "v0"
  lazy val endpoint = s"$serviceUrl/$baseUri"

  override def toString = {
    s"endpoint: $endpoint, http, version: $version"
  }
}

case class GROEnglandAndWales() extends BirthConnectorConfig {
  override val serviceUrl = baseUrl("birth-registration-matching")
  override def username = getConfString("birth-registration-matching.username", throw new RuntimeException("no configuration found for username"))
  override val baseUri = s"api/$version/events/birth"
  override def httpPost = WSHttp
  override def httpGet : WSGet = WSHttp
}

case class GRONorthernIreland() extends BirthConnectorConfig {
  override val serviceUrl = baseUrl("ni")
  override def username = getConfString("ni.key", "")
  override val baseUri = ""
  override def httpPost = WSHttp
  override def httpGet : WSGet = WSHttp
}

case class NRS() extends BirthConnectorConfig {
  override val serviceUrl = baseUrl("nrs")
  override def username = getConfString("nrs.key", "")
  override val baseUri = ""
  override def httpPost = WSHttp
  override def httpGet : WSGet = WSHttp
}

trait BirthConnector {
  protected val config : BirthConnectorConfig

  def getReference(ref: String)(implicit hc : HeaderCarrier) : Future[JsValue]
}

object GROEnglandAndWalesConnector extends BirthConnector {
  override protected val config : BirthConnectorConfig = GROEnglandAndWales()

  def GROHeaderCarrier()(implicit hc : HeaderCarrier) = {
    HeaderCarrier()
      .withExtraHeaders("Authorization" -> s"Bearer ")
      .withExtraHeaders("X-Auth-Downstream-Username" -> config.username)
  }

//  implicit val httpReads: HttpReads[HttpResponse] = new HttpReads[HttpResponse] {
//    override def read(method: String, url: String, response: HttpResponse) = response
//  }

  override def getReference(ref: String)(implicit hc : HeaderCarrier) : Future[JsValue] = {
    Logger.debug(s"connector: $config, hc: ${GROHeaderCarrier()}")
//    val payload = Json.parse(
//      s"""
//        |{
//        | "ref" : $ref
//        |}
//      """.stripMargin)

//    config.httpGet.POST[JsValue, HttpResponse](config.endpoint, payload) map {
    config.httpGet.GET[HttpResponse](config.endpoint + s"/$ref")(hc = GROHeaderCarrier(), rds = HttpReads.readRaw) map {
      response =>
        response.status match {
          case Status.OK =>
            response.json
          case e =>
            throw new Upstream5xxResponse("something went wrong", e, Status.INTERNAL_SERVER_ERROR)
        }
    }
  }
}

//class BirthService(connector: BirthConnector) {
//
//  def getEvent(ref : String) = {
//    connector match {
//      case c @ GROEnglandAndWalesConnector =>
//        c.getReference(ref)
//      case _ =>
//        Json.toJson("")
//    }
//  }
//
//}
//
