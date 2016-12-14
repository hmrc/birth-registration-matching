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

import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.brm.audit.{BRMAudit, NorthernIrelandAuditEvent, ScotlandAuditEvent}
import uk.gov.hmrc.brm.config.WSHttp
import uk.gov.hmrc.brm.models.brm.Payload
import uk.gov.hmrc.brm.utils.{BrmLogger, Keygenerator, NameFormat}
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http._

import scala.concurrent.Future

trait BirthConnector extends ServicesConfig {

  val serviceUrl: String
  val httpPost: HttpPost

  val baseUri: String
  val detailsUri: String
  val referenceUri: String

  abstract class RequestType

  case class ReferenceRequest() extends RequestType

  case class DetailsRequest() extends RequestType

  case class RequestDetail(uri: String, jsonBody: Option[JsValue] = None){
    val headers = Seq(
                BrmLogger.BRM_KEY -> Keygenerator.geKey(),
                "Content-Type" -> "application/json; charset=utf-8")
  }

  private val referenceBody: PartialFunction[Payload, (String, JsValue)] = {
    case Payload(Some(brn), _, _, _, _) =>
      (referenceUri, Json.parse(
        s"""
           |{
           |  "reference" : "$brn"
           |}
         """.stripMargin))
  }

  private val detailsBody: PartialFunction[Payload, (String, JsValue)] = {
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

  private def buildRequest(payload: Payload, operation: RequestType): RequestDetail = {

    val f = operation match {
      case ReferenceRequest() => referenceBody
      case DetailsRequest() => detailsBody
    }
    val requestDetail = f(payload)
    RequestDetail(requestDetail._1, Some(requestDetail._2))
  }

  private def sendRequest(requestDetail: RequestDetail)(implicit hc: HeaderCarrier) = {
    httpPost.POST(requestDetail.uri, requestDetail.jsonBody, requestDetail.headers)
  }

  def getReference(payload: Payload)(implicit hc: HeaderCarrier) = {
    BrmLogger.info(s"BirthConnector", "getReference", "calling request with reference number")
    val requestData = buildRequest(payload, ReferenceRequest())
    sendRequest(requestData)
  }

  def getChildDetails(payload: Payload)(implicit hc: HeaderCarrier) = {
    BrmLogger.info(s"BirthConnector", "getChildDetails", "calling request with child details")
    val requestData = buildRequest(payload, DetailsRequest())
    sendRequest(requestData)
  }
}

object GROEnglandConnector extends BirthConnector {
  override val serviceUrl = baseUrl("birth-registration-matching")
  override val httpPost: HttpPost = WSHttp
  override val baseUri = "birth-registration-matching-proxy"
  override val detailsUri = s"$serviceUrl/$baseUri/match/details"
  override val referenceUri = s"$serviceUrl/$baseUri/match/reference"
}

object NirsConnector extends BirthConnector {
  override val serviceUrl = ""
  override val httpPost: HttpPost = WSHttp
  override val baseUri = ""
  override val detailsUri = s"$serviceUrl/$baseUri"
  override val referenceUri = s"$serviceUrl/$baseUri"

  override def getReference(payload: Payload)(implicit hc: HeaderCarrier) = {
    BrmLogger.debug(s"NRSConnector", "getChildDetails", s"requesting child's record from GRO-NI")

    val result: Map[String, String] = Map("match" -> "false")
    val event = new NorthernIrelandAuditEvent(result)
    BRMAudit.event(event)

    Future.failed(new NotImplementedException("No getReference method available for GRONI connector."))
  }

  override def getChildDetails(payload: Payload)(implicit hc: HeaderCarrier) = {
    BrmLogger.debug(s"NRSConnector", "getChildDetails", s"requesting child's record from GRO-NI")

    val result: Map[String, String] = Map("match" -> "false")
    val event = new ScotlandAuditEvent(result)
    BRMAudit.event(event)

    Future.failed(new NotImplementedException("No getChildDetails method available for GRONI connector."))
  }
}

object NrsConnector extends BirthConnector {
  override val serviceUrl = ""
  override val httpPost: HttpPost = WSHttp
  override val baseUri = ""
  override val detailsUri = s"$serviceUrl/$baseUri"
  override val referenceUri = s"$serviceUrl/$baseUri"

  override def getReference(payload: Payload)(implicit hc: HeaderCarrier) = {
    BrmLogger.debug(s"NRSConnector", "getReference", s"requesting child's record from NRS")

    val result: Map[String, String] = Map("match" -> "false")
    val event = new ScotlandAuditEvent(result)
    BRMAudit.event(event)

    Future.failed(new NotImplementedException("No getReference method available for NRS connector."))
  }

  override def getChildDetails(payload: Payload)(implicit hc: HeaderCarrier) = {
    BrmLogger.debug(s"NRSConnector", "getReference", s"requesting child's record from NRS")

    val result: Map[String, String] = Map("match" -> "false")
    val event = new ScotlandAuditEvent(result)
    BRMAudit.event(event)

    Future.failed(new NotImplementedException("No getChildDetails method available for NRS connector."))
  }
}
