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

package uk.gov.hmrc.brm.switches

import org.joda.time.LocalDate
import org.scalatest.{Tag, TestData}
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.brm.filters._
import uk.gov.hmrc.brm.models.brm.Payload
import uk.gov.hmrc.brm.utils.BirthRegisterCountry
import org.scalatest.{Matchers, OptionValues, WordSpecLike}

trait FiltersSpec extends WordSpecLike with Matchers with OptionValues with GuiceOneAppPerTest {

  val groFilter: GROFilter = app.injector.instanceOf[GROFilter]
  val groReferenceFilter: GROReferenceFilter = app.injector.instanceOf[GROReferenceFilter]
  val groDetailsFilter: GRODetailsFilter = app.injector.instanceOf[GRODetailsFilter]
  val dateOfBirthFilter: DateOfBirthFilter = app.injector.instanceOf[DateOfBirthFilter]
  val nrsFilter: NRSFilter = app.injector.instanceOf[NRSFilter]
  val nrsReferenceFilter: NRSReferenceFilter = app.injector.instanceOf[NRSReferenceFilter]
  val nrsDetailsFilter: NRSDetailsFilter = app.injector.instanceOf[NRSDetailsFilter]
  val groniFilter: GRONIFilter = app.injector.instanceOf[GRONIFilter]
  val groniReferenceFilter: GRONIReferenceFilter = app.injector.instanceOf[GRONIReferenceFilter]
  val groniDetailsFilter: GRONIDetailsFilter = app.injector.instanceOf[GRONIDetailsFilter]

  val testFilters: Filters = app.injector.instanceOf[Filters]

  def switchEnabled: Map[String, _] = Map(
    "microservice.services.birth-registration-matching.features.gro.enabled" -> true,
    "microservice.services.birth-registration-matching.features.gro.reference.enabled" -> true,
    "microservice.services.birth-registration-matching.features.gro.details.enabled" -> true,
    "microservice.services.birth-registration-matching.features.nrs.enabled" -> true,
    "microservice.services.birth-registration-matching.features.nrs.reference.enabled" -> true,
    "microservice.services.birth-registration-matching.features.nrs.details.enabled" -> true,
    "microservice.services.birth-registration-matching.features.groni.enabled" -> true,
    "microservice.services.birth-registration-matching.features.groni.reference.enabled" -> true,
    "microservice.services.birth-registration-matching.features.groni.details.enabled" -> true,
    "microservice.services.birth-registration-matching.features.dobValidation.enabled" -> true
  )

  def switchDisabled: Map[String, _] = Map(
    "microservice.services.birth-registration-matching.features.dobValidation.enabled" -> false
  )

  override def newAppForTest(testData: TestData): Application = GuiceApplicationBuilder()
    .configure {
    if (testData.tags.contains("enabled")) {
      switchEnabled
    } else if (testData.tags.contains("disabled")) {
      switchDisabled
    } else {
      switchEnabled
    }
  }
    .build()

  val payloadWithReference: Payload = Payload(Some("123456789"), "Adam", None, "Smith", LocalDate.now, BirthRegisterCountry.ENGLAND)
  val nrsPayloadWithReference: Payload = Payload(Some("1234567890"), "Adam", None, "Smith", LocalDate.now, BirthRegisterCountry.SCOTLAND)
  val nrsPayloadWithoutReference: Payload = Payload(None, "Adam", None, "Smith", LocalDate.now, BirthRegisterCountry.SCOTLAND)
  val groNIPayloadWithReference: Payload = Payload(Some("1234567890"), "Adam", None, "Smith", LocalDate.now, BirthRegisterCountry.NORTHERN_IRELAND)
  val groNIPayloadWithoutReference: Payload = Payload(None, "Adam", None, "Smith", LocalDate.now, BirthRegisterCountry.NORTHERN_IRELAND)

  val payloadWithoutReference: Payload = Payload(None, "Adam", None, "Smith", LocalDate.now, BirthRegisterCountry.ENGLAND)
  val payloadInvalidDateOfBirth: Payload = Payload(None, "Adam", None, "Smith", LocalDate.parse("2008-12-12"), BirthRegisterCountry.ENGLAND)

