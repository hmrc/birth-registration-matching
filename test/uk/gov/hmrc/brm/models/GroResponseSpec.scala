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

package uk.gov.hmrc.brm.models

import org.joda.time.LocalDate
import play.api.libs.json.{JsError, JsSuccess, Json}
import uk.gov.hmrc.play.test.UnitSpec

/**
  * Created by chrisianson on 09/08/16.
  */
class GroResponseSpec extends UnitSpec {

  /**
    * Should
    * - should return a JsSuccess object on successful child mappings
    * - should return a JsSuccess object when birthReferenceNumber key is missing
    * - should return a JsSuccess object when givenName key is missing
    * - should return a JsSuccess object when surname key is missing
    * - should return a JsSuccess object when dateOfBirth key is missing
    * - should return a JsSuccess object when name key is missing
    * - should return a JsSuccess object when subjects key is missing
    * - should return a JsSuccess object when json is empty object
    * - should return a JsonMappingException when json contains nothing
    *
    * TODO: Max length (double check what this is)
    * TODO: UTF-8
    * TODO: Invalid types
    * TODO: hyphenated/apostraphe
    * TODO: multiple firstname??
    */

  lazy val jsonValid = Json.parse(
    """
      |{
      | "subjects" : {
      |  "child" : {
      |   "name" : {
      |    "givenName" : "John",
      |    "surname" : "Jones"
      |   },
      |   "dateOfBirth" : "2007-02-18"
      |  },
      |  "systemNumber" : "500035710",
      |  "id": 2135
      | }
      |}
    """.stripMargin)

  lazy val jsonMissingGivenNameKey = Json.parse(
    """
      |{
      | "subjects" : {
      |  "child" : {
      |   "name" : {
      |    "surname" : "Jones"
      |   },
      |   "dateOfBirth" : "2007-02-18"
      |  },
      |  "systemNumber" : "500035710"
      | }
      |}
    """.stripMargin)

  lazy val jsonMissingSurnameKey = Json.parse(
    """
      |{
      | "subjects" : {
      |  "child" : {
      |   "name" : {
      |    "givenName" : "John"
      |   },
      |   "dateOfBirth" : "2007-02-18"
      |  },
      |  "systemNumber" : "500035710"
      | }
      |}
    """.stripMargin)

  lazy val jsonMissingSystemNumberKey = Json.parse(
    """
      |{
      | "subjects" : {
      |  "child" : {
      |   "name" : {
      |    "givenName" : "John",
      |    "surname" : "Jones"
      |   },
      |   "dateOfBirth" : "2007-02-18"
      |  }
      | }
      |}
    """.stripMargin)

  lazy val jsonMissingDateOfBirthKey = Json.parse(
    """
      |{
      | "subjects" : {
      |  "child" : {
      |   "name" : {
      |    "givenName" : "John",
      |    "surname" : "Jones"
      |   }
      |  },
      |  "systemNumber" : "500035710"
      | }
      |}
    """.stripMargin)

  lazy val jsonMissingNameKey = Json.parse(
    """
      |{
      | "subjects" : {
      |  "child" : {
      |   "dateOfBirth" : "2007-02-18"
      |  },
      |  "systemNumber" : "500035710"
      | }
      |}
    """.stripMargin)

  lazy val jsonMissingSubjectsKey = Json.parse(
    """
      |{
      | "subjects" : {
      |  "systemNumber" : "500035710"
      | }
      |}
    """.stripMargin)

  lazy val jsonMissingEmptyObject = Json.parse(
    """
      |{
      |}
    """.stripMargin)

  lazy val jsonNoObject = Json.parse("")

