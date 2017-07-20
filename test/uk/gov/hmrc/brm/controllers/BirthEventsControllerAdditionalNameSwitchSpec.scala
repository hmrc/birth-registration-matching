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

package uk.gov.hmrc.brm.controllers

import org.joda.time.LocalDate
import org.scalatest.TestData
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.OneAppPerTest
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import play.api.test.Helpers._
import uk.gov.hmrc.brm.models.brm.Payload
import uk.gov.hmrc.brm.utils.{BaseUnitSpec, BirthRegisterCountry, MockErrorResponses}
import uk.gov.hmrc.brm.utils.Mocks._
import uk.gov.hmrc.play.test.UnitSpec

class BirthEventsControllerAdditionalNameSwitchSpec extends UnitSpec with OneAppPerTest with MockitoSugar with BaseUnitSpec {

  import uk.gov.hmrc.brm.utils.TestHelper._

  val config: Map[String, _] = Map(
    "microservice.services.birth-registration-matching.matching.ignoreAdditionalNames" -> false
  )

  override def newAppForTest(testData: TestData) = new GuiceApplicationBuilder()
    .disable[com.kenshoo.play.metrics.PlayModule]
    .configure(config)
    .build()

  "validating match when feature to ignore additional name is false." should {

    "return matched value of true when reference request has additional names and record has same value" in {
      mockReferenceResponse(groResponseWithAdditionalName)
      val payload = Json.toJson(Payload(Some("500035710"), "Adam", Some("test"), "SMITH", new LocalDate("2009-07-01"),
        BirthRegisterCountry.ENGLAND))
      val result = makeRequest(payload)
      checkResponse(result, OK, true)
    }

    "return matched value of true when reference request has  more than one additional names and record has same value" in {
      mockReferenceResponse(groResponseWithMoreAdditionalName)
      val payload = Json.toJson(Payload(Some("500035712"), "Adam", Some("test david"), "SMITH", new LocalDate("2009-07-01"),
        BirthRegisterCountry.ENGLAND))
      val result = makeRequest(payload)
      checkResponse(result, OK, true)
    }

    "return matched value of true when reference request has  more than one additional names with space and record has same value without space" in {
      mockReferenceResponse(groResponseWithMoreAdditionalName)
      val payload = Json.toJson(Payload(Some("500035712"), "Adam", Some(" test david "), "SMITH", new LocalDate("2009-07-01"),
        BirthRegisterCountry.ENGLAND))
      val result = makeRequest(payload)
      checkResponse(result, OK, true)
    }

    "return matched value of false when reference request has additional names and record does not have middle name in it." in {
      mockReferenceResponse(groResponseWithoutAdditionalName)
      val payload = Json.toJson(Payload(Some("500035711"), "Adam", Some("test"), "SMITH", new LocalDate("2009-07-01"),
        BirthRegisterCountry.ENGLAND))
      val result = makeRequest(payload)
      checkResponse(result, OK, false)
    }


    "return matched value of true when user does not provide additional name and record also does not have it " in {

      mockReferenceResponse(groResponseWithoutAdditionalName)
      val payload = Json.toJson(Payload(Some("500035711"), "Adam", None, "SMITH", new LocalDate("2009-07-01"),
        BirthRegisterCountry.ENGLAND))
      val result = makeRequest(payload)
      checkResponse(result, OK, true)
    }

    "return matched value of false when user provide additional name and record does not have it " in {

      mockReferenceResponse(groResponseWithoutAdditionalName)
      val payload = Json.toJson(Payload(Some("500035711"), "Adam", Some("test"), "SMITH", new LocalDate("2009-07-01"),
        BirthRegisterCountry.ENGLAND))
      val result = makeRequest(payload)
      checkResponse(result, OK, false)
    }


    "return matched value of true when reference request has additional names with special character and record has same value" in {
      mockReferenceResponse(groResponseWithSpecialCharacter)
      val payload = Json.toJson(Payload(Some("500035713"), "Mary-Ann ", Some("O'Leary"), "Smith-Johnson", new LocalDate("2009-07-01"),
        BirthRegisterCountry.ENGLAND))
      val result = makeRequest(payload)
      checkResponse(result, OK, true)
    }

    "return matched value of true when reference request firstname has additional name with special character and record has same value" in {
      mockReferenceResponse(groResponseWithSpecialCharacter)
      val payload = Json.toJson(Payload(Some("500035713"), "Mary-Ann O'Leary ",None, "Smith-Johnson", new LocalDate("2009-07-01"),
        BirthRegisterCountry.ENGLAND))
      val result = makeRequest(payload)
      checkResponse(result, OK, true)
    }


    "return matched value of true when reference request firstname and additional names has more space seprated names and record has same name on it." in {
      mockReferenceResponse(groResponse500036682)
      val payload = Json.toJson(Payload(Some("500036682"), "Ivor Test Hywel Tom Jones ",Some("Welcome In The Valleys Grand Slam"), "WILLIAMS JONES", new LocalDate("2009-11-23"),
        BirthRegisterCountry.ENGLAND))
      val result = makeRequest(payload)
      checkResponse(result, OK, true)
    }

    "return matched value of false when reference request firstname has multiple names ,no additional name and record has addiional name on it.." in {
      mockReferenceResponse(groResponse500036682)
      val payload = Json.toJson(Payload(Some("500036682"), "Ivor Test Hywel Tom Jones ",None, "WILLIAMS JONES", new LocalDate("2009-11-23"),
        BirthRegisterCountry.ENGLAND))
      val result = makeRequest(payload)
      checkResponse(result, OK, false)
    }

    "return matched value of false when reference request firstname has multiple names and  additional name and record does not have same name on it." in {
      mockReferenceResponse(groResponse500036682)
      val payload = Json.toJson(Payload(Some("500036682"), "Ivor Test Hywel Tom Jones ",Some("Welcome In The Valleys Grand"), "WILLIAMS JONES", new LocalDate("2009-11-23"),
        BirthRegisterCountry.ENGLAND))
      val result = makeRequest(payload)
      checkResponse(result, OK, false)
    }

    //details
    "return matched value of true when detail request has additional names and record has same value" in {
      mockDetailsResponse(groResponseWithAdditionalName)
      val payload = Json.toJson(Payload(None, "Adam", Some("test"), "SMITH", new LocalDate("2009-07-01"),
        BirthRegisterCountry.ENGLAND))
      val result = makeRequest(payload)
      checkResponse(result, OK, true)
    }

    "return matched value of true when detail request has more that one additional names and record has same value" in {
      mockDetailsResponse(groResponseWithMoreAdditionalName)
      val payload = Json.toJson(Payload(None, "Adam", Some("test david"), "SMITH", new LocalDate("2009-07-01"),
        BirthRegisterCountry.ENGLAND))
      val result = makeRequest(payload)
      checkResponse(result, OK, true)
    }

    "return matched value of false when detail request has more that one additional names and record has only one additional name." in {
      mockDetailsResponse(groResponseWithMoreAdditionalName)
      val payload = Json.toJson(Payload(None, "Adam", Some("test"), "SMITH", new LocalDate("2009-07-01"),
        BirthRegisterCountry.ENGLAND))
      val result = makeRequest(payload)
      checkResponse(result, OK, false)
    }

    "return matched value of false when detail request has additional names and record does not have middle name in it." in {
      mockDetailsResponse(groResponseWithoutAdditionalName)
      val payload = Json.toJson(Payload(None, "Adam", Some("david"), "SMITH", new LocalDate("2009-07-01"),
        BirthRegisterCountry.ENGLAND))
      val result = makeRequest(payload)
      checkResponse(result, OK, false)
    }

    "return matched value of true when detail request does not have additional name and record also does not have it " in {
      mockDetailsResponse(groResponseWithoutAdditionalName)
      val payload = Json.toJson(Payload(None, "Adam", None, "SMITH", new LocalDate("2009-07-01"),
        BirthRegisterCountry.ENGLAND))
      val result = makeRequest(payload)
      checkResponse(result, OK, true)
    }

    "return matched value of true when details request has additional name with special character and record has same value" in {
      mockDetailsResponse(groResponseWithSpecialCharacter)
      val payload = Json.toJson(Payload(None, "Mary-Ann  ",Some("O'Leary"), "Smith-Johnson", new LocalDate("2009-07-01"),
        BirthRegisterCountry.ENGLAND))
      val result = makeRequest(payload)
      checkResponse(result, OK, true)
    }

    "return matched value of true when details request fistname has additiona name with special character  and record has same value" in {
      mockDetailsResponse(groResponseWithSpecialCharacter)
      val payload = Json.toJson(Payload(None, "Mary-Ann O'Leary ",None, "Smith-Johnson", new LocalDate("2009-07-01"),
        BirthRegisterCountry.ENGLAND))
      val result = makeRequest(payload)
      checkResponse(result, OK, true)
    }

    "return matched value of true when details request firstname and additional names has more space seprated names and record has same name on it." in {
      mockDetailsResponse(groResponse500036682)
      val payload = Json.toJson(Payload(None, "Ivor Test Hywel Tom Jones ",Some("Welcome In The Valleys Grand Slam"), "WILLIAMS JONES", new LocalDate("2009-11-23"),
        BirthRegisterCountry.ENGLAND))
      val result = makeRequest(payload)
      checkResponse(result, OK, true)
    }


    "return matched value of false when details request firstname and additional names has more space seprated names and record has same different name on it." in {
      mockDetailsResponse(groResponse500036682)
      val payload = Json.toJson(Payload(None, "Ivor Test Hywel Tom Jones ",Some("Welcome In The Valleys Grand"), "WILLIAMS JONES", new LocalDate("2009-11-23"),
        BirthRegisterCountry.ENGLAND))
      val result = makeRequest(payload)
      checkResponse(result, OK, false)
    }

  }


  "validate additionalNames when ignoreAdditionalName is false." should {

    "return response code 400 if request contains additionalName key but no value" in {
      val request = postRequest(additionalNamesKeyNoValue)
      val result = await(MockController.post().apply(request))
      checkResponse(result,BAD_REQUEST, MockErrorResponses.INVALID_ADDITIONALNAMES.json)
    }

    "return response code 400 if request contains special characters in additionalName" in {
      val request = postRequest(additionalNameWithSpecialCharacters)
      val result = await(MockController.post().apply(request))
      checkResponse(result,BAD_REQUEST, MockErrorResponses.INVALID_ADDITIONALNAMES.json)
    }

    "return response code 400 if request contains more than 250 characters in additionalName" in {
      val request = postRequest(additionalNameWithMoreThan250Characters)
      val result = await(MockController.post().apply(request))
      checkResponse(result,BAD_REQUEST, MockErrorResponses.INVALID_ADDITIONALNAMES.json)
    }
  }

  def makeRequest(jsonRequest: JsValue): Result = {
    val request = postRequest(jsonRequest)
    val result = await(MockController.post().apply(request))
    result
  }
}
