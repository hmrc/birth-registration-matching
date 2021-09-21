/*
 * Copyright 2021 HM Revenue & Customs
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
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatest.{BeforeAndAfter, OptionValues}
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

import scala.concurrent.Future

class LookupServiceSpec extends AnyWordSpecLike with Matchers with OptionValues with GuiceOneAppPerSuite with MockitoSugar with BeforeAndAfter with ScalaFutures {

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

        when(mockGroConnector.getReference(any())(any(), any()))
          .thenReturn(Future.successful(HttpResponse(Status.OK, groResponseInvalid, Map.empty[String, Seq[String]])))

        when(mockAuditor.audit(any(), any())(any()))
          .thenReturn(Future.successful(AuditResult.Success))

        when(mockAuditConnector.sendEvent(any())(any(), any()))
          .thenReturn(Future.successful(AuditResult.Success))

        when(mockMatchingservice.performMatch(any(), any(), any())(any()))
          .thenReturn(badMatch)

        val service = MockLookupService
        implicit val payload: Payload = Payload(Some("999999920"), "Adam", None, "Conder", LocalDate.now, BirthRegisterCountry.ENGLAND)
        val result: BirthMatchResponse = service.lookup.futureValue
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

        when(mockGroConnector.getReference(any())(any(), any()))
          .thenReturn(Future.successful(HttpResponse(Status.OK, groResponseValid, Map.empty[String, Seq[String]])))
        when(mockAuditConnector.sendEvent(any())(any(), any())).thenReturn(Future.successful(AuditResult.Success))

        when(mockMatchingservice.performMatch(any(), any(), any())(any()))
          .thenReturn(goodMatch)

        val service = MockLookupService
        implicit val payload: Payload = Payload(Some("123456789"), "Chris", None, "Jones", new LocalDate("2012-02-16"), BirthRegisterCountry.ENGLAND)
        val result = service.lookup.futureValue
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

        when(mockGroConnector.getChildDetails(any())(any(), any()))
          .thenReturn(Future.successful(HttpResponse(Status.OK, groResponseInvalid, Map.empty[String, Seq[String]])))
        when(mockAuditConnector.sendEvent(any())(any(), any())).thenReturn(Future.successful(AuditResult.Success))

        when(mockMatchingservice.performMatch(any(), any(), any())(any()))
          .thenReturn(badMatch)

        val service = MockLookupService
        implicit val payload: Payload = Payload(None, "Adam", None, "Conder", LocalDate.now, BirthRegisterCountry.ENGLAND)
        val result: BirthMatchResponse = service.lookup.futureValue
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

        when(mockGroConnector.getChildDetails(any())(any(), any()))
          .thenReturn(Future.successful(HttpResponse(Status.OK, groResponseValid, Map.empty[String, Seq[String]])))
        when(mockAuditConnector.sendEvent(any())(any(), any())).thenReturn(Future.successful(AuditResult.Success))

        when(mockMatchingservice.performMatch(any(), any(), any())(any()))
          .thenReturn(goodMatch)

        val service = MockLookupService
        implicit val payload: Payload = Payload(None, "Chris", None, "Jones", new LocalDate("2012-02-16"), BirthRegisterCountry.ENGLAND)
        val result = service.lookup.futureValue
        result shouldBe BirthMatchResponse(true)
      }

    }

    "requesting Scotland" should {

      "accept Payload as an argument" in {
        when(mockAuditConnector.sendEvent(any())(any(), any())).thenReturn(Future.successful(AuditResult.Success))
        when(mockNrsConnector.getReference(any())(any(), any()))
          .thenReturn(Future.successful(HttpResponse(Status.OK, validNrsJsonResponseObject, Map.empty[String, Seq[String]])))
        val service = MockLookupService
        implicit val payload: Payload = nrsRequestPayload
        val result = service.lookup.futureValue

        result shouldBe BirthMatchResponse(true)
      }

      "accept payload without reference number as argument" in {
        when(mockAuditConnector.sendEvent(any())(any(), any())).thenReturn(Future.successful(AuditResult.Success))
        when(mockNrsConnector.getChildDetails(any())(any(), any()))
          .thenReturn(Future.successful(HttpResponse(Status.OK, validNrsJsonResponseObject, Map.empty[String, Seq[String]])))
        val service = MockLookupService
        implicit val payload: Payload = nrsRequestPayloadWithoutBrn
        val result = service.lookup.futureValue
        result shouldBe BirthMatchResponse(true)

      }


      "accept payload with reference number as argument and returns true as matched." in {
        when(mockAuditConnector.sendEvent(any())(any(), any())).thenReturn(Future.successful(AuditResult.Success))
        when(mockNrsConnector.getReference(any())(any(), any()))
          .thenReturn(Future.successful(HttpResponse(Status.OK, validNrsJsonResponseObject, Map.empty[String, Seq[String]])))
        val service = MockLookupService
        implicit val payload: Payload = nrsRequestPayload
        val result = service.lookup.futureValue
        result shouldBe BirthMatchResponse(true)

      }

      "accept payload with special character and returns match true as matched." in {
        when(mockAuditConnector.sendEvent(any())(any(), any())).thenReturn(Future.successful(AuditResult.Success))
        when(mockNrsConnector.getReference(any())(any(), any()))
          .thenReturn(Future.successful(HttpResponse(Status.OK, validNrsJsonResponse2017350007, Map.empty[String, Seq[String]])))
        val service = MockLookupService
        implicit val payload: Payload = nrsRequestPayloadWithSpecialChar
        val result = service.lookup.futureValue
        result shouldBe BirthMatchResponse(true)

      }

      "accept payload with special character and returns match false as first name doesn't match." in {
        when(mockAuditConnector.sendEvent(any())(any(), any())).thenReturn(Future.successful(AuditResult.Success))
        when(mockNrsConnector.getReference(any())(any(), any()))
          .thenReturn(Future.successful(HttpResponse(Status.OK, validNrsJsonResponse2017350007, Map.empty[String, Seq[String]])))

        when(mockMatchingservice.performMatch(any(), any(), any())(any()))
          .thenReturn(badMatch)

        val service = MockLookupService

        implicit val payload: Payload = nrsRequestPayloadWithFirstNameWrong
        val result = service.lookup.futureValue
        result shouldBe BirthMatchResponse()

      }

    }

    "requesting Northern Ireland" should {

      "accept Payload as an argument" in {
          when(mockAuditConnector.sendEvent(any())(any(), any())).thenReturn(Future.successful(AuditResult.Success))
          when(mockGroniConnector.getReference(any())(any(), any()))
            .thenReturn(Future.failed(new NotImplementedException("No getReference method available for GRONI connector.")))
          val service = MockLookupService
          implicit val payload: Payload = Payload(Some("123456789"), "Chris", None, "Jones", new LocalDate("2012-02-16"), BirthRegisterCountry.NORTHERN_IRELAND)
          assert(service.lookup.failed.futureValue.isInstanceOf[NotImplementedException])
      }

      "accept payload without reference number as argument" in {
          when(mockAuditConnector.sendEvent(any())(any(), any())).thenReturn(Future.successful(AuditResult.Success))
          when(mockGroniConnector.getChildDetails(any())(any(), any()))
            .thenReturn(Future.failed(new NotImplementedException("No getChildDetails method available for GRONI connector.")))
          val service = MockLookupService
          implicit val payload: Payload = Payload(None, "Chris", None, "Jones", new LocalDate("2012-02-16"), BirthRegisterCountry.NORTHERN_IRELAND)
          assert(service.lookup.failed.futureValue.isInstanceOf[NotImplementedException])
      }

    }

  }

}
