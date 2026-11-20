/*
 * Copyright 2026 HM Revenue & Customs
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

import com.github.tomakehurst.wiremock.client.WireMock.*
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import play.api.http.Status
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.brm.models.brm.Payload
import uk.gov.hmrc.brm.utils.CommonConstant.*
import uk.gov.hmrc.brm.utils.Mocks.*
import uk.gov.hmrc.brm.utils.{BaseUnitSpec, BirthRegisterCountry, CommonUtil, JsonUtils, WireMockHelper}
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.play.audit.http.connector.AuditResult

import java.time.LocalDate
import scala.concurrent.Future
import org.scalatest.concurrent.PatienceConfiguration.Timeout
import org.scalatest.time.{Seconds, Span}
import uk.gov.hmrc.brm.config.BrmConfig
import uk.gov.hmrc.brm.utils.TestHelper.*

class BirthConnectorSpec extends BaseUnitSpec with WireMockHelper {

  override lazy val app = new GuiceApplicationBuilder()
    .configure(
      Map(
        "microservice.services.birth-registration-matching.matching.ignoreAdditionalNames" -> true,
        "microservice.services.birth-registration-matching.host" -> "localhost",
        "microservice.services.birth-registration-matching.port" -> server.port(),
        "auditing.enabled" -> false
      )
    )
    .build()

  lazy val nrsapp =
    new GuiceApplicationBuilder()
      .configure(
        Map(
          "microservice.services.des.host" -> "localhost",
          "microservice.services.des.port" -> server.port(),
          "birth-registration-matching.matching.ignoreAdditionalNames" -> true,
          "auditing.enabled" -> false
        )
      )
      .build()

  private lazy val nrsConnector =
    nrsapp.injector.instanceOf[NRSConnector]

  private val nrsUrl =
    "/national-records/births"

  private lazy val groniConnector =
    app.injector.instanceOf[GRONIConnector]

  private lazy val commonUtil =
      app.injector.instanceOf[CommonUtil]

  private lazy val brmConfig =
    app.injector.instanceOf[BrmConfig]

  trait BirthConnectorSpecSetup {
    when(mockAuditConnector.sendEvent(any())(any(), any())).thenReturn(Future.successful(AuditResult.Success))
  }

  val groJsonResponseObject = JsonUtils.getJsonFromFile("gro", "500035710")
  val nrsJsonResponseObject = JsonUtils.getJsonFromFile("nrs", "2017734003")
  private lazy val groConnector = app.injector.instanceOf[GROConnector]
  private val groUrl = "/birth-registration-matching-proxy/match/reference"
  val url = "/birth-registration-matching-proxy/match/details"

  "GROConnector" should {

    "getReference returns json response" in {
      server.stubFor(post(urlEqualTo(groUrl)).willReturn(okJson(groJsonResponseObject.toString())))
      val result = groConnector.getReference(payload).futureValue(Timeout(Span(3, Seconds)))
      checkResponse(result, Status.OK)
      server.verify(postRequestedFor(urlEqualTo(groUrl)))
    }

    "getReference returns http 500 when GRO is offline" in {
      server.stubFor(post(urlEqualTo(groUrl)).willReturn(aResponse().withStatus(Status.INTERNAL_SERVER_ERROR)))
      val result = groConnector.getReference(payload).futureValue(Timeout(Span(3, Seconds)))
      checkResponse(result, Status.INTERNAL_SERVER_ERROR)
    }

    "getReference returns http 400 for BadRequest" in {
      server.stubFor(post(urlEqualTo(groUrl)).willReturn(aResponse().withStatus(Status.BAD_REQUEST)))
      val result = groConnector.getReference(payload).futureValue(Timeout(Span(3, Seconds)))
      checkResponse(result, Status.BAD_REQUEST)
    }

    "getReference returns http 404 when GRO has not found data" in {
      server.stubFor(post(urlEqualTo(groUrl)).willReturn(aResponse().withStatus(Status.NOT_FOUND)))
      val result = groConnector.getReference(payload).futureValue(Timeout(Span(3, Seconds)))
      checkResponse(result, Status.NOT_FOUND)
    }

    "getChildDetails returns json response" in {
      server.stubFor(post(urlEqualTo(url)).willReturn(okJson(groJsonResponseObject.toString())))
      val result = groConnector.getChildDetails(payloadNoReference).futureValue(Timeout(Span(3, Seconds)))
      checkResponse(result, Status.OK)
    }

    "getChildDetails call should not pass additional name to gro." in {
      val payload = Payload(None, "Adam", Some("test"), "SMITH", LocalDate.of(2009, 7, 1), BirthRegisterCountry.ENGLAND)

      server.stubFor(post(urlEqualTo(url)).withRequestBody(matchingJsonPath("$.forenames", equalTo("Adam")))
          .withRequestBody(notContaining("test")).willReturn(okJson(groResponseWithAdditionalName.toString())))

      val result = groConnector.getChildDetails(payload).futureValue(Timeout(Span(3, Seconds)))
      checkResponse(result, Status.OK)
      server.verify(postRequestedFor(urlEqualTo(url)).withRequestBody(matchingJsonPath("$.forenames", equalTo("Adam")))
          .withRequestBody(notContaining("test")))
    }

    "getChildDetails returns http 500 when GRO is offline" in {
      server.stubFor(post(urlEqualTo(url)).willReturn(aResponse().withStatus(Status.INTERNAL_SERVER_ERROR)))
      val result = groConnector.getChildDetails(payloadNoReference).futureValue(Timeout(Span(3, Seconds)))
      checkResponse(result, Status.INTERNAL_SERVER_ERROR)
    }

    "getChildDetails returns http 400 for BadRequest" in {
      server.stubFor(post(urlEqualTo(url)).willReturn(aResponse().withStatus(Status.BAD_REQUEST)))
      val result = groConnector.getChildDetails(payloadNoReference).futureValue(Timeout(Span(3, Seconds)))
      checkResponse(result, Status.BAD_REQUEST)
    }

    "getChildDetails returns http 404 when GRO has not found data" in {
      server.stubFor(post(urlEqualTo(url)).willReturn(aResponse().withStatus(Status.NOT_FOUND)))
      val result = groConnector.getChildDetails(payloadNoReference).futureValue(Timeout(Span(3, Seconds)))
      checkResponse(result, Status.NOT_FOUND)
    }

    "NRSConnector" should {

      "getReference returns 200 status with json response when record was found. " in {
        server.stubFor(post(urlEqualTo(nrsUrl)).willReturn(okJson(nrsJsonResponseObject.toString())))
        val result = nrsConnector.getReference(nrsRequestPayload).futureValue(Timeout(Span(3, Seconds)))
        checkResponse(result, Status.OK)
      }

      "getReference returns 403 forbidden response when record was not found." in {
        server.stubFor(post(urlEqualTo(nrsUrl)).willReturn(aResponse().withStatus(Status.FORBIDDEN)))
        val result = nrsConnector.getReference(nrsRequestPayload).futureValue(Timeout(Span(3, Seconds)))
        checkResponse(result, Status.FORBIDDEN)
      }

      "getReference returns 503 when NRS is down." in {
        server.stubFor(post(urlEqualTo(nrsUrl)).willReturn(aResponse().withStatus(Status.SERVICE_UNAVAILABLE)))
        val result = nrsConnector.getReference(nrsRequestPayload).futureValue(Timeout(Span(3, Seconds)))
        checkResponse(result, Status.SERVICE_UNAVAILABLE)
      }

      "getReference returns http 500 when DES is offline" in {
        server.stubFor(post(urlEqualTo(nrsUrl)).willReturn(aResponse().withStatus(Status.INTERNAL_SERVER_ERROR)))
        val result = nrsConnector.getReference(nrsRequestPayload).futureValue(Timeout(Span(3, Seconds)))
        checkResponse(result, Status.INTERNAL_SERVER_ERROR)
      }

      "getChildDetails returns json response" in {
        server.stubFor(post(urlEqualTo(nrsUrl)).willReturn(okJson(nrsJsonResponseObject.toString())))
        val result =nrsConnector.getChildDetails(nrsRequestPayloadWithoutBrn).futureValue(Timeout(Span(3, Seconds)))
        checkResponse(result, Status.OK)
      }

      "getChildDetails call should not pass additional name to nrs." in {
        val requestWithAdditionalName = Payload(None, "Adam", Some("test"), "SMITH", LocalDate.of(2009, 11, 12), BirthRegisterCountry.SCOTLAND)

        println(s"ignoreAdditionalNames = ${brmConfig.ignoreAdditionalNames}")
        println(
          s"forenames = ${commonUtil.forenames("Adam", Some("test"))}"
        )
        server.stubFor(post(urlEqualTo(nrsUrl)).withRequestBody(matchingJsonPath(s"$$.$JSON_FIRSTNAME_PATH", equalTo("Adam")))
            .withRequestBody(matchingJsonPath(s"$$.$JSON_LASTNAME_PATH", equalTo("SMITH")))
            .withRequestBody(matchingJsonPath(s"$$.$JSON_DATEOFBIRTH_PATH", equalTo("2009-11-12")))
            .withRequestBody(notContaining("test"))
            .willReturn(okJson(nrsJsonResponseObject.toString())))

        val result = nrsConnector.getChildDetails(requestWithAdditionalName).futureValue(Timeout(Span(3, Seconds)))
        checkResponse(result, Status.OK)
      }

      "getChildDetails returns 403 forbidden response when record was not found." in {
        server.stubFor(post(urlEqualTo(nrsUrl)).willReturn(aResponse().withStatus(Status.FORBIDDEN)))
        val result = nrsConnector.getChildDetails(nrsRequestPayloadWithoutBrn).futureValue(Timeout(Span(3, Seconds)))
        checkResponse(result, Status.FORBIDDEN)
      }

      "getChildDetails returns 503 (SERVICE_UNAVAILABLE) when NRS is down." in {
        server.stubFor(post(urlEqualTo(nrsUrl)).willReturn(aResponse().withStatus(Status.SERVICE_UNAVAILABLE)))
        val result = nrsConnector.getChildDetails(nrsRequestPayloadWithoutBrn).futureValue(Timeout(Span(3, Seconds)))
        checkResponse(result, Status.SERVICE_UNAVAILABLE)
      }

      "getChildDetails returns http 500 when DES is offline" in {
        server.stubFor(post(urlEqualTo(nrsUrl)).willReturn(aResponse().withStatus(Status.INTERNAL_SERVER_ERROR)))
        val result =nrsConnector.getChildDetails(nrsRequestPayloadWithoutBrn).futureValue(Timeout(Span(3, Seconds)))
        checkResponse(result, Status.INTERNAL_SERVER_ERROR)
      }

    }

    "GRONIConnector" should {

      "initialise with correct properties" in {
        groniConnector.http shouldBe a[HttpClientV2]
      }

      "getReference returns http 501 Not Implemented" in new BirthConnectorSpecSetup {
        val result = groniConnector.getReference(payload).futureValue(Timeout(Span(3, Seconds)))
        groniConnector.headers.isEmpty shouldBe true
        result.status                                    shouldBe Status.NOT_IMPLEMENTED
        result.body                                      shouldBe "GRO-NI getReference not implemented"
      }

      "getChildDetails returns http 501 Not Implemented" in new BirthConnectorSpecSetup {
        val result = groniConnector.getChildDetails(payloadNoReferenceNorthernIreland).futureValue
         groniConnector.headers.isEmpty shouldBe true
        result.status                                    shouldBe Status.NOT_IMPLEMENTED
        result.body                                      shouldBe "GRO-NI getChildDetails not implemented"
      }
    }
  }

}
