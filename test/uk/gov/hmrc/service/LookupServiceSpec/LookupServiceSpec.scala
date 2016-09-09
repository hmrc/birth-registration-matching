/*
 * Copyright 2016 HM Revenue & Customs
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

package uk.gov.hmrc.service.LookupServiceSpec

import org.joda.time.LocalDate
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.http.Status
import play.api.libs.json.Json
import uk.gov.hmrc.brm.connectors.BirthConnector
import uk.gov.hmrc.brm.models.brm.Payload
import uk.gov.hmrc.brm.models.matching.BirthMatchResponse
import uk.gov.hmrc.brm.services.LookupService
import uk.gov.hmrc.brm.utils.BirthRegisterCountry
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future

/**
 * Created by adamconder on 09/09/2016.
 */
class LookupServiceSpec extends UnitSpec with WithFakeApplication with MockitoSugar {

  val mockConnector = mock[BirthConnector]
  object MockService extends LookupService {
    override val groConnector = mockConnector
    override val nrsConnector = mockConnector
    override val nirsConnector = mockConnector
  }

  implicit val hc = HeaderCarrier()

  "LookupService" should {

    "initialise with dependencies" in {
      LookupService.groConnector shouldBe a[BirthConnector]
      LookupService.nrsConnector shouldBe a[BirthConnector]
      LookupService.nirsConnector shouldBe a[BirthConnector]
    }

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
      when(MockService.groConnector.getReference(Matchers.any())(Matchers.any())).thenReturn(Future.successful(HttpResponse(Status.OK, Some(groResponseInvalid))))
      val service = MockService
      val payload = Payload(Some("999999920"), "Adam", "Conder", LocalDate.now, BirthRegisterCountry.ENGLAND)
      val result = await(service.lookup(payload))
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
      when(MockService.groConnector.getReference(Matchers.any())(Matchers.any())).thenReturn(Future.successful(HttpResponse(Status.OK, Some(groResponseValid))))
      val service = MockService
      val payload = Payload(Some("123456789"), "Chris", "Jones", new LocalDate("2012-02-16"), BirthRegisterCountry.ENGLAND)
      val result = await(service.lookup(payload))
      result shouldBe BirthMatchResponse(true)
    }

  }

}
