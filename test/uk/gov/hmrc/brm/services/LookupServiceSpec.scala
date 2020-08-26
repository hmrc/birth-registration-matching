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

package uk.gov.hmrc.brm.services

import org.joda.time.LocalDate
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfter
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status
import play.api.libs.json.Json
import uk.gov.hmrc.brm.audit.EnglandAndWalesAudit
import uk.gov.hmrc.brm.metrics.EnglandAndWalesBirthRegisteredCountMetrics
import uk.gov.hmrc.brm.models.brm.Payload
import uk.gov.hmrc.brm.models.matching.{BirthMatchResponse, MatchingResult}
import uk.gov.hmrc.brm.services.matching.{Bad, Good}
import uk.gov.hmrc.brm.services.parser.NameParser.Names
import uk.gov.hmrc.brm.utils.BirthRegisterCountry
import uk.gov.hmrc.brm.utils.TestHelper._
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, NotImplementedException}
import uk.gov.hmrc.play.audit.http.connector.AuditResult
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.Future
import scala.concurrent.duration.Duration

class LookupServiceSpec extends UnitSpec with GuiceOneAppPerSuite with MockitoSugar with BeforeAndAfter {

  import uk.gov.hmrc.brm.utils.Mocks._

  implicit val hc: HeaderCarrier = HeaderCarrier()

  implicit val engAuditor: EnglandAndWalesAudit = mock[EnglandAndWalesAudit]
  implicit val engMetrics: EnglandAndWalesBirthRegisteredCountMetrics = mock[EnglandAndWalesBirthRegisteredCountMetrics]

  val goodMatch: MatchingResult = MatchingResult(Good(), Good(),Good(),Good(),Good(),Names(List(), List(), List()))
  val badMatch: MatchingResult = MatchingResult(Bad(), Bad(),Bad(),Bad(),Bad(),Names(List(), List(), List()))

  before {
    reset(mockAuditConnector)
  }

  val FIVE = 5

