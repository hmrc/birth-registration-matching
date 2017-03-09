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

package uk.gov.hmrc.brm.config

import org.joda.time.LocalDate
import org.mockito.Matchers.{eq => mockEq}
import org.scalatest.{BeforeAndAfter, Tag, TestData}
import org.scalatestplus.play.OneAppPerTest
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.brm.models.brm.Payload
import uk.gov.hmrc.brm.utils.BirthRegisterCountry
import uk.gov.hmrc.brm.utils.BirthRegisterCountry.BirthRegisterCountry
import uk.gov.hmrc.play.test.UnitSpec

class FeatureSwitchSpec extends UnitSpec with BeforeAndAfter with OneAppPerTest {

  import uk.gov.hmrc.brm.config.BrmConfig._

  lazy val switchDisabled: Map[String, _] = Map(
    "microservice.services.birth-registration-matching.features.dobValidation.enabled" -> false,
    "microservice.services.birth-registration-matching.features.gro.enabled" -> false,
    "microservice.services.birth-registration-matching.features.gro.reference.enabled" -> false,
    "microservice.services.birth-registration-matching.features.gro.details.enabled" -> false,
    "microservice.services.birth-registration-matching.features.nrs.enabled" -> false,
    "microservice.services.birth-registration-matching.features.nrs.reference.enabled" -> false,
    "microservice.services.birth-registration-matching.features.nrs.details.enabled" -> false
  )

  lazy val switchEnabled: Map[String, _] = Map(
    "microservice.services.birth-registration-matching.features.dobValidation.enabled" -> true,
    "microservice.services.birth-registration-matching.features.gro.enabled" -> true,
    "microservice.services.birth-registration-matching.features.gro.reference.enabled" -> true,
    "microservice.services.birth-registration-matching.features.gro.details.enabled" -> true,
    "microservice.services.birth-registration-matching.features.nrs.enabled" -> true,
    "microservice.services.birth-registration-matching.features.nrs.reference.enabled" -> true,
    "microservice.services.birth-registration-matching.features.nrs.details.enabled" -> true
  )

  lazy val parentFeatureDisabled: Map[String, _] = Map(
    "microservice.services.birth-registration-matching.features.dobValidation.enabled" -> false,
    "microservice.services.birth-registration-matching.features.gro.enabled" -> false,
    "microservice.services.birth-registration-matching.features.gro.reference.enabled" -> true,
    "microservice.services.birth-registration-matching.features.gro.details.enabled" -> true,
    "microservice.services.birth-registration-matching.features.nrs.enabled" -> false,
    "microservice.services.birth-registration-matching.features.nrs.reference.enabled" -> true,
    "microservice.services.birth-registration-matching.features.nrs.details.enabled" -> true
  )

  lazy val referenceFeatureDisabled: Map[String, _] = Map(
    "microservice.services.birth-registration-matching.features.dobValidation.enabled" -> false,
    "microservice.services.birth-registration-matching.features.gro.enabled" -> true,
    "microservice.services.birth-registration-matching.features.gro.reference.enabled" -> false,
    "microservice.services.birth-registration-matching.features.gro.details.enabled" -> false,
    "microservice.services.birth-registration-matching.features.nrs.enabled" -> true,
    "microservice.services.birth-registration-matching.features.nrs.reference.enabled" -> false,
    "microservice.services.birth-registration-matching.features.nrs.details.enabled" -> false
  )

  lazy val referenceFeatureEnabled: Map[String, _] = Map(
    "microservice.services.birth-registration-matching.features.dobValidation.enabled" -> false,
    "microservice.services.birth-registration-matching.features.gro.enabled" -> true,
    "microservice.services.birth-registration-matching.features.gro.reference.enabled" -> true,
    "microservice.services.birth-registration-matching.features.gro.details.enabled" -> false,
    "microservice.services.birth-registration-matching.features.nrs.enabled" -> true,
    "microservice.services.birth-registration-matching.features.nrs.reference.enabled" -> true,
    "microservice.services.birth-registration-matching.features.nrs.details.enabled" -> false
  )

  lazy val detailFeatureDisabled: Map[String, _] = Map(
    "microservice.services.birth-registration-matching.features.dobValidation.enabled" -> false,
    "microservice.services.birth-registration-matching.features.gro.enabled" -> true,
    "microservice.services.birth-registration-matching.features.gro.reference.enabled" -> false,
    "microservice.services.birth-registration-matching.features.gro.details.enabled" -> false,
    "microservice.services.birth-registration-matching.features.nrs.enabled" -> true,
    "microservice.services.birth-registration-matching.features.nrs.reference.enabled" -> false,
    "microservice.services.birth-registration-matching.features.nrs.details.enabled" -> false
  )

