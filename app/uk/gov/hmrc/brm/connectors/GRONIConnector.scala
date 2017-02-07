package uk.gov.hmrc.brm.connectors

import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.brm.audit.{BRMAudit, NorthernIrelandAuditEvent}
import uk.gov.hmrc.brm.config.WSHttp
import uk.gov.hmrc.brm.models.brm.Payload
import uk.gov.hmrc.brm.utils.BrmLogger
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpPost, NotImplementedException}

import scala.concurrent.Future

/**
  * Created by adamconder on 07/02/2017.
  */
object GRONIConnector extends BirthConnector {

  override val serviceUrl = ""
  override val httpPost: HttpPost = WSHttp
  private val baseUri = ""
  private val detailsUri = s"$serviceUrl/$baseUri"
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
    BrmLogger.debug(s"NRSConnector", "getChildDetails", s"requesting child's record from GRO-NI")

    val result: Map[String, String] = Map("match" -> "false")
    val event = new NorthernIrelandAuditEvent(result, referenceUri)
    BRMAudit.event(event)

    Future.failed(new NotImplementedException("No getReference method available for GRONI connector."))
  }

  override def getChildDetails(payload: Payload)(implicit hc: HeaderCarrier) = {
    BrmLogger.debug(s"NRSConnector", "getChildDetails", s"requesting child's record from GRO-NI")

    val result: Map[String, String] = Map("match" -> "false")
    val event = new NorthernIrelandAuditEvent(result, detailsUri)
    BRMAudit.event(event)

    Future.failed(new NotImplementedException("No getChildDetails method available for GRONI connector."))
  }

}
