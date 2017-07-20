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
import org.scalatest.BeforeAndAfterAll
import org.scalatest.mock.MockitoSugar
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeApplication
import play.api.test.Helpers._
import uk.gov.hmrc.brm.config.BrmConfig
import uk.gov.hmrc.brm.models.brm.Payload
import uk.gov.hmrc.brm.utils.TestHelper._
import uk.gov.hmrc.brm.utils.{BirthRegisterCountry, MatchingType}
import uk.gov.hmrc.brm.{BRMFakeApplication, BaseConfig}
import uk.gov.hmrc.play.audit.http.connector.AuditResult
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future

class PartialMatchingSpec extends UnitSpec with MockitoSugar with BeforeAndAfterAll {

  import uk.gov.hmrc.brm.utils.Mocks._

  val configFirstName: Map[String, _] = BaseConfig.config ++ Map(
    "microservice.services.birth-registration-matching.matching.firstName" -> true,
    "microservice.services.birth-registration-matching.matching.ignoreAdditionalNames" -> true,
    "microservice.services.birth-registration-matching.matching.lastName" -> false,
    "microservice.services.birth-registration-matching.matching.dateOfBirth" -> false
  )

  val configAdditionalAndFirstNames: Map[String, _] = BaseConfig.config ++ Map(
    "microservice.services.birth-registration-matching.matching.firstName" -> true,
    "microservice.services.birth-registration-matching.matching.ignoreAdditionalNames" -> false,
    "microservice.services.birth-registration-matching.matching.lastName" -> false,
    "microservice.services.birth-registration-matching.matching.dateOfBirth" -> false
  )

  val configAdditionalNames: Map[String, _] = BaseConfig.config ++ Map(
    "microservice.services.birth-registration-matching.matching.firstName" -> false,
    "microservice.services.birth-registration-matching.matching.ignoreAdditionalNames" -> false,
    "microservice.services.birth-registration-matching.matching.lastName" -> false,
    "microservice.services.birth-registration-matching.matching.dateOfBirth" -> false
  )

  val configLastName: Map[String, _] = BaseConfig.config ++ Map(
    "microservice.services.birth-registration-matching.matching.firstName" -> false,
    "microservice.services.birth-registration-matching.matching.ignoreAdditionalNames" -> true,
    "microservice.services.birth-registration-matching.matching.lastName" -> true,
    "microservice.services.birth-registration-matching.matching.dateOfBirth" -> false
  )

  val configDob: Map[String, _] = BaseConfig.config ++ Map(
    "microservice.services.birth-registration-matching.matching.firstName" -> false,
    "microservice.services.birth-registration-matching.matching.ignoreAdditionalNames" -> true,
    "microservice.services.birth-registration-matching.matching.lastName" -> false,
    "microservice.services.birth-registration-matching.matching.dateOfBirth" -> true
  )

  val configFirstNameLastName: Map[String, _] = BaseConfig.config ++ Map(
    "microservice.services.birth-registration-matching.matching.firstName" -> true,
    "microservice.services.birth-registration-matching.matching.ignoreAdditionalNames" -> true,
    "microservice.services.birth-registration-matching.matching.lastName" -> true,
    "microservice.services.birth-registration-matching.matching.dateOfBirth" -> false
  )

  implicit val hc = HeaderCarrier()

  val firstNameApp = GuiceApplicationBuilder(disabled = Seq(classOf[com.kenshoo.play.metrics.PlayModule])).configure(configFirstName).build()
  val additionalNamesFirstNameApp = GuiceApplicationBuilder(disabled = Seq(classOf[com.kenshoo.play.metrics.PlayModule])).configure(configAdditionalAndFirstNames).build()
  val additionalNamesApp = GuiceApplicationBuilder(disabled = Seq(classOf[com.kenshoo.play.metrics.PlayModule])).configure(configAdditionalNames).build()
  val lastNameApp = GuiceApplicationBuilder(disabled = Seq(classOf[com.kenshoo.play.metrics.PlayModule])).configure(configLastName).build()
  val dobApp = GuiceApplicationBuilder(disabled = Seq(classOf[com.kenshoo.play.metrics.PlayModule])).configure(configDob).build()
  val firstNameLastNameApp = GuiceApplicationBuilder(disabled = Seq(classOf[com.kenshoo.play.metrics.PlayModule])).configure(configFirstNameLastName).build()

