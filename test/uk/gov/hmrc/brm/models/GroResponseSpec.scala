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
import uk.gov.hmrc.brm.utils.JsonUtils
import uk.gov.hmrc.play.test.UnitSpec

/**
  * Created by chrisianson on 09/08/16.
  */
class GroResponseSpec extends UnitSpec {

  /**
    * Should
    * - should be an instance of GroResponse
    * - should return GroResponse object with all Child attributes when json is valid and complete (ASCII)
    * - should return GroResponse object with all Child attributes when json is valid and complete with ASCII-Extended characters
    * - should return GroResponse object with all Child attributes when json is valid and complete with UTF-8 characters
    * - should return GroResponse object with all Child attributes when json is valid and complete max length
    * - should return GroResponse object with null Child attributes when json is empty
    * - should return GroResponse object with Child object when systemNumber key is missing
    * - should return GroResponse object with Child object when givenName key is missing
    * - should return GroResponse object with Child object when surname key is missing
    * - should return GroResponse object with Child object when dateOfBirth key is missing
    * - should return GroResponse object with Child object when name key is missing
    * - should return GroResponse object with Child object when dateOfBirth value is invalid format
    * - should return a JsonParseException from a broken json object
    * - should return an JsonMappingException from an invalid json object
    */

  lazy val jsonFullRecord = JsonUtils.getJsonFromFile("500035710")

  lazy val maxLengthString = "XuLEjzWmZGzHbzVwxWhHjKBdGorAZNVxNdXHfwXemCXkfYPoeWbBJvtMrVuEfSfVZEkmNzhMQsscKFQLRXScwAhCWkndDQeAVRpTDbbkzDYxWHAMtYDBRDDHFHGwRQak"

