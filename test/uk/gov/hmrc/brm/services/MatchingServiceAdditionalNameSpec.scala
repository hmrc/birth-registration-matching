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
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeApplication
import play.api.test.Helpers._
import uk.gov.hmrc.brm.{BRMFakeApplication, BaseConfig}
import uk.gov.hmrc.brm.models.brm.Payload
import uk.gov.hmrc.brm.models.response.Child
import uk.gov.hmrc.brm.services.matching.MatchingService
import uk.gov.hmrc.brm.utils.{BaseUnitSpec, BirthRegisterCountry, MatchingType}
import uk.gov.hmrc.brm.utils.TestHelper._
import uk.gov.hmrc.play.audit.http.connector.AuditResult
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.Future


/**
  *
  * TODO: update these tests to check for result.names.$ shouldBe ""
  */

class MatchingServiceAdditionalNameSpec extends UnitSpec with MockitoSugar with BRMFakeApplication with BaseUnitSpec {

    import uk.gov.hmrc.brm.utils.Mocks._

    val ignoreAdditionalNamesEnabled: Map[String, _] = BaseConfig.config ++ Map(
      "microservice.services.birth-registration-matching.matching.ignoreAdditionalNames" -> true
    )

    val ignoreAdditionalNamesDisabled: Map[String, _] = BaseConfig.config ++ Map(
      "microservice.services.birth-registration-matching.matching.ignoreAdditionalNames" -> false
    )
    implicit val hc = HeaderCarrier()

    def getApp(config: Map[String, _]) = GuiceApplicationBuilder(disabled = Seq(classOf[com.kenshoo.play.metrics.PlayModule])).configure(config).build()

    "configuring" should {

      "set match-on-multiple switch" in {
        MatchingService.matchOnMultiple.isInstanceOf[Boolean] shouldBe true
      }
    }

    "Matching" when {

      /*
        Multiple records
        Currently always returning false as we don't iterate over multiple records
       */

      "multiple records" when {

        "matching on multiple is true" should {

          "should return true if a minimum of one record matches" in {

            val payload = Payload(Some("123456789"), "Chris", None, "Jones", new LocalDate("2012-02-16"), BirthRegisterCountry.ENGLAND)
            val resultMatch = MockMatchingService.performMatch(payload, List(invalidRecord, validRecord), MatchingType.FULL)
            resultMatch.matched shouldBe true
          }

          "return false if no records match" in {

            val payload = Payload(Some("123456789"), "Chris", None, "Jones", new LocalDate("2012-02-16"), BirthRegisterCountry.ENGLAND)
            val resultMatch = MockMatchingService.performMatch(payload, List(invalidRecord, invalidRecord), MatchingType.FULL)
            resultMatch.matched shouldBe false
          }

          "return false result match when List is empty" in {

            val payload = Payload(Some("123456789"), "Chris", None, "Jones", new LocalDate("2012-02-16"), BirthRegisterCountry.ENGLAND)
            val resultMatch = MockMatchingService.performMatch(payload, List(), MatchingType.FULL)
            resultMatch.matched shouldBe false
          }

        }

        "matching on multiple is false" should {

          "return false for more than 1 record" in {

            val payload = Payload(Some("123456789"), "Chris", None, "Jones", new LocalDate("2012-02-16"), BirthRegisterCountry.ENGLAND)
            val resultMatch = MockMatchingServiceMatchMultipleFalse.performMatch(payload, List(validRecord, validRecord, validRecord), MatchingType.FULL)
            resultMatch.matched shouldBe false
          }

          "return true for a match on a single record" in {
            when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))

            val payload = Payload(Some("123456789"), "Chris", None, "Jones", new LocalDate("2012-02-16"), BirthRegisterCountry.ENGLAND)
            val resultMatch = MockMatchingServiceMatchMultipleFalse.performMatch(payload, List(validRecord), MatchingType.FULL)
            resultMatch.matched shouldBe true
          }

          "return false for a no match on a single record" in {
            when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))