  "Partial Matching (feature switch turned off)" when {

    "match with reference" should {

      "return true result for firstName only" in running(
        firstNameApp
      ) {
        when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))

        val payload = Payload(Some("123456789"), "Chris", Some("test"), "wrongLastName", new LocalDate("2008-02-16"), BirthRegisterCountry.ENGLAND)
        val resultMatch = MockMatchingService.performMatch(payload, List(validRecord), MatchingType.PARTIAL)
        BrmConfig.matchLastName shouldBe false
        BrmConfig.matchFirstName shouldBe true
        resultMatch.matched shouldBe true
      }

      "return true result for additionalName only" in running(
        additionalNamesApp
      ) {
        when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
        val payload = Payload(Some("123456789"), "wrongFirstname", Some("David"), "wrongLastName", new LocalDate("2008-02-16"), BirthRegisterCountry.ENGLAND)
        val resultMatch = MockMatchingService.performMatch(payload, List(validRecordMiddleNames), MatchingType.PARTIAL)
        BrmConfig.ignoreAdditionalNames shouldBe false
        BrmConfig.matchFirstName shouldBe false
        BrmConfig.matchLastName shouldBe false
        BrmConfig.matchDateOfBirth shouldBe false
        resultMatch.matched shouldBe true
      }

      "return true result for firstName and additionalName  only" in running(
        additionalNamesFirstNameApp
      ) {
        when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
        val payload = Payload(Some("123456789"), "Adam", Some("David"), "wrongLastName", new LocalDate("2008-02-16"), BirthRegisterCountry.ENGLAND)
        val resultMatch = MockMatchingService.performMatch(payload, List(validRecordMiddleNames), MatchingType.PARTIAL)
        BrmConfig.ignoreAdditionalNames shouldBe false
        BrmConfig.matchFirstName shouldBe true
        resultMatch.matched shouldBe true
      }

