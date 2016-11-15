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
import org.scalatest.BeforeAndAfterAll
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.OneAppPerSuite
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers._
import uk.gov.hmrc.brm.config.BrmConfig
import uk.gov.hmrc.brm.models.brm.Payload
import uk.gov.hmrc.brm.utils.TestHelper._
import uk.gov.hmrc.brm.utils.{BirthRegisterCountry, MatchingType}
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.test.UnitSpec


class MatchingServiceConfigSpec extends UnitSpec with MockitoSugar with BeforeAndAfterAll {

  val configFirstName: Map[String, _] = Map(
    "microservice.services.birth-registration-matching.matching.firstName" -> true,
    "microservice.services.birth-registration-matching.matching.lastName" -> false,
    "microservice.services.birth-registration-matching.matching.dateOfBirth" -> false
  )

  val configLastName: Map[String, _] = Map(
    "microservice.services.birth-registration-matching.matching.firstName" -> false,
    "microservice.services.birth-registration-matching.matching.lastName" -> true,
    "microservice.services.birth-registration-matching.matching.dateOfBirth" -> false
  )

  val configDob: Map[String, _] = Map(
    "microservice.services.birth-registration-matching.matching.firstName" -> false,
    "microservice.services.birth-registration-matching.matching.lastName" -> false,
    "microservice.services.birth-registration-matching.matching.dateOfBirth" -> true
  )

  val configFirstNameLastName: Map[String, _] = Map(
    "microservice.services.birth-registration-matching.matching.firstName" -> true,
    "microservice.services.birth-registration-matching.matching.lastName" -> true,
    "microservice.services.birth-registration-matching.matching.dateOfBirth" -> false
  )

  implicit val hc = HeaderCarrier()

  val firstNameApp = GuiceApplicationBuilder(disabled = Seq(classOf[com.kenshoo.play.metrics.PlayModule])).configure(configFirstName).build()
  val lastNameApp = GuiceApplicationBuilder(disabled = Seq(classOf[com.kenshoo.play.metrics.PlayModule])).configure(configLastName).build()
  val dobApp = GuiceApplicationBuilder(disabled = Seq(classOf[com.kenshoo.play.metrics.PlayModule])).configure(configDob).build()
  val firstNameLastNameApp = GuiceApplicationBuilder(disabled = Seq(classOf[com.kenshoo.play.metrics.PlayModule])).configure(configFirstNameLastName).build()

  "valid payload and valid Record " should {

    "return true result for firstName only match for partial matching" in running(
      firstNameApp
    ) {
      val payload = Payload(Some("123456789"), "Chris", "wrongLastName", new LocalDate("2008-02-16"), BirthRegisterCountry.ENGLAND)
      val resultMatch = MatchingService.performMatch(payload, validRecord, MatchingType.PARTIAL)
      BrmConfig.matchLastName shouldBe false
      resultMatch.isMatch shouldBe true
    }

    "return true result for lastName only match for partial matching" in running(
      lastNameApp
    ) {
      val payload = Payload(Some("123456789"), "wrongFirstName", "Jones", new LocalDate("2008-02-16"), BirthRegisterCountry.ENGLAND)
      val resultMatch = MatchingService.performMatch(payload, validRecord, MatchingType.PARTIAL)
      BrmConfig.matchFirstName shouldBe false
      BrmConfig.matchDateOfBirth shouldBe false
      resultMatch.isMatch shouldBe true
    }

    "return true result for date of birth only match for partial matching" in running(
      dobApp
    ) {
      val payload = Payload(Some("123456789"), "wrongFirstName", "wrongLastName", new LocalDate("2012-02-16"), BirthRegisterCountry.ENGLAND)
      val resultMatch = MatchingService.performMatch(payload, validRecord, MatchingType.PARTIAL)
      BrmConfig.matchFirstName shouldBe false
      resultMatch.isMatch shouldBe true
    }

    "return true result for firstName and LastName only match for partial matching" in running(
      firstNameLastNameApp
    ) {
      val payload = Payload(Some("123456789"), "chris", "Jones", new LocalDate("2012-02-16"), BirthRegisterCountry.ENGLAND)
      val resultMatch = MatchingService.performMatch(payload, validRecord, MatchingType.PARTIAL)
      resultMatch.isMatch shouldBe true
    }
  }
}

class MatchingServiceSpec extends UnitSpec with OneAppPerSuite with MockitoSugar {

  implicit val hc = HeaderCarrier()

  "valid request payload and valid Record " should {

    "return true result match" in {
      val payload = Payload(Some("123456789"), "Chris", "Jones", new LocalDate("2012-02-16"), BirthRegisterCountry.ENGLAND)
      val resultMatch = MatchingService.performMatch(payload, validRecord, MatchingType.FULL)
      resultMatch.isMatch shouldBe true
    }

    "return true when case is different for firstname, lastname" in {
      val payload = Payload(Some("123456789"), "chRis", "joNes", new LocalDate("2012-02-16"), BirthRegisterCountry.ENGLAND)
      val resultMatch = MatchingService.performMatch(payload, validRecord, MatchingType.FULL)
      resultMatch.isMatch shouldBe true
    }


    "return true when case is different for firstName only" in {
      val payload = Payload(Some("123456789"), "chRis", "Jones", new LocalDate("2012-02-16"), BirthRegisterCountry.ENGLAND)
      val resultMatch = MatchingService.performMatch(payload, validRecord, MatchingType.FULL)
      resultMatch.isMatch shouldBe true
    }

    "return true when case is different for lastName only" in {
      val payload = Payload(Some("123456789"), "Chris", "joNES", new LocalDate("2012-02-16"), BirthRegisterCountry.ENGLAND)
      val resultMatch = MatchingService.performMatch(payload, validRecord, MatchingType.FULL)
      resultMatch.isMatch shouldBe true
    }
  }

  "valid request payload and invalid Record " should {

    "return false result match" in {
      val payload = Payload(Some("123456789"), "Chris", "Jones", new LocalDate("2012-02-16"), BirthRegisterCountry.ENGLAND)
      val resultMatch = MatchingService.performMatch(payload, invalidRecord, MatchingType.FULL)
      resultMatch.isMatch shouldBe false
    }

    "return false when firstName not match" in {
      val payload = Payload(Some("123456789"), "Chris", "Jones", new LocalDate("2012-02-16"), BirthRegisterCountry.ENGLAND)
      val resultMatch = MatchingService.performMatch(payload, firstNameNotMatchedRecord, MatchingType.FULL)
      resultMatch.isMatch shouldBe false
    }

    "return false when lastName not match" in {
      val payload = Payload(Some("123456789"), "Chris", "Jones", new LocalDate("2012-02-16"), BirthRegisterCountry.ENGLAND)
      val resultMatch = MatchingService.performMatch(payload, lastNameNotMatchRecord, MatchingType.FULL)
      resultMatch.isMatch shouldBe false
    }

    "return false when dob not match" in {
      val payload = Payload(Some("123456789"), "Chris", "Jones", new LocalDate("2012-02-16"), BirthRegisterCountry.ENGLAND)
      val resultMatch = MatchingService.performMatch(payload, dobNotMatchRecord, MatchingType.FULL)
      resultMatch.isMatch shouldBe false
    }
  }

}
