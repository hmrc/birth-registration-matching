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

package uk.gov.hmrc.brm.controllers

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import play.api.test.Helpers._
import uk.gov.hmrc.brm.config.BrmConfig
import uk.gov.hmrc.brm.models.brm.Payload
import uk.gov.hmrc.brm.models.matching.BirthMatchResponse
import uk.gov.hmrc.brm.utils.Mocks._
import uk.gov.hmrc.brm.utils.{BaseUnitSpec, BirthRegisterCountry, HeaderValidator, MockErrorResponses}
import uk.gov.hmrc.play.audit.http.connector.AuditResult

import java.time.LocalDate
import scala.concurrent.Future

class BirthEventsControllerAdditionalNameSwitchSpec extends BaseUnitSpec {

  import uk.gov.hmrc.brm.utils.TestHelper._

  val config: Map[String, _] = Map(
    "microservice.services.birth-registration-matching.matching.ignoreAdditionalNames" -> false
  )

  val dateOfBirth: LocalDate    = LocalDate.of(2009, 7, 1)
  val altDateOfBirth: LocalDate = LocalDate.of(2009, 11, 23)

  val testController: BirthEventsController = new BirthEventsController(
    mockLookupService,
    auditorFixtures.whereBirthRegisteredAudit,
    MockAuditFactory,
    app.injector.instanceOf[BrmConfig],
    auditorFixtures.transactionAudit,
    auditorFixtures.errorAudit,
    auditorFixtures.matchingAudit,
    app.injector.instanceOf[HeaderValidator],
    stubControllerComponents(),
    mockCommonUtil,
    mockBrmLogger,
    mockMetricsFactory,
    mockFilters,
    mockEngWalesMetric,
    mockIreMetric,
    mockScotMetric,
    mockInvalidMetric
  )

  override lazy val app: Application = new GuiceApplicationBuilder()
    .configure(config)
    .build()

  def makeRequest(jsonRequest: JsValue): Result = {
    val request = postRequest(jsonRequest)
    val result  = testController.post().apply(request).futureValue
    result
  }

