/*
 * Copyright 2023 HM Revenue & Customs
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

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import play.api.http.Status
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.brm.models.brm.Payload
import uk.gov.hmrc.brm.utils.CommonConstant._
import uk.gov.hmrc.brm.utils.Mocks._
import uk.gov.hmrc.brm.utils.{BaseUnitSpec, BirthRegisterCountry, JsonUtils}
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.play.audit.http.connector.AuditResult

import java.time.LocalDate
import scala.concurrent.Future
import scala.util.{Failure, Success}

class BirthConnectorSpec extends BaseUnitSpec {

  import uk.gov.hmrc.brm.utils.TestHelper._

  override lazy val app = new GuiceApplicationBuilder()
    .configure(
      Map(
        "microservice.services.birth-registration-matching.matching.ignoreAdditionalNames" -> true
      )
    )
    .build()

  trait BirthConnectorSpecSetup {
    when(mockAuditConnector.sendEvent(any())(any(), any())).thenReturn(Future.successful(AuditResult.Success))
  }

  val groJsonResponseObject = JsonUtils.getJsonFromFile("gro", "500035710")
  val nrsJsonResponseObject = JsonUtils.getJsonFromFile("nrs", "2017734003")

  "GROConnector" should {

    "getReference returns json response" in {
      val result = connectorFixtures.groConnector.getReference(payload).futureValue
      checkResponse(result, Status.OK)
    }

    "getReference returns http 500 when GRO is offline" in {
      mockHttpPostResponse(Status.INTERNAL_SERVER_ERROR, None)
      val result = connectorFixtures.groConnector.getReference(payload).futureValue
      checkResponse(result, Status.INTERNAL_SERVER_ERROR)
    }

    "getReference returns http 400 for BadRequest" in {
      mockHttpPostResponse(Status.BAD_REQUEST, None)
      val result = connectorFixtures.groConnector.getReference(payload).futureValue
      checkResponse(result, Status.BAD_REQUEST)
    }

    "getReference returns http 404 when GRO has not found data" in {
      mockHttpPostResponse(Status.NOT_FOUND, None)
      val result = connectorFixtures.groConnector.getReference(payload).futureValue
      checkResponse(result, Status.NOT_FOUND)
    }

    "getChildDetails returns json response" in {
      mockHttpPostResponse(Status.OK, Some(groJsonResponseObject))
      val result = connectorFixtures.groConnector.getChildDetails(payloadNoReference).futureValue
      checkResponse(result, Status.OK)
    }

    "getChildDetails call should not pass additional name to gro." in {
      when(mockCommonUtil.forenames(any(), any()))
        .thenReturn("Adam")

      val argumentCapture = mockHttpPostResponse(Status.OK, Some(groResponseWithAdditionalName))
      val payload         =
        Payload(None, "Adam", Some("test"), "SMITH", LocalDate.of(2009, 7, 1), BirthRegisterCountry.ENGLAND)
      val result          = connectorFixtures.groConnector.getChildDetails(payload).futureValue
      checkResponse(result, Status.OK)
      argumentCapture.value.toString().contains("test") shouldBe false
      (argumentCapture.value \ "forenames").as[String]  shouldBe "Adam"
    }

    "getChildDetails returns http 500 when GRO is offline" in {
      mockHttpPostResponse(Status.INTERNAL_SERVER_ERROR, None)
      val result = connectorFixtures.groConnector.getChildDetails(payloadNoReference).futureValue
      checkResponse(result, Status.INTERNAL_SERVER_ERROR)
    }

    "getChildDetails returns http 400 for BadRequest" in {
      mockHttpPostResponse(Status.BAD_REQUEST, None)
      val result = connectorFixtures.groConnector.getChildDetails(payloadNoReference).futureValue
      checkResponse(result, Status.BAD_REQUEST)
    }

    "getChildDetails returns http 404 when GRO has not found data" in {
      mockHttpPostResponse(Status.NOT_FOUND, None)
      val result = connectorFixtures.groConnector.getChildDetails(payloadNoReference).futureValue
      checkResponse(result, Status.NOT_FOUND)
    }

    "NRSConnector" should {

      "getReference returns 200 status with json response when record was found. " in {
        mockHttpPostResponse(Status.OK, Some(nrsJsonResponseObject))
        val result = connectorFixtures.nrsConnector.getReference(nrsRequestPayload).futureValue
        checkResponse(result, Status.OK)
      }

      "getReference returns 403 forbidden response when record was not found." in {
        mockHttpPostResponse(Status.FORBIDDEN, None)
        val result = connectorFixtures.nrsConnector.getReference(nrsRequestPayload).futureValue
        checkResponse(result, Status.FORBIDDEN)
      }

      "getReference returns 503 when NRS is down." in {
        mockHttpPostResponse(Status.SERVICE_UNAVAILABLE, None)
        val result = connectorFixtures.nrsConnector.getReference(nrsRequestPayload).futureValue
        checkResponse(result, Status.SERVICE_UNAVAILABLE)
      }

      "getReference returns http 500 when DES is offline" in {
        mockHttpPostResponse(Status.INTERNAL_SERVER_ERROR, None)
        val result = connectorFixtures.nrsConnector.getReference(nrsRequestPayload).futureValue
        checkResponse(result, Status.INTERNAL_SERVER_ERROR)
      }

      "getChildDetails returns json response" in {
        mockHttpPostResponse(Status.OK, Some(nrsJsonResponseObject))
        val result = connectorFixtures.nrsConnector.getChildDetails(nrsRequestPayloadWithoutBrn).futureValue
        checkResponse(result, Status.OK)
      }

      "getChildDetails call should not pass additional name to nrs." in {
        val argumentCapture           = mockHttpPostResponse(Status.OK, Some(nrsJsonResponseObject))
        val requestWithAdditionalName =
          Payload(None, "Adam", Some("test"), "SMITH", LocalDate.of(2009, 11, 12), BirthRegisterCountry.SCOTLAND)
        val result                    = connectorFixtures.nrsConnector.getChildDetails(requestWithAdditionalName).futureValue
        checkResponse(result, Status.OK)
        (argumentCapture.value \ JSON_FIRSTNAME_PATH).as[String]   shouldBe "Adam"
        (argumentCapture.value \ JSON_LASTNAME_PATH).as[String]    shouldBe "SMITH"
        (argumentCapture.value \ JSON_DATEOFBIRTH_PATH).as[String] shouldBe "2009-11-12"
      }

      "getChildDetails returns 403 forbidden response when record was not found." in {
        mockHttpPostResponse(Status.FORBIDDEN, None)
        val result = connectorFixtures.nrsConnector.getChildDetails(nrsRequestPayloadWithoutBrn).futureValue
        checkResponse(result, Status.FORBIDDEN)
      }

      "getChildDetails returns 503 (SERVICE_UNAVAILABLE) when NRS is down." in {
        mockHttpPostResponse(Status.SERVICE_UNAVAILABLE, None)
        val result = connectorFixtures.nrsConnector.getChildDetails(nrsRequestPayloadWithoutBrn).futureValue
        checkResponse(result, Status.SERVICE_UNAVAILABLE)
      }

      "getChildDetails returns http 500 when DES is offline" in {
        mockHttpPostResponse(Status.INTERNAL_SERVER_ERROR, None)
        val result = connectorFixtures.nrsConnector.getChildDetails(nrsRequestPayloadWithoutBrn).futureValue
        checkResponse(result, Status.INTERNAL_SERVER_ERROR)
      }

    }

    "GRONIConnector" should {

      "initialise with correct properties" in {
        connectorFixtures.groniConnector.http shouldBe a[HttpClientV2]
      }

      "getReference returns http NotImplementedException" in new BirthConnectorSpecSetup {
        val future = connectorFixtures.groniConnector.getReference(payload)
        future.onComplete {
          case Failure(e) =>
            connectorFixtures.groniConnector.headers.isEmpty shouldBe true
            e.getMessage                                     shouldBe "No getReference method available for GRONI connector."
          case Success(_) =>
            throw new Exception
        }
      }

      "getChildDetails returns http NotImplementedException" in new BirthConnectorSpecSetup {
        val future = connectorFixtures.groniConnector.getChildDetails(payloadNoReferenceNorthernIreland)
        future.onComplete {
          case Failure(e) =>
            connectorFixtures.groniConnector.headers.isEmpty shouldBe true
            e.getMessage                                     shouldBe "No getChildDetails method available for GRONI connector."
          case Success(_) =>
            throw new Exception
        }
      }
    }
  }

}
