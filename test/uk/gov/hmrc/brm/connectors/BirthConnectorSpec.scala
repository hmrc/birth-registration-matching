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
import play.api.http.Status
import uk.gov.hmrc.brm.models.brm.Payload
import uk.gov.hmrc.brm.utils.{BirthRegisterCountry, WireMockHelperTrait}
import uk.gov.hmrc.http.client.HttpClientV2

import java.time.LocalDate
import org.scalatest.concurrent.PatienceConfiguration.Timeout
import org.scalatest.time.{Seconds, Span}
import uk.gov.hmrc.brm.utils.TestHelper.*

class BirthConnectorSpec extends WireMockHelperTrait {

  override val ignoreAdditionalNames: Boolean = true

  "GROConnector" should {

    "getReference returns json response" in {
      server.stubFor(post(urlEqualTo(groReferenceUrl)).willReturn(okJson(groJsonResponseObject.toString())))
      val result = groConnector.getReference(payload).futureValue(Timeout(Span(3, Seconds)))
      checkResponse(result, Status.OK)
      server.verify(postRequestedFor(urlEqualTo(groReferenceUrl)))
    }

    "getReference returns http 500 when GRO is offline" in {
      server.stubFor(post(urlEqualTo(groReferenceUrl)).willReturn(aResponse().withStatus(Status.INTERNAL_SERVER_ERROR)))
      val result = groConnector.getReference(payload).futureValue(Timeout(Span(3, Seconds)))
      checkResponse(result, Status.INTERNAL_SERVER_ERROR)
    }

    "getReference returns http 400 for BadRequest" in {
      server.stubFor(post(urlEqualTo(groReferenceUrl)).willReturn(aResponse().withStatus(Status.BAD_REQUEST)))
      val result = groConnector.getReference(payload).futureValue(Timeout(Span(3, Seconds)))
      checkResponse(result, Status.BAD_REQUEST)
    }

    "getReference returns http 404 when GRO has not found data" in {
      server.stubFor(post(urlEqualTo(groReferenceUrl)).willReturn(aResponse().withStatus(Status.NOT_FOUND)))
      val result = groConnector.getReference(payload).futureValue(Timeout(Span(3, Seconds)))
      checkResponse(result, Status.NOT_FOUND)
    }

    "getChildDetails returns json response" in {
      server.stubFor(post(urlEqualTo(groDetailsUrl)).willReturn(okJson(groJsonResponseObject.toString())))
      val result = groConnector.getChildDetails(payloadNoReference).futureValue(Timeout(Span(3, Seconds)))
      checkResponse(result, Status.OK)
    }

    "getChildDetails call should not pass additional name to gro." in {
      val payload = Payload(None, "Adam", Some("test"), "SMITH", LocalDate.of(2009, 7, 1), BirthRegisterCountry.ENGLAND)

      server.stubFor(
        post(urlEqualTo(groDetailsUrl))
          .withRequestBody(matchingJsonPath("$.forenames", equalTo("Adam")))
          .withRequestBody(notContaining("test"))
          .willReturn(okJson(groResponseWithAdditionalName.toString()))
      )

      val result = groConnector.getChildDetails(payload).futureValue(Timeout(Span(3, Seconds)))
      checkResponse(result, Status.OK)
      server.verify(
        postRequestedFor(urlEqualTo(groDetailsUrl))
          .withRequestBody(matchingJsonPath("$.forenames", equalTo("Adam")))
          .withRequestBody(notContaining("test"))
      )
    }

    "getChildDetails returns http 500 when GRO is offline" in {
      server.stubFor(post(urlEqualTo(groDetailsUrl)).willReturn(aResponse().withStatus(Status.INTERNAL_SERVER_ERROR)))
      val result = groConnector.getChildDetails(payloadNoReference).futureValue(Timeout(Span(3, Seconds)))
      checkResponse(result, Status.INTERNAL_SERVER_ERROR)
    }

    "getChildDetails returns http 400 for BadRequest" in {
      server.stubFor(post(urlEqualTo(groDetailsUrl)).willReturn(aResponse().withStatus(Status.BAD_REQUEST)))
      val result = groConnector.getChildDetails(payloadNoReference).futureValue(Timeout(Span(3, Seconds)))
      checkResponse(result, Status.BAD_REQUEST)
    }

    "getChildDetails returns http 404 when GRO has not found data" in {
      server.stubFor(post(urlEqualTo(groDetailsUrl)).willReturn(aResponse().withStatus(Status.NOT_FOUND)))
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
        val result = nrsConnector.getChildDetails(nrsRequestPayloadWithoutBrn).futureValue(Timeout(Span(3, Seconds)))
        checkResponse(result, Status.OK)
      }

      "getChildDetails call should not pass additional name to nrs." in {
        val requestWithAdditionalName =
          Payload(None, "Adam", Some("test"), "SMITH", LocalDate.of(2009, 11, 12), BirthRegisterCountry.SCOTLAND)

        server.stubFor(
          post(urlEqualTo(nrsUrl))
            .withRequestBody(matchingJsonPath("$.firstName", equalTo("Adam")))
            .withRequestBody(matchingJsonPath("$.lastName", equalTo("SMITH")))
            .withRequestBody(matchingJsonPath("$.dateOfBirth", equalTo("2009-11-12")))
            .withRequestBody(notContaining("test"))
            .willReturn(okJson(nrsJsonResponseObject.toString()))
        )

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
        val result = nrsConnector.getChildDetails(nrsRequestPayloadWithoutBrn).futureValue(Timeout(Span(3, Seconds)))
        checkResponse(result, Status.INTERNAL_SERVER_ERROR)
      }

    }

    "GRONIConnector" should {

      "initialise with correct properties" in {
        groniConnector.http shouldBe a[HttpClientV2]
      }

      "getReference returns http 501 Not Implemented" in {
        val result = groniConnector.getReference(payload).futureValue(Timeout(Span(3, Seconds)))
        groniConnector.headers.isEmpty shouldBe true
        result.status                  shouldBe Status.NOT_IMPLEMENTED
        result.body                    shouldBe "GRO-NI getReference not implemented"
      }

      "getChildDetails returns http 501 Not Implemented" in {
        val result = groniConnector.getChildDetails(payloadNoReferenceNorthernIreland).futureValue
        groniConnector.headers.isEmpty shouldBe true
        result.status                  shouldBe Status.NOT_IMPLEMENTED
        result.body                    shouldBe "GRO-NI getChildDetails not implemented"
      }
    }
  }

}
