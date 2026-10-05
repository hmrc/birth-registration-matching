/*
 * Copyright 2026 HM Revenue & Customs
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

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatest.{OptionValues, Tag, TestData}
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.brm.filters.*
import uk.gov.hmrc.brm.filters.Filter.{DetailsFilter, GeneralFilter, ReferenceFilter}
import uk.gov.hmrc.brm.models.brm.Payload
import uk.gov.hmrc.brm.utils.BirthRegisterCountry

import java.time.LocalDate

class FiltersSpec extends AnyWordSpecLike with Matchers with OptionValues with GuiceOneAppPerTest {

  def groReferenceFilter: GROReferenceFilter     = app.injector.instanceOf[GROReferenceFilter]
  def groDetailsFilter: GRODetailsFilter         = app.injector.instanceOf[GRODetailsFilter]
  def dateOfBirthFilter: DateOfBirthFilter       = app.injector.instanceOf[DateOfBirthFilter]
  def groniFilter: GRONIFilter                   = app.injector.instanceOf[GRONIFilter]
  def groniReferenceFilter: GRONIReferenceFilter = app.injector.instanceOf[GRONIReferenceFilter]
  def groniDetailsFilter: GRONIDetailsFilter     = app.injector.instanceOf[GRONIDetailsFilter]

  def testFilters: Filters = app.injector.instanceOf[Filters]

  def switchEnabled: Map[String, _] = Map(
    "microservice.services.birth-registration-matching.features.gro.enabled"             -> true,
    "microservice.services.birth-registration-matching.features.gro.reference.enabled"   -> true,
    "microservice.services.birth-registration-matching.features.gro.details.enabled"     -> true,
    "microservice.services.birth-registration-matching.features.nrs.enabled"             -> true,
    "microservice.services.birth-registration-matching.features.nrs.reference.enabled"   -> true,
    "microservice.services.birth-registration-matching.features.nrs.details.enabled"     -> true,
    "microservice.services.birth-registration-matching.features.groni.enabled"           -> true,
    "microservice.services.birth-registration-matching.features.groni.reference.enabled" -> true,
    "microservice.services.birth-registration-matching.features.groni.details.enabled"   -> true,
    "microservice.services.birth-registration-matching.features.dobValidation.enabled"   -> true
  )

  def switchDisabled: Map[String, _] = Map(
    "microservice.services.birth-registration-matching.features.dobValidation.enabled" -> false
  )

  override def newAppForTest(testData: TestData): Application = GuiceApplicationBuilder()
    .configure(
      if (testData.tags.contains("disabled")) switchDisabled else switchEnabled
    )
    .build()

  val payloadWithReference: Payload =
    Payload(Some("123456789"), "Adam", None, "Smith", LocalDate.now, BirthRegisterCountry.ENGLAND)

  val nrsPayloadWithReference: Payload =
    Payload(Some("1234567890"), "Adam", None, "Smith", LocalDate.now, BirthRegisterCountry.SCOTLAND)

  val nrsPayloadWithoutReference: Payload =
    Payload(None, "Adam", None, "Smith", LocalDate.now, BirthRegisterCountry.SCOTLAND)

  val groNIPayloadWithReference: Payload =
    Payload(Some("1234567890"), "Adam", None, "Smith", LocalDate.now, BirthRegisterCountry.NORTHERN_IRELAND)

  val groNIPayloadWithoutReference: Payload =
    Payload(None, "Adam", None, "Smith", LocalDate.now, BirthRegisterCountry.NORTHERN_IRELAND)

  val payloadWithoutReference: Payload =
    Payload(None, "Adam", None, "Smith", LocalDate.now, BirthRegisterCountry.ENGLAND)

  val payloadInvalidDateOfBirth: Payload =
    Payload(None, "Adam", None, "Smith", LocalDate.parse("2008-12-12"), BirthRegisterCountry.ENGLAND)

  "Filters" when {

    "processing DateOfBirthFilter" should {

      "skip filter if not enabled" taggedAs Tag("disabled") in {
        dateOfBirthFilter.process(payloadWithReference) shouldBe true
      }

    }

    "gro" should {

      "contain GRO reference filters" in {
        val filters   = List(classOf[GROFilter], classOf[GROReferenceFilter], classOf[DateOfBirthFilter])
        val excluded  = List(
          classOf[NRSFilter],
          classOf[NRSReferenceFilter],
          classOf[NRSDetailsFilter],
          classOf[GRONIFilter],
          classOf[GRONIReferenceFilter],
          classOf[GRONIDetailsFilter]
        )
        val toProcess = testFilters.getFilters(payloadWithReference).map(_.getClass)
        for (filter <- excluded) yield toProcess should not contain filter
        for (filter <- filters) yield toProcess  should contain(filter)

        toProcess.length shouldBe filters.length
      }

      "contain GRO details filters" in {
        val filters   = List(classOf[GROFilter], classOf[GRODetailsFilter], classOf[DateOfBirthFilter])
        val excluded  = List(
          classOf[GROReferenceFilter],
          classOf[NRSFilter],
          classOf[NRSReferenceFilter],
          classOf[NRSDetailsFilter],
          classOf[GRONIFilter],
          classOf[GRONIReferenceFilter],
          classOf[GRONIDetailsFilter]
        )
        val toProcess = testFilters.getFilters(payloadWithoutReference).map(_.getClass)

        for (filter <- excluded) yield toProcess should not contain filter
        for (filter <- filters) yield toProcess  should contain(filter)
        toProcess.length                       shouldBe filters.length
      }

    }

    "nrs" should {

      "contain NRS reference filters" in {
        val filters   = List(classOf[NRSFilter], classOf[NRSReferenceFilter], classOf[DateOfBirthFilter])
        val excluded  = List(
          classOf[NRSDetailsFilter],
          classOf[GROFilter],
          classOf[GROReferenceFilter],
          classOf[GRODetailsFilter],
          classOf[GRONIFilter],
          classOf[GRONIReferenceFilter],
          classOf[GRONIDetailsFilter]
        )
        val toProcess = testFilters.getFilters(nrsPayloadWithReference).map(_.getClass)

        for (filter <- excluded) yield toProcess should not contain filter
        for (filter <- filters) yield toProcess  should contain(filter)
        toProcess.length                       shouldBe filters.length
      }

      "contain NRS details filters" in {
        val filters   = List(classOf[NRSFilter], classOf[NRSDetailsFilter], classOf[DateOfBirthFilter])
        val excluded  = List(
          classOf[NRSReferenceFilter],
          classOf[GROFilter],
          classOf[GROReferenceFilter],
          classOf[GRODetailsFilter],
          classOf[GRONIFilter],
          classOf[GRONIReferenceFilter],
          classOf[GRONIDetailsFilter]
        )
        val toProcess = testFilters.getFilters(nrsPayloadWithoutReference).map(_.getClass)

        for (filter <- excluded) yield toProcess should not contain filter
        for (filter <- filters) yield toProcess  should contain(filter)
        toProcess.length                       shouldBe filters.length
      }

    }

    "gro-ni" should {

      "contain GRO-NI reference filters" in {
        val filters   = List(classOf[DateOfBirthFilter], classOf[GRONIFilter], classOf[GRONIReferenceFilter])
        val excluded  = List(
          classOf[GRONIDetailsFilter],
          classOf[GROFilter],
          classOf[GROReferenceFilter],
          classOf[GRODetailsFilter],
          classOf[NRSFilter],
          classOf[NRSReferenceFilter],
          classOf[NRSDetailsFilter]
        )
        val toProcess = testFilters.getFilters(groNIPayloadWithReference).map(_.getClass)

        for (filter <- excluded) yield toProcess should not contain filter
        for (filter <- filters) yield toProcess  should contain(filter)
        toProcess.length                       shouldBe filters.length
      }

      "contain GRO-NI details filters" in {
        val filters   = List(classOf[DateOfBirthFilter], classOf[GRONIFilter], classOf[GRONIDetailsFilter])
        val excluded  = List(
          classOf[GRONIReferenceFilter],
          classOf[GROFilter],
          classOf[GROReferenceFilter],
          classOf[GRODetailsFilter],
          classOf[NRSFilter],
          classOf[NRSReferenceFilter],
          classOf[NRSDetailsFilter]
        )
        val toProcess = testFilters.getFilters(groNIPayloadWithoutReference).map(_.getClass)

        for (filter <- excluded) yield toProcess should not contain filter
        for (filter <- filters) yield toProcess  should contain(filter)
        toProcess.length                       shouldBe filters.length
      }

      "have correct general GRO-NI filter details" in {
        groniFilter.filterType  shouldBe GeneralFilter
        groniFilter.switch.name shouldBe "groni"
        groniFilter.toString    shouldBe "GRONIFilter"
      }

      "have correct GRO-NI details filter details" in {
        groniDetailsFilter.filterType  shouldBe DetailsFilter
        groniDetailsFilter.switch.name shouldBe "groni.details"
        groniDetailsFilter.toString    shouldBe "GRONIDetailsFilter"
      }

      "have correct GRO-NI reference filter details" in {
        groniReferenceFilter.filterType  shouldBe ReferenceFilter
        groniReferenceFilter.switch.name shouldBe "groni.reference"
        groniReferenceFilter.toString    shouldBe "GRONIReferenceFilter"
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
        testFilters.process(payloadInvalidDateOfBirth).map(_.getClass) shouldBe List(classOf[DateOfBirthFilter])
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
