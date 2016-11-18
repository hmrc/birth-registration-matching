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

import java.net.URLEncoder

import play.api.Logger
import play.api.Play.current
import play.api.libs.ws.WS
import uk.gov.hmrc.brm.audit.{BRMAudit, EnglandAndWalesAuditEvent, NorthernIrelandAuditEvent, ScotlandAuditEvent}
import uk.gov.hmrc.brm.config.WSHttp
import uk.gov.hmrc.brm.models.brm.Payload
import uk.gov.hmrc.brm.utils.{BirthRegisterCountry, BrmLogger, Keygenerator, NameFormat}
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http._

import scala.concurrent.Future

trait BirthConnector extends ServicesConfig {

  val serviceUrl : String
  val httpGet : HttpGet

  val baseUri : String
  val detailsUri : String

  abstract class RequestType
  case class ReferenceRequest() extends RequestType
  case class DetailsRequest() extends RequestType

  private val referenceURI : PartialFunction[Payload, String] = {
    case Payload(Some(birthReferenceNumber), _, _, _, _) =>
      s"$detailsUri/$birthReferenceNumber"
  }

  private val detailsURI : PartialFunction[Payload, String] = {
    case Payload(None, f, l, d, _) =>
      val nameValuePair = Map(
        "forenames" -> NameFormat(f),
        "lastname" -> NameFormat(l),
        "dateofbirth" -> s"$d"
      )

      val query = nameValuePair.map(pair => pair._1 + "=" + URLEncoder.encode(pair._2, "UTF-8")).mkString("&")
      detailsUri.concat(s"?$query")
  }

  private def request(payload: Payload, operation: RequestType)(implicit hc: HeaderCarrier) = {
    val f = operation match {
      case ReferenceRequest() => referenceURI
      case DetailsRequest() => detailsURI
    }

    val uri = f(payload)
    val newHc = hc.withExtraHeaders(BrmLogger.BRM_KEY-> Keygenerator.geKey())
    httpGet.GET[HttpResponse](uri)(implicitly[HttpReads[HttpResponse]], newHc)
  }

  def getReference(payload: Payload)(implicit hc : HeaderCarrier) = {
    BrmLogger.info(s"BirthConnector", "getReference", "calling request with reference number")
    request(payload, ReferenceRequest())
  }

  def getChildDetails(payload: Payload)(implicit hc : HeaderCarrier) = {
    BrmLogger.info(s"BirthConnector", "getChildDetails", "calling request with child details")
    request(payload, DetailsRequest())
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

  override def getReference(payload: Payload)(implicit hc : HeaderCarrier)  = {
    BrmLogger.debug(s"NRSConnector", "getChildDetails", s"requesting child's record from GRO-NI")

    val result : Map[String, String] = Map("match" -> "false")
    val event = new NorthernIrelandAuditEvent(result)
    BRMAudit.event(event)

    Future.failed(new NotImplementedException("No getReference method available for GRONI connector."))
  }

  override def getChildDetails(payload: Payload)(implicit hc : HeaderCarrier)  = {
    BrmLogger.debug(s"NRSConnector", "getChildDetails", s"requesting child's record from GRO-NI")

    val result : Map[String, String] = Map("match" -> "false")
    val event = new ScotlandAuditEvent(result)
    BRMAudit.event(event)

    Future.failed(new NotImplementedException("No getChildDetails method available for GRONI connector."))
  }
}

object NrsConnector extends BirthConnector {
  override val serviceUrl = ""
  override val httpGet : HttpGet = WSHttp
  override val baseUri = ""
  override val detailsUri = s"$serviceUrl/$baseUri"

  override def getReference(payload: Payload)(implicit hc : HeaderCarrier)  = {
    BrmLogger.debug(s"NRSConnector", "getReference", s"requesting child's record from NRS")

    val result : Map[String, String] = Map("match" -> "false")
    val event = new ScotlandAuditEvent(result)
    BRMAudit.event(event)

    Future.failed(new NotImplementedException("No getReference method available for NRS connector."))
  }

  override def getChildDetails(payload: Payload)(implicit hc : HeaderCarrier)  = {
    BrmLogger.debug(s"NRSConnector", "getReference", s"requesting child's record from NRS")

    val result : Map[String, String] = Map("match" -> "false")
    val event = new ScotlandAuditEvent(result)
    BRMAudit.event(event)

    Future.failed(new NotImplementedException("No getChildDetails method available for NRS connector."))
  }
}