      "return true result for lastName only" in running(
        lastNameApp
      ) {
        when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))

        val payload = Payload(Some("123456789"), "wrongFirstName", None, "Jones", new LocalDate("2008-02-16"), BirthRegisterCountry.ENGLAND)
        val resultMatch = MockMatchingService.performMatch(payload, List(validRecord), MatchingType.PARTIAL)
        BrmConfig.matchFirstName shouldBe false
        BrmConfig.matchDateOfBirth shouldBe false
        resultMatch.matched shouldBe true
      }

      "return true result for date of birth only" in running(
        dobApp
      ) {
        when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))

        val payload = Payload(Some("123456789"), "wrongFirstName", None, "wrongLastName", new LocalDate("2012-02-16"), BirthRegisterCountry.ENGLAND)
        val resultMatch = MockMatchingService.performMatch(payload, List(validRecord), MatchingType.PARTIAL)
        BrmConfig.matchFirstName shouldBe false
        resultMatch.matched shouldBe true
      }

      "return true result for firstName and LastName only" in running(
        firstNameLastNameApp
      ) {
        when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))

        val payload = Payload(Some("123456789"), "chris", None, "Jones", new LocalDate("2012-02-16"), BirthRegisterCountry.ENGLAND)
        val resultMatch = MockMatchingService.performMatch(payload, List(validRecord), MatchingType.PARTIAL)
        resultMatch.matched shouldBe true
      }

    }

    "match without reference" should {

      "return true result for firstName only" in running(
        firstNameApp
      ) {
        when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))

        val payload = Payload(None, "Chris", None, "wrongLastName", new LocalDate("2008-02-16"), BirthRegisterCountry.ENGLAND)
        val resultMatch = MockMatchingService.performMatch(payload, List(validRecord), MatchingType.PARTIAL)
        BrmConfig.matchLastName shouldBe false
        resultMatch.matched shouldBe true
      }

      "return true result for additionalName only" in running(
        additionalNamesApp
      ) {
        when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
        val payload = Payload(None, "wrongFirstname", Some("David"), "wrongLastName", new LocalDate("2008-02-16"), BirthRegisterCountry.ENGLAND)
        val resultMatch = MockMatchingService.performMatch(payload, List(validRecordMiddleNames), MatchingType.PARTIAL)
        BrmConfig.ignoreAdditionalNames shouldBe false
        BrmConfig.matchFirstName shouldBe false
        BrmConfig.matchLastName shouldBe false
        BrmConfig.matchDateOfBirth shouldBe false
        resultMatch.matched shouldBe true
      }

      "return true result for firstName and additionalName  only" in running(
        additionalNamesFirstNameApp
      ) {
        when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
        val payload = Payload(None, "Adam", Some("David"), "wrongLastName", new LocalDate("2008-02-16"), BirthRegisterCountry.ENGLAND)
        val resultMatch = MockMatchingService.performMatch(payload, List(validRecordMiddleNames), MatchingType.PARTIAL)
        BrmConfig.ignoreAdditionalNames shouldBe false
        BrmConfig.matchFirstName shouldBe true
        resultMatch.matched shouldBe true
      }

      "return true result for lastName only" in running(
        lastNameApp
      ) {
        when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))

        val payload = Payload(None, "wrongFirstName", None, "Jones", new LocalDate("2008-02-16"), BirthRegisterCountry.ENGLAND)
        val resultMatch = MockMatchingService.performMatch(payload, List(validRecord), MatchingType.PARTIAL)
        BrmConfig.matchFirstName shouldBe false
        BrmConfig.matchDateOfBirth shouldBe false
        resultMatch.matched shouldBe true
      }

      "return true result for date of birth only" in running(
        dobApp
      ) {
        when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))

        val payload = Payload(None, "wrongFirstName", None, "wrongLastName", new LocalDate("2012-02-16"), BirthRegisterCountry.ENGLAND)
        val resultMatch = MockMatchingService.performMatch(payload, List(validRecord), MatchingType.PARTIAL)
        BrmConfig.matchFirstName shouldBe false
        resultMatch.matched shouldBe true
      }

      "return true result for firstName and LastName only" in running(
        firstNameLastNameApp
      ) {
        when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))

        val payload = Payload(None, "chris", None, "Jones", new LocalDate("2012-02-16"), BirthRegisterCountry.ENGLAND)
        val resultMatch = MockMatchingService.performMatch(payload, List(validRecord), MatchingType.PARTIAL)
        resultMatch.matched shouldBe true
      }

    }

  }

}


/**
  * TODO: add unit tests where additional names is provided
  * UPDATE: This is being tested in MatchingServiceAdditionalNameSpec
  */
class MatchingServiceSpec extends UnitSpec with MockitoSugar with WithFakeApplication {

  import uk.gov.hmrc.brm.utils.Mocks._

  implicit val hc = HeaderCarrier()
  val references = List(Some("123456789"), None)

  val configIgnoreAdditionalNames: Map[String, _] = BaseConfig.config ++ Map(
    "microservice.services.birth-registration-matching.matching.ignoreAdditionalNames" -> false
  )

  def getApp(config: Map[String, _]) = GuiceApplicationBuilder(disabled = Seq(classOf[com.kenshoo.play.metrics.PlayModule])).configure(configIgnoreAdditionalNames).build()

