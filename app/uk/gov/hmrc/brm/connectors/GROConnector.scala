package uk.gov.hmrc.brm.connectors

import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.brm.config.WSHttp
import uk.gov.hmrc.brm.models.brm.Payload
import uk.gov.hmrc.brm.utils.{BrmLogger, Keygenerator, NameFormat}
import uk.gov.hmrc.play.http.HttpPost

/**
  * Created by adamconder on 07/02/2017.
  */

object GROConnector extends BirthConnector {

  // TODO CURRENTLY THE ENGLANDANDWALESAUDITEVENT IS NOT USED



  override val serviceUrl = baseUrl("birth-registration-matching")
  override val httpPost: HttpPost = WSHttp

  private val baseUri = "birth-registration-matching-proxy"
  private val detailsUri = s"$serviceUrl/$baseUri/match/details"
  private val referenceUri = s"$serviceUrl/$baseUri/match/reference"

  override val headers = Seq(
    BrmLogger.BRM_KEY -> Keygenerator.geKey(),
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
