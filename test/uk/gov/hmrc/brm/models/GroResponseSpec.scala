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
      |  "systemNumber" : "500035710"
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

  "GroResponse" should {
    "be an instance of GroResponse" in {
      val response = new GroResponse(birthReferenceNumber = "500035710", firstName = "John", surname = "Jones", dateOfBirth = new LocalDate("2007-02-18"))
      response shouldBe a[GroResponse]
    }

    "return a JsSuccess object on successful mappings" in {
      val result = jsonValid.validate[GroResponse]

      result match {
        case JsSuccess(x, _) => {
          x shouldBe a[GroResponse]
          x.birthReferenceNumber shouldBe "500035710"
          x.firstName shouldBe "John"
          x.surname shouldBe "Jones"
          x.dateOfBirth.toString shouldBe "2007-02-18"
          x.dateOfBirth shouldBe a[LocalDate]
        }
        case JsError(x) => {
          throw new Exception
        }
      }
    }

    "return a JsError object when givenName key is missing" in {
      val result = jsonMissingGivenNameKey.validate[GroResponse]
      result should not be a[JsSuccess[_]]
    }

    "return a JsError object when surname key is missing" in {
      val result = jsonMissingSurnameKey.validate[GroResponse]
      result should not be a[JsSuccess[_]]
    }

    "return a JsError object when systemNumber key is missing" in {
      val result = jsonMissingSystemNumberKey.validate[GroResponse]
      result should not be a[JsSuccess[_]]
    }

    "return a JsError object when dateOfBirth key is missing" in {
      val result = jsonMissingDateOfBirthKey.validate[GroResponse]
      result should not be a[JsSuccess[_]]
    }
  }

}
