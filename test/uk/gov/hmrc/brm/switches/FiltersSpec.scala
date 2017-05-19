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
import uk.gov.hmrc.brm.filters._
import uk.gov.hmrc.brm.models.brm.Payload
import uk.gov.hmrc.brm.utils.BirthRegisterCountry
import uk.gov.hmrc.play.test.UnitSpec

class FiltersSpec extends UnitSpec with OneAppPerTest {

  val payloadWithReference = Payload(Some("123456789"), "Adam", None, "Smith", LocalDate.now, BirthRegisterCountry.ENGLAND)
  val nrsPayloadWithReference = Payload(Some("1234567890"), "Adam", None, "Smith", LocalDate.now, BirthRegisterCountry.SCOTLAND)
  val nrsPayloadWithoutReference = Payload(None, "Adam", None, "Smith", LocalDate.now, BirthRegisterCountry.SCOTLAND)
  val groNIPayloadWithReference = Payload(Some("1234567890"), "Adam", None, "Smith", LocalDate.now, BirthRegisterCountry.NORTHERN_IRELAND)
  val groNIPayloadWithoutReference = Payload(None, "Adam", None, "Smith", LocalDate.now, BirthRegisterCountry.NORTHERN_IRELAND)

  val payloadWithoutReference = Payload(None, "Adam", None, "Smith", LocalDate.now, BirthRegisterCountry.ENGLAND)
  val payloadInvalidDateOfBirth = Payload(None, "Adam", None, "Smith", LocalDate.parse("2008-12-12"), BirthRegisterCountry.ENGLAND)

  "Filters" when {

    "gro" should {

      "contain GRO reference filters" in {
        val filters = List(GROFilter, GROReferenceFilter, DateOfBirthFilter)
        val excluded = List(GRODetailsFilter, NRSFilter, NRSReferenceFilter, NRSDetailsFilter, GRONIFilter, GRONIReferenceFilter, GRONIDetailsFilter)
        val toProcess = Filters.getFilters(payloadWithReference)

        for(filter <- excluded) yield toProcess should not contain filter
        for(filter <- filters) yield toProcess should contain(filter)
        toProcess.length shouldBe filters.length
      }

      "contain GRO details filters" in {
        val filters = List(GROFilter, GRODetailsFilter, DateOfBirthFilter)
        val excluded = List(GROReferenceFilter, NRSFilter, NRSReferenceFilter, NRSDetailsFilter, GRONIFilter, GRONIReferenceFilter, GRONIDetailsFilter)
        val toProcess = Filters.getFilters(payloadWithoutReference)

        for(filter <- excluded) yield toProcess should not contain filter
        for(filter <- filters) yield toProcess should contain(filter)
        toProcess.length shouldBe filters.length
      }

    }

    "nrs" should {

      "contain NRS reference filters" in {
        val filters = List(NRSFilter, NRSReferenceFilter, DateOfBirthFilter)
        val excluded = List(NRSDetailsFilter, GROFilter, GROReferenceFilter, GRODetailsFilter, GRONIFilter, GRONIReferenceFilter, GRONIDetailsFilter)
        val toProcess = Filters.getFilters(nrsPayloadWithReference)

        for(filter <- excluded) yield toProcess should not contain filter
        for(filter <- filters) yield toProcess should contain(filter)
        toProcess.length shouldBe filters.length
      }

      "contain NRS details filters" in {
        val filters = List(NRSFilter, NRSDetailsFilter, DateOfBirthFilter)
        val excluded = List(NRSReferenceFilter, GROFilter, GROReferenceFilter, GRODetailsFilter, GRONIFilter, GRONIReferenceFilter, GRONIDetailsFilter)
        val toProcess = Filters.getFilters(nrsPayloadWithoutReference)

        for(filter <- excluded) yield toProcess should not contain filter
        for(filter <- filters) yield toProcess should contain(filter)
        toProcess.length shouldBe filters.length
      }

    }

    "gro-ni" should {

      "contain GRO-NI reference filters" in {
        val filters = List(DateOfBirthFilter, GRONIFilter, GRONIReferenceFilter)
        val excluded = List(GRONIDetailsFilter, GROFilter, GROReferenceFilter, GRODetailsFilter, NRSFilter, NRSReferenceFilter, NRSDetailsFilter)
        val toProcess = Filters.getFilters(groNIPayloadWithReference)

        for(filter <- excluded) yield toProcess should not contain filter
        for(filter <- filters) yield toProcess should contain(filter)
        toProcess.length shouldBe filters.length
      }

      "contain GRO-NI details filters" in {
        val filters = List(DateOfBirthFilter, GRONIFilter, GRONIDetailsFilter)
        val excluded = List(GRONIReferenceFilter, GROFilter, GROReferenceFilter, GRODetailsFilter, NRSFilter, NRSReferenceFilter, NRSDetailsFilter)
        val toProcess = Filters.getFilters(groNIPayloadWithoutReference)

        for(filter <- excluded) yield toProcess should not contain filter
        for(filter <- filters) yield toProcess should contain(filter)
        toProcess.length shouldBe filters.length
      }

    }

    "processing a filter" should {

      "process BRN specific filters when the request has a Birth Reference Number" in {
        Filters.shouldProcessFilter(GROReferenceFilter, payloadWithReference) shouldBe true
      }

      "not process BRN specific filters when the request does not have a Birth Reference Number" in {
        Filters.shouldProcessFilter(GROReferenceFilter, payloadWithoutReference) shouldBe false
      }

      "process Details specific filters when the request does not have a Birth Reference Number" in {
        Filters.shouldProcessFilter(GRODetailsFilter, payloadWithoutReference) shouldBe true
      }

      "not process Details specific filters when the request has a Birth Reference Number" in {
        Filters.shouldProcessFilter(GRODetailsFilter, payloadWithReference) shouldBe false
      }

      "process a GeneralFilter when request has a Birth Reference Number" in {
        Filters.shouldProcessFilter(DateOfBirthFilter, payloadWithReference) shouldBe true
      }

      "process a GeneralFilter when request does not have a Birth Reference Number" in {
        Filters.shouldProcessFilter(DateOfBirthFilter, payloadWithoutReference) shouldBe true
      }

    }

    "for all requests" should {

      "process filters for a request with a valid date of birth" in {
        Filters.process(payloadWithReference) shouldBe (true, Nil)
      }

      "process filters for a request with a failure due to date of birth" in {
        Filters.process(payloadInvalidDateOfBirth) shouldBe (false, List(DateOfBirthFilter))
      }

    }

    "request has BRN" should {

      "process filters for a request" in {
        Filters.process(payloadWithReference) shouldBe (true, Nil)
      }

    }

    "request does not have BRN" should {

      "process filters for a request" in {
        Filters.process(payloadWithoutReference) shouldBe (true, Nil)
      }

    }

  }

}
