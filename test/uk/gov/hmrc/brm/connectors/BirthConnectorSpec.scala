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

import org.mockito.Matchers
import org.mockito.Matchers.{eq => mockEq}
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfter
import org.scalatest.mock.MockitoSugar
import play.api.http.Status
import play.api.libs.json.JsValue
import play.api.test.FakeApplication
import uk.gov.hmrc.brm.utils.JsonUtils
import uk.gov.hmrc.play.http._
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future

/**
  * Created by chrisianson on 01/08/16.
  */
class BirthConnectorSpec extends UnitSpec with WithFakeApplication with MockitoSugar with BeforeAndAfter {

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
  }

  object MockBirthConnectorInvalidUsername extends BirthConnector {
    override val httpGet = mockHttpGet
  }

  val groJsonResponseObject = JsonUtils.getJsonFromFile("500035710")

  val childDetailPayload = Map(
    "firstName" -> "Adam",
    "lastName" -> "Wilson",
    "dateOfBirth" -> "2006-11-12"
  )

  before {
    reset(mockHttpGet)
  }

  "BirthConnector" should {

    "getReference returns json response" in {

      when(mockHttpGet.GET[HttpResponse](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(HttpResponse(Status.OK, Some(groJsonResponseObject))))
      val result = await(MockBirthConnector.getReference("500035710"))
      result shouldBe a[JsValue]
    }

    "getChildDetails returns json response" in {

      when(mockHttpGet.GET[HttpResponse](Matchers.any())(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(HttpResponse(Status.OK, Some(groJsonResponseObject))))
      val result = await(MockBirthConnector.getChildDetails(childDetailPayload))
      result shouldBe a[JsValue]
    }

    "getReference returns http 500 when GRO is offline" in {

      when(mockHttpGet.GET[HttpResponse](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(HttpResponse(Status.INTERNAL_SERVER_ERROR, None)))
      intercept[Upstream5xxResponse] {
        val result = await(MockBirthConnector.getReference("50003570"))
        result shouldBe a[JsValue]
      }
    }

    "getReference returns http 400 for BadRequest" in {

      when(mockHttpGet.GET[HttpResponse](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(HttpResponse(Status.BAD_REQUEST, None)))
      intercept[Upstream4xxResponse] {
        val result = await(MockBirthConnector.getReference("50003570"))
        result shouldBe a[JsValue]
      }
    }
  }
}
