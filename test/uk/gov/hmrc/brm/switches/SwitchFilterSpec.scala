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

package uk.gov.hmrc.brm.switches

import org.joda.time.LocalDate
import org.scalatestplus.play.OneAppPerTest
import uk.gov.hmrc.brm.models.brm.Payload
import uk.gov.hmrc.brm.utils.BirthRegisterCountry
import uk.gov.hmrc.play.test.UnitSpec

/**
  * Created by mew on 15/05/2017.
  */
class SwitchFilterSpec extends UnitSpec with OneAppPerTest {

  val payloadWithReference = Payload(Some("123456789"), "Adam", "Smith", LocalDate.now, BirthRegisterCountry.ENGLAND)
  val payloadWithoutReference = Payload(None, "Adam", "Smith", LocalDate.now, BirthRegisterCountry.ENGLAND)
  val payloadInvalidDateOfBirth = Payload(None, "Adam", "Smith", LocalDate.parse("2008-12-12"), BirthRegisterCountry.ENGLAND)

  "SwitchFilter" when {

    "for all requests" should {

      "process filters for a request with a valid date of birth" in {
        Filters.process(payloadWithReference) shouldBe true
      }

      "process filters for a request with a failure due to date of birth" in {
        Filters.process(payloadInvalidDateOfBirth) shouldBe false
      }

    }

    "request has BRN" should {

      "process filters for a request" in {
        Filters.process(payloadWithReference) shouldBe true
      }

    }

    "request does not have BRN" should {

      "process filters for a request" in {
        Filters.process(payloadWithoutReference) shouldBe true
      }

    }

  }

}
