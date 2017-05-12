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
import org.scalatest.mock.MockitoSugar
import org.scalatest.{BeforeAndAfter, TestData}
import org.scalatestplus.play.OneAppPerTest
import play.api.http.Status
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers._
import uk.gov.hmrc.brm.models.brm.Payload
import uk.gov.hmrc.brm.utils.Mocks._
import uk.gov.hmrc.brm.utils.{BaseUnitSpec, BirthRegisterCountry, JsonUtils}
import uk.gov.hmrc.play.http._
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.brm.utils.CommonConstant._

class BirthConnectorWithAdditionalNameSwitch extends UnitSpec with OneAppPerTest with MockitoSugar with BeforeAndAfter with BaseUnitSpec {

  import uk.gov.hmrc.brm.utils.TestHelper._

  implicit val hc = HeaderCarrier()

  val nrsJsonResponseObject = JsonUtils.getJsonFromFile("nrs", "2017734003")
  val nrsJsonResponseObjectWithotuAdditionalName = JsonUtils.getJsonFromFile("nrs", "2017350006")

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

      (argumentCapture.value \ "forenames").as[String] shouldBe "Adam test"
      (argumentCapture.value \ "lastname").as[String] shouldBe "SMITH"
      (argumentCapture.value \ "dateofbirth").as[String] shouldBe "2009-07-01"
    }

    "getChildDetails call pass additional name to gro in proper format." in {
      val argumentCapture = mockHttpPostResponse(Status.OK, Some(groResponseWithAdditionalName))
      val payload = Payload(None, " Adam ", Some(" test "), " SMITH ", new LocalDate("2009-07-01"),
        BirthRegisterCountry.ENGLAND)
      val result = await(connectorFixtures.groConnector.getChildDetails(payload))
      checkResponse(result, 200)

      (argumentCapture.value \ "forenames").as[String] shouldBe "Adam test"
      (argumentCapture.value \ "lastname").as[String] shouldBe "SMITH"
      (argumentCapture.value \ "dateofbirth").as[String] shouldBe "2009-07-01"
    }

    "getChildDetails call pass additional name to gro in proper format when multiple additional name are present." in {
      val argumentCapture = mockHttpPostResponse(Status.OK, Some(groResponseWithMoreAdditionalName))
      val payload = Payload(None, " Adam ", Some(" test david "), " SMITH ", new LocalDate("2009-07-01"),
        BirthRegisterCountry.ENGLAND)
      val result = await(connectorFixtures.groConnector.getChildDetails(payload))
      checkResponse(result, 200)

      (argumentCapture.value \ "forenames").as[String] shouldBe "Adam test david"
      (argumentCapture.value \ "lastname").as[String] shouldBe "SMITH"
      (argumentCapture.value \ "dateofbirth").as[String] shouldBe "2009-07-01"
    }

    "getChildDetails call to gro should pass only firstname when additionalName value is empty" in {
      val argumentCapture = mockHttpPostResponse(Status.OK, Some(groResponseWithoutAdditionalName))
      val payload = Payload(None, "Adam", None, "SMITH", new LocalDate("2009-07-01"),
        BirthRegisterCountry.ENGLAND)
      val result = await(connectorFixtures.groConnector.getChildDetails(payload))
      checkResponse(result, 200)
      (argumentCapture.value \ "forenames").as[String] shouldBe "Adam"
      (argumentCapture.value \ "lastname").as[String] shouldBe "SMITH"
      (argumentCapture.value \ "dateofbirth").as[String] shouldBe "2009-07-01"
    }


    "NRSConnector" should {
      "getChildDetails call pass additional name to nrs." in {
        val argumentCapture = mockHttpPostResponse(Status.OK, Some(nrsJsonResponseObject))
        val requestWithAdditionalName = Payload(None, "Adam", Some("test"), "SMITH", new LocalDate("2009-11-12"),
          BirthRegisterCountry.SCOTLAND)
        val result = await(connectorFixtures.nrsConnector.getChildDetails(requestWithAdditionalName))
        checkResponse(result, 200)
        (argumentCapture.value \ JSON_FIRSTNAME_PATH).as[String] shouldBe "Adam test"
        (argumentCapture.value \ JSON_LASTNAME_PATH).as[String] shouldBe "SMITH"
        (argumentCapture.value \ JSON_DATEOFBIRTH_PATH).as[String] shouldBe "2009-11-12"

      }

      "getChildDetails call pass additional name to nrs in proper format." in {
        val argumentCapture = mockHttpPostResponse(Status.OK, Some(nrsJsonResponseObject))
        val requestWithAdditionalName = Payload(None, " Adam ", Some(" test "), " SMITH ", new LocalDate("2009-11-12"),
          BirthRegisterCountry.SCOTLAND)
        val result = await(connectorFixtures.nrsConnector.getChildDetails(requestWithAdditionalName))
        checkResponse(result, 200)
        (argumentCapture.value \ JSON_FIRSTNAME_PATH).as[String] shouldBe "Adam test"
        (argumentCapture.value \ JSON_LASTNAME_PATH).as[String] shouldBe "SMITH"
        (argumentCapture.value \ JSON_DATEOFBIRTH_PATH).as[String] shouldBe "2009-11-12"

      }

      "getChildDetails call to nrs pass only firstname when additionalName value is empty" in {
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
