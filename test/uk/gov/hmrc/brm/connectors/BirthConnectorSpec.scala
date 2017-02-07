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

import org.mockito.Matchers
import org.mockito.Matchers.{eq => mockEq}
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfter
import org.scalatest.mock.MockitoSugar
import play.api.http.Status
import play.api.libs.json.JsValue
import uk.gov.hmrc.brm.utils.JsonUtils
import uk.gov.hmrc.play.http._
import uk.gov.hmrc.play.http.ws.{WSGet, WSPost}
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future
import scala.util.{Failure, Success}

class BirthConnectorSpec extends UnitSpec with WithFakeApplication with MockitoSugar with BeforeAndAfter {

  import uk.gov.hmrc.brm.utils.TestHelper._

  import scala.concurrent.ExecutionContext.Implicits.global

  implicit val hc = HeaderCarrier()

  val mockHttpGet = mock[HttpGet]
  val mockHttpPost = mock[HttpPost]

  object MockBirthConnector extends BirthConnector {
    override val httpPost = mockHttpPost
    override val serviceUrl = ""
    override val baseUri = ""
    override val detailsUri = ""
    override val referenceUri = ""
  }

  val groJsonResponseObject = JsonUtils.getJsonFromFile("500035710")

  val childDetailPayload = Map(
    "firstName" -> "Adam",
    "lastName" -> "Wilson",
    "dateOfBirth" -> "2006-11-12"
  )

  before {
    reset(mockHttpGet)
    reset(mockHttpPost)
  }

  "BirthConnector" should {

    "getReference returns json response" in {
      when(mockHttpPost.POST[JsValue, HttpResponse](Matchers.any(), Matchers.any(), Matchers.any())
        (Matchers.any(),Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(HttpResponse(Status.OK, Some(groJsonResponseObject))))
      val result = await(MockBirthConnector.getReference(payload))
      result shouldBe a[HttpResponse]
      result.status shouldBe 200
    }

    "getReference returns http 500 when GRO is offline" in {
      when(mockHttpPost.POST[JsValue, HttpResponse](Matchers.any(), Matchers.any(), Matchers.any())
        (Matchers.any(),Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(HttpResponse(Status.INTERNAL_SERVER_ERROR, None)))
      val result = await(MockBirthConnector.getReference(payload))
      result shouldBe a[HttpResponse]
      result.status shouldBe 500
    }

    "getReference returns http 400 for BadRequest" in {
      when(mockHttpPost.POST[JsValue, HttpResponse](Matchers.any(), Matchers.any(), Matchers.any())
        (Matchers.any(),Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(HttpResponse(Status.BAD_REQUEST, None)))
      val result = await(MockBirthConnector.getReference(payload))
      result shouldBe a[HttpResponse]
      result.status shouldBe 400
    }

    "getReference returns http 404 when GRO has not found data" in {
      when(mockHttpPost.POST[JsValue, HttpResponse](Matchers.any(), Matchers.any(), Matchers.any())
        (Matchers.any(),Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(HttpResponse(Status.NOT_FOUND, None)))
      val result = await(MockBirthConnector.getReference(payload))
      result shouldBe a[HttpResponse]
      result.status shouldBe 404
    }

    "getChildDetails returns json response" in {
      when(mockHttpPost.POST[JsValue, HttpResponse](Matchers.any(), Matchers.any(), Matchers.any())
        (Matchers.any(),Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(HttpResponse(Status.OK, Some(groJsonResponseObject))))
      val result = await(MockBirthConnector.getChildDetails(payloadNoReference))
      result shouldBe a[HttpResponse]
      result.status shouldBe 200
    }

    "getChildDetails returns http 500 when GRO is offline" in {
      when(mockHttpPost.POST[JsValue, HttpResponse](Matchers.any(), Matchers.any(), Matchers.any())
        (Matchers.any(),Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(HttpResponse(Status.INTERNAL_SERVER_ERROR, None)))
      val result = await(MockBirthConnector.getChildDetails(payloadNoReference))
      result shouldBe a[HttpResponse]
      result.status shouldBe 500
    }

    "getChildDetails returns http 400 for BadRequest" in {
      when(mockHttpPost.POST[JsValue, HttpResponse](Matchers.any(), Matchers.any(), Matchers.any())
        (Matchers.any(),Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(HttpResponse(Status.BAD_REQUEST, None)))
      val result = await(MockBirthConnector.getChildDetails(payloadNoReference))
      result shouldBe a[HttpResponse]
      result.status shouldBe 400
    }

    "getChildDetails returns http 404 when GRO has not found data" in {
      when(mockHttpPost.POST[JsValue, HttpResponse](Matchers.any(), Matchers.any(), Matchers.any())
        (Matchers.any(),Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(HttpResponse(Status.NOT_FOUND, None)))
      val result = await(MockBirthConnector.getChildDetails(payloadNoReference))
      result shouldBe a[HttpResponse]
      result.status shouldBe 404
    }

    "GROEnglandConnector" should {

      "initialise with correct properties" in {
        GROConnector.httpPost shouldBe a[WSPost]
        GROConnector.detailsUri shouldBe "http://localhost:9006/birth-registration-matching-proxy/match/details"
      }

    }

    "NRSConnector" should {

      "initialise with correct properties" in {
        NrsConnector.httpPost shouldBe a[WSPost]
        NrsConnector.detailsUri shouldBe "/"
      }

      "getReference returns http NotImplementedException" in {
        val future = NrsConnector.getReference(payloadNoReferenceScotland)
        future.onComplete {
          case Failure(e) =>
            e.getMessage shouldBe "No getReference method available for NRS connector."
          case Success(_) =>
            throw new Exception
        }
      }

      "getChildDetails returns http NotImplementedException" in {
        val future = NrsConnector.getChildDetails(payloadNoReferenceScotland)
        future.onComplete {
          case Failure(e) =>
            e.getMessage shouldBe "No getChildDetails method available for NRS connector."
          case Success(_) =>
            throw new Exception
        }
      }

    }

    "GRONIConnector" should {

      "initialise with correct properties" in {
        NirsConnector.httpPost shouldBe a[WSPost]
        NirsConnector.detailsUri shouldBe "/"
      }

      "getReference returns http NotImplementedException" in {
        val future = NirsConnector.getReference(payloadNoReferenceNorthernIreland)
        future.onComplete {
          case Failure(e) =>
            e.getMessage shouldBe "No getReference method available for GRONI connector."
          case Success(_) =>
            throw new Exception
        }
      }

      "getChildDetails returns http NotImplementedException" in {
        val future = NirsConnector.getChildDetails(payloadNoReferenceNorthernIreland)
        future.onComplete {
          case Failure(e) =>
            e.getMessage shouldBe "No getChildDetails method available for GRONI connector."
          case Success(_) =>
            throw new Exception
        }
      }

    }

  }

}