            val payload = Payload(Some("123456789"), "Christopher", None, "Jones", new LocalDate("2012-02-16"), BirthRegisterCountry.ENGLAND)
            val resultMatch = MockMatchingServiceMatchMultipleFalse.performMatch(payload, List(validRecord), MatchingType.FULL)
            resultMatch.matched shouldBe false
          }

          "return false result match when List is empty" in {
            when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))

            val payload = Payload(Some("123456789"), "Chris", None, "Jones", new LocalDate("2012-02-16"), BirthRegisterCountry.ENGLAND)
            val resultMatch = MockMatchingServiceMatchMultipleFalse.performMatch(payload, List(), MatchingType.FULL)
            resultMatch.matched shouldBe false
          }

        }

      }

      /*
        Single record

        - Exact match
        - Camel case matching
        - Lower case matching
        - Upper case on input matching
        - Upper case on record matching
       */

      "matching a single record" should {

        val references = List(Some("123456789"), None)

        references.foreach(
          reference => {

            val name = reference match {
              case Some(x) => "with reference"
              case None => "without reference"
            }

            "ignore middle names with feature toggle enabled" should {

              s"($name) match when firstName argument has all middle names on input that are on the record" in {
                running(FakeApplication(additionalConfiguration = ignoreAdditionalNamesEnabled)) {

                  val payload = Payload(reference, "Adam David", None, "Jones", new LocalDate("2012-02-16"), BirthRegisterCountry.ENGLAND)
                  val resultMatch = MockMatchingService.performMatch(payload, List(validRecordMiddleNames), MatchingType.FULL)
                  resultMatch.matched shouldBe true
                  resultMatch.names.firstNames shouldBe "Adam David"
                  resultMatch.names.additionalNames shouldBe empty
                  resultMatch.names.lastNames shouldBe "Jones"
                }
              }


              s"($name) match when firstName argument has all middle names on input that are on the record and ignore any additional name provided in payload." in {
                running(FakeApplication(additionalConfiguration = ignoreAdditionalNamesEnabled)) {

                  val payload = Payload(reference, "Adam David", Some("test"), "Jones", new LocalDate("2012-02-16"), BirthRegisterCountry.ENGLAND)
                  val resultMatch = MockMatchingService.performMatch(payload, List(validRecordMiddleNames), MatchingType.FULL)
                  resultMatch.matched shouldBe true
                  resultMatch.names.firstNames shouldBe "Adam David"
                  resultMatch.names.additionalNames shouldBe empty
                  resultMatch.names.lastNames shouldBe "Jones"
                }
              }

              s"($name) match when firstName argument has all middle names on input that on are the record, with additional spaces" in {
                running(FakeApplication(additionalConfiguration = ignoreAdditionalNamesEnabled)) {

                  val payload = Payload(reference, " Adam    David   ", None, "Jones", new LocalDate("2012-02-16"), BirthRegisterCountry.ENGLAND)
                  val resultMatch = MockMatchingService.performMatch(payload, List(validRecordMiddleNames), MatchingType.FULL)
                  resultMatch.matched shouldBe true
                  resultMatch.names.firstNames shouldBe "Adam David"
                  resultMatch.names.additionalNames shouldBe empty
                  resultMatch.names.lastNames shouldBe "Jones"
                }
              }

              s"($name) match when firstName argument has all middle names on input that on are the record, with additional spaces on the record" in {
                running(FakeApplication(additionalConfiguration = ignoreAdditionalNamesEnabled)) {

                  val payload = Payload(reference, "Adam David", None, "Jones", new LocalDate("2012-02-16"), BirthRegisterCountry.ENGLAND)
                  val resultMatch = MockMatchingService.performMatch(payload, List(validRecordMiddleNamesWithSpaces), MatchingType.FULL)
                  resultMatch.matched shouldBe true
                  resultMatch.names.firstNames shouldBe "Adam David"
                  resultMatch.names.additionalNames shouldBe empty
                  resultMatch.names.lastNames shouldBe "Jones"
                }
              }

              s"($name) match when firstName argument has middle names with punctuation, with additional names on record" in {
                running(FakeApplication(additionalConfiguration = ignoreAdditionalNamesEnabled)) {

                  val payload = Payload(reference, "   Jamie  Mary-Ann'é ", None, "Jones", new LocalDate("2012-02-16"), BirthRegisterCountry.ENGLAND)
                  val resultMatch = MockMatchingService.performMatch(payload, List(validRecordMiddleNamesWithSpacesAndPunctuation), MatchingType.FULL)
                  resultMatch.matched shouldBe true
                  resultMatch.names.firstNames shouldBe "Jamie Mary-Ann'é"
                  resultMatch.names.additionalNames shouldBe empty
                  resultMatch.names.lastNames shouldBe "Jones"
                }
              }

              s"($name) match when firstName argument has no middle names on input that are on the record" in {
                running(FakeApplication(additionalConfiguration = ignoreAdditionalNamesEnabled)) {

                  val payload = Payload(reference, "Adam", None, "Jones", new LocalDate("2012-02-16"), BirthRegisterCountry.ENGLAND)
                  val resultMatch = MockMatchingService.performMatch(payload, List(validRecordMiddleNames), MatchingType.FULL)
                  resultMatch.matched shouldBe true
                  resultMatch.names.firstNames shouldBe "Adam"
                  resultMatch.names.additionalNames shouldBe empty
                  resultMatch.names.lastNames shouldBe "Jones"
                }
              }

              s"($name) match when firstName argument has no middle names on input that are on the record, with additional spaces" in {
                running(FakeApplication(additionalConfiguration = ignoreAdditionalNamesEnabled)) {

                  val payload = Payload(reference, "    Adam     ", None, "Jones", new LocalDate("2012-02-16"), BirthRegisterCountry.ENGLAND)
                  val resultMatch = MockMatchingService.performMatch(payload, List(validRecordMiddleNames), MatchingType.FULL)
                  resultMatch.matched shouldBe true
                  resultMatch.names.firstNames shouldBe "Adam"
                  resultMatch.names.additionalNames shouldBe empty
                  resultMatch.names.lastNames shouldBe "Jones"
                }
              }

              s"($name) not match when firstName argument has too many names not on the record" in {
                running(FakeApplication(additionalConfiguration = ignoreAdditionalNamesEnabled)) {
                  when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))

                  val payload = Payload(reference, "Adam David James", None, "Jones", new LocalDate("2012-02-16"), BirthRegisterCountry.ENGLAND)
                  val resultMatch = MockMatchingService.performMatch(payload, List(validRecordMiddleNames), MatchingType.FULL)
                  resultMatch.matched shouldBe false
                  resultMatch.names.firstNames shouldBe "Adam David"
                  resultMatch.names.additionalNames shouldBe empty
                  resultMatch.names.lastNames shouldBe "Jones"
                }
              }

              s"($name) not match when firstName argument has too many names not on the record, with additional spaces" in {
                running(FakeApplication(additionalConfiguration = ignoreAdditionalNamesEnabled)) {
                  when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))

                  val payload = Payload(reference, "   Adam  David     James  ", None, "Jones", new LocalDate("2012-02-16"), BirthRegisterCountry.ENGLAND)
                  val resultMatch = MockMatchingService.performMatch(payload, List(validRecordMiddleNames), MatchingType.FULL)
                  resultMatch.matched shouldBe false
                  resultMatch.names.firstNames shouldBe "Adam David"
                  resultMatch.names.additionalNames shouldBe empty
                  resultMatch.names.lastNames shouldBe "Jones"
                }
              }

              s"($name) match when lastName from record contains multiple spaces between names and includes space at beginning and end of string" in {
                running(FakeApplication(additionalConfiguration = ignoreAdditionalNamesEnabled)) {
                  when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))

                  val payload = Payload(reference, "Chris", None, "Jones Smith", new LocalDate("2012-02-16"), BirthRegisterCountry.ENGLAND)
                  val resultMatch = MockMatchingService.performMatch(payload, List(validRecordLastNameMultipleSpaceBeginningTrailing), MatchingType.FULL)
                  resultMatch.matched shouldBe true
                  resultMatch.names.firstNames shouldBe "Chris"
                  resultMatch.names.additionalNames shouldBe empty
                  resultMatch.names.lastNames shouldBe "Jones Smith"
                }
              }

            }

            "not ignore middle names with feature toggle disabled" should {

              s"($name) match when firstName argument has all middle names on input that are on the record" in {
                running(FakeApplication(additionalConfiguration = ignoreAdditionalNamesDisabled)) {

                  val payload = Payload(reference, "Adam David", None, "Jones", new LocalDate("2012-02-16"), BirthRegisterCountry.ENGLAND)
                  val resultMatch = MockMatchingService.performMatch(payload, List(validRecordMiddleNames), MatchingType.FULL)
                  resultMatch.matched shouldBe true
                  resultMatch.names.firstNames shouldBe "Adam David"
                  resultMatch.names.additionalNames shouldBe empty
                  resultMatch.names.lastNames shouldBe "Jones"
                }
              }

              s"($name) match true when payload has more than one additional name and record has same addtional name." in {
                running(FakeApplication(additionalConfiguration = ignoreAdditionalNamesDisabled)) {

                  val payload = Payload(reference, "Adam", Some("test test"), "Jones", new LocalDate("2012-02-16"), BirthRegisterCountry.ENGLAND)
                  val resultMatch = MockMatchingService.performMatch(payload, List(adamTestTestJonesRecord), MatchingType.FULL)
                  resultMatch.matched shouldBe true
                  resultMatch.names.firstNames shouldBe "Adam"
                  resultMatch.names.additionalNames shouldBe "test test"
                  resultMatch.names.lastNames shouldBe "Jones"
                }
              }


              s"($name) match when firstName with additional name  has all middle names on input that are on the record" in {
                running(FakeApplication(additionalConfiguration = ignoreAdditionalNamesDisabled)) {

                  val payload = Payload(reference, "Adam ", Some("David"), "Jones", new LocalDate("2012-02-16"), BirthRegisterCountry.ENGLAND)
                  val resultMatch = MockMatchingService.performMatch(payload, List(validRecordMiddleNames), MatchingType.FULL)
                  resultMatch.matched shouldBe true
                  resultMatch.names.firstNames shouldBe "Adam"
                  resultMatch.names.additionalNames shouldBe "David"
                  resultMatch.names.lastNames shouldBe "Jones"
                }
              }


              s"($name) match false when payload has no additional name but record has additional name." in {
                running(FakeApplication(additionalConfiguration = ignoreAdditionalNamesDisabled)) {

                  val payload = Payload(reference, "Adam ", None, "Jones", new LocalDate("2012-02-16"), BirthRegisterCountry.ENGLAND)
                  val resultMatch = MockMatchingService.performMatch(payload, List(validRecordMiddleNames), MatchingType.FULL)
                  resultMatch.matched shouldBe false
                  resultMatch.names.firstNames shouldBe "Adam"
                  resultMatch.names.additionalNames shouldBe "David"
                  resultMatch.names.lastNames shouldBe "Jones"
                }
              }

              s"($name) match false when record has empty firstName." in {
                running(FakeApplication(additionalConfiguration = ignoreAdditionalNamesDisabled)) {

                  val payload = Payload(reference, "Adam ", None, "Jones", new LocalDate("2012-02-16"), BirthRegisterCountry.ENGLAND)
                  val resultMatch = MockMatchingService.performMatch(payload, List(invalidRecordFirstName), MatchingType.FULL)
                  resultMatch.matched shouldBe false
                  resultMatch.names.firstNames shouldBe empty
                  resultMatch.names.additionalNames shouldBe empty
                  resultMatch.names.lastNames shouldBe empty
                }
              }

              s"($name) not match when firstName argument has a missing first name on input that is on the record" in {
                running(FakeApplication(additionalConfiguration = ignoreAdditionalNamesDisabled)) {

                  val payload = Payload(reference, "Adam", None, "Jones", new LocalDate("2012-02-16"), BirthRegisterCountry.ENGLAND)
                  val resultMatch = MockMatchingService.performMatch(payload, List(validRecordMiddleNames), MatchingType.FULL)
                  resultMatch.matched shouldBe false
                  resultMatch.names.firstNames shouldBe "Adam"
                  resultMatch.names.additionalNames shouldBe "David"
                  resultMatch.names.lastNames shouldBe "Jones"
                }
              }

              s"($name) match when firstName argument has all middle names on input that on are the record, with additional spaces" in {
                running(FakeApplication(additionalConfiguration = ignoreAdditionalNamesDisabled)) {

                  val payload = Payload(reference, " Adam    David   ", None, "Jones", new LocalDate("2012-02-16"), BirthRegisterCountry.ENGLAND)
                  val resultMatch = MockMatchingService.performMatch(payload, List(validRecordMiddleNames), MatchingType.FULL)
                  resultMatch.matched shouldBe true
                  resultMatch.names.firstNames shouldBe "Adam David"
                  resultMatch.names.additionalNames shouldBe empty
                  resultMatch.names.lastNames shouldBe "Jones"
                }
              }

              s"($name) match when firstName argument has all middle names on input that on are the record, with additional spaces on the record" in {
                running(FakeApplication(additionalConfiguration = ignoreAdditionalNamesDisabled)) {

                  val payload = Payload(reference, "Adam David", None, "Jones", new LocalDate("2012-02-16"), BirthRegisterCountry.ENGLAND)
                  val resultMatch = MockMatchingService.performMatch(payload, List(validRecordMiddleNamesWithSpaces), MatchingType.FULL)
                  resultMatch.matched shouldBe true
                  resultMatch.names.firstNames shouldBe "Adam David"
                  resultMatch.names.additionalNames shouldBe empty
                  resultMatch.names.lastNames shouldBe "Jones"
                }
              }

              s"($name) not match when firstName argument has too many names not on the record" in {
                running(FakeApplication(additionalConfiguration = ignoreAdditionalNamesDisabled)) {

                  val payload = Payload(reference, "Adam David James", None, "Jones", new LocalDate("2012-02-16"), BirthRegisterCountry.ENGLAND)
                  val resultMatch = MockMatchingService.performMatch(payload, List(validRecordMiddleNames), MatchingType.FULL)
                  resultMatch.matched shouldBe false
                  resultMatch.names.firstNames shouldBe "Adam David"
                  resultMatch.names.additionalNames shouldBe empty
                  resultMatch.names.lastNames shouldBe "Jones"
                }
              }

              s"($name) not match when firstName with additional name has too many names not on the record" in {
                running(FakeApplication(additionalConfiguration = ignoreAdditionalNamesDisabled)) {

                  val payload = Payload(reference, "Adam ", Some(" David James "), "Jones", new LocalDate("2012-02-16"), BirthRegisterCountry.ENGLAND)
                  val resultMatch = MockMatchingService.performMatch(payload, List(validRecordMiddleNames), MatchingType.FULL)
                  resultMatch.matched shouldBe false
                  resultMatch.names.firstNames shouldBe "Adam"
                  resultMatch.names.additionalNames shouldBe "David"
                  resultMatch.names.lastNames shouldBe "Jones"
                }
              }

              s"($name) not match when firstName argument has too many names not on the record, with additional spaces on record" in {
                running(FakeApplication(additionalConfiguration = ignoreAdditionalNamesDisabled)) {

                  val payload = Payload(reference, "Adam David James", None, "Jones", new LocalDate("2012-02-16"), BirthRegisterCountry.ENGLAND)
                  val resultMatch = MockMatchingService.performMatch(payload, List(validRecordMiddleNamesWithSpaces), MatchingType.FULL)
                  resultMatch.matched shouldBe false
                  resultMatch.names.firstNames shouldBe "Adam David"
                  resultMatch.names.additionalNames shouldBe empty
                  resultMatch.names.lastNames shouldBe "Jones"
                }
              }

              s"($name) match when lastName from record contains multiple spaces between names and includes space at beginning and end of string" in {
                running(FakeApplication(additionalConfiguration = ignoreAdditionalNamesDisabled)) {

                  val payload = Payload(reference, "Chris", None, "Jones Smith", new LocalDate("2012-02-16"), BirthRegisterCountry.ENGLAND)
                  val resultMatch = MockMatchingService.performMatch(payload, List(validRecordLastNameMultipleSpaceBeginningTrailing), MatchingType.FULL)
                  resultMatch.matched shouldBe true
                  resultMatch.names.firstNames shouldBe "Chris"
                  resultMatch.names.additionalNames shouldBe empty
                  resultMatch.names.lastNames shouldBe "Jones Smith"
                }
              }

              s"($name) match when firstname contains multiple name, addtiional contains multiple names and record has same on it." in {
                running(FakeApplication(additionalConfiguration = ignoreAdditionalNamesDisabled)) {
                  var record = getRecord("Manish test   test      one  test    two    three", "joshi")
                  val payload = Payload(reference, "Manish test", Some("test   one    test    two  three  "), "joshi", new LocalDate("2012-02-16"), BirthRegisterCountry.ENGLAND)
                  val resultMatch = MockMatchingService.performMatch(payload, List(record), MatchingType.FULL)
                  resultMatch.matched shouldBe true
                  resultMatch.names.firstNames shouldBe "Manish test"
                  resultMatch.names.additionalNames shouldBe "test one test two three"
                  resultMatch.names.lastNames shouldBe "joshi"
                }
              }

              s"($name) not match when firstname contains multiple name, addtiional contains multiple names and record dont have same name." in {
                running(FakeApplication(additionalConfiguration = ignoreAdditionalNamesDisabled)) {
                  var record = getRecord("Manish test test one test two three", "joshi")
                  val payload = Payload(reference, "Manish test", Some("test one test two  "), "joshi", new LocalDate("2012-02-16"), BirthRegisterCountry.ENGLAND)
                  val resultMatch = MockMatchingService.performMatch(payload, List(record), MatchingType.FULL)
                  resultMatch.matched shouldBe false
                  resultMatch.names.firstNames shouldBe "Manish test"
                  resultMatch.names.additionalNames shouldBe "test one test two three"
                  resultMatch.names.lastNames shouldBe "joshi"
                }
              }

              s"($name) not match when firstname contains multiple name, addtiional contains multiple names and record dont have in same order." in {
                running(FakeApplication(additionalConfiguration = ignoreAdditionalNamesDisabled)) {
                  var record = getRecord("Manish  test         test  one test      two three", "joshi")
                  val payload = Payload(reference, "Manish test", Some("test  three      test  two   one "), "joshi", new LocalDate("2012-02-16"), BirthRegisterCountry.ENGLAND)
                  val resultMatch = MockMatchingService.performMatch(payload, List(record), MatchingType.FULL)
                  resultMatch.matched shouldBe false
                  resultMatch.names.firstNames shouldBe "Manish test"
                  resultMatch.names.additionalNames shouldBe "test one test two three"
                  resultMatch.names.lastNames shouldBe "joshi"
                }
              }

              s"($name) match when firstname and additional name  contains special character name and record have same name." in {
                running(FakeApplication(additionalConfiguration = ignoreAdditionalNamesDisabled)) {
                  var record = getRecord("ÀÁÂÃÄÅÆÇÈÉÊËÌÍ ÏÐÑÒÓÔÕÖ×ØÙÚÛÜÝÞßàáâãäåæçèéêëìíîïðñòóôõö÷øùúûüýþÿÀÁÂÃÄÅÆÇÈÉÊËÌÍ ÏÐÑÒÓÔÕÖ×ØÙÚÛÜÝÞßàáâãäåæçèéêëìíîïðñòóôõö÷øùú111111ÀÁÂÃÄÅÆÇÈÉÊËÌÍ ÏÐÑÒÓÔÕÖ×ØÙÚÛÜÝÞßàáâãäåæçèéêëìíîïðñòóôõö÷øùúûüýþÿÀÁÂÃÄÅÆÇÈÉÊËÌÍ ÏÐÑÒÓÔÕÖ×ØÙÚÛÜÝÞßàáâãäåæçèéêëìíîïðñòóôõö÷ø", "joshi")
                  val payload = Payload(reference, "ÀÁÂÃÄÅÆÇÈÉÊËÌÍ ÏÐÑÒÓÔÕÖ×ØÙÚÛÜÝÞßàáâãäåæçèéêëìíîïðñòóôõö÷øùúûüýþÿÀÁÂÃÄÅÆÇÈÉÊËÌÍ ÏÐÑÒÓÔÕÖ×ØÙÚÛÜÝÞßàáâãäåæçèéêëìíîïðñòóôõö÷øùú111111ÀÁÂÃÄÅÆÇÈÉÊËÌÍ ÏÐÑÒÓÔÕÖ×ØÙÚÛÜÝÞßàáâãäåæçèéêëìíîïðñòóôõö÷øùúûüýþÿÀÁÂÃÄÅÆÇÈÉÊËÌÍ ", Some("ÏÐÑÒÓÔÕÖ×ØÙÚÛÜÝÞßàáâãäåæçèéêëìíîïðñòóôõö÷ø"), "joshi", new LocalDate("2012-02-16"), BirthRegisterCountry.ENGLAND)
                  val resultMatch = MockMatchingService.performMatch(payload, List(record), MatchingType.FULL)
                  resultMatch.matched shouldBe true
                  resultMatch.names.firstNames shouldBe "ÀÁÂÃÄÅÆÇÈÉÊËÌÍ ÏÐÑÒÓÔÕÖ×ØÙÚÛÜÝÞßàáâãäåæçèéêëìíîïðñòóôõö÷øùúûüýþÿÀÁÂÃÄÅÆÇÈÉÊËÌÍ ÏÐÑÒÓÔÕÖ×ØÙÚÛÜÝÞßàáâãäåæçèéêëìíîïðñòóôõö÷øùú111111ÀÁÂÃÄÅÆÇÈÉÊËÌÍ ÏÐÑÒÓÔÕÖ×ØÙÚÛÜÝÞßàáâãäåæçèéêëìíîïðñòóôõö÷øùúûüýþÿÀÁÂÃÄÅÆÇÈÉÊËÌÍ"
                  resultMatch.names.additionalNames shouldBe "ÏÐÑÒÓÔÕÖ×ØÙÚÛÜÝÞßàáâãäåæçèéêëìíîïðñòóôõö÷ø"
                  resultMatch.names.lastNames shouldBe "joshi"
                }
              }


              s"($name) match when firstname and additional name with very long multiple names and record have same name." in {
                running(FakeApplication(additionalConfiguration = ignoreAdditionalNamesDisabled)) {
                  var record = getRecord("Henry TEST George Martin Malcolm Arthur Cameron McTavish Glenny Alberto Turton Felicity Andrew Starship Trooper Neil", "JONES")
                  val payload = Payload(reference, "Henry", Some("test  George Martin Malcolm Arthur Cameron  McTavish Glenny Alberto Turton Felicity Andrew Starship Trooper Neil"), "JONES", new LocalDate("2012-02-16"), BirthRegisterCountry.ENGLAND)
                  val resultMatch = MockMatchingService.performMatch(payload, List(record), MatchingType.FULL)
                  resultMatch.matched shouldBe true
                  resultMatch.names.firstNames shouldBe "Henry"
                  resultMatch.names.additionalNames shouldBe "TEST George Martin Malcolm Arthur Cameron McTavish Glenny Alberto Turton Felicity Andrew Starship Trooper Neil"
                  resultMatch.names.lastNames shouldBe "JONES"
                }
              }
            }
          }
        )
      }
    }
  }
