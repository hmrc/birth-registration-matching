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
import play.api.libs.ws.WS
import uk.gov.hmrc.brm.config.WSHttp
import uk.gov.hmrc.brm.utils.BrmLogger
import uk.gov.hmrc.brm.utils.Keygenerator
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http._

import scala.concurrent.Future

trait BirthConnector extends ServicesConfig {

  val serviceUrl : String
  val httpGet : HttpGet

  val baseUri : String
  val detailsUri : String

  private def requestReference(reference: String)(implicit hc : HeaderCarrier) = {
    val newHc = hc.withExtraHeaders(BrmLogger.BRM_KEY-> Keygenerator.geKey())
    BrmLogger.info(s"BirthConnector", "requestReference", s"endpoint: $detailsUri")
    httpGet.GET[HttpResponse](s"$detailsUri/$reference")(implicitly[HttpReads[HttpResponse]], newHc)
  }

  private def requestDetails(params : Map[String, String])(implicit hc : HeaderCarrier) = {
    val endpoint = WS.url(detailsUri).withQueryString(params.toList: _*).url
    BrmLogger.info(s"BirthConnector", "requestDetails", s"endpoint: $endpoint")
    httpGet.GET[HttpResponse](endpoint)
  }

  def getReference(reference: String)(implicit hc : HeaderCarrier) = {
    BrmLogger.debug(s"BirthConnector", "getReference", s"$reference")
    BrmLogger.info(s"BirthConnector", "getReference", "calling getReference")
    requestReference(reference)
  }

  def getChildDetails(params : Map[String, String])(implicit hc : HeaderCarrier) = {
    BrmLogger.debug(s"BirthConnector", "getDetails", s"$params")
    BrmLogger.info(s"BirthConnector", "getChildDetails", "calling getChildDetails")
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
  override val serviceUrl = ""
  override val httpGet : HttpGet = WSHttp
  override val baseUri = ""
  override val detailsUri = s"$serviceUrl/$baseUri"

  override def getReference(reference: String)(implicit hc : HeaderCarrier)  = {
    BrmLogger.debug(s"NirsConnector", "getReference", s"$reference")
    Future.failed(new NotImplementedException("No service available for GRONI connector."))
  }
}

object NrsConnector extends BirthConnector {
  override val serviceUrl = ""
  override val httpGet : HttpGet = WSHttp
  override val baseUri = ""
  override val detailsUri = s"$serviceUrl/$baseUri"

  override def getReference(reference: String)(implicit hc : HeaderCarrier)  = {
    BrmLogger.debug(s"NRSConnector", "getReference", s"$reference")
    Future.failed(new NotImplementedException("No service available for NRS connector."))
  }
}
