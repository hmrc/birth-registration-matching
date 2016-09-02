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
import play.api.Play.current
import play.api.http.Status
import play.api.libs.json.JsValue
import play.api.libs.ws.WS
import uk.gov.hmrc.brm.config.WSHttp
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait BirthConnector extends ServicesConfig {

  val serviceUrl : String
  val httpGet : HttpGet

  val baseUri : String
  val detailsUri : String


  def getReference(reference: String)(implicit hc : HeaderCarrier) : Future[JsValue] = {
    Logger.debug(s"[BirthConnector][getReference]: $reference")
    requestReference(reference)
  }

  protected def requestReference(reference: String)(implicit hc : HeaderCarrier) = {
    httpGet.GET[HttpResponse](s"$detailsUri/$reference") map {
      response =>
        handleResponse(response)
    }
  }

  private def requestDetails(params : Map[String, String])(implicit hc : HeaderCarrier) = {
    val endpoint = WS.url(detailsUri).withQueryString(params.toList: _*).url
    Logger.debug(s"Request details endpoint: $endpoint")
    httpGet.GET[HttpResponse](endpoint) map {
      response =>
        handleResponse(response)
    }
  }

  private def handleResponse(response : HttpResponse) = {
    response.status match {
      case Status.OK =>
        response.json
      case e @ Status.NOT_FOUND =>
        throw new Upstream4xxResponse(s"[${super.getClass.getName}][NotFound]", e, Status.NOT_FOUND)
      case e @ Status.BAD_REQUEST =>
        throw new Upstream4xxResponse(s"[${super.getClass.getName}][BadRequest]", e, Status.BAD_REQUEST)
      case e @ _ =>
        throw new Upstream5xxResponse(s"[${super.getClass.getName}][InternalServerError]", e, Status.INTERNAL_SERVER_ERROR)
    }
  }



  def getChildDetails(params : Map[String, String])(implicit hc : HeaderCarrier) : Future[JsValue] = {
    Logger.debug(s"[GROEnglandAndWalesConnector][getDetails]: $params")
    requestDetails(params)
  }
}

object GROEnglandConnector extends BirthConnector {
  override val serviceUrl = baseUrl("birth-registration-matching")
  override val httpGet : HttpGet = WSHttp

  override val baseUri = "birth-registration-matching-proxy"
  override val detailsUri = s"$serviceUrl/$baseUri/match"



}

object NirsConnector extends BirthConnector {
  override val serviceUrl = null
  override val httpGet : HttpGet = null

  override val baseUri = ""
  override val detailsUri = ""


  override  def getReference(reference: String)(implicit hc : HeaderCarrier) : Future[JsValue] = {
    Logger.debug(s"[NirsConnector][getReference]: $reference")
    Future.failed(new NotImplementedException("No service available for NRS connector."))
  }
}

object NrsConnector extends BirthConnector {
  override val serviceUrl = null
  override val httpGet : HttpGet = null

  override val baseUri = ""
  override val detailsUri = ""

  override  def getReference(reference: String)(implicit hc : HeaderCarrier) : Future[JsValue] = {
    Logger.debug(s"[NrsConnector][getReference]: $reference")
    Future.failed(new NotImplementedException("No service available for NRS connector."))
  }
}




