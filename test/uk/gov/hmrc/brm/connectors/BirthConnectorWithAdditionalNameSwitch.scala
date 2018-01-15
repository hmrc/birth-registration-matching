/*
 * Copyright 2018 HM Revenue & Customs
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
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.OneAppPerSuite
import play.api.http.Status
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.brm.models.brm.Payload
import uk.gov.hmrc.brm.utils.CommonConstant._
import uk.gov.hmrc.brm.utils.Mocks._
import uk.gov.hmrc.brm.utils.TestHelper._
import uk.gov.hmrc.brm.utils.{BaseUnitSpec, BirthRegisterCountry, JsonUtils}
import uk.gov.hmrc.play.http._
import uk.gov.hmrc.play.test.UnitSpec

class BirthConnectorWithAdditionalNameSwitch extends UnitSpec
  with OneAppPerSuite
  with MockitoSugar
  with BaseUnitSpec {

  implicit val hc = HeaderCarrier()

  val FORNAMES: String = "forenames"
  val LASTNAME: String = "lastname"
  val DATE_OF_BIRTH: String = "dateofbirth"

  val nrsJsonResponseObject = JsonUtils.getJsonFromFile("nrs", "2017734003")
  val nrsJsonResponseObjectWithotuAdditionalName = JsonUtils.getJsonFromFile("nrs", "2017350006")

  val config: Map[String, _] = Map(
    "microservice.services.birth-registration-matching.matching.ignoreAdditionalNames" -> false
  )

  override lazy val app = new GuiceApplicationBuilder()
    .configure(config)
    .build()

  "GROConnector" when {

    "getChildDetails called" should {

      "pass additionalNames to gro" in {
        val argumentCapture = mockHttpPostResponse(Status.OK, Some(groResponseWithAdditionalName))
        val payload = Payload(None, "Adam", Some("test"), "SMITH", new LocalDate("2009-07-01"),
          BirthRegisterCountry.ENGLAND)
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
        val argumentCapture = mockHttpPostResponse(Status.OK, Some(nrsJsonResponseObjectWithotuAdditionalName))
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