  lazy val detailFeatureEnabled: Map[String, _] = Map(
    "microservice.services.birth-registration-matching.features.dobValidation.enabled" -> false,
    "microservice.services.birth-registration-matching.features.gro.enabled" -> true,
    "microservice.services.birth-registration-matching.features.gro.reference.enabled" -> false,
    "microservice.services.birth-registration-matching.features.gro.details.enabled" -> true,
    "microservice.services.birth-registration-matching.features.nrs.enabled" -> true,
    "microservice.services.birth-registration-matching.features.nrs.reference.enabled" -> false,
    "microservice.services.birth-registration-matching.features.nrs.details.enabled" -> true
  )



  def buildPayload(reference: Option[String], country: BirthRegisterCountry, dob: String = "2010-01-13"): Payload = {
    Payload(
      birthReferenceNumber = reference,
      firstName = "John",
      lastName = "Smith",
      dateOfBirth = new LocalDate(dob),
      whereBirthRegistered = country
    )
  }

  override def newAppForTest(testData: TestData) = new GuiceApplicationBuilder(
    disabled = Seq(classOf[com.kenshoo.play.metrics.PlayModule])
  ).configure {
    if (testData.tags.contains("enabled")) {
      switchEnabled
    } else if (testData.tags.contains("disabled")) {
      switchDisabled
    } else if (testData.tags.contains("parentdisabled")) {
      parentFeatureDisabled
  } else if (testData.tags.contains("referencefeaturedisabled")) {
      referenceFeatureDisabled
  } else if (testData.tags.contains("referencefeatureenabled")) {
      referenceFeatureEnabled
  } else if (testData.tags.contains("detailfeaturedisabled")) {
    detailFeatureDisabled
  } else if (testData.tags.contains("detailfeatureenabled")) {
      detailFeatureEnabled
  }
    else {
      Map(
        "microservice.services.birth-registration-matching.features.gro.enabled" -> "test"
      )
    }
  }.build()



  "GROFeature" should {
    "set child features to false if parent is false" taggedAs Tag("parentdisabled") in {
      GROReferenceFeature().enabled shouldBe false
      GRODetailsFeature().enabled shouldBe false
    }
  }

  "NRSFeature" should {
    "set child features to false if parent is false" taggedAs Tag("parentdisabled") in {
      NRSReferenceFeature().enabled shouldBe false
      NRSDetailsFeature().enabled shouldBe false
    }
  }

  "DateOfBirthValidationFeature" should {
    "validate false value for dobValidation" taggedAs Tag("enabled") in {
      DateOfBirthValidationFeature().enabled shouldBe true
      DateOfBirthValidationFeature().value shouldBe "2009-07-01"
    }

    "validate true value for dobValidation" taggedAs Tag("disabled") in {
      DateOfBirthValidationFeature().enabled shouldBe false
    }
  }

  "Feature.enabled" should {
    "throw RuntimeException if config value is wrong Type" taggedAs Tag("") in {
      val e = intercept[RuntimeException] {
        GROFeature().enabled() shouldBe false
      }
      e.getMessage shouldBe "problem obtaining valid config value"
    }

  }

  "GRO" when {

    "enabled" should {
      "have switch for GRO" taggedAs Tag("enabled") in {
        GROFeature().enabled shouldBe true
      }

      "have reference switch for GRO" taggedAs Tag("enabled") in {
        GROReferenceFeature().enabled shouldBe true
      }

      "have details switch for GRO" taggedAs Tag("enabled") in {
        GRODetailsFeature().enabled shouldBe true
      }
    }

    "disabled" should {
      "have switch for GRO" taggedAs Tag("disabled") in {
        GROFeature().enabled shouldBe false
      }

      "have reference switch for GRO" taggedAs Tag("disabled") in {
        GROReferenceFeature().enabled shouldBe false
      }

      "have details switch for GRO" taggedAs Tag("disabled") in {
        GRODetailsFeature().enabled shouldBe false
      }
    }

  }

