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

package uk.gov.hmrc.brm.services

import org.joda.time.LocalDate
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.http.Status
import play.api.libs.json.Json
import uk.gov.hmrc.brm.audit.{EnglandAndWalesAudit, TransactionAuditor}
import uk.gov.hmrc.brm.connectors.BirthConnector
import uk.gov.hmrc.brm.metrics.BRMMetrics
import uk.gov.hmrc.brm.models.brm.Payload
import uk.gov.hmrc.brm.models.matching.BirthMatchResponse
import uk.gov.hmrc.brm.utils.BirthRegisterCountry
import uk.gov.hmrc.play.audit.http.connector.{AuditConnector, AuditResult}
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpResponse, NotImplementedException}
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future
import scala.concurrent.duration.Duration

class LookupServiceSpec extends UnitSpec with WithFakeApplication with MockitoSugar {

  import uk.gov.hmrc.brm.utils.Mocks._

//  val mockConnector = mock[BirthConnector]
//  val mockAuditConnector = mock[AuditConnector]
//  implicit val auditor = new EnglandAndWalesAudit(mockAuditConnector)
//  implicit val metrics = mock[BRMMetrics]
  implicit val hc = HeaderCarrier()

//  object MockLookupService extends LookupService {
//    override val groConnector = mockConnector
//    override val nrsConnector = mockConnector
//    override val groniConnector = mockConnector
//    override val matchingService = MatchingService
//    override val transactionAuditor: TransactionAuditor = new TransactionAuditor(mockAuditConnector)
//  }

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
        implicit val payload = Payload(Some("999999920"), "Adam", "Conder", LocalDate.now, BirthRegisterCountry.ENGLAND)
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
        implicit val payload = Payload(Some("123456789"), "Chris", "Jones", new LocalDate("2012-02-16"), BirthRegisterCountry.ENGLAND)
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
        implicit val payload = Payload(None, "Adam", "Conder", LocalDate.now, BirthRegisterCountry.ENGLAND)
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
        implicit val payload = Payload(None, "Chris", "Jones", new LocalDate("2012-02-16"), BirthRegisterCountry.ENGLAND)
        val result = await(service.lookup)
        result shouldBe BirthMatchResponse(true)
      }

    }

    "requesting Scotland" should {

      "accept Payload as an argument" in {
        intercept[NotImplementedException] {
          when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))

          val service = MockLookupService
          implicit val payload = Payload(Some("123456789"), "Chris", "Jones", new LocalDate("2012-02-16"), BirthRegisterCountry.SCOTLAND)
          val result = await(service.lookup)
          result should not be BirthMatchResponse(false)
        }
      }

      "accept payload without reference number as argument" in {
        intercept[NotImplementedException] {
          when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))

          val service = MockLookupService
          implicit val payload = Payload(None, "Chris", "Jones", new LocalDate("2012-02-16"), BirthRegisterCountry.SCOTLAND)
          val result = await(service.lookup)
          result should not be BirthMatchResponse(false)
        }
      }

    }

    "requesting Northern Ireland" should {

      "accept Payload as an argument" in {
        intercept[NotImplementedException] {
          when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))

          val service = MockLookupService
          implicit val payload = Payload(Some("123456789"), "Chris", "Jones", new LocalDate("2012-02-16"), BirthRegisterCountry.NORTHERN_IRELAND)
          val result = await(service.lookup)
          result should not be BirthMatchResponse(false)
        }
      }

      "accept payload without reference number as argument" in {
        intercept[NotImplementedException] {
          when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))

          val service = MockLookupService
          implicit val payload = Payload(None, "Chris", "Jones", new LocalDate("2012-02-16"), BirthRegisterCountry.NORTHERN_IRELAND)
          val result = await(service.lookup)
          result should not be BirthMatchResponse(false)
        }
      }

    }

  }

}
