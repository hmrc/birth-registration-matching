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
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.brm.models.brm.Payload
import uk.gov.hmrc.brm.services.matching.{FullMatching, MatchingService}
import uk.gov.hmrc.brm.utils.TestHelper._
import uk.gov.hmrc.brm.utils.{BaseUnitSpec, BirthRegisterCountry, MatchingType}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.test.UnitSpec

class MatchingServiceAdditionalNameDisabledSpec extends UnitSpec with GuiceOneAppPerSuite with MockitoSugar with BaseUnitSpec {

  import uk.gov.hmrc.brm.utils.Mocks._

  implicit val hc: HeaderCarrier = HeaderCarrier()

  val mockFull: FullMatching = new FullMatching(mockConfig)
  val testMatchingService = new MatchingService(
    mockConfig,
    mockMatchingAudit,
    mockFull,
    mockPartialMatching,
    mockBrmLogger
  )

	when(mockConfig.ignoreAdditionalNames).thenReturn(false)


	"Matching" when {

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
            case Some(_) => "with reference"
            case None => "without reference"
          }

          "not ignore middle names with feature toggle disabled" should {

            s"($name) match when firstName argument has all middle names on input that are on the record" in {
              when(mockConfig.matchOnMultiple).thenReturn(true)

              mockAuditSuccess
              val payload = Payload(reference, "Adam David", None, "Jones", new LocalDate("2012-02-16"), BirthRegisterCountry.ENGLAND)
              val resultMatch = testMatchingService.performMatch(payload, List(validRecordMiddleNames), MatchingType.FULL)
              resultMatch.matched shouldBe true
              resultMatch.names.firstNames shouldBe "Adam David"
              resultMatch.names.additionalNames shouldBe empty
              resultMatch.names.lastNames shouldBe "Jones"
            }

            s"($name) match true when payload has more than one additional name and record has same additional name." in {
              mockAuditSuccess
              val payload = Payload(reference, "Adam", Some("test test"), "Jones", new LocalDate("2012-02-16"), BirthRegisterCountry.ENGLAND)
              val resultMatch = testMatchingService.performMatch(payload, List(adamTestTestJonesRecord), MatchingType.FULL)
              resultMatch.matched shouldBe true
              resultMatch.names.firstNames shouldBe "Adam"
              resultMatch.names.additionalNames shouldBe "test test"
              resultMatch.names.lastNames shouldBe "Jones"
            }

            s"($name) match when firstName with additional name  has all middle names on input that are on the record" in {
              mockAuditSuccess
              val payload = Payload(reference, "Adam ", Some("David"), "Jones", new LocalDate("2012-02-16"), BirthRegisterCountry.ENGLAND)
              val resultMatch = testMatchingService.performMatch(payload, List(validRecordMiddleNames), MatchingType.FULL)
              resultMatch.matched shouldBe true
              resultMatch.names.firstNames shouldBe "Adam"
              resultMatch.names.additionalNames shouldBe "David"
              resultMatch.names.lastNames shouldBe "Jones"
            }

            s"($name) match false when payload has no additional name but record has additional name." in {
              mockAuditSuccess
              val payload = Payload(reference, "Adam ", None, "Jones", new LocalDate("2012-02-16"), BirthRegisterCountry.ENGLAND)
              val resultMatch = testMatchingService.performMatch(payload, List(validRecordMiddleNames), MatchingType.FULL)
              resultMatch.matched shouldBe false
              resultMatch.names.firstNames shouldBe "Adam"
              resultMatch.names.additionalNames shouldBe "David"
              resultMatch.names.lastNames shouldBe "Jones"
            }

            s"($name) match false when record has empty firstName." in {
              mockAuditSuccess
              val payload = Payload(reference, "Adam ", None, "Jones", new LocalDate("2012-02-16"), BirthRegisterCountry.ENGLAND)
              val resultMatch = testMatchingService.performMatch(payload, List(invalidRecordFirstName), MatchingType.FULL)
              resultMatch.matched shouldBe false
              resultMatch.names.firstNames shouldBe empty
              resultMatch.names.additionalNames shouldBe empty
              resultMatch.names.lastNames shouldBe empty
            }

            s"($name) not match when firstName argument has a missing first name on input that is on the record" in {
              mockAuditSuccess
              val payload = Payload(reference, "Adam", None, "Jones", new LocalDate("2012-02-16"), BirthRegisterCountry.ENGLAND)
              val resultMatch = testMatchingService.performMatch(payload, List(validRecordMiddleNames), MatchingType.FULL)
              resultMatch.matched shouldBe false
              resultMatch.names.firstNames shouldBe "Adam"
              resultMatch.names.additionalNames shouldBe "David"
              resultMatch.names.lastNames shouldBe "Jones"
            }

            s"($name) match when firstName argument has all middle names on input that on are the record, with additional spaces" in {
              mockAuditSuccess
              val payload = Payload(reference, " Adam    David   ", None, "Jones", new LocalDate("2012-02-16"), BirthRegisterCountry.ENGLAND)
              val resultMatch = testMatchingService.performMatch(payload, List(validRecordMiddleNames), MatchingType.FULL)
              resultMatch.matched shouldBe true
              resultMatch.names.firstNames shouldBe "Adam David"
              resultMatch.names.additionalNames shouldBe empty
              resultMatch.names.lastNames shouldBe "Jones"
            }

            s"($name) match when firstName argument has all middle names on input that on are the record, with additional spaces on the record" in {
              mockAuditSuccess
              val payload = Payload(reference, "Adam David", None, "Jones", new LocalDate("2012-02-16"), BirthRegisterCountry.ENGLAND)
              val resultMatch = testMatchingService.performMatch(payload, List(validRecordMiddleNamesWithSpaces), MatchingType.FULL)
              resultMatch.matched shouldBe true
              resultMatch.names.firstNames shouldBe "Adam David"
              resultMatch.names.additionalNames shouldBe empty
              resultMatch.names.lastNames shouldBe "Jones"
            }

            s"($name) not match when firstName argument has too many names not on the record" in {
              mockAuditSuccess
              val payload = Payload(reference, "Adam David James", None, "Jones", new LocalDate("2012-02-16"), BirthRegisterCountry.ENGLAND)
              val resultMatch = testMatchingService.performMatch(payload, List(validRecordMiddleNames), MatchingType.FULL)
              resultMatch.matched shouldBe false
              resultMatch.names.firstNames shouldBe "Adam David"
              resultMatch.names.additionalNames shouldBe empty
              resultMatch.names.lastNames shouldBe "Jones"
            }

            s"($name) not match when firstName with additional name has too many names not on the record" in {
              mockAuditSuccess
              val payload = Payload(reference, "Adam ", Some(" David James "), "Jones", new LocalDate("2012-02-16"), BirthRegisterCountry.ENGLAND)
              val resultMatch = testMatchingService.performMatch(payload, List(validRecordMiddleNames), MatchingType.FULL)
              resultMatch.matched shouldBe false
              resultMatch.names.firstNames shouldBe "Adam"
              resultMatch.names.additionalNames shouldBe "David"
              resultMatch.names.lastNames shouldBe "Jones"
            }

            s"($name) not match when firstName argument has too many names not on the record, with additional spaces on record" in {
              mockAuditSuccess
              val payload = Payload(reference, "Adam David James", None, "Jones", new LocalDate("2012-02-16"), BirthRegisterCountry.ENGLAND)
              val resultMatch = testMatchingService.performMatch(payload, List(validRecordMiddleNamesWithSpaces), MatchingType.FULL)
              resultMatch.matched shouldBe false
              resultMatch.names.firstNames shouldBe "Adam David"
              resultMatch.names.additionalNames shouldBe empty
              resultMatch.names.lastNames shouldBe "Jones"
            }

            s"($name) match when lastName from record contains multiple spaces between names and includes space at beginning and end of string" in {
              mockAuditSuccess
              val payload = Payload(reference, "Chris", None, "Jones Smith", new LocalDate("2012-02-16"), BirthRegisterCountry.ENGLAND)
              val resultMatch = testMatchingService.performMatch(payload, List(validRecordLastNameMultipleSpaceBeginningTrailing), MatchingType.FULL)
              resultMatch.matched shouldBe true
              resultMatch.names.firstNames shouldBe "Chris"
              resultMatch.names.additionalNames shouldBe empty
              resultMatch.names.lastNames shouldBe "Jones Smith"
            }

            s"($name) match when firstname contains multiple name, addtiional contains multiple names and record has same on it." in {
              mockAuditSuccess
              val record = getRecord("Manish test   test      one  test    two    three", "joshi")
              val payload = Payload(reference, "Manish test", Some("test   one    test    two  three  "), "joshi",
                new LocalDate("2012-02-16"), BirthRegisterCountry.ENGLAND)
              val resultMatch = testMatchingService.performMatch(payload, List(record), MatchingType.FULL)
              resultMatch.matched shouldBe true
              resultMatch.names.firstNames shouldBe "Manish test"
              resultMatch.names.additionalNames shouldBe "test one test two three"
              resultMatch.names.lastNames shouldBe "joshi"
            }

            s"($name) not match when firstname contains multiple name, addtiional contains multiple names and record dont have same name." in {
              mockAuditSuccess
              val record = getRecord("Manish test test one test two three", "joshi")
              val payload = Payload(reference, "Manish test", Some("test one test two  "), "joshi", new LocalDate("2012-02-16"), BirthRegisterCountry.ENGLAND)
              val resultMatch = testMatchingService.performMatch(payload, List(record), MatchingType.FULL)
              resultMatch.matched shouldBe false
              resultMatch.names.firstNames shouldBe "Manish test"
              resultMatch.names.additionalNames shouldBe "test one test two three"
              resultMatch.names.lastNames shouldBe "joshi"
            }

            s"($name) not match when firstname contains multiple name, addtiional contains multiple names and record dont have in same order." in {
              mockAuditSuccess
              val record = getRecord("Manish  test         test  one test      two three", "joshi")
              val payload = Payload(reference, "Manish test", Some("test  three      test  two   one "), "joshi",
                new LocalDate("2012-02-16"), BirthRegisterCountry.ENGLAND)
              val resultMatch = testMatchingService.performMatch(payload, List(record), MatchingType.FULL)
              resultMatch.matched shouldBe false
              resultMatch.names.firstNames shouldBe "Manish test"
              resultMatch.names.additionalNames shouldBe "test one test two three"
              resultMatch.names.lastNames shouldBe "joshi"
            }

            s"($name) match when firstname and additional name  contains special character name and record have same name." in {
              mockAuditSuccess
              val record = getRecord("ÀÁÂÃÄÅÆÇÈÉÊËÌÍ ÏÐÑÒÓÔÕÖ×ØÙÚÛÜÝÞßàáâãäåæçèéêëìíîïðñòóôõö÷øùúûüýþÿÀÁÂÃÄÅÆÇÈÉÊËÌÍ" +
                " ÏÐÑÒÓÔÕÖ×ØÙÚÛÜÝÞßàáâãäåæçèéêëìíîïðñòóôõö÷øùú111111ÀÁÂÃÄÅÆÇÈÉÊËÌÍ ÏÐÑÒÓÔÕÖ×ØÙÚÛÜÝÞßàáâãäåæçèéêëìíîïðñòóôõö÷øùú" +
                "ûüýþÿÀÁÂÃÄÅÆÇÈÉÊËÌÍ ÏÐÑÒÓÔÕÖ×ØÙÚÛÜÝÞßàáâãäåæçèéêëìíîïðñòóôõö÷ø", "joshi")
              val payload = Payload(reference, "ÀÁÂÃÄÅÆÇÈÉÊËÌÍ ÏÐÑÒÓÔÕÖ×ØÙÚÛÜÝÞßàáâãäåæçèéêëìíîïðñòóôõö÷øùúûüýþÿÀÁÂÃÄÅÆÇÈÉÊËÌÍ " +
                "ÏÐÑÒÓÔÕÖ×ØÙÚÛÜÝÞßàáâãäåæçèéêëìíîïðñòóôõö÷øùú111111ÀÁÂÃÄÅÆÇÈÉÊËÌÍ ÏÐÑÒÓÔÕÖ×ØÙÚÛÜÝÞßàáâãäåæçèéêëìíîïðñòóôõö÷øùúû" +
                "üýþÿÀÁÂÃÄÅÆÇÈÉÊËÌÍ ", Some("ÏÐÑÒÓÔÕÖ×ØÙÚÛÜÝÞßàáâãäåæçèéêëìíîïðñòóôõö÷ø"), "joshi", new LocalDate("2012-02-16"), BirthRegisterCountry.ENGLAND)
              val resultMatch = testMatchingService.performMatch(payload, List(record), MatchingType.FULL)
              resultMatch.matched shouldBe true
              resultMatch.names.firstNames shouldBe "ÀÁÂÃÄÅÆÇÈÉÊËÌÍ ÏÐÑÒÓÔÕÖ×ØÙÚÛÜÝÞßàáâãäåæçèéêëìíîïðñòóôõö÷øùúûüýþÿÀÁÂÃÄÅÆÇÈÉ" +
                "ÊËÌÍ ÏÐÑÒÓÔÕÖ×ØÙÚÛÜÝÞßàáâãäåæçèéêëìíîïðñòóôõö÷øùú111111ÀÁÂÃÄÅÆÇÈÉÊËÌÍ ÏÐÑÒÓÔÕÖ×ØÙÚÛÜÝÞßàáâãäåæçèéêëìíîïðñòóôõö" +
                "÷øùúûüýþÿÀÁÂÃÄÅÆÇÈÉÊËÌÍ"
              resultMatch.names.additionalNames shouldBe "ÏÐÑÒÓÔÕÖ×ØÙÚÛÜÝÞßàáâãäåæçèéêëìíîïðñòóôõö÷ø"
              resultMatch.names.lastNames shouldBe "joshi"
            }

            s"($name) match when firstname and additional name with very long multiple names and record have same name." in {
              mockAuditSuccess
              val record = getRecord("Henry TEST George Martin Malcolm Arthur Cameron McTavish Glenny Alberto Turton " +
                "Felicity Andrew Starship Trooper Neil", "JONES")
              val payload = Payload(reference, "Henry", Some("test  George Martin Malcolm Arthur Cameron  McTavish Glenny Alberto " +
                "Turton Felicity Andrew Starship Trooper Neil"), "JONES", new LocalDate("2012-02-16"), BirthRegisterCountry.ENGLAND)
              val resultMatch = testMatchingService.performMatch(payload, List(record), MatchingType.FULL)
              resultMatch.matched shouldBe true
              resultMatch.names.firstNames shouldBe "Henry"
              resultMatch.names.additionalNames shouldBe "TEST George Martin Malcolm Arthur Cameron McTavish Glenny Alberto Turton " +
                "Felicity Andrew Starship Trooper Neil"
              resultMatch.names.lastNames shouldBe "JONES"
            }
          }
        }
      )
    }
  }
}
