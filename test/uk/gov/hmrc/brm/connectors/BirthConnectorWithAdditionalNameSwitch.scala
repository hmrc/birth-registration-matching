/*
 * Copyright 2026 HM Revenue & Customs
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

import com.github.tomakehurst.wiremock.client.WireMock.*
import org.apache.pekko.http.scaladsl.model.StatusCodes.*
import org.scalatest.concurrent.PatienceConfiguration.Timeout
import org.scalatest.time.{Seconds, Span}
import uk.gov.hmrc.brm.models.brm.Payload
import uk.gov.hmrc.brm.utils.CommonConstant.*
import uk.gov.hmrc.brm.utils.TestHelper.*
import uk.gov.hmrc.brm.utils.{BirthRegisterCountry, WireMockHelperTrait}

import java.time.LocalDate

class BirthConnectorWithAdditionalNameSwitch extends WireMockHelperTrait {

  override val ignoreAdditionalNames: Boolean = false

  "GROConnector" when {

    "getChildDetails called" should {

      "pass additionalNames to gro" in {

        val payload = Payload(None, "Adam", Some("test"), "SMITH", dateOfBirth, BirthRegisterCountry.ENGLAND)

        server.stubFor(
          post(urlEqualTo(groDetailsUrl))
            .withRequestBody(matchingJsonPath(s"$$.$FORNAMES", equalTo("Adam test")))
            .withRequestBody(matchingJsonPath(s"$$.$LASTNAME", equalTo("SMITH")))
            .withRequestBody(matchingJsonPath(s"$$.$DATE_OF_BIRTH", equalTo("2009-07-01")))
            .willReturn(okJson(groResponseWithAdditionalName.toString()))
        )

        val result = groConnector.getChildDetails(payload).futureValue(Timeout(Span(3, Seconds)))
        checkResponse(result, OK.intValue)
      }

      "pass additionalNames to gro in proper format" in {

        val payload = Payload(None, " Adam ", Some(" test "), " SMITH ", dateOfBirth, BirthRegisterCountry.ENGLAND)

        server.stubFor(
          post(urlEqualTo(groDetailsUrl))
            .withRequestBody(matchingJsonPath(s"$$.$FORNAMES", equalTo("Adam test")))
            .withRequestBody(matchingJsonPath(s"$$.$LASTNAME", equalTo("SMITH")))
            .withRequestBody(matchingJsonPath(s"$$.$DATE_OF_BIRTH", equalTo("2009-07-01")))
            .willReturn(okJson(groResponseWithAdditionalName.toString()))
        )

        val result = groConnector.getChildDetails(payload).futureValue(Timeout(Span(3, Seconds)))
        checkResponse(result, OK.intValue)
      }

      "pass additionalNames to gro in proper format when multiple additional names are present" in {

        val payload =
          Payload(None, " Adam ", Some(" test    david "), " SMITH ", dateOfBirth, BirthRegisterCountry.ENGLAND)

        server.stubFor(
          post(urlEqualTo(groDetailsUrl))
            .withRequestBody(matchingJsonPath(s"$$.$FORNAMES", equalTo("Adam test david")))
            .withRequestBody(matchingJsonPath(s"$$.$LASTNAME", equalTo("SMITH")))
            .withRequestBody(matchingJsonPath(s"$$.$DATE_OF_BIRTH", equalTo("2009-07-01")))
            .willReturn(okJson(groResponseWithMoreAdditionalName.toString()))
        )

        val result = groConnector.getChildDetails(payload).futureValue(Timeout(Span(3, Seconds)))
        checkResponse(result, OK.intValue)
      }

      "pass only firstName when additionalNames value is empty" in {

        val payload = Payload(None, "Adam", None, "SMITH", dateOfBirth, BirthRegisterCountry.ENGLAND)

        server.stubFor(
          post(urlEqualTo(groDetailsUrl))
            .withRequestBody(matchingJsonPath(s"$$.$FORNAMES", equalTo("Adam")))
            .withRequestBody(matchingJsonPath(s"$$.$LASTNAME", equalTo("SMITH")))
            .withRequestBody(matchingJsonPath(s"$$.$DATE_OF_BIRTH", equalTo("2009-07-01")))
            .willReturn(okJson(groResponseWithoutAdditionalName.toString()))
        )

        val result = groConnector.getChildDetails(payload).futureValue(Timeout(Span(3, Seconds)))
        checkResponse(result, OK.intValue)
      }
    }
  }

  "NRSConnector" when {

    "getChildDetails called" should {

      "pass additionalNames to nrs" in {

        val requestWithAdditionalName =
          Payload(None, "Adam", Some("test"), "SMITH", LocalDate.of(2009, 11, 12), BirthRegisterCountry.SCOTLAND)

        server.stubFor(
          post(urlEqualTo(nrsUrl))
            .withRequestBody(matchingJsonPath(s"$$.$JSON_FIRSTNAME_PATH", equalTo("Adam test")))
            .withRequestBody(matchingJsonPath(s"$$.$JSON_LASTNAME_PATH", equalTo("SMITH")))
            .withRequestBody(matchingJsonPath(s"$$.$JSON_DATEOFBIRTH_PATH", equalTo("2009-11-12")))
            .willReturn(okJson(nrsJsonResponseObject.toString()))
        )

        val result = nrsConnector.getChildDetails(requestWithAdditionalName).futureValue(Timeout(Span(3, Seconds)))
        checkResponse(result, OK.intValue)
      }

      "pass additionalNames to nrs in proper format" in {

        val requestWithAdditionalName =
          Payload(None, " Adam ", Some(" test "), " SMITH ", LocalDate.of(2009, 11, 12), BirthRegisterCountry.SCOTLAND)

        server.stubFor(
          post(urlEqualTo(nrsUrl))
            .withRequestBody(matchingJsonPath(s"$$.$JSON_FIRSTNAME_PATH", equalTo("Adam test")))
            .withRequestBody(matchingJsonPath(s"$$.$JSON_LASTNAME_PATH", equalTo("SMITH")))
            .withRequestBody(matchingJsonPath(s"$$.$JSON_DATEOFBIRTH_PATH", equalTo("2009-11-12")))
            .willReturn(okJson(nrsJsonResponseObject.toString()))
        )

        val result = nrsConnector.getChildDetails(requestWithAdditionalName).futureValue(Timeout(Span(3, Seconds)))
        checkResponse(result, OK.intValue)
      }

      "pass additionalNames to nrs in proper format when multiple additional names are present" in {

        val payload =
          Payload(None, " Adam ", Some(" test    david "), " SMITH ", dateOfBirth, BirthRegisterCountry.SCOTLAND)

        server.stubFor(
          post(urlEqualTo(nrsUrl))
            .withRequestBody(matchingJsonPath(s"$$.$JSON_FIRSTNAME_PATH", equalTo("Adam test david")))
            .withRequestBody(matchingJsonPath(s"$$.$JSON_LASTNAME_PATH", equalTo("SMITH")))
            .withRequestBody(matchingJsonPath(s"$$.$JSON_DATEOFBIRTH_PATH", equalTo("2009-07-01")))
            .willReturn(okJson(nrsJsonResponseObject.toString()))
        )

        val result = nrsConnector.getChildDetails(payload).futureValue(Timeout(Span(3, Seconds)))
        checkResponse(result, OK.intValue)
      }

      "pass only firstName when additionalNames value is empty" in {

        val requestWithoutAdditionalName =
          Payload(None, "ANTHONY", None, "ANDREWS", LocalDate.of(2016, 11, 8), BirthRegisterCountry.SCOTLAND)

        server.stubFor(
          post(urlEqualTo(nrsUrl))
            .withRequestBody(matchingJsonPath(s"$$.$JSON_FIRSTNAME_PATH", equalTo("ANTHONY")))
            .withRequestBody(matchingJsonPath(s"$$.$JSON_LASTNAME_PATH", equalTo("ANDREWS")))
            .withRequestBody(matchingJsonPath(s"$$.$JSON_DATEOFBIRTH_PATH", equalTo("2016-11-08")))
            .willReturn(okJson(nrsJsonResponseObjectWithoutAdditionalName.toString()))
        )

        val result = nrsConnector.getChildDetails(requestWithoutAdditionalName).futureValue(Timeout(Span(3, Seconds)))
        checkResponse(result, OK.intValue)
      }
    }

  }

}
