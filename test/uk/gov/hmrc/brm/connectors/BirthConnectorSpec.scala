/*
 * Copyright 2017 HM Revenue & Customs
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

import org.mockito.Matchers.{eq => mockEq}
import org.scalatest.BeforeAndAfter
import org.scalatest.mock.MockitoSugar
import play.api.http.Status
import uk.gov.hmrc.brm.utils.Mocks._
import uk.gov.hmrc.brm.utils.{BaseUnitSpec, JsonUtils}
import uk.gov.hmrc.play.http._
import uk.gov.hmrc.play.http.ws.WSPost
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.util.{Failure, Success}

class BirthConnectorSpec extends UnitSpec with WithFakeApplication with MockitoSugar with BeforeAndAfter with BaseUnitSpec {

  import uk.gov.hmrc.brm.utils.TestHelper._

  import scala.concurrent.ExecutionContext.Implicits.global

  implicit val hc = HeaderCarrier()

  val groJsonResponseObject = JsonUtils.getJsonFromFile("gro", "500035710")
  val nrsJsonResponseObject = JsonUtils.getJsonFromFile("nrs", "2017734003")

  val childDetailPayload = Map(
    "firstName" -> "Adam",
    "lastName" -> "Wilson",
    "dateOfBirth" -> "2006-11-12"
  )

  "GROConnector" should {

    "getReference returns json response" in {
      mockHttpPostResponse(Status.OK,Some(groJsonResponseObject))
      val result = await(connectorFixtures.groConnector.getReference(payload))
      checkResponse(result, 200)
    }

    "getReference returns http 500 when GRO is offline" in {
      mockHttpPostResponse(Status.INTERNAL_SERVER_ERROR,None)
      val result = await(connectorFixtures.groConnector.getReference(payload))
      checkResponse(result, 500)
    }

    "getReference returns http 400 for BadRequest" in {
      mockHttpPostResponse(Status.BAD_REQUEST,None)
      val result = await(connectorFixtures.groConnector.getReference(payload))
      checkResponse(result, 400)
    }

    "getReference returns http 404 when GRO has not found data" in {
      mockHttpPostResponse(Status.NOT_FOUND,None)
      val result = await(connectorFixtures.groConnector.getReference(payload))
      checkResponse(result, 404)
    }

    "getChildDetails returns json response" in {
      mockHttpPostResponse(Status.OK, Some(groJsonResponseObject))
      val result = await(connectorFixtures.groConnector.getChildDetails(payloadNoReference))
      checkResponse(result, 200)
    }

    "getChildDetails returns http 500 when GRO is offline" in {
      mockHttpPostResponse(Status.INTERNAL_SERVER_ERROR, None)
      val result = await(connectorFixtures.groConnector.getChildDetails(payloadNoReference))
      checkResponse(result, 500)
    }

    "getChildDetails returns http 400 for BadRequest" in {
      mockHttpPostResponse(Status.BAD_REQUEST, None)
      val result = await(connectorFixtures.groConnector.getChildDetails(payloadNoReference))
      checkResponse(result, 400)
    }

    "getChildDetails returns http 404 when GRO has not found data" in {
      mockHttpPostResponse(Status.NOT_FOUND, None)
      val result = await(connectorFixtures.groConnector.getChildDetails(payloadNoReference))
      checkResponse(result, 404)
    }

    "NRSConnector" should {

      "getReference returns 200 status with json response when record was found. " in {
        mockHttpPostResponse(Status.OK, Some(nrsJsonResponseObject))
        val result = await(connectorFixtures.nrsConnector.getReference(nrsRequestPayload))
        checkResponse(result, 200)
      }


      "getReference returns 403 forbidden response when record was not found." in {
        mockHttpPostResponse(Status.FORBIDDEN, None)
        val result = await(connectorFixtures.nrsConnector.getReference(nrsRequestPayload))
        checkResponse(result, 403)
      }

      "getReference returns 503 when NRS is down." in {
        mockHttpPostResponse(Status.SERVICE_UNAVAILABLE, None)
        val result = await(connectorFixtures.nrsConnector.getReference(nrsRequestPayload))
        checkResponse(result, 503)
      }

      "getReference returns http 500 when DES is offline" in {
        mockHttpPostResponse(Status.INTERNAL_SERVER_ERROR, None)
        val result = await(connectorFixtures.nrsConnector.getReference(nrsRequestPayload))
        checkResponse(result, 500)
      }

      "getChildDetails returns json response" in {
        mockHttpPostResponse(Status.OK, Some(nrsJsonResponseObject))
        val result = await(connectorFixtures.nrsConnector.getChildDetails(nrsRequestPayloadWithoutBrn))
        checkResponse(result, 200)
      }

      "getChildDetails returns 403 forbidden response when record was not found." in {
        mockHttpPostResponse(Status.FORBIDDEN, None)
        val result = await(connectorFixtures.nrsConnector.getChildDetails(nrsRequestPayloadWithoutBrn))
        checkResponse(result, 403)
      }

      "getChildDetails returns 503 (SERVICE_UNAVAILABLE) when NRS is down." in {
        mockHttpPostResponse(Status.SERVICE_UNAVAILABLE, None)
        val result = await(connectorFixtures.nrsConnector.getChildDetails(nrsRequestPayloadWithoutBrn))
        checkResponse(result, 503)
      }

      "getChildDetails returns http 500 when DES is offline" in {
        mockHttpPostResponse(Status.INTERNAL_SERVER_ERROR, None)
        val result = await(connectorFixtures.nrsConnector.getChildDetails(nrsRequestPayloadWithoutBrn))
        checkResponse(result, 500)
      }

    }

    "GRONIConnector" should {

      "initialise with correct properties" in {
        connectorFixtures.groniConnector.httpPost shouldBe a[WSPost]
      }

      "getReference returns http NotImplementedException" in {
        val future = connectorFixtures.groniConnector.getReference(payload)
        future.onComplete {
          case Failure(e) =>
            connectorFixtures.groniConnector.headers.isEmpty shouldBe true
            e.getMessage shouldBe "No getReference method available for GRONI connector."
          case Success(_) =>
            throw new Exception
        }
      }

      "getChildDetails returns http NotImplementedException" in {
        val future = connectorFixtures.groniConnector.getChildDetails(payloadNoReferenceNorthernIreland)
        future.onComplete {
          case Failure(e) =>
            connectorFixtures.groniConnector.headers.isEmpty shouldBe true
            e.getMessage shouldBe "No getChildDetails method available for GRONI connector."
          case Success(_) =>
            throw new Exception
        }
      }
    }
  }
}