  "GroResponse" should {
    "be an instance of GroResponse" in {
      val response = new GroResponse(child = Child(
        birthReferenceNumber = "500035710",
        firstName = "John",
        lastName = "Jones",
        dateOfBirth = Option(new LocalDate("2007-02-18"))))
      response shouldBe a[GroResponse]
    }

    "return GroResponse object with all Child attributes when json is valid and complete" in {
      val result = jsonValid.validate[GroResponse]

      result match {
        case JsSuccess(x, _) => {
          x shouldBe a[GroResponse]
          x.child shouldBe a[Child]
          x.child.birthReferenceNumber shouldBe "500035710"
          x.child.firstName shouldBe "John"
          x.child.lastName shouldBe "Jones"
          x.child.dateOfBirth.get.toString shouldBe "2007-02-18"
          x.child.dateOfBirth.get shouldBe a[LocalDate]
        }
        case JsError(x) =>
          throw new Exception
      }
    }

    "return GroResponse object with null Child attributes when json is empty" in {
      val result = jsonMissingEmptyObject.validate[GroResponse]
      result match {
        case JsSuccess(x, _) => {
          x shouldBe a[GroResponse]
          x.child shouldBe a[Child]
          x.child.birthReferenceNumber shouldBe ""
          x.child.firstName shouldBe ""
          x.child.lastName shouldBe ""
          x.child.dateOfBirth shouldBe None
        }
        case JsError(x) => {
          throw new Exception
        }
      }
    }

    "return GroResponse object with Child object when systemNumber key is missing" in {
      val result = jsonMissingSystemNumberKey.validate[GroResponse]
      result shouldBe a[JsSuccess[_]]
      result match {
        case JsSuccess(x, _) => {
          x shouldBe a[GroResponse]
          x.child shouldBe a[Child]
          x.child.birthReferenceNumber shouldBe ""
          x.child.firstName shouldBe "John"
          x.child.lastName shouldBe "Jones"
          x.child.dateOfBirth.get.toString shouldBe "2007-02-18"
          x.child.dateOfBirth.get shouldBe a[LocalDate]
        }
        case JsError(x) => {
          throw new Exception
        }
      }
    }

    "return GroResponse object with Child object when givenName key is missing" in {
      val result = jsonMissingGivenNameKey.validate[GroResponse]
      result shouldBe a[JsSuccess[_]]
      result match {
        case JsSuccess(x, _) => {
          x shouldBe a[GroResponse]
          x.child shouldBe a[Child]
          x.child.birthReferenceNumber shouldBe "500035710"
          x.child.firstName shouldBe ""
          x.child.lastName shouldBe "Jones"
          x.child.dateOfBirth.get.toString shouldBe "2007-02-18"
          x.child.dateOfBirth.get shouldBe a[LocalDate]
        }
        case JsError(x) => {
          throw new Exception
        }
      }
    }

    "return GroResponse object with Child object when surname key is missing" in {
      val result = jsonMissingSurnameKey.validate[GroResponse]
      result shouldBe a[JsSuccess[_]]
      result match {
        case JsSuccess(x, _) => {
          x shouldBe a[GroResponse]
          x.child shouldBe a[Child]
          x.child.birthReferenceNumber shouldBe "500035710"
          x.child.firstName shouldBe "John"
          x.child.lastName shouldBe ""
          x.child.dateOfBirth.get.toString shouldBe "2007-02-18"
          x.child.dateOfBirth.get shouldBe a[LocalDate]
        }
        case JsError(x) => {
          throw new Exception
        }
      }
    }

    "return GroResponse object with Child object when dateofBirth key is missing" in {
      val result = jsonMissingDateOfBirthKey.validate[GroResponse]
      result shouldBe a[JsSuccess[_]]
      result match {
        case JsSuccess(x, _) => {
          x shouldBe a[GroResponse]
          x.child shouldBe a[Child]
          x.child.birthReferenceNumber shouldBe "500035710"
          x.child.firstName shouldBe "John"
          x.child.lastName shouldBe "Jones"
          x.child.dateOfBirth shouldBe None
        }
        case JsError(x) => {
          throw new Exception
        }
      }
    }

    "return GroResponse object with Child object when name key is missing" in {
      val result = jsonMissingSubjectsKey.validate[GroResponse]
      result shouldBe a[JsSuccess[_]]
      result match {
        case JsSuccess(x, _) => {
          x shouldBe a[GroResponse]
          x.child shouldBe a[Child]
          x.child.birthReferenceNumber shouldBe "500035710"
          x.child.firstName shouldBe ""
          x.child.lastName shouldBe ""
          x.child.dateOfBirth shouldBe None
        }
        case JsError(x) => {
          throw new Exception
        }
      }
    }

    "return an JsonMappingException from an invalid json object" in {
      intercept[com.fasterxml.jackson.databind.JsonMappingException] {
        jsonNoObject.validate[GroResponse]
      }

    }

  }

}
