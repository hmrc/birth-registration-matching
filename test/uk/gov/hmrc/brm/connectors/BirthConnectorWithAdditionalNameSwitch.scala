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

import org.joda.time.LocalDate
import org.mockito.Matchers.{eq => mockEq}
import org.scalatest.{BeforeAndAfter, TestData}
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.OneAppPerTest
import play.api.http.Status
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import uk.gov.hmrc.brm.models.brm.Payload
import uk.gov.hmrc.brm.utils.Mocks._
import uk.gov.hmrc.brm.utils.{BaseUnitSpec, BirthRegisterCountry, JsonUtils}
import uk.gov.hmrc.play.http._
import uk.gov.hmrc.play.http.ws.WSPost
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.util.{Failure, Success}

class BirthConnectorWithAdditionalNameSwitch extends UnitSpec with OneAppPerTest with MockitoSugar with BeforeAndAfter with BaseUnitSpec {

  import uk.gov.hmrc.brm.utils.TestHelper._

  import scala.concurrent.ExecutionContext.Implicits.global

  implicit val hc = HeaderCarrier()


  val config: Map[String, _] = Map(
    //dont ignore additional name values.
    "microservice.services.birth-registration-matching.features.additionalNames.ignore.enabled" -> false,
    //while matching dont ignore middle names.s
    "microservice.services.birth-registration-matching.matching.ignoreMiddleNames" -> false
  )

  override def newAppForTest(testData: TestData) = new GuiceApplicationBuilder().configure(
    config
  ).build()


  "GROConnector" should {

    "getChildDetails call pass additional name to gro." in {
      val argumentCapture = mockHttpPostResponse(Status.OK, Some(groResponseWithAdditionalName))
      val payload = Payload(None, "Adam", Some("test"), "SMITH", new LocalDate("2009-07-01"),
        BirthRegisterCountry.ENGLAND)
      val result = await(connectorFixtures.groConnector.getChildDetails(payload))
      checkResponse(result, 200)

      argumentCapture.value.toString.contains("Adam test") shouldBe true
      argumentCapture.value.toString.contains("SMITH") shouldBe true
      argumentCapture.value.toString.contains("2009-07-01") shouldBe true
    }

    "getChildDetails call to gro should not pass additionalName values when empty" in {
      val argumentCapture = mockHttpPostResponse(Status.OK, Some(groResponseWithAdditionalName))
      val payload = Payload(None, "Adam", None, "SMITH", new LocalDate("2009-07-01"),
        BirthRegisterCountry.ENGLAND)
      val result = await(connectorFixtures.groConnector.getChildDetails(payload))
      checkResponse(result, 200)
      argumentCapture.value.toString.contains("Adam") shouldBe true
      argumentCapture.value.toString.contains("SMITH") shouldBe true
      argumentCapture.value.toString.contains("2009-07-01") shouldBe true
    }



    /*"NRSConnector" should {

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

    }*/

  }

}