  "Filters" when {

    "processing DateOfBirthFilter" should {

      "skip filter if not enabled" taggedAs Tag("disabled") in {
        dateOfBirthFilter.process(payloadWithReference) shouldBe true
      }

    }

    "gro" should {

      "contain GRO reference filters" in {
        val filters = List(groFilter, groReferenceFilter, dateOfBirthFilter)
        val excluded = List(groDetailsFilter, nrsFilter, nrsReferenceFilter, nrsDetailsFilter, groniFilter, groniReferenceFilter, groniDetailsFilter)
        val toProcess = testFilters.getFilters(payloadWithReference)

        for(filter <- excluded) yield toProcess should not contain filter
        for(filter <- filters) yield toProcess should contain(filter)
        toProcess.length shouldBe filters.length
      }

      "contain GRO details filters" in {
        val filters = List(groFilter, groDetailsFilter, dateOfBirthFilter)
        val excluded = List(groReferenceFilter, nrsFilter, nrsReferenceFilter, nrsDetailsFilter, groniFilter, groniReferenceFilter, groniDetailsFilter)
        val toProcess = testFilters.getFilters(payloadWithoutReference)

        for(filter <- excluded) yield toProcess should not contain filter
        for(filter <- filters) yield toProcess should contain(filter)
        toProcess.length shouldBe filters.length
      }

    }

    "nrs" should {

      "contain NRS reference filters" in {
        val filters = List(nrsFilter, nrsReferenceFilter, dateOfBirthFilter)
        val excluded = List(nrsDetailsFilter, groFilter, groReferenceFilter, groDetailsFilter, groniFilter, groniReferenceFilter, groniDetailsFilter)
        val toProcess = testFilters.getFilters(nrsPayloadWithReference)

        for(filter <- excluded) yield toProcess should not contain filter
        for(filter <- filters) yield toProcess should contain(filter)
        toProcess.length shouldBe filters.length
      }

      "contain NRS details filters" in {
        val filters = List(nrsFilter, nrsDetailsFilter, dateOfBirthFilter)
        val excluded = List(nrsReferenceFilter, groFilter, groReferenceFilter, groDetailsFilter, groniFilter, groniReferenceFilter, groniDetailsFilter)
        val toProcess = testFilters.getFilters(nrsPayloadWithoutReference)

        for(filter <- excluded) yield toProcess should not contain filter
        for(filter <- filters) yield toProcess should contain(filter)
        toProcess.length shouldBe filters.length
      }

    }

    "gro-ni" should {

      "contain GRO-NI reference filters" in {
        val filters = List(dateOfBirthFilter, groniFilter, groniReferenceFilter)
        val excluded = List(groniDetailsFilter, groFilter, groReferenceFilter, groDetailsFilter, nrsFilter, nrsReferenceFilter, nrsDetailsFilter)
        val toProcess = testFilters.getFilters(groNIPayloadWithReference)

        for(filter <- excluded) yield toProcess should not contain filter
        for(filter <- filters) yield toProcess should contain(filter)
        toProcess.length shouldBe filters.length
      }

      "contain GRO-NI details filters" in {
        val filters = List(dateOfBirthFilter, groniFilter, groniDetailsFilter)
        val excluded = List(groniReferenceFilter, groFilter, groReferenceFilter, groDetailsFilter, nrsFilter, nrsReferenceFilter, nrsDetailsFilter)
        val toProcess = testFilters.getFilters(groNIPayloadWithoutReference)

        for(filter <- excluded) yield toProcess should not contain filter
        for(filter <- filters) yield toProcess should contain(filter)
        toProcess.length shouldBe filters.length
      }

    }

    "processing a filter" should {

      "process BRN specific filters when the request has a Birth Reference Number" in {
        testFilters.shouldProcessFilter(groReferenceFilter, payloadWithReference) shouldBe true
      }

      "not process BRN specific filters when the request does not have a Birth Reference Number" in {
        testFilters.shouldProcessFilter(groReferenceFilter, payloadWithoutReference) shouldBe false
      }

      "process Details specific filters when the request does not have a Birth Reference Number" in {
        testFilters.shouldProcessFilter(groDetailsFilter, payloadWithoutReference) shouldBe true
      }

      "not process Details specific filters when the request has a Birth Reference Number" in {
        testFilters.shouldProcessFilter(groDetailsFilter, payloadWithReference) shouldBe false
      }

      "process a GeneralFilter when request has a Birth Reference Number" in {
        testFilters.shouldProcessFilter(dateOfBirthFilter, payloadWithReference) shouldBe true
      }

      "process a GeneralFilter when request does not have a Birth Reference Number" in {
        testFilters.shouldProcessFilter(dateOfBirthFilter, payloadWithoutReference) shouldBe true
      }

    }

    "for all requests" should {

      "process filters for a request with a valid date of birth" in {
        testFilters.process(payloadWithReference) shouldBe Nil
      }

      "process filters for a request with a failure due to date of birth" in {
        testFilters.process(payloadInvalidDateOfBirth) shouldBe List(dateOfBirthFilter)
      }

    }

    "request has BRN" should {

      "process filters for a request" in {
        testFilters.process(payloadWithReference) shouldBe Nil
      }

    }

    "request does not have BRN" should {

      "process filters for a request" in {
        testFilters.process(payloadWithoutReference) shouldBe Nil
      }

    }

  }

}
