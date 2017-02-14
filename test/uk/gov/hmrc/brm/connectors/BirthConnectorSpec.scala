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
import uk.gov.hmrc.play.http.ws.WSPost
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future
import scala.util.{Failure, Success}

class BirthConnectorSpec extends UnitSpec with WithFakeApplication with MockitoSugar with BeforeAndAfter {

  import uk.gov.hmrc.brm.utils.TestHelper._

  import scala.concurrent.ExecutionContext.Implicits.global

  implicit val hc = HeaderCarrier()

  val mockHttpGet = mock[HttpGet]
  val mockHttpPost = mock[HttpPost]

  val groJsonResponseObject = JsonUtils.getJsonFromFile("500035710")

  val childDetailPayload = Map(
    "firstName" -> "Adam",
    "lastName" -> "Wilson",
    "dateOfBirth" -> "2006-11-12"
  )

  def fixture = {
    new {
      val groConnector = new GROConnector(mockHttpPost)
      val nrsConnector = new NRSConnector()
      val groniConnector = new GRONIConnector()
    }
  }

  "BirthConnector" should {

    "getReference returns json response" in {
      when(mockHttpPost.POST[JsValue, HttpResponse](Matchers.any(), Matchers.any(), Matchers.any())
        (Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(HttpResponse(Status.OK, Some(groJsonResponseObject))))
      val result = await(fixture.groConnector.getReference(payload))
      result shouldBe a[HttpResponse]
      result.status shouldBe 200
    }

    "getReference returns http 500 when GRO is offline" in {
      when(mockHttpPost.POST[JsValue, HttpResponse](Matchers.any(), Matchers.any(), Matchers.any())
        (Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(HttpResponse(Status.INTERNAL_SERVER_ERROR, None)))
      val result = await(fixture.groConnector.getReference(payload))
      result shouldBe a[HttpResponse]
      result.status shouldBe 500
    }

    "getReference returns http 400 for BadRequest" in {
      when(mockHttpPost.POST[JsValue, HttpResponse](Matchers.any(), Matchers.any(), Matchers.any())
        (Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(HttpResponse(Status.BAD_REQUEST, None)))
      val result = await(fixture.groConnector.getReference(payload))
      result shouldBe a[HttpResponse]
      result.status shouldBe 400
    }

    "getReference returns http 404 when GRO has not found data" in {
      when(mockHttpPost.POST[JsValue, HttpResponse](Matchers.any(), Matchers.any(), Matchers.any())
        (Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(HttpResponse(Status.NOT_FOUND, None)))
      val result = await(fixture.groConnector.getReference(payload))
      result shouldBe a[HttpResponse]
      result.status shouldBe 404
    }

    "getChildDetails returns json response" in {
      when(mockHttpPost.POST[JsValue, HttpResponse](Matchers.any(), Matchers.any(), Matchers.any())
        (Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(HttpResponse(Status.OK, Some(groJsonResponseObject))))
      val result = await(fixture.groConnector.getChildDetails(payloadNoReference))
      result shouldBe a[HttpResponse]
      result.status shouldBe 200
    }

    "getChildDetails returns http 500 when GRO is offline" in {
      when(mockHttpPost.POST[JsValue, HttpResponse](Matchers.any(), Matchers.any(), Matchers.any())
        (Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(HttpResponse(Status.INTERNAL_SERVER_ERROR, None)))
      val result = await(fixture.groConnector.getChildDetails(payloadNoReference))
      result shouldBe a[HttpResponse]
      result.status shouldBe 500
    }

    "getChildDetails returns http 400 for BadRequest" in {
      when(mockHttpPost.POST[JsValue, HttpResponse](Matchers.any(), Matchers.any(), Matchers.any())
        (Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(HttpResponse(Status.BAD_REQUEST, None)))
      val result = await(fixture.groConnector.getChildDetails(payloadNoReference))
      result shouldBe a[HttpResponse]
      result.status shouldBe 400
    }

    "getChildDetails returns http 404 when GRO has not found data" in {
      when(mockHttpPost.POST[JsValue, HttpResponse](Matchers.any(), Matchers.any(), Matchers.any())
        (Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(HttpResponse(Status.NOT_FOUND, None)))
      val result = await(fixture.groConnector.getChildDetails(payloadNoReference))
      result shouldBe a[HttpResponse]
      result.status shouldBe 404
    }

    "NRSConnector" should {

      "initialise with correct properties" in {
        fixture.nrsConnector.httpPost shouldBe a[WSPost]
      }

      "getReference returns http NotImplementedException" in {
        val future = fixture.nrsConnector.getReference(payload)
        future.onComplete {
          case Failure(e) =>
            e.getMessage shouldBe "No getReference method available for NRS connector."
          case Success(_) =>
            throw new Exception
        }
      }

      "getChildDetails returns http NotImplementedException" in {
        val future = fixture.nrsConnector.getChildDetails(payloadNoReferenceScotland)
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
        fixture.groniConnector.httpPost shouldBe a[WSPost]
      }

      "getReference returns http NotImplementedException" in {
        val future = fixture.groniConnector.getReference(payload)
        future.onComplete {
          case Failure(e) =>
            e.getMessage shouldBe "No getReference method available for GRONI connector."
          case Success(_) =>
            throw new Exception
        }
      }

      "getChildDetails returns http NotImplementedException" in {
        val future = fixture.groniConnector.getChildDetails(payloadNoReferenceNorthernIreland)
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
