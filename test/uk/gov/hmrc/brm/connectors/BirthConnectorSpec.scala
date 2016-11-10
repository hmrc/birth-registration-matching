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

import org.joda.time.LocalDate
import org.mockito.Matchers
import org.mockito.Matchers.{eq => mockEq}
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfter
import org.scalatest.mock.MockitoSugar
import play.api.http.Status
import uk.gov.hmrc.brm.models.brm.Payload
import uk.gov.hmrc.brm.utils.{BirthRegisterCountry, JsonUtils}
import uk.gov.hmrc.play.http._
import uk.gov.hmrc.play.http.ws.WSGet
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future
import scala.util.{Failure, Success}

class BirthConnectorSpec extends UnitSpec with WithFakeApplication with MockitoSugar with BeforeAndAfter {

  import scala.concurrent.ExecutionContext.Implicits.global

  /**
    * - Should
    * - getReference returns json response
    * - getChildDetails returns json response
    * - getReference returns http 500 when GRO is offline
    * - getReference returns http 400 for BadRequest
    */

  implicit val hc = HeaderCarrier()

  val mockHttpGet = mock[HttpGet]

  object MockBirthConnector extends BirthConnector {
    override val httpGet = mockHttpGet
    override val serviceUrl = ""
    override val baseUri = ""
    override val detailsUri = ""
  }

  val groJsonResponseObject = JsonUtils.getJsonFromFile("500035710")

  val childDetailPayload = Map(
    "firstName" -> "Adam",
    "lastName" -> "Wilson",
    "dateOfBirth" -> "2006-11-12"
  )

  val payload = Payload(Some("500035710"), "Adam", "Wilson", new LocalDate("2006-11-12"), BirthRegisterCountry.ENGLAND)

  val payloadNoReference = Payload(None, "Adam", "Wilson", new LocalDate("2006-11-12"), BirthRegisterCountry.ENGLAND)

  before {
    reset(mockHttpGet)
  }

  "BirthConnector" should {

    "getReference returns json response" in {
      when(mockHttpGet.GET[HttpResponse](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(HttpResponse(Status.OK, Some(groJsonResponseObject))))
      val result = await(MockBirthConnector.getReference(payload))
      result shouldBe a[HttpResponse]
      result.status shouldBe 200
    }

    "getChildDetails returns json response" in {
      when(mockHttpGet.GET[HttpResponse](Matchers.any())(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(HttpResponse(Status.OK, Some(groJsonResponseObject))))
      val result = await(MockBirthConnector.getChildDetails(payloadNoReference))
      result shouldBe a[HttpResponse]
      result.status shouldBe 200
    }

    "getReference returns http 500 when GRO is offline" in {
      when(mockHttpGet.GET[HttpResponse](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(HttpResponse(Status.INTERNAL_SERVER_ERROR, None)))
      val result = await(MockBirthConnector.getReference(payload))
      result shouldBe a[HttpResponse]
      result.status shouldBe 500
    }

    "getReference returns http 400 for BadRequest" in {

      when(mockHttpGet.GET[HttpResponse](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(HttpResponse(Status.BAD_REQUEST, None)))
        val result = await(MockBirthConnector.getReference(payload))
        result shouldBe a[HttpResponse]
        result.status shouldBe 400
    }

    "getReference returns http 400 when GRO is not found data" in {
      when(mockHttpGet.GET[HttpResponse](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(HttpResponse(Status.NOT_FOUND, None)))
        val result = await(MockBirthConnector.getReference(payload))
        result shouldBe a[HttpResponse]
        result.status shouldBe 404
    }
  }

  "GROEnglandConnector" should {

    "initialise with correct properties" in {
      GROEnglandConnector.httpGet shouldBe a[WSGet]
      GROEnglandConnector.detailsUri shouldBe "http://localhost:9006/birth-registration-matching-proxy/match"
    }

  }

  "NRSConnector" should {

    "initialise with correct properties" in {
      NrsConnector.httpGet shouldBe a[WSGet]
      NrsConnector.detailsUri shouldBe "/"
    }

    "getReference returns http NotImplementedException" in {
      val future = NrsConnector.getReference(payload)
      future.onComplete {
        case Failure(e) =>
          e.getMessage shouldBe "No service available for NRS connector."
        case Success(_) =>
          throw new Exception
      }
    }

  }

  "GRONIConnector" should {

    "initialise with correct properties" in {
      NirsConnector.httpGet shouldBe a[WSGet]
      NirsConnector.detailsUri shouldBe "/"
    }

    "getReference returns http NotImplementedException" in {
      val future = NirsConnector.getReference(payload)
      future.onComplete {
        case Failure(e) =>
          e.getMessage shouldBe "No service available for GRONI connector."
        case Success(_) =>
          throw new Exception
      }
    }

  }

}
