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
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatest.OptionValues
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import uk.gov.hmrc.brm.models.brm.Payload
import uk.gov.hmrc.brm.services.matching.{FullMatching, MatchingService}
import uk.gov.hmrc.brm.utils.TestHelper._
import uk.gov.hmrc.brm.utils.{BaseUnitSpec, BirthRegisterCountry, MatchingType}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditResult

import scala.concurrent.Future

class MatchingServiceAdditionalNameEnabledSpec extends AnyWordSpecLike with Matchers with OptionValues with MockitoSugar with GuiceOneAppPerSuite with BaseUnitSpec {

  import uk.gov.hmrc.brm.utils.Mocks._

  implicit val hc: HeaderCarrier = HeaderCarrier()

  val mockFull: FullMatching = new FullMatching(mockConfig)

  val testMatchingService: MatchingService = new MatchingService(
    mockConfig,
    mockMatchingAudit,
    mockFull,
    mockPartialMatching,
    mockBrmLogger
  ){
    override val matchOnMultiple: Boolean = true
  }

  val testMatchingServiceNoMultiple: MatchingService = new MatchingService(
    mockConfig,
    mockMatchingAudit,
    mockFull,
    mockPartialMatching,
    mockBrmLogger
  ){
    override val matchOnMultiple: Boolean = false
  }