  "LookupService" when {

    "requesting england or wales" should {

      "accept Payload as an argument - false match" in {
        val groResponseInvalid = Json.parse(
          """
            |{
            |  "location": {
            |
            |  },
            |  "subjects": {
            |    "child": {
            |      "name": {
            |
            |      },
            |      "originalName": {
            |
            |      }
            |    },
            |    "father": {
            |      "name": {
            |
            |      }
            |    },
            |    "mother": {
            |      "name": {
            |
            |      }
            |    },
            |    "informant": {
            |      "name": {
            |
            |      }
            |    }
            |  },
            |  "systemNumber": 999999920,
            |  "id": 999999920,
            |  "status": {
            |    "blockedRegistration": false
            |  },
            |  "previousRegistration": {}
            |
            |  }
          """.stripMargin)

        when(mockGroConnector.getReference(any())(any()))
          .thenReturn(Future.successful(HttpResponse(Status.OK, Some(groResponseInvalid))))

        when(mockAuditor.audit(any(), any())(any()))
          .thenReturn(Future.successful(AuditResult.Success))

        when(mockAuditConnector.sendEvent(any())(any(), any()))
          .thenReturn(Future.successful(AuditResult.Success))

        when(mockMatchingservice.performMatch(any(), any(), any())(any()))
          .thenReturn(badMatch)

        val service = MockLookupService
        implicit val payload: Payload = Payload(Some("999999920"), "Adam", None, "Conder", LocalDate.now, BirthRegisterCountry.ENGLAND)
        val result: BirthMatchResponse = await(service.lookup)(Duration.create(FIVE, "seconds"))
        result shouldBe BirthMatchResponse()
      }

      "accept Payload as an argument - true match" in {
        val groResponseValid = Json.parse(
          """
            |{
            |  "location": {
            |
            |  },
            |  "subjects": {
            |    "child" : {
            |   "name" : {
            |    "givenName" : "Chris",
            |    "surname" : "Jones"
            |   },
            |   "dateOfBirth" : "2012-02-16"
            |  },
            |    "father": {
            |      "name": {
            |
            |      }
            |    },
            |    "mother": {
            |      "name": {
            |
            |      }
            |    },
            |    "informant": {
            |      "name": {
            |
            |      }
            |    }
            |  },
            |  "systemNumber": 123456789,
            |  "id": 123456789,
            |  "status": {
            |    "blockedRegistration": false
            |  },
            |  "previousRegistration": {}
            |
            |  }
          """.stripMargin)

        when(mockGroConnector.getReference(any())(any())).thenReturn(Future.successful(HttpResponse(Status.OK, Some(groResponseValid))))
        when(mockAuditConnector.sendEvent(any())(any(), any())).thenReturn(Future.successful(AuditResult.Success))

        when(mockMatchingservice.performMatch(any(), any(), any())(any()))
          .thenReturn(goodMatch)

        val service = MockLookupService
        implicit val payload: Payload = Payload(Some("123456789"), "Chris", None, "Jones", new LocalDate("2012-02-16"), BirthRegisterCountry.ENGLAND)
        val result = await(service.lookup)
        result shouldBe BirthMatchResponse(true)
      }

      "accept Payload as an argument without reference number - false match" in {
        val groResponseInvalid = Json.parse(
          """
            |{
            |  "location": {
            |
            |  },
            |  "subjects": {
            |    "child": {
            |      "name": {
            |
            |      },
            |      "originalName": {
            |
            |      }
            |    },
            |    "father": {
            |      "name": {
            |
            |      }
            |    },
            |    "mother": {
            |      "name": {
            |
            |      }
            |    },
            |    "informant": {
            |      "name": {
            |
            |      }
            |    }
            |  },
            |  "systemNumber": 999999920,
            |  "id": 999999920,
            |  "status": {
            |    "blockedRegistration": false
            |  },
            |  "previousRegistration": {}
            |
            |  }
          """.stripMargin)

        when(mockGroConnector.getChildDetails(any())(any())).thenReturn(Future.successful(HttpResponse(Status.OK, Some(groResponseInvalid))))
        when(mockAuditConnector.sendEvent(any())(any(), any())).thenReturn(Future.successful(AuditResult.Success))

        when(mockMatchingservice.performMatch(any(), any(), any())(any()))
          .thenReturn(badMatch)

        val service = MockLookupService
        implicit val payload: Payload = Payload(None, "Adam", None, "Conder", LocalDate.now, BirthRegisterCountry.ENGLAND)
        val result: BirthMatchResponse = await(service.lookup)(Duration.create(FIVE, "seconds"))
        result shouldBe BirthMatchResponse()
      }

      "accept payload as an argument without reference number - true match" in {
        val groResponseValid = Json.parse(
          """
            |{
            |  "location": {
            |
            |  },
            |  "subjects": {
            |    "child" : {
            |   "name" : {
            |    "givenName" : "Chris",
            |    "surname" : "Jones"
            |   },
            |   "dateOfBirth" : "2012-02-16"
            |  },
            |    "father": {
            |      "name": {
            |
            |      }
            |    },
            |    "mother": {
            |      "name": {
            |
            |      }
            |    },
            |    "informant": {
            |      "name": {
            |
            |      }
            |    }
            |  },
            |  "systemNumber": 123456789,
            |  "id": 123456789,
            |  "status": {
            |    "blockedRegistration": false
            |  },
            |  "previousRegistration": {}
            |
            |  }
          """.stripMargin)

        when(mockGroConnector.getChildDetails(any())(any())).thenReturn(Future.successful(HttpResponse(Status.OK, Some(groResponseValid))))
        when(mockAuditConnector.sendEvent(any())(any(), any())).thenReturn(Future.successful(AuditResult.Success))

        when(mockMatchingservice.performMatch(any(), any(), any())(any()))
          .thenReturn(goodMatch)

        val service = MockLookupService
        implicit val payload: Payload = Payload(None, "Chris", None, "Jones", new LocalDate("2012-02-16"), BirthRegisterCountry.ENGLAND)
        val result = await(service.lookup)
        result shouldBe BirthMatchResponse(true)
      }

    }

    "requesting Scotland" should {

      "accept Payload as an argument" in {
        when(mockAuditConnector.sendEvent(any())(any(), any())).thenReturn(Future.successful(AuditResult.Success))
        when(mockNrsConnector.getReference(any())(any())).thenReturn(Future.successful(HttpResponse(Status.OK, Some(validNrsJsonResponseObject))))
        val service = MockLookupService
        implicit val payload: Payload = nrsRequestPayload
        val result = await(service.lookup)

        result shouldBe BirthMatchResponse(true)
      }

      "accept payload without reference number as argument" in {
        when(mockAuditConnector.sendEvent(any())(any(), any())).thenReturn(Future.successful(AuditResult.Success))
        when(mockNrsConnector.getChildDetails(any())(any())).thenReturn(Future.successful(HttpResponse(Status.OK, Some(validNrsJsonResponseObject))))
        val service = MockLookupService
        implicit val payload: Payload = nrsRequestPayloadWithoutBrn
        val result = await(service.lookup)
        result shouldBe BirthMatchResponse(true)

      }


      "accept payload with reference number as argument and returns true as matched." in {
        when(mockAuditConnector.sendEvent(any())(any(), any())).thenReturn(Future.successful(AuditResult.Success))
        when(mockNrsConnector.getReference(any())(any())).thenReturn(Future.successful(HttpResponse(Status.OK, Some(validNrsJsonResponseObject))))
        val service = MockLookupService
        implicit val payload: Payload = nrsRequestPayload
        val result = await(service.lookup)
        result shouldBe BirthMatchResponse(true)

      }

      "accept payload with special character and returns match true as matched." in {
        when(mockAuditConnector.sendEvent(any())(any(), any())).thenReturn(Future.successful(AuditResult.Success))
        when(mockNrsConnector.getReference(any())(any())).thenReturn(Future.successful(HttpResponse(Status.OK, Some(validNrsJsonResponse2017350007))))
        val service = MockLookupService
        implicit val payload: Payload = nrsRequestPayloadWithSpecialChar
        val result = await(service.lookup)
        result shouldBe BirthMatchResponse(true)

      }

      "accept payload with special character and returns match false as first name doesn't match." in {
        when(mockAuditConnector.sendEvent(any())(any(), any())).thenReturn(Future.successful(AuditResult.Success))
        when(mockNrsConnector.getReference(any())(any())).thenReturn(Future.successful(HttpResponse(Status.OK, Some(validNrsJsonResponse2017350007))))

        when(mockMatchingservice.performMatch(any(), any(), any())(any()))
          .thenReturn(badMatch)

        val service = MockLookupService

        implicit val payload: Payload = nrsRequestPayloadWithFirstNameWrong
        val result = await(service.lookup)
        result shouldBe BirthMatchResponse()

      }

    }

    "requesting Northern Ireland" should {

      "accept Payload as an argument" in {
        intercept[NotImplementedException] {
          when(mockAuditConnector.sendEvent(any())(any(), any())).thenReturn(Future.successful(AuditResult.Success))
          when(mockGroniConnector.getReference(any())(any()))
            .thenReturn(Future.failed(new NotImplementedException("No getReference method available for GRONI connector.")))
          val service = MockLookupService
          implicit val payload: Payload = Payload(Some("123456789"), "Chris", None, "Jones", new LocalDate("2012-02-16"), BirthRegisterCountry.NORTHERN_IRELAND)
          await(service.lookup)
        }
      }

      "accept payload without reference number as argument" in {
        intercept[NotImplementedException] {
          when(mockAuditConnector.sendEvent(any())(any(), any())).thenReturn(Future.successful(AuditResult.Success))
          when(mockGroniConnector.getChildDetails(any())(any()))
            .thenReturn(Future.failed(new NotImplementedException("No getChildDetails method available for GRONI connector.")))
          val service = MockLookupService
          implicit val payload: Payload = Payload(None, "Chris", None, "Jones", new LocalDate("2012-02-16"), BirthRegisterCountry.NORTHERN_IRELAND)
          await(service.lookup)
        }
      }

    }

  }

}
