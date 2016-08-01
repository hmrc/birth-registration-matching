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
import play.api.libs.ws.WS
import uk.gov.hmrc.play.http._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import play.api.Play.current

trait BirthConnectorConfig extends ServicesConfig {
  protected val serviceUrl : String
  def username : String
  def password : String
  protected val baseUri : String
  val httpPost : HttpPost
  val httpGet : HttpGet
  protected val version : String = "v0"
  lazy val endpoint = s"$serviceUrl/$baseUri"
  val authUri : String
  val eventUri : String
  val eventEndpoint : String
  val authEndpoint : String
  override def toString = {
    s"endpoint: $endpoint, http, version: $version"
  }
}

case class GROEnglandAndWales() extends BirthConnectorConfig {
  override val serviceUrl = baseUrl("birth-registration-matching")
  override def username = getConfString("birth-registration-matching.username", throw new RuntimeException("no configuration found for username"))
  override val password = ""
  override val baseUri = s"api/$version/events/birth"
  override val httpPost : HttpPost = WSHttp
  override val httpGet : HttpGet = WSHttp
  override val version : String = "v0"
  override val eventUri = s"api/$version/events/birth"
  override val authUri = s"oauth/login"
  override val eventEndpoint = s"$serviceUrl/$eventUri"
  override val authEndpoint = s"$serviceUrl/$authUri"
}

trait BirthConnector {
  protected val config : BirthConnectorConfig

  def getReference(reference: Option[String])(implicit hc : HeaderCarrier) : Future[JsValue]

  def getChildDetails(params : Map[String, String])(implicit hc : HeaderCarrier) : Future[JsValue]
}

object GROEnglandAndWalesConnector extends BirthConnector {
  override protected val config : BirthConnectorConfig = GROEnglandAndWales()

  private def GROEventHeaderCarrier = {
    HeaderCarrier()
      .withExtraHeaders("Authorization" -> s"Bearer ")
      .withExtraHeaders("X-Auth-Downstream-Username" -> config.username)
  }

  private def requestReference(reference: String)(implicit hc : HeaderCarrier) = {
    config.httpGet.GET[HttpResponse](s"$config.eventEndpoint/$reference")(hc = GROEventHeaderCarrier, rds = HttpReads.readRaw) map {
      response =>
        handleResponse(response)
    }
  }

  private def requestDetails(params : Map[String, String])(implicit hc : HeaderCarrier) = {

    val endpoint = WS.url(config.eventEndpoint).withQueryString(params.toList: _*).url
    Logger.debug(s"Request details endpoint: $endpoint")
    config.httpGet.GET[HttpResponse](endpoint)(hc = GROEventHeaderCarrier, rds = HttpReads.readRaw) map {
      response =>
        handleResponse(response)
    }
  }

  private def handleResponse(response : HttpResponse) = {
    response.status match {
      case Status.OK =>
        response.json
      case e =>
        throw new Upstream5xxResponse("[GROEnglandAndWalesConnector][Invalid Response]", e, Status.INTERNAL_SERVER_ERROR)
    }
  }

  def getReference(reference: Option[String])(implicit hc : HeaderCarrier) : Future[JsValue] = {
    Logger.debug(s"[GROEnglandAndWalesConnector][getReference]: $reference")
    requestReference(reference.get)
  }

  def getChildDetails(params : Map[String, String])(implicit hc : HeaderCarrier) : Future[JsValue] = {
    Logger.debug(s"[GROEnglandAndWalesConnector][getDetails]: $params")
    requestDetails(params)
  }
}