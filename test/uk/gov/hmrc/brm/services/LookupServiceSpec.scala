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
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfter
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.OneAppPerSuite
import play.api.http.Status
import play.api.libs.json.Json
import uk.gov.hmrc.brm.connectors.BirthConnector
import uk.gov.hmrc.brm.models.brm.Payload
import uk.gov.hmrc.brm.models.matching.BirthMatchResponse
import uk.gov.hmrc.brm.utils.BirthRegisterCountry
import uk.gov.hmrc.brm.utils.TestHelper._
import uk.gov.hmrc.play.audit.http.connector.AuditResult
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.Future
import scala.concurrent.duration.Duration
import uk.gov.hmrc.http.{ HeaderCarrier, HttpResponse, NotImplementedException }

class LookupServiceSpec extends UnitSpec with OneAppPerSuite with MockitoSugar with BeforeAndAfter {

  import uk.gov.hmrc.brm.utils.Mocks._

  implicit val hc = HeaderCarrier()

  before {
    reset(MockLookupService.groConnector)
    reset(MockLookupService.nrsConnector)
    reset(MockLookupService.groniConnector)
    reset(mockAuditConnector)
  }

  "LookupService" when {

    "initialising" should {

      "initialise with dependencies" in {
        LookupService.groConnector shouldBe a[BirthConnector]
        LookupService.nrsConnector shouldBe a[BirthConnector]
        LookupService.groniConnector shouldBe a[BirthConnector]
      }

    }

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

        when(MockLookupService.groConnector.getReference(Matchers.any())(Matchers.any())).thenReturn(Future.successful(HttpResponse(Status.OK, Some(groResponseInvalid))))
        when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))

        val service = MockLookupService
        implicit val payload = Payload(Some("999999920"), "Adam", None, "Conder", LocalDate.now, BirthRegisterCountry.ENGLAND)
        val result = await(service.lookup)(Duration.create(5, "seconds"))
        result shouldBe BirthMatchResponse(false)
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

        when(MockLookupService.groConnector.getReference(Matchers.any())(Matchers.any())).thenReturn(Future.successful(HttpResponse(Status.OK, Some(groResponseValid))))
        when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))

        val service = MockLookupService
        implicit val payload = Payload(Some("123456789"), "Chris", None, "Jones", new LocalDate("2012-02-16"), BirthRegisterCountry.ENGLAND)
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

        when(MockLookupService.groConnector.getChildDetails(Matchers.any())(Matchers.any())).thenReturn(Future.successful(HttpResponse(Status.OK, Some(groResponseInvalid))))
        when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))

        val service = MockLookupService
        implicit val payload = Payload(None, "Adam", None, "Conder", LocalDate.now, BirthRegisterCountry.ENGLAND)
        val result = await(service.lookup)(Duration.create(5, "seconds"))
        result shouldBe BirthMatchResponse(false)
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

        when(MockLookupService.groConnector.getChildDetails(Matchers.any())(Matchers.any())).thenReturn(Future.successful(HttpResponse(Status.OK, Some(groResponseValid))))
        when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))

        val service = MockLookupService
        implicit val payload = Payload(None, "Chris", None, "Jones", new LocalDate("2012-02-16"), BirthRegisterCountry.ENGLAND)
        val result = await(service.lookup)
        result shouldBe BirthMatchResponse(true)
      }

    }

    "requesting Scotland" should {

      "accept Payload as an argument" in {
        when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
        when(MockLookupService.nrsConnector.getReference(Matchers.any())(Matchers.any())).thenReturn(Future.successful(HttpResponse(Status.OK, Some(validNrsJsonResponseObject))))
        val service = MockLookupService
        implicit val payload = nrsRequestPayload
        val result = await(service.lookup)

        result shouldBe BirthMatchResponse(true)
      }

      "accept payload without reference number as argument" in {
        when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
        when(MockLookupService.nrsConnector.getChildDetails(Matchers.any())(Matchers.any())).thenReturn(Future.successful(HttpResponse(Status.OK, Some(validNrsJsonResponseObject))))
        val service = MockLookupService
        implicit val payload = nrsRequestPayloadWithoutBrn
        val result = await(service.lookup)
        result shouldBe BirthMatchResponse(true)

      }


      "accept payload with reference number as argument and returns true as matched." in {
        when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
        when(MockLookupService.nrsConnector.getReference(Matchers.any())(Matchers.any())).thenReturn(Future.successful(HttpResponse(Status.OK, Some(validNrsJsonResponseObject))))
        val service = MockLookupService
        implicit val payload = nrsRequestPayload
        val result = await(service.lookup)
        result shouldBe BirthMatchResponse(true)

      }

      "accept payload with special character and returns match true as matched." in {
        when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
        when(MockLookupService.nrsConnector.getReference(Matchers.any())(Matchers.any())).thenReturn(Future.successful(HttpResponse(Status.OK, Some(validNrsJsonResponse2017350007))))
        val service = MockLookupService
        implicit val payload = nrsRequestPayloadWithSpecialChar
        val result = await(service.lookup)
        result shouldBe BirthMatchResponse(true)

      }

      "accept payload with special character and returns match false as first name don't match." in {
        when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
        when(MockLookupService.nrsConnector.getReference(Matchers.any())(Matchers.any())).thenReturn(Future.successful(HttpResponse(Status.OK, Some(validNrsJsonResponse2017350007))))
        val service = MockLookupService

        implicit val payload = nrsRequestPayloadWithFirstNameWrong
        val result = await(service.lookup)
        result shouldBe BirthMatchResponse(false)

      }

    }

    "requesting Northern Ireland" should {

      "accept Payload as an argument" in {
        intercept[NotImplementedException] {
          when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
          when(MockLookupService.groniConnector.getReference(Matchers.any())(Matchers.any())).thenReturn(Future.failed(new NotImplementedException("No getReference method available for GRONI connector.")))
          val service = MockLookupService
          implicit val payload = Payload(Some("123456789"), "Chris", None, "Jones", new LocalDate("2012-02-16"), BirthRegisterCountry.NORTHERN_IRELAND)
          await(service.lookup)
        }
      }

      "accept payload without reference number as argument" in {
        intercept[NotImplementedException] {
          when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
          when(MockLookupService.groniConnector.getChildDetails(Matchers.any())(Matchers.any())).thenReturn(Future.failed(new NotImplementedException("No getChildDetails method available for GRONI connector.")))
          val service = MockLookupService
          implicit val payload = Payload(None, "Chris", None, "Jones", new LocalDate("2012-02-16"), BirthRegisterCountry.NORTHERN_IRELAND)
          await(service.lookup)
        }
      }

    }

  }

}