  "Matching" when {

    /*
      Multiple records
      Currently always returning false as we don't iterate over multiple records
     */

    "multiple records" when {

      "matching on multiple is true" should {

        "should return true if a minimum of one record matches" in {
          when(mockAuditConnector.sendEvent(any())(any(), any())).thenReturn(Future.successful(AuditResult.Success))

          val payload = Payload(Some("123456789"), "Chris", None, "Jones", new LocalDate("2012-02-16"), BirthRegisterCountry.ENGLAND)
          val resultMatch = testMatchingService.performMatch(payload, List(invalidRecord, validRecord), MatchingType.FULL)
          resultMatch.matched shouldBe true
        }

        "return false if no records match" in {
          when(mockAuditConnector.sendEvent(any())(any(), any())).thenReturn(Future.successful(AuditResult.Success))

          val payload = Payload(Some("123456789"), "Chris", None, "Jones", new LocalDate("2012-02-16"), BirthRegisterCountry.ENGLAND)
          val resultMatch = testMatchingService.performMatch(payload, List(invalidRecord, invalidRecord), MatchingType.FULL)
          resultMatch.matched shouldBe false
        }

        "return false result match when List is empty" in {
          when(mockAuditConnector.sendEvent(any())(any(), any())).thenReturn(Future.successful(AuditResult.Success))

          val payload = Payload(Some("123456789"), "Chris", None, "Jones", new LocalDate("2012-02-16"), BirthRegisterCountry.ENGLAND)
          val resultMatch = testMatchingService.performMatch(payload, List(), MatchingType.FULL)
          resultMatch.matched shouldBe false
        }

      }

      "matching on multiple is false" should {

        "return false for more than 1 record" in {
          when(mockAuditConnector.sendEvent(any())(any(), any())).thenReturn(Future.successful(AuditResult.Success))

          val payload = Payload(Some("123456789"), "Chris", None, "Jones", new LocalDate("2012-02-16"), BirthRegisterCountry.ENGLAND)
          val resultMatch = testMatchingServiceNoMultiple.performMatch(payload, List(validRecord, validRecord, validRecord), MatchingType.FULL)
          resultMatch.matched shouldBe false
        }

        "return true for a match on a single record" in {
          when(mockAuditConnector.sendEvent(any())(any(), any())).thenReturn(Future.successful(AuditResult.Success))

          val payload = Payload(Some("123456789"), "Chris", None, "Jones", new LocalDate("2012-02-16"), BirthRegisterCountry.ENGLAND)
          val resultMatch = testMatchingServiceNoMultiple.performMatch(payload, List(validRecord), MatchingType.FULL)
          resultMatch.matched shouldBe true
        }

        "return false for a no match on a single record" in {
          when(mockAuditConnector.sendEvent(any())(any(), any())).thenReturn(Future.successful(AuditResult.Success))

          val payload = Payload(Some("123456789"), "Christopher", None, "Jones", new LocalDate("2012-02-16"), BirthRegisterCountry.ENGLAND)
          val resultMatch = testMatchingServiceNoMultiple.performMatch(payload, List(validRecord), MatchingType.FULL)
          resultMatch.matched shouldBe false
        }

        "return false result match when List is empty" in {
          when(mockAuditConnector.sendEvent(any())(any(), any())).thenReturn(Future.successful(AuditResult.Success))

          val payload = Payload(Some("123456789"), "Chris", None, "Jones", new LocalDate("2012-02-16"), BirthRegisterCountry.ENGLAND)
          val resultMatch = testMatchingServiceNoMultiple.performMatch(payload, List(), MatchingType.FULL)
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
            case Some(_) => "with reference"
            case None => "without reference"
          }

          "ignore middle names with feature toggle enabled" should {

            s"($name) match when firstName argument has all middle names on input that are on the record" in {

                val payload = Payload(reference, "Adam David", None, "Jones", new LocalDate("2012-02-16"), BirthRegisterCountry.ENGLAND)
                val resultMatch = testMatchingService.performMatch(payload, List(validRecordMiddleNames), MatchingType.FULL)
                resultMatch.matched shouldBe true
                resultMatch.names.firstNames shouldBe "Adam David"
                resultMatch.names.additionalNames shouldBe empty
                resultMatch.names.lastNames shouldBe "Jones"
            }


            s"($name) match when firstName argument has all middle names on input that are " +
              s"on the record and ignore any additional name provided in payload." in {
                when(mockConfig.ignoreAdditionalNames).thenReturn(true)
                val payload = Payload(reference, "Adam David", Some("test"), "Jones", new LocalDate("2012-02-16"), BirthRegisterCountry.ENGLAND)
                val resultMatch = testMatchingService.performMatch(payload, List(validRecordMiddleNames), MatchingType.FULL)
                resultMatch.matched shouldBe true
                resultMatch.names.firstNames shouldBe "Adam David"
                resultMatch.names.additionalNames shouldBe empty
                resultMatch.names.lastNames shouldBe "Jones"
            }

            s"($name) match when firstName argument has all middle names on input that on are the record, with additional spaces" in {

                val payload = Payload(reference, " Adam    David   ", None, "Jones", new LocalDate("2012-02-16"), BirthRegisterCountry.ENGLAND)
                val resultMatch = testMatchingService.performMatch(payload, List(validRecordMiddleNames), MatchingType.FULL)
                resultMatch.matched shouldBe true
                resultMatch.names.firstNames shouldBe "Adam David"
                resultMatch.names.additionalNames shouldBe empty
                resultMatch.names.lastNames shouldBe "Jones"
            }

            s"($name) match when firstName argument has all middle names on input that on are the record, with additional spaces on the record" in {

                val payload = Payload(reference, "Adam David", None, "Jones", new LocalDate("2012-02-16"), BirthRegisterCountry.ENGLAND)
                val resultMatch = testMatchingService.performMatch(payload, List(validRecordMiddleNamesWithSpaces), MatchingType.FULL)
                resultMatch.matched shouldBe true
                resultMatch.names.firstNames shouldBe "Adam David"
                resultMatch.names.additionalNames shouldBe empty
                resultMatch.names.lastNames shouldBe "Jones"
            }

            s"($name) match when firstName argument has middle names with punctuation, with additional names on record" in {

                val payload = Payload(reference, "   Jamie  Mary-Ann'é ", None, "Jones", new LocalDate("2012-02-16"), BirthRegisterCountry.ENGLAND)
                val resultMatch = testMatchingService.performMatch(payload, List(validRecordMiddleNamesWithSpacesAndPunctuation), MatchingType.FULL)
                resultMatch.matched shouldBe true
                resultMatch.names.firstNames shouldBe "Jamie Mary-Ann'é"
                resultMatch.names.additionalNames shouldBe empty
                resultMatch.names.lastNames shouldBe "Jones"
            }

            s"($name) match when firstName argument has no middle names on input that are on the record" in {

                val payload = Payload(reference, "Adam", None, "Jones", new LocalDate("2012-02-16"), BirthRegisterCountry.ENGLAND)
                val resultMatch = testMatchingService.performMatch(payload, List(validRecordMiddleNames), MatchingType.FULL)
                resultMatch.matched shouldBe true
                resultMatch.names.firstNames shouldBe "Adam"
                resultMatch.names.additionalNames shouldBe empty
                resultMatch.names.lastNames shouldBe "Jones"
            }

            s"($name) match when firstName argument has no middle names on input that are on the record, with additional spaces" in {

                val payload = Payload(reference, "    Adam     ", None, "Jones", new LocalDate("2012-02-16"), BirthRegisterCountry.ENGLAND)
                val resultMatch = testMatchingService.performMatch(payload, List(validRecordMiddleNames), MatchingType.FULL)
                resultMatch.matched shouldBe true
                resultMatch.names.firstNames shouldBe "Adam"
                resultMatch.names.additionalNames shouldBe empty
                resultMatch.names.lastNames shouldBe "Jones"
            }

            s"($name) not match when firstName argument has too many names not on the record" in {

                when(mockAuditConnector.sendEvent(any())(any(), any()))
                  .thenReturn(Future.successful(AuditResult.Success))

                val payload = Payload(reference, "Adam David James", None, "Jones", new LocalDate("2012-02-16"), BirthRegisterCountry.ENGLAND)
                val resultMatch = testMatchingService.performMatch(payload, List(validRecordMiddleNames), MatchingType.FULL)
                resultMatch.matched shouldBe false
                resultMatch.names.firstNames shouldBe "Adam David"
                resultMatch.names.additionalNames shouldBe empty
                resultMatch.names.lastNames shouldBe "Jones"
            }

            s"($name) not match when firstName argument has too many names not on the record, with additional spaces" in {

                when(mockAuditConnector.sendEvent(any())(any(), any()))
                  .thenReturn(Future.successful(AuditResult.Success))

                val payload = Payload(reference, "   Adam  David     James  ", None, "Jones", new LocalDate("2012-02-16"), BirthRegisterCountry.ENGLAND)
                val resultMatch = testMatchingService.performMatch(payload, List(validRecordMiddleNames), MatchingType.FULL)
                resultMatch.matched shouldBe false
                resultMatch.names.firstNames shouldBe "Adam David"
                resultMatch.names.additionalNames shouldBe empty
                resultMatch.names.lastNames shouldBe "Jones"
            }

            s"($name) match when lastName from record contains multiple spaces between names and includes space at beginning and end of string" in {

                when(mockAuditConnector.sendEvent(any())(any(), any()))
                  .thenReturn(Future.successful(AuditResult.Success))

                val payload = Payload(reference, "Chris", None, "Jones Smith", new LocalDate("2012-02-16"), BirthRegisterCountry.ENGLAND)
                val resultMatch = testMatchingService.performMatch(payload, List(validRecordLastNameMultipleSpaceBeginningTrailing), MatchingType.FULL)
                resultMatch.matched shouldBe true
                resultMatch.names.firstNames shouldBe "Chris"
                resultMatch.names.additionalNames shouldBe empty
                resultMatch.names.lastNames shouldBe "Jones Smith"
            }

          }
        }
      )
    }
  }
}
