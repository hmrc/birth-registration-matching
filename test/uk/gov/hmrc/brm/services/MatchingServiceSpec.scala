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

package uk.gov.hmrc.brm.services

import org.joda.time.LocalDate
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.brm.models.brm.Payload
import uk.gov.hmrc.brm.utils.{BirthRegisterCountry, MatchingType}
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import uk.gov.hmrc.brm.utils.TestHelper._
import org.mockito.Matchers
import org.mockito.Mockito._

/**
  * Created by user on 29/09/16.
  */
class MatchingServiceSpec extends UnitSpec with WithFakeApplication with MockitoSugar {

  object MockMatchingService extends MatchingService {

  }


  "valid request payload and valid groresponse " should {
    "return true result match" in {

      val payload = Payload(Some("123456789"), "Chris", "Jones", new LocalDate("2012-02-16"), BirthRegisterCountry.ENGLAND)
      var resultMatch = MockMatchingService.performMatch(payload, validGroRespons, MatchingType.FULL)
      resultMatch.isMatch shouldBe true
    }
  }

  "valid request payload and invalid groresponse " should {
    "return false result match" in {

      val payload = Payload(Some("123456789"), "Chris", "Jones", new LocalDate("2012-02-16"), BirthRegisterCountry.ENGLAND)
      var resultMatch = MockMatchingService.performMatch(payload, invalidGroResponse, MatchingType.FULL)
      resultMatch.isMatch shouldBe false
    }

    "return false when firstName not match" in {

      val payload = Payload(Some("123456789"), "Chris", "Jones", new LocalDate("2012-02-16"), BirthRegisterCountry.ENGLAND)
      var resultMatch = MockMatchingService.performMatch(payload, invalidGroResponse, MatchingType.FULL)
      resultMatch.isMatch shouldBe false
    }

    "return false when lastName not match" in {

      val payload = Payload(Some("123456789"), "Chris", "Jones", new LocalDate("2012-02-16"), BirthRegisterCountry.ENGLAND)
      var resultMatch = MockMatchingService.performMatch(payload, invalidGroResponse, MatchingType.FULL)
      resultMatch.isMatch shouldBe false
    }


    "return false when dob not match" in {

      val payload = Payload(Some("123456789"), "Chris", "Jones", new LocalDate("2012-02-16"), BirthRegisterCountry.ENGLAND)
      var resultMatch = MockMatchingService.performMatch(payload, invalidGroResponse, MatchingType.FULL)
      resultMatch.isMatch shouldBe false
    }
  }




}