  "NRS" when {

    "enabled" should {
      "have switch for NRS" taggedAs Tag("enabled") in {
        NRSFeature().enabled shouldBe true
      }

      "have reference switch for NRS" taggedAs Tag("enabled") in {
        NRSReferenceFeature().enabled shouldBe true
      }

      "have details switch for NRS" taggedAs Tag("enabled") in {
        NRSDetailsFeature().enabled shouldBe true
      }

    }

    "disabled" should {

      "have switch for NRS" taggedAs Tag("disabled") in {
        NRSFeature().enabled shouldBe false
      }

      "have reference switch for NRS" taggedAs Tag("disabled") in {
        NRSReferenceFeature().enabled shouldBe false
      }

      "have details switch for NRS" taggedAs Tag("disabled") in {
        NRSDetailsFeature().enabled shouldBe false
      }

    }

  }

  "FeatureFactory" should {
//    "return GROConcreteFeature for birth registered in england" taggedAs Tag("enabled") in {
//      val feature = FeatureFactory(buildPayload(Some("123456789"), BirthRegisterCountry.ENGLAND))
//      feature.isInstanceOf[GROConcreteFeature] shouldBe true
//      feature.payload.isInstanceOf[Payload] shouldBe true
//      feature.dateOfBirthValidation shouldBe true
//      feature.feature shouldBe true
//      feature.referenceFeature shouldBe true
//      feature.detailsFeature shouldBe true
//    }

    "return GROConcreteFeature for birth registered in wales" taggedAs Tag("enabled") in {
      implicit val payload: Payload  = buildPayload(Some("123456789"), BirthRegisterCountry.WALES)
      FeatureFactory().isInstanceOf[FeatureFactory] shouldBe true
    }

//    "return NRSConcreteFeature for birth registered in scotland" taggedAs Tag("enabled") in {
//      FeatureFactory(buildPayload(Some("123456789"), BirthRegisterCountry.SCOTLAND)).isInstanceOf[NRSConcreteFeature] shouldBe true
//    }
//
    "validate request" when {

      "reference number is provided" should {

        "return false when parent feature is disabled" taggedAs Tag("disabled") in {
          implicit val payload: Payload = buildPayload(Some("123456789"), BirthRegisterCountry.ENGLAND)
          FeatureFactory().validate() shouldBe false
        }

        "return false when parent feature is enabled but reference feature is disabled" taggedAs Tag("referencefeaturedisabled") in {
          implicit val payload: Payload = buildPayload(Some("123456789"), BirthRegisterCountry.ENGLAND)
          FeatureFactory().validate() shouldBe false
        }

        "return true when parent feature is enabled and reference feature is enabled" taggedAs Tag("referencefeatureenabled") in {
          implicit val payload: Payload = buildPayload(Some("123456789"), BirthRegisterCountry.ENGLAND)
          FeatureFactory().validate() shouldBe true
        }

        "return false when date of birth is invalid" taggedAs Tag("enabled") in {
          implicit val payload: Payload = buildPayload(Some("123456789"), BirthRegisterCountry.ENGLAND, "2005-02-03")
          FeatureFactory().validate() shouldBe false
        }

        "return true when date of birth is invalid but switch is off" taggedAs Tag("referencefeatureenabled") in {
          implicit val payload: Payload = buildPayload(Some("123456789"), BirthRegisterCountry.ENGLAND, "2005-02-03")
          FeatureFactory().validate() shouldBe true
        }

      }

      "reference number NOT provided" should {
        "return false when parent feature is disabled" taggedAs Tag("disabled") in {
          implicit val payload: Payload = buildPayload(None, BirthRegisterCountry.ENGLAND)
          FeatureFactory().validate() shouldBe false
        }

        "return false when parent feature is enabled but detail feature is disabled" taggedAs Tag("detailfeaturedisabled") in {
          implicit val payload: Payload = buildPayload(None, BirthRegisterCountry.ENGLAND)
          FeatureFactory().validate() shouldBe false
        }

        "return true when parent feature is enabled and detail feature is enabled" taggedAs Tag("detailfeatureenabled") in {
          implicit val payload: Payload = buildPayload(None, BirthRegisterCountry.ENGLAND)
          FeatureFactory().validate() shouldBe true
        }

        "return false when date of birth is invalid" taggedAs Tag("enabled") in {
          implicit val payload: Payload = buildPayload(None, BirthRegisterCountry.ENGLAND, "2005-02-03")
          FeatureFactory().validate() shouldBe false
        }

        "return true when date of birth is invalid but switch is off" taggedAs Tag("detailfeatureenabled") in {
          implicit val payload: Payload = buildPayload(None, BirthRegisterCountry.ENGLAND, "2005-02-03")
          FeatureFactory().validate() shouldBe true
        }

      }

    }

  }

}