  "validating match when feature to ignore additional name is false." should {

    "return matched value of true when reference request has additional names and record has same value" in {
      when(mockLookupService.lookup()(any(), any(), any(), any()))
        .thenReturn(Future.successful(BirthMatchResponse(true)))

      when(mockFilters.process(any()))
        .thenReturn(List())

      when(mockAuditor.audit(any(), any())(any()))
        .thenReturn(Future.successful(AuditResult.Success))

      when(mockMetricsFactory.getMetrics()(any()))
        .thenReturn(mockEngWalesMetric)

      mockAuditSuccess
      mockReferenceResponse(groResponseWithAdditionalName)
      val payload = Json.toJson(
        Payload(
          Some("500035710"),
          "Adam",
          Some("test"),
          "SMITH",
          dateOfBirth,
          BirthRegisterCountry.ENGLAND
        )
      )
      val result  = makeRequest(payload)
      checkResponse(result, OK, matchResponse = true)
    }

    "return matched value of true when reference request has  more than one additional names and record has same value" in {
      mockAuditSuccess
      mockReferenceResponse(groResponseWithMoreAdditionalName)
      val payload = Json.toJson(
        Payload(
          Some("500035712"),
          "Adam",
          Some("test david"),
          "SMITH",
          dateOfBirth,
          BirthRegisterCountry.ENGLAND
        )
      )
      val result  = makeRequest(payload)
      checkResponse(result, OK, matchResponse = true)
    }

    "return matched value of true when reference request has  more than one additional names with space and record has same value without space" in {
      mockAuditSuccess
      mockReferenceResponse(groResponseWithMoreAdditionalName)
      val payload = Json.toJson(
        Payload(
          Some("500035712"),
          "Adam",
          Some(" test david "),
          "SMITH",
          dateOfBirth,
          BirthRegisterCountry.ENGLAND
        )
      )
      val result  = makeRequest(payload)
      checkResponse(result, OK, matchResponse = true)
    }

    "return matched value of false when reference request has additional names and record does not have middle name in it." in {
      when(mockLookupService.lookup()(any(), any(), any(), any()))
        .thenReturn(Future.successful(BirthMatchResponse()))
      mockAuditSuccess
      mockReferenceResponse(groResponseWithoutAdditionalName)
      val payload = Json.toJson(
        Payload(
          Some("500035711"),
          "Adam",
          Some("test"),
          "SMITH",
          dateOfBirth,
          BirthRegisterCountry.ENGLAND
        )
      )
      val result  = makeRequest(payload)
      checkResponse(result, OK, matchResponse = false)
    }

    "return matched value of true when user does not provide additional name and record also does not have it " in {
      when(mockLookupService.lookup()(any(), any(), any(), any()))
        .thenReturn(Future.successful(BirthMatchResponse(true)))

      mockAuditSuccess
      mockReferenceResponse(groResponseWithoutAdditionalName)
      val payload = Json.toJson(
        Payload(Some("500035711"), "Adam", None, "SMITH", dateOfBirth, BirthRegisterCountry.ENGLAND)
      )
      val result  = makeRequest(payload)
      checkResponse(result, OK, matchResponse = true)
    }

    "return matched value of false when user provide additional name and record does not have it " in {
      when(mockLookupService.lookup()(any(), any(), any(), any()))
        .thenReturn(Future.successful(BirthMatchResponse()))

      mockAuditSuccess
      mockReferenceResponse(groResponseWithoutAdditionalName)
      val payload = Json.toJson(
        Payload(
          Some("500035711"),
          "Adam",
          Some("test"),
          "SMITH",
          dateOfBirth,
          BirthRegisterCountry.ENGLAND
        )
      )
      val result  = makeRequest(payload)
      checkResponse(result, OK, matchResponse = false)
    }

    "return matched value of true when reference request has additional names with special character and record has same value" in {
      when(mockLookupService.lookup()(any(), any(), any(), any()))
        .thenReturn(Future.successful(BirthMatchResponse(true)))

      mockAuditSuccess
      mockReferenceResponse(groResponseWithSpecialCharacter)
      val payload = Json.toJson(
        Payload(
          Some("500035713"),
          "Mary-Ann ",
          Some("O'Leary"),
          "Smith-Johnson",
          dateOfBirth,
          BirthRegisterCountry.ENGLAND
        )
      )
      val result  = makeRequest(payload)
      checkResponse(result, OK, matchResponse = true)
    }

    "return matched value of true when reference request firstname has additional name with special character and record has same value" in {
      mockAuditSuccess
      mockReferenceResponse(groResponseWithSpecialCharacter)
      val payload = Json.toJson(
        Payload(
          Some("500035713"),
          "Mary-Ann O'Leary ",
          None,
          "Smith-Johnson",
          dateOfBirth,
          BirthRegisterCountry.ENGLAND
        )
      )
      val result  = makeRequest(payload)
      checkResponse(result, OK, matchResponse = true)
    }

    "return matched value of true when reference request firstname and additional names has more space seprated names and record has same name on it." in {
      mockAuditSuccess
      mockReferenceResponse(groResponse500036682)
      val payload = Json.toJson(
        Payload(
          Some("500036682"),
          "Ivor Test Hywel Tom Jones ",
          Some("Welcome In The Valleys Grand Slam"),
          "WILLIAMS JONES",
          altDateOfBirth,
          BirthRegisterCountry.ENGLAND
        )
      )
      val result  = makeRequest(payload)
      checkResponse(result, OK, matchResponse = true)
    }

    "return matched value of false when reference request firstname has multiple names ,no additional name and record has addiional name on it.." in {
      when(mockLookupService.lookup()(any(), any(), any(), any()))
        .thenReturn(Future.successful(BirthMatchResponse()))

      mockAuditSuccess
      mockReferenceResponse(groResponse500036682)
      val payload = Json.toJson(
        Payload(
          Some("500036682"),
          "Ivor Test Hywel Tom Jones ",
          None,
          "WILLIAMS JONES",
          altDateOfBirth,
          BirthRegisterCountry.ENGLAND
        )
      )
      val result  = makeRequest(payload)
      checkResponse(result, OK, matchResponse = false)
    }

    "return matched value of false when reference request firstname has multiple names and  additional name and record does not have same name on it." in {
      mockAuditSuccess
      mockReferenceResponse(groResponse500036682)
      val payload = Json.toJson(
        Payload(
          Some("500036682"),
          "Ivor Test Hywel Tom Jones ",
          Some("Welcome In The Valleys Grand"),
          "WILLIAMS JONES",
          altDateOfBirth,
          BirthRegisterCountry.ENGLAND
        )
      )
      val result  = makeRequest(payload)
      checkResponse(result, OK, matchResponse = false)
    }

    "return matched value of true when detail request has additional names and record has same value" in {
      when(mockLookupService.lookup()(any(), any(), any(), any()))
        .thenReturn(Future.successful(BirthMatchResponse(true)))

      mockAuditSuccess
      mockDetailsResponse(groResponseWithAdditionalName)
      val payload = Json.toJson(
        Payload(None, "Adam", Some("test"), "SMITH", dateOfBirth, BirthRegisterCountry.ENGLAND)
      )
      val result  = makeRequest(payload)
      checkResponse(result, OK, matchResponse = true)
    }

    "return matched value of true when detail request has more that one additional names and record has same value" in {
      mockAuditSuccess
      mockDetailsResponse(groResponseWithMoreAdditionalName)
      val payload = Json.toJson(
        Payload(None, "Adam", Some("test david"), "SMITH", dateOfBirth, BirthRegisterCountry.ENGLAND)
      )
      val result  = makeRequest(payload)
      checkResponse(result, OK, matchResponse = true)
    }

    "return matched value of false when detail request has more that one additional names and record has only one additional name." in {
      when(mockLookupService.lookup()(any(), any(), any(), any()))
        .thenReturn(Future.successful(BirthMatchResponse()))

      mockAuditSuccess
      mockDetailsResponse(groResponseWithMoreAdditionalName)
      val payload = Json.toJson(
        Payload(None, "Adam", Some("test"), "SMITH", dateOfBirth, BirthRegisterCountry.ENGLAND)
      )
      val result  = makeRequest(payload)
      checkResponse(result, OK, matchResponse = false)
    }

    "return matched value of false when detail request has additional names and record does not have middle name in it." in {
      mockAuditSuccess
      mockDetailsResponse(groResponseWithoutAdditionalName)
      val payload = Json.toJson(
        Payload(None, "Adam", Some("david"), "SMITH", dateOfBirth, BirthRegisterCountry.ENGLAND)
      )
      val result  = makeRequest(payload)
      checkResponse(result, OK, matchResponse = false)
    }

    "return matched value of true when detail request does not have additional name and record also does not have it " in {
      when(mockLookupService.lookup()(any(), any(), any(), any()))
        .thenReturn(Future.successful(BirthMatchResponse(true)))

      mockAuditSuccess
      mockDetailsResponse(groResponseWithoutAdditionalName)
      val payload =
        Json.toJson(Payload(None, "Adam", None, "SMITH", dateOfBirth, BirthRegisterCountry.ENGLAND))
      val result  = makeRequest(payload)
      checkResponse(result, OK, matchResponse = true)
    }

    "return matched value of true when details request has additional name with special character and record has same value" in {
      mockAuditSuccess
      mockDetailsResponse(groResponseWithSpecialCharacter)
      val payload = Json.toJson(
        Payload(
          None,
          "Mary-Ann  ",
          Some("O'Leary"),
          "Smith-Johnson",
          dateOfBirth,
          BirthRegisterCountry.ENGLAND
        )
      )
      val result  = makeRequest(payload)
      checkResponse(result, OK, matchResponse = true)
    }

    "return matched value of true when details request fistname has additiona name with special character  and record has same value" in {
      mockAuditSuccess
      mockDetailsResponse(groResponseWithSpecialCharacter)
      val payload = Json.toJson(
        Payload(
          None,
          "Mary-Ann O'Leary ",
          None,
          "Smith-Johnson",
          dateOfBirth,
          BirthRegisterCountry.ENGLAND
        )
      )
      val result  = makeRequest(payload)
      checkResponse(result, OK, matchResponse = true)
    }

    "return matched value of true when details request firstname and additional names has more space seprated names and record has same name on it." in {
      mockAuditSuccess
      mockDetailsResponse(groResponse500036682)
      val payload = Json.toJson(
        Payload(
          None,
          "Ivor Test Hywel Tom Jones ",
          Some("Welcome In The Valleys Grand Slam"),
          "WILLIAMS JONES",
          altDateOfBirth,
          BirthRegisterCountry.ENGLAND
        )
      )
      val result  = makeRequest(payload)
      checkResponse(result, OK, matchResponse = true)
    }

    "return matched value of false when details request firstname and additional names " +
      "has more space seprated names and record has same different name on it." in {
        when(mockLookupService.lookup()(any(), any(), any(), any()))
          .thenReturn(Future.successful(BirthMatchResponse()))

        mockAuditSuccess
        mockDetailsResponse(groResponse500036682)
        val payload = Json.toJson(
          Payload(
            None,
            "Ivor Test Hywel Tom Jones ",
            Some("Welcome In The Valleys Grand"),
            "WILLIAMS JONES",
            altDateOfBirth,
            BirthRegisterCountry.ENGLAND
          )
        )
        val result  = makeRequest(payload)
        checkResponse(result, OK, matchResponse = false)
      }

  }

  "validate additionalNames when ignoreAdditionalName is false." should {

    "return response code 400 if request contains additionalName key but no value" in {
      mockAuditSuccess
      val request = postRequest(additionalNamesKeyNoValue)
      val result  = testController.post().apply(request).futureValue
      checkResponse(result, BAD_REQUEST, MockErrorResponses.INVALID_ADDITIONALNAMES.json)
    }

    "return response code 400 if request contains special characters in additionalName" in {
      mockAuditSuccess
      val request = postRequest(additionalNameWithSpecialCharacters)
      val result  = testController.post().apply(request).futureValue
      checkResponse(result, BAD_REQUEST, MockErrorResponses.INVALID_ADDITIONALNAMES.json)
    }

    "return response code 400 if request contains more than 250 characters in additionalName" in {
      mockAuditSuccess
      val request = postRequest(additionalNameWithMoreThan250Characters)
      val result  = testController.post().apply(request).futureValue
      checkResponse(result, BAD_REQUEST, MockErrorResponses.INVALID_ADDITIONALNAMES.json)
    }
  }
}
