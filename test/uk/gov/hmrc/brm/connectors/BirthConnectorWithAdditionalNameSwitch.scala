/*
 * Copyright 2020 HM Revenue & Customs
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
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import org.specs2.mock.mockito.ArgumentCapture
import play.api.Application
import play.api.http.Status
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.JsValue
import uk.gov.hmrc.brm.models.brm.Payload
import uk.gov.hmrc.brm.utils.CommonConstant._
import uk.gov.hmrc.brm.utils.Mocks._
import uk.gov.hmrc.brm.utils.TestHelper._
import uk.gov.hmrc.brm.utils.{BaseUnitSpec, BirthRegisterCountry, JsonUtils}
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.Future

class BirthConnectorWithAdditionalNameSwitch extends UnitSpec with GuiceOneAppPerSuite with MockitoSugar with BaseUnitSpec {

  implicit val hc: HeaderCarrier = HeaderCarrier()

  val FORNAMES: String = "forenames"
  val LASTNAME: String = "lastname"
  val DATE_OF_BIRTH: String = "dateofbirth"

  val nrsJsonResponseObject: JsValue = JsonUtils.getJsonFromFile("nrs", "2017734003")
  val nrsJsonResponseObjectWithoutAdditionalName: JsValue = JsonUtils.getJsonFromFile("nrs", "2017350006")

  val config: Map[String, _] = Map(
    "microservice.services.birth-registration-matching.matching.ignoreAdditionalNames" -> false
  )

  override lazy val app: Application = new GuiceApplicationBuilder()
    .configure(config)
    .build()

  "GROConnector" when {

    "getChildDetails called" should {

      "pass additionalNames to gro" in {
        when(mockConfig.serviceUrl)
          .thenReturn("test")
        when(mockConfig.desUrl)
          .thenReturn("test")
        when(mockCommonUtil.forenames(any(), any()))
          .thenReturn("Adam test")

        val argumentCapture = mockHttpPostResponse(Status.OK, Some(groResponseWithAdditionalName))

        val payload = Payload(None, "Adam", Some("test"), "SMITH", new LocalDate("2009-07-01"), BirthRegisterCountry.ENGLAND)
        val result = await(connectorFixtures.groConnector.getChildDetails(payload))
        checkResponse(result, 200)

        (argumentCapture.value \ FORNAMES).as[String] shouldBe "Adam test"
        (argumentCapture.value \ LASTNAME).as[String] shouldBe "SMITH"
        (argumentCapture.value \ DATE_OF_BIRTH).as[String] shouldBe "2009-07-01"
      }

      "pass additionalNames to gro in proper format" in {
        val argumentCapture = mockHttpPostResponse(Status.OK, Some(groResponseWithAdditionalName))
        val payload = Payload(None, " Adam ", Some(" test "), " SMITH ", new LocalDate("2009-07-01"),
          BirthRegisterCountry.ENGLAND)
        val result = await(connectorFixtures.groConnector.getChildDetails(payload))
        checkResponse(result, 200)

        (argumentCapture.value \ FORNAMES).as[String] shouldBe "Adam test"
        (argumentCapture.value \ LASTNAME).as[String] shouldBe "SMITH"
        (argumentCapture.value \ DATE_OF_BIRTH).as[String] shouldBe "2009-07-01"
      }

      "pass additionalNames to gro in proper format when multiple additional names are present" in {
        when(mockCommonUtil.forenames(any(), any()))
          .thenReturn("Adam test david")

        val argumentCapture = mockHttpPostResponse(Status.OK, Some(groResponseWithMoreAdditionalName))
        val payload = Payload(None, " Adam ", Some(" test    david "), " SMITH ", new LocalDate("2009-07-01"),
          BirthRegisterCountry.ENGLAND)
        val result = await(connectorFixtures.groConnector.getChildDetails(payload))
        checkResponse(result, 200)

        (argumentCapture.value \ FORNAMES).as[String] shouldBe "Adam test david"
        (argumentCapture.value \ LASTNAME).as[String] shouldBe "SMITH"
        (argumentCapture.value \ DATE_OF_BIRTH).as[String] shouldBe "2009-07-01"
      }

      "pass only firstName when additionalNames value is empty" in {
        when(mockCommonUtil.forenames(any(), any()))
          .thenReturn("Adam")

        val argumentCapture = mockHttpPostResponse(Status.OK, Some(groResponseWithoutAdditionalName))
        val payload = Payload(None, "Adam", None, "SMITH", new LocalDate("2009-07-01"),
          BirthRegisterCountry.ENGLAND)
        val result = await(connectorFixtures.groConnector.getChildDetails(payload))
        checkResponse(result, 200)
        (argumentCapture.value \ FORNAMES).as[String] shouldBe "Adam"
        (argumentCapture.value \ LASTNAME).as[String] shouldBe "SMITH"
        (argumentCapture.value \ DATE_OF_BIRTH).as[String] shouldBe "2009-07-01"
      }
    }

  }

  "NRSConnector" when {

    "getChildDetails called" should {
      "pass additionalNames to nrs" in {
        when(mockCommonUtil.forenames(any(), any()))
          .thenReturn("Adam test")

        val argumentCapture = mockHttpPostResponse(Status.OK, Some(nrsJsonResponseObject))
        val requestWithAdditionalName = Payload(None, "Adam", Some("test"), "SMITH", new LocalDate("2009-11-12"),
          BirthRegisterCountry.SCOTLAND)
        val result = await(connectorFixtures.nrsConnector.getChildDetails(requestWithAdditionalName))
        checkResponse(result, 200)
        (argumentCapture.value \ JSON_FIRSTNAME_PATH).as[String] shouldBe "Adam test"
        (argumentCapture.value \ JSON_LASTNAME_PATH).as[String] shouldBe "SMITH"
        (argumentCapture.value \ JSON_DATEOFBIRTH_PATH).as[String] shouldBe "2009-11-12"

      }

      "pass additionalNames to nrs in proper format" in {
        val argumentCapture = mockHttpPostResponse(Status.OK, Some(nrsJsonResponseObject))
        val requestWithAdditionalName = Payload(None, " Adam ", Some(" test "), " SMITH ", new LocalDate("2009-11-12"),
          BirthRegisterCountry.SCOTLAND)
        val result = await(connectorFixtures.nrsConnector.getChildDetails(requestWithAdditionalName))
        checkResponse(result, 200)
        (argumentCapture.value \ JSON_FIRSTNAME_PATH).as[String] shouldBe "Adam test"
        (argumentCapture.value \ JSON_LASTNAME_PATH).as[String] shouldBe "SMITH"
        (argumentCapture.value \ JSON_DATEOFBIRTH_PATH).as[String] shouldBe "2009-11-12"

      }

      "pass additionalNames to gro in proper format when multiple additional names are present" in {
        when(mockCommonUtil.forenames(any(), any()))
          .thenReturn("Adam test david")

        val argumentCapture = mockHttpPostResponse(Status.OK, Some(nrsJsonResponseObject))
        val payload = Payload(None, " Adam ", Some(" test    david "), " SMITH ", new LocalDate("2009-07-01"),
          BirthRegisterCountry.SCOTLAND)
        val result = await(connectorFixtures.nrsConnector.getChildDetails(payload))
        checkResponse(result, 200)

        (argumentCapture.value \ JSON_FIRSTNAME_PATH).as[String] shouldBe "Adam test david"
        (argumentCapture.value \ JSON_LASTNAME_PATH).as[String] shouldBe "SMITH"
        (argumentCapture.value \ JSON_DATEOFBIRTH_PATH).as[String] shouldBe "2009-07-01"
      }

      "pass only firstName when additionalNames value is empty" in {
        when(mockCommonUtil.forenames(any(), any()))
          .thenReturn("ANTHONY")

        val argumentCapture = mockHttpPostResponse(Status.OK, Some(nrsJsonResponseObjectWithoutAdditionalName))
        val requestWithoutAdditionalName = Payload(None, "ANTHONY", None, "ANDREWS", new LocalDate("2016-11-08"),
          BirthRegisterCountry.SCOTLAND)
        val result = await(connectorFixtures.nrsConnector.getChildDetails(requestWithoutAdditionalName))
        checkResponse(result, 200)
        (argumentCapture.value \ JSON_FIRSTNAME_PATH).as[String] shouldBe "ANTHONY"
        (argumentCapture.value \ JSON_LASTNAME_PATH).as[String] shouldBe "ANDREWS"
        (argumentCapture.value \ JSON_DATEOFBIRTH_PATH).as[String] shouldBe "2016-11-08"

      }

    }

  }

}