  lazy val jsonValidWithUTF8 = Json.parse(
    """
      |{
      | "subjects" : {
      |  "child" : {
      |   "name" : {
      |    "givenName" : "JohͿͿŀŀŀnƷȸȸȸ- ƷġÊÊÊÊÊƂƂƂ'  ÐÐġġġÐÐÐÐœœœÐÐÐ  ÐÐÆġÆÆÅÅƼƼƼıııÅÅ",
      |    "surname" : "JonesƷġÊÊÊÊÊƂƂƂ-'"
      |   },
      |   "dateOfBirth" : "2007-02-18"
      |  }
      | },
      | "systemNumber" : 500035710
      |}
    """.stripMargin)

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
      |  }
      | },
      | "systemNumber" : 500035710
      |}
    """.stripMargin)

  lazy val jsonValidWithASCIIExtended = Json.parse(
    """
      |{
      | "subjects" : {
      |  "child" : {
      |   "name" : {
      |    "givenName" : "Johnéë",
      |    "surname" : "Jonésë"
      |   },
      |   "dateOfBirth" : "2007-02-18"
      |  }
      | },
      | "systemNumber" : 500035710
      |}
    """.stripMargin)

  /**
    * Max Length response from GRO with x1 FirstName at max length
    * x3 Max Length strings for middle names
    * x1 Max Length string for lastName
    */
  lazy val jsonValidMaxLength = Json.parse(
    s"""
       |{
       | "subjects" : {
       |  "child" : {
       |   "name" : {
       |    "givenName" : "$maxLengthString $maxLengthString $maxLengthString $maxLengthString",
       |    "surname" : "$maxLengthString"
       |   },
       |   "dateOfBirth" : "2007-02-18"
       |  }
       | },
       | "systemNumber" : 500035710
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
      |  }
      | },
      | "systemNumber" : 500035710
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
      |  }
      | },
      | "systemNumber" : 500035710
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
      |  }
      | },
      | "systemNumber" : 500035710
      |}
    """.stripMargin)

  lazy val jsonMissingNameKey = Json.parse(
    """
      |{
      | "subjects" : {
      |  "child" : {
      |   "dateOfBirth" : "2007-02-18"
      |  }
      | },
      | "systemNumber" : 500035710
      |}
    """.stripMargin)

  lazy val jsonMissingSubjectsKey = Json.parse(
    """
      |{
      | "systemNumber" : 500035710
      |}
    """.stripMargin)

  lazy val jsonMissingEmptyObject = Json.parse(
    """
      |{
      |}
    """.stripMargin)

  lazy val jsonInavlidDateOfBirthFormat = Json.parse(
    """
      |{
      | "subjects" : {
      |  "child" : {
      |   "name" : {
      |    "givenName" : "John",
      |    "surname" : "Jones"
      |   },
      |   "dateOfBirth" : "20-02-207"
      |  }
      | },
      | "systemNumber" : 500035710
      |}
    """.stripMargin)

  lazy val jsonInvalidSystemNumberType = Json.parse(
    """
      |{
      | "subjects" : {
      |  "child" : {
      |   "name" : {
      |    "givenName" : "John",
      |    "surname" : "Jones"
      |   },
      |   "dateOfBirth" : "20-02-207"
      |  }
      | },
      | "systemNumber" : "500035710"
      |}
    """.stripMargin)

  lazy val jsonNoObject = Json.parse("")

  lazy val jsonBrokenObject = Json.parse("{")

  lazy val jsonAllStatusFlags = Json.parse(
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
      | },
      | "systemNumber" : 500035710,
      | "status": {
      |    "potentiallyFictitiousBirth": false,
      |    "correction": "None",
      |    "cancelled": false,
      |    "blockedRegistration": false,
      |    "marginalNote": "None",
      |    "reRegistered": "None"
      |  }
      |}
    """.stripMargin)

  lazy val jsonStatusFlagsExcludingPotentiallyFicticiousBirth = Json.parse(
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
      | },
      | "systemNumber" : 500035710,
      | "status": {
      |    "correction": "None",
      |    "cancelled": false,
      |    "blockedRegistration": false,
      |    "marginalNote": "None",
      |    "reRegistered": "None"
      |  }
      |}
    """.stripMargin)

  lazy val jsonStatusFlagsExcludingCorrection = Json.parse(
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
      | },
      | "systemNumber" : 500035710,
      | "status": {
      |    "potentiallyFictitiousBirth": false,
      |    "cancelled": false,
      |    "blockedRegistration": false,
      |    "marginalNote": "None",
      |    "reRegistered": "None"
      |  }
      |}
    """.stripMargin)

  lazy val jsonStatusFlagsExcludingCancelled = Json.parse(
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
      | },
      | "systemNumber" : 500035710,
      | "status": {
      |    "potentiallyFictitiousBirth": false,
      |    "correction": "None",
      |    "blockedRegistration": false,
      |    "marginalNote": "None",
      |    "reRegistered": "None"
      |  }
      |}
    """.stripMargin)

  lazy val jsonStatusFlagsExcludingBlockedRegistration = Json.parse(
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
      | },
      | "systemNumber" : 500035710,
      | "status": {
      |    "potentiallyFictitiousBirth": false,
      |    "correction": "None",
      |    "cancelled": false,
      |    "marginalNote": "None",
      |    "reRegistered": "None"
      |  }
      |}
    """.stripMargin)

  lazy val jsonStatusFlagsExcludingMarginalNote = Json.parse(
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
      | },
      | "systemNumber" : 500035710,
      | "status": {
      |    "potentiallyFictitiousBirth": false,
      |    "correction": "None",
      |    "cancelled": false,
      |    "blockedRegistration": false,
      |    "reRegistered": "None"
      |  }
      |}
    """.stripMargin)

  lazy val jsonStatusFlagsExcludingReRegistered = Json.parse(
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
      | },
      | "systemNumber" : 500035710,
      | "status": {
      |    "potentiallyFictitiousBirth": false,
      |    "correction": "None",
      |    "cancelled": false,
      |    "blockedRegistration": false,
      |    "marginalNote": "None"
      |  }
      |}
    """.stripMargin)



  "GroResponse" should {
    "be an instance of GroResponse" in {
      val response = new GroResponse(child = Child(
        birthReferenceNumber = Some(500035710),
        firstName = "John",
        lastName = "Jones",
        dateOfBirth = Option(new LocalDate("2007-02-18"))))
      response shouldBe a[GroResponse]
      response.child shouldBe a[Child]
      response.status shouldBe None
    }

    "return GroResponse object with all Child attributes when json is a full record" in {
      val result = jsonFullRecord.validate[GroResponse]
      result match {
        case JsSuccess(x, _) => {
          x shouldBe a[GroResponse]
          x.child shouldBe a[Child]
          x.child.birthReferenceNumber.get shouldBe 500035710
          x.child.firstName shouldBe "Adam TEST"
          x.child.lastName shouldBe "SMITH"
          x.child.dateOfBirth.get.toString shouldBe "2006-11-12"
          x.child.dateOfBirth.get shouldBe a[LocalDate]
          x.status.get shouldBe a[Status]
          x.status.get.potentiallyFictitiousBirth shouldBe false
          x.status.get.correction.get shouldBe "None"
          x.status.get.cancelled shouldBe false
          x.status.get.blockedRegistration shouldBe false
          x.status.get.marginalNote.get shouldBe "None"
          x.status.get.reRegistered.get shouldBe "None"
        }
        case JsError(x) => {
          throw new Exception
        }
      }
    }

    "return GroResponse object with all Child attributes when json is valid and complete (ASCII)" in {
      val result = jsonValid.validate[GroResponse]
      result match {
        case JsSuccess(x, _) => {
          x shouldBe a[GroResponse]
          x.child shouldBe a[Child]
          x.child.birthReferenceNumber.get shouldBe 500035710
          x.child.firstName shouldBe "John"
          x.child.lastName shouldBe "Jones"
          x.child.dateOfBirth.get.toString shouldBe "2007-02-18"
          x.child.dateOfBirth.get shouldBe a[LocalDate]
          x.status shouldBe None
        }
        case JsError(x) =>
          throw new Exception
      }
    }

    "return GroResponse object with all Child attributes when json is valid and complete with ASCII-Extended characters" in {
      val result = jsonValidWithASCIIExtended.validate[GroResponse]
      result match {
        case JsSuccess(x, _) => {
          x shouldBe a[GroResponse]
          x.child shouldBe a[Child]
          x.child.birthReferenceNumber.get shouldBe 500035710
          x.child.firstName shouldBe "Johnéë"
          x.child.lastName shouldBe "Jonésë"
          x.child.dateOfBirth.get.toString shouldBe "2007-02-18"
          x.child.dateOfBirth.get shouldBe a[LocalDate]
          x.status shouldBe None
        }
        case JsError(x) =>
          throw new Exception
      }
    }

    "return GroResponse object with all Child attributes when json is valid and complete with UTF-8 characters" in {
      val result = jsonValidWithUTF8.validate[GroResponse]
      result match {
        case JsSuccess(x, _) => {
          x shouldBe a[GroResponse]
          x.child shouldBe a[Child]
          x.child.birthReferenceNumber.get shouldBe 500035710
          x.child.firstName shouldBe "JohͿͿŀŀŀnƷȸȸȸ- ƷġÊÊÊÊÊƂƂƂ'  ÐÐġġġÐÐÐÐœœœÐÐÐ  ÐÐÆġÆÆÅÅƼƼƼıııÅÅ"
          x.child.lastName shouldBe "JonesƷġÊÊÊÊÊƂƂƂ-'"
          x.child.dateOfBirth.get.toString shouldBe "2007-02-18"
          x.child.dateOfBirth.get shouldBe a[LocalDate]
          x.status shouldBe None
        }
        case JsError(x) =>
          throw new Exception
      }
    }

    "return GroResponse object with all Child attributes when json is valid and complete max length" in {
      val result = jsonValidMaxLength.validate[GroResponse]
      result match {
        case JsSuccess(x, _) => {
          x shouldBe a[GroResponse]
          x.child shouldBe a[Child]
          x.child.birthReferenceNumber.get shouldBe 500035710
          x.child.firstName shouldBe "XuLEjzWmZGzHbzVwxWhHjKBdGorAZNVxNdXHfwXemCXkfYPoeWbBJvtMrVuEfSfVZEkmNzhMQsscKFQLRXScwAhCWkndDQeAVRpTDbbkzDYxWHAMtYDBRDDHFHGwRQak XuLEjzWmZGzHbzVwxWhHjKBdGorAZNVxNdXHfwXemCXkfYPoeWbBJvtMrVuEfSfVZEkmNzhMQsscKFQLRXScwAhCWkndDQeAVRpTDbbkzDYxWHAMtYDBRDDHFHGwRQak XuLEjzWmZGzHbzVwxWhHjKBdGorAZNVxNdXHfwXemCXkfYPoeWbBJvtMrVuEfSfVZEkmNzhMQsscKFQLRXScwAhCWkndDQeAVRpTDbbkzDYxWHAMtYDBRDDHFHGwRQak XuLEjzWmZGzHbzVwxWhHjKBdGorAZNVxNdXHfwXemCXkfYPoeWbBJvtMrVuEfSfVZEkmNzhMQsscKFQLRXScwAhCWkndDQeAVRpTDbbkzDYxWHAMtYDBRDDHFHGwRQak"
          x.child.lastName shouldBe "XuLEjzWmZGzHbzVwxWhHjKBdGorAZNVxNdXHfwXemCXkfYPoeWbBJvtMrVuEfSfVZEkmNzhMQsscKFQLRXScwAhCWkndDQeAVRpTDbbkzDYxWHAMtYDBRDDHFHGwRQak"
          x.child.dateOfBirth.get.toString shouldBe "2007-02-18"
          x.child.dateOfBirth.get shouldBe a[LocalDate]
          x.status shouldBe None
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
          x.child.birthReferenceNumber shouldBe None
          x.child.firstName shouldBe ""
          x.child.lastName shouldBe ""
          x.child.dateOfBirth shouldBe None
          x.status shouldBe None
        }
        case JsError(x) => {
          throw new Exception
        }
      }
    }

    "return GroResponse object with null Child attributes when systemNumber is a string" in {
      val result = jsonInvalidSystemNumberType.validate[GroResponse]
      result match {
        case JsSuccess(x, _) => {
          x shouldBe a[GroResponse]
          x.child shouldBe a[Child]
          x.child.birthReferenceNumber shouldBe None
          x.child.firstName shouldBe "John"
          x.child.lastName shouldBe "Jones"
          x.child.dateOfBirth shouldBe None
          x.status shouldBe None
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
          x.child.birthReferenceNumber shouldBe None
          x.child.firstName shouldBe "John"
          x.child.lastName shouldBe "Jones"
          x.child.dateOfBirth.get.toString shouldBe "2007-02-18"
          x.child.dateOfBirth.get shouldBe a[LocalDate]
          x.status shouldBe None
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
          x.child.birthReferenceNumber.get shouldBe 500035710
          x.child.firstName shouldBe ""
          x.child.lastName shouldBe "Jones"
          x.child.dateOfBirth.get.toString shouldBe "2007-02-18"
          x.child.dateOfBirth.get shouldBe a[LocalDate]
          x.status shouldBe None
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
          x.child.birthReferenceNumber.get shouldBe 500035710
          x.child.firstName shouldBe "John"
          x.child.lastName shouldBe ""
          x.child.dateOfBirth.get.toString shouldBe "2007-02-18"
          x.child.dateOfBirth.get shouldBe a[LocalDate]
          x.status shouldBe None
        }
        case JsError(x) => {
          throw new Exception
        }
      }
    }

    "return GroResponse object with Child object when dateOfBirth key is missing" in {
      val result = jsonMissingDateOfBirthKey.validate[GroResponse]
      result shouldBe a[JsSuccess[_]]
      result match {
        case JsSuccess(x, _) => {
          x shouldBe a[GroResponse]
          x.child shouldBe a[Child]
          x.child.birthReferenceNumber.get shouldBe 500035710
          x.child.firstName shouldBe "John"
          x.child.lastName shouldBe "Jones"
          x.child.dateOfBirth shouldBe None
          x.status shouldBe None
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
          x.child.birthReferenceNumber.get shouldBe 500035710
          x.child.firstName shouldBe ""
          x.child.lastName shouldBe ""
          x.child.dateOfBirth shouldBe None
          x.status shouldBe None
        }
        case JsError(x) => {
          throw new Exception
        }
      }
    }

    "return GroResponse object with Child object when dateOfBirth value is invalid format" in {
      val result = jsonInavlidDateOfBirthFormat.validate[GroResponse]
      result shouldBe a[JsSuccess[_]]
      result match {
        case JsSuccess(x, _) => {
          x shouldBe a[GroResponse]
          x.child shouldBe a[Child]
          x.child.birthReferenceNumber.get shouldBe 500035710
          x.child.firstName shouldBe "John"
          x.child.lastName shouldBe "Jones"
          x.child.dateOfBirth shouldBe None
          x.status shouldBe None
        }
        case JsError(x) => {
          throw new Exception
        }
      }
    }

    "return GroResponse object with Status object when all status flags exist" in {
      val result = jsonAllStatusFlags.validate[GroResponse]
      result shouldBe a[JsSuccess[_]]
      result match {
        case JsSuccess(x, _) => {
          x shouldBe a[GroResponse]
          x.child shouldBe a[Child]
          x.child.birthReferenceNumber.get shouldBe 500035710
          x.child.firstName shouldBe "John"
          x.child.lastName shouldBe "Jones"
          x.child.dateOfBirth.get.toString shouldBe "2007-02-18"
          x.child.dateOfBirth.get shouldBe a[LocalDate]
          x.status.get shouldBe a[Status]
          x.status.get.potentiallyFictitiousBirth shouldBe false
          x.status.get.correction.get shouldBe "None"
          x.status.get.cancelled shouldBe false
          x.status.get.blockedRegistration shouldBe false
          x.status.get.marginalNote.get shouldBe "None"
          x.status.get.reRegistered.get shouldBe "None"
        }
        case JsError(x) => {
          throw new Exception
        }
      }
    }

    "return GroResponse object with Status object when potentiallyFictitiousBirth key is excluded" in {
      val result = jsonStatusFlagsExcludingPotentiallyFicticiousBirth.validate[GroResponse]
      result shouldBe a[JsSuccess[_]]
      result match {
        case JsSuccess(x, _) => {
          x shouldBe a[GroResponse]
          x.child shouldBe a[Child]
          x.child.birthReferenceNumber.get shouldBe 500035710
          x.child.firstName shouldBe "John"
          x.child.lastName shouldBe "Jones"
          x.child.dateOfBirth.get.toString shouldBe "2007-02-18"
          x.child.dateOfBirth.get shouldBe a[LocalDate]
          x.status.get shouldBe a[Status]
          x.status.get.potentiallyFictitiousBirth shouldBe false
          x.status.get.correction.get shouldBe "None"
          x.status.get.cancelled shouldBe false
          x.status.get.blockedRegistration shouldBe false
          x.status.get.marginalNote.get shouldBe "None"
          x.status.get.reRegistered.get shouldBe "None"
        }
        case JsError(x) => {
          throw new Exception
        }
      }
    }

    "return GroResponse object with Status object when correction key is excluded" in {
      val result = jsonStatusFlagsExcludingCorrection.validate[GroResponse]
      result shouldBe a[JsSuccess[_]]
      result match {
        case JsSuccess(x, _) => {
          x shouldBe a[GroResponse]
          x.child shouldBe a[Child]
          x.child.birthReferenceNumber.get shouldBe 500035710
          x.child.firstName shouldBe "John"
          x.child.lastName shouldBe "Jones"
          x.child.dateOfBirth.get.toString shouldBe "2007-02-18"
          x.child.dateOfBirth.get shouldBe a[LocalDate]
          x.status.get shouldBe a[Status]
          x.status.get.potentiallyFictitiousBirth shouldBe false
          x.status.get.correction shouldBe None
          x.status.get.cancelled shouldBe false
          x.status.get.blockedRegistration shouldBe false
          x.status.get.marginalNote.get shouldBe "None"
          x.status.get.reRegistered.get shouldBe "None"
        }
        case JsError(x) => {
          throw new Exception
        }
      }
    }

    "return GroResponse object with Status object when cancelled key is excluded" in {
      val result = jsonStatusFlagsExcludingCancelled.validate[GroResponse]
      result shouldBe a[JsSuccess[_]]
      result match {
        case JsSuccess(x, _) => {
          x shouldBe a[GroResponse]
          x.child shouldBe a[Child]
          x.child.birthReferenceNumber.get shouldBe 500035710
          x.child.firstName shouldBe "John"
          x.child.lastName shouldBe "Jones"
          x.child.dateOfBirth.get.toString shouldBe "2007-02-18"
          x.child.dateOfBirth.get shouldBe a[LocalDate]
          x.status.get shouldBe a[Status]
          x.status.get.potentiallyFictitiousBirth shouldBe false
          x.status.get.correction.get shouldBe "None"
          x.status.get.cancelled shouldBe false
          x.status.get.blockedRegistration shouldBe false
          x.status.get.marginalNote.get shouldBe "None"
          x.status.get.reRegistered.get shouldBe "None"
        }
        case JsError(x) => {
          throw new Exception
        }
      }
    }

    "return GroResponse object with Status object when blockedRegistration key is excluded" in {
      val result = jsonStatusFlagsExcludingBlockedRegistration.validate[GroResponse]
      result shouldBe a[JsSuccess[_]]
      result match {
        case JsSuccess(x, _) => {
          x shouldBe a[GroResponse]
          x.child shouldBe a[Child]
          x.child.birthReferenceNumber.get shouldBe 500035710
          x.child.firstName shouldBe "John"
          x.child.lastName shouldBe "Jones"
          x.child.dateOfBirth.get.toString shouldBe "2007-02-18"
          x.child.dateOfBirth.get shouldBe a[LocalDate]
          x.status.get shouldBe a[Status]
          x.status.get.potentiallyFictitiousBirth shouldBe false
          x.status.get.correction.get shouldBe "None"
          x.status.get.cancelled shouldBe false
          x.status.get.blockedRegistration shouldBe false
          x.status.get.marginalNote.get shouldBe "None"
          x.status.get.reRegistered.get shouldBe "None"
        }
        case JsError(x) => {
          throw new Exception
        }
      }
    }

    "return GroResponse object with Status object when marginalNote key is excluded" in {
      val result = jsonStatusFlagsExcludingMarginalNote.validate[GroResponse]
      result shouldBe a[JsSuccess[_]]
      result match {
        case JsSuccess(x, _) => {
          x shouldBe a[GroResponse]
          x.child shouldBe a[Child]
          x.child.birthReferenceNumber.get shouldBe 500035710
          x.child.firstName shouldBe "John"
          x.child.lastName shouldBe "Jones"
          x.child.dateOfBirth.get.toString shouldBe "2007-02-18"
          x.child.dateOfBirth.get shouldBe a[LocalDate]
          x.status.get shouldBe a[Status]
          x.status.get.potentiallyFictitiousBirth shouldBe false
          x.status.get.correction.get shouldBe "None"
          x.status.get.cancelled shouldBe false
          x.status.get.blockedRegistration shouldBe false
          x.status.get.marginalNote shouldBe None
          x.status.get.reRegistered.get shouldBe "None"
        }
        case JsError(x) => {
          throw new Exception
        }
      }
    }

    "return GroResponse object with Status object when reRegistered key is excluded" in {
      val result = jsonStatusFlagsExcludingReRegistered.validate[GroResponse]
      result shouldBe a[JsSuccess[_]]
      result match {
        case JsSuccess(x, _) => {
          x shouldBe a[GroResponse]
          x.child shouldBe a[Child]
          x.child.birthReferenceNumber.get shouldBe 500035710
          x.child.firstName shouldBe "John"
          x.child.lastName shouldBe "Jones"
          x.child.dateOfBirth.get.toString shouldBe "2007-02-18"
          x.child.dateOfBirth.get shouldBe a[LocalDate]
          x.status.get shouldBe a[Status]
          x.status.get.potentiallyFictitiousBirth shouldBe false
          x.status.get.correction.get shouldBe "None"
          x.status.get.cancelled shouldBe false
          x.status.get.blockedRegistration shouldBe false
          x.status.get.marginalNote.get shouldBe "None"
          x.status.get.reRegistered shouldBe None
        }
        case JsError(x) => {
          throw new Exception
        }
      }
    }

    "return a JsonParseException from a broken json object" in {
      intercept[com.fasterxml.jackson.core.JsonParseException] {
        jsonBrokenObject.validate[GroResponse]
      }
    }

    "return a JsonMappingException from an invalid json object" in {
      intercept[com.fasterxml.jackson.databind.JsonMappingException] {
        jsonNoObject.validate[GroResponse]
      }

    }

  }

}