  references.foreach(
    reference => {

      val name = reference match {
        case Some(x) => "with reference"
        case None => "without reference"
      }

      "MatchingService" should {

        s"($name) match when firstName contains special characters" in {
          running(fakeApplication) {
            when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))

            val payload = Payload(reference, "Chris-Jame's", None, "Jones", new LocalDate("2012-02-16"), BirthRegisterCountry.ENGLAND)
            val resultMatch = MockMatchingService.performMatch(payload, List(validRecordSpecialCharactersFirstName), MatchingType.FULL)
            resultMatch.matched shouldBe true
          }
        }

        s"($name) match when lastName contains special characters" in {
          running(fakeApplication) {
            when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))

            val payload = Payload(reference, "Chris", None, "Jones--Smith", new LocalDate("2012-02-16"), BirthRegisterCountry.ENGLAND)
            val resultMatch = MockMatchingService.performMatch(payload, List(validRecordSpecialCharactersLastName), MatchingType.FULL)
            resultMatch.matched shouldBe true
          }
        }

        s"($name) match when firstName contains space" in {
          running(fakeApplication) {
            when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))

            val payload = Payload(reference, "Chris James", None, "Jones", new LocalDate("2012-02-16"), BirthRegisterCountry.ENGLAND)
            val resultMatch = MockMatchingService.performMatch(payload, List(validRecordFirstNameSpace), MatchingType.FULL)
            resultMatch.matched shouldBe true
          }
        }

        s"($name) match when lastName contains space" in {
          running(fakeApplication) {
            when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))

            val payload = Payload(reference, "Chris", None, "Jones Smith", new LocalDate("2012-02-16"), BirthRegisterCountry.ENGLAND)
            val resultMatch = MockMatchingService.performMatch(payload, List(validRecordLastNameSpace), MatchingType.FULL)
            resultMatch.matched shouldBe true
          }
        }

        s"($name) match when lastName from record contains multiple spaces between names" in {
          running(fakeApplication) {
            when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))

            val payload = Payload(reference, "Chris", None, "Jones  Smith", new LocalDate("2012-02-16"), BirthRegisterCountry.ENGLAND)
            val resultMatch = MockMatchingService.performMatch(payload, List(validRecordLastNameSpace), MatchingType.FULL)
            resultMatch.matched shouldBe true
          }
        }

        s"($name) match when lastName from payload contains multiple spaces between names and includes space at beginning and end of string" in {
          running(fakeApplication) {
            when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))

            val payload = Payload(reference, "Chris", None, "  Jones  Smith  ", new LocalDate("2012-02-16"), BirthRegisterCountry.ENGLAND)
            val resultMatch = MockMatchingService.performMatch(payload, List(validRecordLastNameSpace), MatchingType.FULL)
            resultMatch.matched shouldBe true
          }
        }

        s"($name) match when lastName from payload contains multiple spaces between names" in {
          running(fakeApplication) {
            when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))

            val payload = Payload(reference, "Chris", None, "Jones Smith", new LocalDate("2012-02-16"), BirthRegisterCountry.ENGLAND)
            val resultMatch = MockMatchingService.performMatch(payload, List(validRecordLastNameMultipleSpace), MatchingType.FULL)
            resultMatch.matched shouldBe true
          }
        }

        s"($name) match when lastName from record contains multiple spaces between names and includes space at beginning and end of string" in {
          running(fakeApplication) {
            when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))

            val payload = Payload(reference, "Chris", None, "Jones Smith", new LocalDate("2012-02-16"), BirthRegisterCountry.ENGLAND)
            val resultMatch = MockMatchingService.performMatch(payload, List(validRecordLastNameMultipleSpaceBeginningTrailing), MatchingType.FULL)
            resultMatch.matched shouldBe true
          }
        }

        s"($name) match when firstName contains UTF-8 characters" in {
          running(fakeApplication) {
            when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))

            val payload = Payload(reference, "Chrîs", None, "Jones", new LocalDate("2012-02-16"), BirthRegisterCountry.ENGLAND)
            val resultMatch = MockMatchingService.performMatch(payload, List(validRecordUTF8FirstName), MatchingType.FULL)
            resultMatch.matched shouldBe true
          }
        }

        s"($name) match when lastName contains UTF-8 characters" in {
          running(fakeApplication) {
            when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))

            val payload = Payload(reference, "Chris", None, "Jonéş", new LocalDate("2012-02-16"), BirthRegisterCountry.ENGLAND)
            val resultMatch = MockMatchingService.performMatch(payload, List(validRecordUTF8LastName), MatchingType.FULL)
            resultMatch.matched shouldBe true
          }
        }

        s"($name) match for exact match on firstName and lastName and dateOfBirth on both input and record" in {
          running(fakeApplication) {
            when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))

            val payload = Payload(reference, "Chris", None, "Jones", new LocalDate("2012-02-16"), BirthRegisterCountry.ENGLAND)
            val resultMatch = MockMatchingService.performMatch(payload, List(validRecord), MatchingType.FULL)
            resultMatch.matched shouldBe true
          }
        }

        s"($name) match when case is different for firstName, lastName on input" in {
          running(fakeApplication) {
            when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))

            val payload = Payload(reference, "chRis", None, "joNes", new LocalDate("2012-02-16"), BirthRegisterCountry.ENGLAND)
            val resultMatch = MockMatchingService.performMatch(payload, List(validRecord), MatchingType.FULL)
            resultMatch.matched shouldBe true
          }
        }

        s"($name) match when case is different for firstName, lastName on record" in {
          running(fakeApplication) {
            when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))

            val payload = Payload(reference, "Chris", None, "Jones", new LocalDate("2012-02-16"), BirthRegisterCountry.ENGLAND)
            val resultMatch = MockMatchingService.performMatch(payload, List(wrongCaseValidRecord), MatchingType.FULL)
            resultMatch.matched shouldBe true
          }
        }

        s"($name) match when case is uppercase for firstName, lastName on input" in {
          running(fakeApplication) {
            when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))

            val payload = Payload(reference, "CHRIS", None, "JONES", new LocalDate("2012-02-16"), BirthRegisterCountry.ENGLAND)
            val resultMatch = MockMatchingService.performMatch(payload, List(validRecord), MatchingType.FULL)
            resultMatch.matched shouldBe true
          }
        }

        s"($name) match when case is uppercase for firstName, lastName on record" in {
          running(fakeApplication) {
            when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))

            val payload = Payload(reference, "CHRIS", None, "JONES", new LocalDate("2012-02-16"), BirthRegisterCountry.ENGLAND)
            val resultMatch = MockMatchingService.performMatch(payload, List(validRecordUppercase), MatchingType.FULL)
            resultMatch.matched shouldBe true
          }
        }

        s"($name) match when case is different for firstName on input" in {
          running(fakeApplication) {
            when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))

            val payload = Payload(reference, "chRis", None, "Jones", new LocalDate("2012-02-16"), BirthRegisterCountry.ENGLAND)
            val resultMatch = MockMatchingService.performMatch(payload, List(validRecord), MatchingType.FULL)
            resultMatch.matched shouldBe true
          }
        }

        s"($name) match when case is different for firstName on record" in {
          running(fakeApplication) {
            when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))

            val payload = Payload(reference, "Chris", None, "Jones", new LocalDate("2012-02-16"), BirthRegisterCountry.ENGLAND)
            val resultMatch = MockMatchingService.performMatch(payload, List(wrongCaseFirstNameValidRecord), MatchingType.FULL)
            resultMatch.matched shouldBe true
          }
        }

        s"($name) match when case is different for lastName on input" in {
          running(fakeApplication) {
            when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))

            val payload = Payload(reference, "Chris", None, "joNES", new LocalDate("2012-02-16"), BirthRegisterCountry.ENGLAND)
            val resultMatch = MockMatchingService.performMatch(payload, List(validRecord), MatchingType.FULL)
            resultMatch.matched shouldBe true
          }
        }

        s"($name) match when case is different for lastName on record" in {
          running(fakeApplication) {
            when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))

            val payload = Payload(reference, "Chris", None, "Jones", new LocalDate("2012-02-16"), BirthRegisterCountry.ENGLAND)
            val resultMatch = MockMatchingService.performMatch(payload, List(wrongCaseLastNameValidRecord), MatchingType.FULL)
            resultMatch.matched shouldBe true
          }
        }

        s"($name) not match when firstName and lastName are different on the input" in {
          running(fakeApplication) {
            when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))

            val payload = Payload(reference, "Christopher", None, "Jones", new LocalDate("2012-02-16"), BirthRegisterCountry.ENGLAND)
            val resultMatch = MockMatchingService.performMatch(payload, List(validRecord), MatchingType.FULL)
            resultMatch.matched shouldBe false
          }
        }

        s"($name) not match when firstName and lastName are different on the record" in {
          running(fakeApplication) {
            when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))

            val payload = Payload(reference, "Chris", None, "Jones", new LocalDate("2012-02-16"), BirthRegisterCountry.ENGLAND)
            val resultMatch = MockMatchingService.performMatch(payload, List(invalidRecord), MatchingType.FULL)
            resultMatch.matched shouldBe false
          }
        }

        s"($name) not match when firstName is different on input" in {
          running(fakeApplication) {
            when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))

            val payload = Payload(reference, "Christopher", None, "Jones", new LocalDate("2012-02-16"), BirthRegisterCountry.ENGLAND)
            val resultMatch = MockMatchingService.performMatch(payload, List(validRecord), MatchingType.FULL)
            resultMatch.matched shouldBe false
          }
        }

        s"($name) not match when firstName is different on record" in {
          running(fakeApplication) {
            when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))

            val payload = Payload(reference, "Christopher", None, "Jones", new LocalDate("2012-02-16"), BirthRegisterCountry.ENGLAND)
            val resultMatch = MockMatchingService.performMatch(payload, List(firstNameNotMatchedRecord), MatchingType.FULL)
            resultMatch.matched shouldBe false
          }
        }

        s"($name) not match when lastName is different on input" in {
          running(fakeApplication) {
            when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))

            val payload = Payload(reference, "Chris", None, "Jone", new LocalDate("2012-02-16"), BirthRegisterCountry.ENGLAND)
            val resultMatch = MockMatchingService.performMatch(payload, List(validRecord), MatchingType.FULL)
            resultMatch.matched shouldBe false
          }
        }

        s"($name) not match when lastName is different on record" in {
          running(fakeApplication) {
            when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))

            val payload = Payload(reference, "Chris", None, "Jones", new LocalDate("2012-02-16"), BirthRegisterCountry.ENGLAND)
            val resultMatch = MockMatchingService.performMatch(payload, List(lastNameNotMatchRecord), MatchingType.FULL)
            resultMatch.matched shouldBe false
          }
        }

        s"($name) not match when dateOfBirth is different on input" in {
          running(fakeApplication) {
            when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))

            val payload = Payload(reference, "Chris", None, "Jones", new LocalDate("2012-02-15"), BirthRegisterCountry.ENGLAND)
            val resultMatch = MockMatchingService.performMatch(payload, List(validRecord), MatchingType.FULL)
            resultMatch.matched shouldBe false
          }
        }

        s"($name) not match when dateOfBirth is different on record" in {
          running(fakeApplication) {
            when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))

            val payload = Payload(reference, "Chris", None, "Jones", new LocalDate("2012-02-15"), BirthRegisterCountry.ENGLAND)
            val resultMatch = MockMatchingService.performMatch(payload, List(dobNotMatchRecord), MatchingType.FULL)
            resultMatch.matched shouldBe false
          }
        }
      }
    })

}

