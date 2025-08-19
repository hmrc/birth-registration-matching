/*
 * Copyright 2025 HM Revenue & Customs
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

import java.time.LocalDate
import play.api.libs.json.{JsError, JsSuccess, JsValue, Json}
import uk.gov.hmrc.brm.models.response.gro.GROStatus
import uk.gov.hmrc.brm.models.response.{Child, Record}
import uk.gov.hmrc.brm.utils.{JsonUtils, ReadsUtil}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatest.OptionValues

/** Created by chrisianson on 09/08/16.
  */
class GroResponseSpec extends AnyWordSpecLike with Matchers with OptionValues {

  /** Should
    *   - should be an instance of GroResponse
    *   - should return GroResponse object with all Child attributes when json is valid and complete (ASCII)
    *   - should return GroResponse object with all Child attributes when json is valid and complete with ASCII-Extended
    *     characters
    *   - should return GroResponse object with all Child attributes when json is valid and complete with UTF-8
    *     characters
    *   - should return GroResponse object with all Child attributes when json is valid and complete max length
    *   - should return GroResponse object with null Child attributes when json is empty
    *   - should return GroResponse object with Child object when systemNumber key is missing
    *   - should return GroResponse object with Child object when givenName key is missing
    *   - should return GroResponse object with Child object when surname key is missing
    *   - should return GroResponse object with Child object when dateOfBirth key is missing
    *   - should return GroResponse object with Child object when name key is missing
    *   - should return GroResponse object with Child object when dateOfBirth value is invalid format
    *   - should return GROResponse object with missing properties in all objects
    *   - should return a JsonParseException from a broken json object
    *   - should return an JsonMappingException from an invalid json object
    */

  lazy val jsonFullRecord: JsValue = JsonUtils.getJsonFromFile("gro", "500035710")

  lazy val jsonFullRecordCollection: JsValue = JsonUtils.getJsonFromFile("gro", "500035710-array")

  lazy val jsonRecordKeysNoValues: JsValue = JsonUtils.getJsonFromFile("gro", "key-no-value")

  lazy val maxLengthString =
    "XuLEjzWmZGzHbzVwxWhHjKBdGorAZNVxNdXHfwXemCXkfYPoeWbBJvtMrVuEfSfVZEkmNzhMQsscKFQLRXScwAhCWkndDQeAVRpTDbbkzDYxWHAMtYDBRDDHFHGwRQak"

  lazy val jsonValidWithUTF8: JsValue = Json.parse(
    """
      |{
      | "subjects" : {
      |  "child" : {
      |   "name" : {
      |    "givenName" : "JohͿͿŀŀŀnƷȸȸȸ- ƷġÊÊÊÊÊƂƂƂ' ÐÐġġġÐÐÐÐœœœÐÐÐ ÐÐÆġÆÆÅÅƼƼƼıııÅÅ",
      |    "surname" : "JonesƷġÊÊÊÊÊƂƂƂ-'"
      |   },
      |   "dateOfBirth" : "2007-02-18"
      |  }
      | },
      | "systemNumber" : 500035710
      |}
    """.stripMargin
  )

  lazy val jsonValid: JsValue = Json.parse(
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
    """.stripMargin
  )

  lazy val jsonValidWithASCIIExtended: JsValue = Json.parse(
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
    """.stripMargin
  )

  /** Max Length response from GRO with x1 FirstName at max length x3 Max Length strings for middle names x1 Max Length
    * string for lastName
    */
  lazy val jsonValidMaxLength: JsValue = Json.parse(
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
    """.stripMargin
  )

  lazy val jsonMissingGivenNameKey: JsValue = Json.parse(
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
    """.stripMargin
  )

  lazy val jsonMissingSurnameKey: JsValue = Json.parse(
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
    """.stripMargin
  )

  lazy val jsonMissingObjectsProperties: JsValue = Json.parse(
    """
    |{
    |  "location": {
    |
    |  },
    |  "subjects": {
    |    "child": {
    |      "name": {
    |
    |      },
    |      "originalName": {
    |
    |      }
    |    },
    |    "father": {
    |      "name": {
    |
    |      }
    |    },
    |    "mother": {
    |      "name": {
    |
    |      }
    |    },
    |    "informant": {
    |      "name": {
    |
    |      }
    |    }
    |  },
    |  "systemNumber": 999999920,
    |  "id": 999999920,
    |  "status": {
    |    "blockedRegistration": false
    |  },
    |  "previousRegistration": {}
    |
    |  }
  """.stripMargin
  )

  lazy val jsonMissingSystemNumberKey: JsValue = Json.parse(
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
    """.stripMargin
  )

  lazy val jsonMissingDateOfBirthKey: JsValue = Json.parse(
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
    """.stripMargin
  )

  lazy val jsonMissingNameKey: JsValue = Json.parse(
    """
      |{
      | "subjects" : {
      |  "child" : {
      |   "dateOfBirth" : "2007-02-18"
      |  }
      | },
      | "systemNumber" : 500035710
      |}
    """.stripMargin
  )

  lazy val jsonMissingSubjectsKey: JsValue = Json.parse(
    """
      |{
      | "systemNumber" : 500035710
      |}
    """.stripMargin
  )

  lazy val jsonMissingEmptyObject: JsValue = Json.parse(
    """
      |{
      |}
    """.stripMargin
  )

  lazy val jsonInavlidDateOfBirthFormat: JsValue = Json.parse(
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
    """.stripMargin
  )

  lazy val jsonInvalidSystemNumberType: JsValue = Json.parse(
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
    """.stripMargin
  )

  lazy val jsonNoObject: JsValue = Json.parse("")

  lazy val jsonBrokenObject: JsValue = Json.parse("{")

  lazy val jsonAllStatusFlags: JsValue = Json.parse(
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
    """.stripMargin
  )

  lazy val jsonAllStatusFlagsPotentiallyFictious: JsValue = Json.parse(
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
      |    "potentiallyFictitiousBirth": true,
      |    "correction": "None",
      |    "cancelled": false,
      |    "blockedRegistration": false,
      |    "marginalNote": "None",
      |    "reRegistered": "None"
      |  }
      |}
    """.stripMargin
  )

  lazy val jsonAllStatusFlagsCorrection: JsValue = Json.parse(
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
      |    "correction": "Typographical",
      |    "cancelled": false,
      |    "blockedRegistration": false,
      |    "marginalNote": "None",
      |    "reRegistered": "None"
      |  }
      |}
    """.stripMargin
  )

  lazy val jsonAllStatusFlagsCancelled: JsValue = Json.parse(
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
      |    "cancelled": true,
      |    "blockedRegistration": false,
      |    "marginalNote": "None",
      |    "reRegistered": "None"
      |  }
      |}
    """.stripMargin
  )

  lazy val jsonAllStatusFlagsBlocked: JsValue = Json.parse(
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
      |    "blockedRegistration": true,
      |    "marginalNote": "None",
      |    "reRegistered": "None"
      |  }
      |}
    """.stripMargin
  )

  lazy val jsonAllStatusFlagsMarginalNote: JsValue = Json.parse(
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
      |    "marginalNote": "Re-registered",
      |    "reRegistered": "None"
      |  }
      |}
    """.stripMargin
  )

  lazy val jsonAllStatusFlagsReregistered: JsValue = Json.parse(
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
      |    "reRegistered": "Re-registered"
      |  }
      |}
    """.stripMargin
  )

  lazy val jsonStatusFlagsExcludingPotentiallyFicticiousBirth: JsValue = Json.parse(
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
    """.stripMargin
  )

  lazy val jsonStatusFlagsExcludingCorrection: JsValue = Json.parse(
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
    """.stripMargin
  )

  lazy val jsonStatusFlagsExcludingCancelled: JsValue = Json.parse(
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
    """.stripMargin
  )

  lazy val jsonStatusFlagsExcludingBlockedRegistration: JsValue = Json.parse(
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
    """.stripMargin
  )

  lazy val jsonStatusFlagsExcludingMarginalNote: JsValue = Json.parse(
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
    """.stripMargin
  )

  lazy val jsonStatusFlagsExcludingReRegistered: JsValue = Json.parse(
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
    """.stripMargin
  )

  "Record" should {

    "return a Map() of flags where potentiallyFictitiousBirth" in {

      val result = jsonAllStatusFlagsPotentiallyFictious.validate[Record](ReadsUtil.groReadRecord).get
      result.status.get.flags shouldBe
        Map(
          "potentiallyFictitiousBirth" -> "true",
          "correction"                 -> "None",
          "cancelled"                  -> "false",
          "blockedRegistration"        -> "false",
          "marginalNote"               -> "None",
          "reRegistered"               -> "None"
        )
    }

    "return a Map() of flags where correction" in {

      val result = jsonAllStatusFlagsCorrection.validate[Record](ReadsUtil.groReadRecord).get
      result.status.get.flags shouldBe
        Map(
          "potentiallyFictitiousBirth" -> "false",
          "correction"                 -> "Correction on record",
          "cancelled"                  -> "false",
          "blockedRegistration"        -> "false",
          "marginalNote"               -> "None",
          "reRegistered"               -> "None"
        )
    }

    "return a Map() of flags where cancelled" in {

      val result = jsonAllStatusFlagsCancelled.validate[Record](ReadsUtil.groReadRecord).get
      result.status.get.flags shouldBe
        Map(
          "potentiallyFictitiousBirth" -> "false",
          "correction"                 -> "None",
          "cancelled"                  -> "true",
          "blockedRegistration"        -> "false",
          "marginalNote"               -> "None",
          "reRegistered"               -> "None"
        )
    }

    "return a Map() of flags where blocked" in {

      val result = jsonAllStatusFlagsBlocked.validate[Record](ReadsUtil.groReadRecord).get
      result.status.get.flags shouldBe
        Map(
          "potentiallyFictitiousBirth" -> "false",
          "correction"                 -> "None",
          "cancelled"                  -> "false",
          "blockedRegistration"        -> "true",
          "marginalNote"               -> "None",
          "reRegistered"               -> "None"
        )
    }

    "return a Map() of flags where marginal note" in {
      // We currently strip out the response for marginalNote and provide a default
      val result = jsonAllStatusFlagsMarginalNote.validate[Record](ReadsUtil.groReadRecord).get
      result.status.get.flags shouldBe
        Map(
          "potentiallyFictitiousBirth" -> "false",
          "correction"                 -> "None",
          "cancelled"                  -> "false",
          "blockedRegistration"        -> "false",
          "marginalNote"               -> "Marginal note on record",
          "reRegistered"               -> "None"
        )
    }

    "return a Map() of flags where re-registered" in {

      val result = jsonAllStatusFlagsReregistered.validate[Record](ReadsUtil.groReadRecord).get
      result.status.get.flags shouldBe Map(
        "potentiallyFictitiousBirth" -> "false",
        "correction"                 -> "None",
        "cancelled"                  -> "false",
        "blockedRegistration"        -> "false",
        "marginalNote"               -> "None",
        "reRegistered"               -> "Re-registration on record"
      )

    }

    "return Record object with all Child attributes when json is a full record within an array" in {

      val listOfRecords = jsonFullRecordCollection.as[List[Record]](ReadsUtil.groRecordsListRead)

      val record = listOfRecords.head

      listOfRecords.length                                                 shouldBe 1
      record                                                               shouldBe a[Record]
      record.child                                                         shouldBe a[Child]
      record.child.birthReferenceNumber                                    shouldBe 500035710
      record.child.forenames                                               shouldBe "Adam TEST"
      record.child.lastName                                                shouldBe "SMITH"
      record.child.dateOfBirth.get.toString                                shouldBe "2006-11-12"
      record.child.dateOfBirth.get                                         shouldBe a[LocalDate]
      record.status.get                                                    shouldBe a[GROStatus]
      record.status.get.asInstanceOf[GROStatus].potentiallyFictitiousBirth shouldBe false
      record.status.get.asInstanceOf[GROStatus].correction.get             shouldBe "None"
      record.status.get.asInstanceOf[GROStatus].cancelled                  shouldBe false
      record.status.get.asInstanceOf[GROStatus].blockedRegistration        shouldBe false
      record.status.get.asInstanceOf[GROStatus].marginalNote.get           shouldBe "None"
      record.status.get.asInstanceOf[GROStatus].reRegistered.get           shouldBe "None"
    }

    "return Record object with all Child attributes when json is valid and complete (ASCII)" in {
      val result = jsonValid.validate[Record](ReadsUtil.groReadRecord)
      result match {
        case JsSuccess(x, _) =>
          x                                shouldBe a[Record]
          x.child                          shouldBe a[Child]
          x.child.birthReferenceNumber     shouldBe 500035710
          x.child.forenames                shouldBe "John"
          x.child.lastName                 shouldBe "Jones"
          x.child.dateOfBirth.get.toString shouldBe "2007-02-18"
          x.child.dateOfBirth.get          shouldBe a[LocalDate]
          x.status                         shouldBe None
        case JsError(_)      =>
          throw new Exception
      }
    }

    "return Record object with all Child attributes when json is valid and complete with ASCII-Extended characters" in {
      val result = jsonValidWithASCIIExtended.validate[Record](ReadsUtil.groReadRecord)
      result match {
        case JsSuccess(x, _) =>
          x                                shouldBe a[Record]
          x.child                          shouldBe a[Child]
          x.child.birthReferenceNumber     shouldBe 500035710
          x.child.forenames                shouldBe "Johnéë"
          x.child.lastName                 shouldBe "Jonésë"
          x.child.dateOfBirth.get.toString shouldBe "2007-02-18"
          x.child.dateOfBirth.get          shouldBe a[LocalDate]
          x.status                         shouldBe None
        case JsError(_)      =>
          throw new Exception
      }
    }

    "return Record object with all Child attributes when json is valid and complete with UTF-8 characters" in {
      val result = jsonValidWithUTF8.validate[Record](ReadsUtil.groReadRecord)
      result match {
        case JsSuccess(x, _) =>
          x                                shouldBe a[Record]
          x.child                          shouldBe a[Child]
          x.child.birthReferenceNumber     shouldBe 500035710
          x.child.forenames                shouldBe "JohͿͿŀŀŀnƷȸȸȸ- ƷġÊÊÊÊÊƂƂƂ' ÐÐġġġÐÐÐÐœœœÐÐÐ ÐÐÆġÆÆÅÅƼƼƼıııÅÅ"
          x.child.lastName                 shouldBe "JonesƷġÊÊÊÊÊƂƂƂ-'"
          x.child.dateOfBirth.get.toString shouldBe "2007-02-18"
          x.child.dateOfBirth.get          shouldBe a[LocalDate]
          x.status                         shouldBe None
        case JsError(_)      =>
          throw new Exception
      }
    }

    "return Record object with all Child attributes when json is valid and complete max length" in {
      val result = jsonValidMaxLength.validate[Record](ReadsUtil.groReadRecord)
      result match {
        case JsSuccess(x, _) =>
          x                                shouldBe a[Record]
          x.child                          shouldBe a[Child]
          x.child.birthReferenceNumber     shouldBe 500035710
          x.child.forenames                shouldBe "XuLEjzWmZGzHbzVwxWhHjKBdGorAZNVxNdXHfwXemCXkfYPoeWbBJvtMrVuEfSfVZEkmNzhMQsscKFQL" +
            "RXScwAhCWkndDQeAVRpTDbbkzDYxWHAMtYDBRDDHFHGwRQak XuLEjzWmZGzHbzVwxWhHjKBdGorAZNVxNdXHfwXemCXkfYPoeWbBJvtM" +
            "rVuEfSfVZEkmNzhMQsscKFQLRXScwAhCWkndDQeAVRpTDbbkzDYxWHAMtYDBRDDHFHGwRQak XuLEjzWmZGzHbzVwxWhHjKBdGorAZNVx" +
            "NdXHfwXemCXkfYPoeWbBJvtMrVuEfSfVZEkmNzhMQsscKFQLRXScwAhCWkndDQeAVRpTDbbkzDYxWHAMtYDBRDDHFHGwRQak XuLEjzWm" +
            "ZGzHbzVwxWhHjKBdGorAZNVxNdXHfwXemCXkfYPoeWbBJvtMrVuEfSfVZEkmNzhMQsscKFQLRXScwAhCWkndDQeAVRpTDbbkzDYxWHAMt" +
            "YDBRDDHFHGwRQak"
          x.child.lastName                 shouldBe "XuLEjzWmZGzHbzVwxWhHjKBdGorAZNVxNdXHfwXemCXkfYPoeWbBJvtMrVuEfSfVZEkmNzhMQsscKFQLR" +
            "XScwAhCWkndDQeAVRpTDbbkzDYxWHAMtYDBRDDHFHGwRQak"
          x.child.dateOfBirth.get.toString shouldBe "2007-02-18"
          x.child.dateOfBirth.get          shouldBe a[LocalDate]
          x.status                         shouldBe None
        case JsError(_)      =>
          throw new Exception
      }
    }

    "return Record object with null Child attributes when json is empty" in {
      val result = jsonMissingEmptyObject.validate[Record](ReadsUtil.groReadRecord)
      result match {
        case JsSuccess(_, _) =>
          throw new Exception
        case JsError(x)      =>
          x.length             shouldBe 1
          x.head._2.length     shouldBe 1
          x.head._1.toString() shouldBe "/systemNumber"
      }
    }

    "return Record object with Child and Status objects when json contains subjects and status keys but no values exist" in {
      val result = jsonRecordKeysNoValues.validate[Record](ReadsUtil.groReadRecord)
      result match {
        case JsSuccess(x, _) =>
          x                                                               shouldBe a[Record]
          x.child                                                         shouldBe a[Child]
          x.child.birthReferenceNumber                                    shouldBe 999999926
          x.child.forenames                                               shouldBe ""
          x.child.lastName                                                shouldBe ""
          x.child.dateOfBirth                                             shouldBe None
          x.status.get                                                    shouldBe a[GROStatus]
          x.status.get.asInstanceOf[GROStatus].potentiallyFictitiousBirth shouldBe false
          x.status.get.asInstanceOf[GROStatus].correction                 shouldBe None
          x.status.get.asInstanceOf[GROStatus].cancelled                  shouldBe false
          x.status.get.asInstanceOf[GROStatus].blockedRegistration        shouldBe false
          x.status.get.asInstanceOf[GROStatus].marginalNote               shouldBe None
          x.status.get.asInstanceOf[GROStatus].reRegistered               shouldBe None
        case JsError(_)      =>
          throw new Exception
      }
    }

    "return Record object with Child object when systemNumber is a string" in {
      val result = jsonInvalidSystemNumberType.validate[Record](ReadsUtil.groReadRecord)
      result match {
        case JsSuccess(_, _) =>
          throw new Exception
        case JsError(x)      =>
          x.length             shouldBe 1
          x.head._2.length     shouldBe 1
          x.head._1.toString() shouldBe "/systemNumber"
      }
    }

    "return Record object with Child object when systemNumber key is missing" in {
      val result = jsonMissingSystemNumberKey.validate[Record](ReadsUtil.groReadRecord)
      result should not be a[JsSuccess[_]]
      result match {
        case JsSuccess(_, _) =>
          throw new Exception
        case JsError(x)      =>
          x.length             shouldBe 1
          x.head._2.length     shouldBe 1
          x.head._1.toString() shouldBe "/systemNumber"
      }
    }

    "return Record object with missing properties in all objects" in {
      val result = jsonMissingObjectsProperties.validate[Record](ReadsUtil.groReadRecord)
      result shouldBe a[JsSuccess[_]]
      result match {
        case JsSuccess(x, _) =>
          x                            shouldBe a[Record]
          x.child                      shouldBe a[Child]
          x.child.birthReferenceNumber shouldBe 999999920
          x.child.forenames            shouldBe empty
          x.child.lastName             shouldBe empty
          x.child.dateOfBirth          shouldBe None
          x.status                     shouldBe Some(
            GROStatus(
              potentiallyFictitiousBirth = false,
              None,
              cancelled = false,
              blockedRegistration = false,
              None,
              None
            )
          )
        case JsError(_)      =>
          throw new Exception
      }
    }

    "return Record object with Child object when givenName key is missing" in {
      val result = jsonMissingGivenNameKey.validate[Record](ReadsUtil.groReadRecord)
      result shouldBe a[JsSuccess[_]]
      result match {
        case JsSuccess(x, _) =>
          x                                shouldBe a[Record]
          x.child                          shouldBe a[Child]
          x.child.birthReferenceNumber     shouldBe 500035710
          x.child.forenames                shouldBe ""
          x.child.lastName                 shouldBe "Jones"
          x.child.dateOfBirth.get.toString shouldBe "2007-02-18"
          x.child.dateOfBirth.get          shouldBe a[LocalDate]
          x.status                         shouldBe None
        case JsError(_)      =>
          throw new Exception
      }
    }

    "return Record object with Child object when surname key is missing" in {
      val result = jsonMissingSurnameKey.validate[Record](ReadsUtil.groReadRecord)
      result shouldBe a[JsSuccess[_]]
      result match {
        case JsSuccess(x, _) =>
          x                                shouldBe a[Record]
          x.child                          shouldBe a[Child]
          x.child.birthReferenceNumber     shouldBe 500035710
          x.child.forenames                shouldBe "John"
          x.child.lastName                 shouldBe ""
          x.child.dateOfBirth.get.toString shouldBe "2007-02-18"
          x.child.dateOfBirth.get          shouldBe a[LocalDate]
          x.status                         shouldBe None
        case JsError(_)      =>
          throw new Exception
      }
    }

    "return Record object with Child object when dateOfBirth key is missing" in {
      val result = jsonMissingDateOfBirthKey.validate[Record](ReadsUtil.groReadRecord)
      result shouldBe a[JsSuccess[_]]
      result match {
        case JsSuccess(x, _) =>
          x                            shouldBe a[Record]
          x.child                      shouldBe a[Child]
          x.child.birthReferenceNumber shouldBe 500035710
          x.child.forenames            shouldBe "John"
          x.child.lastName             shouldBe "Jones"
          x.child.dateOfBirth          shouldBe None
          x.status                     shouldBe None
        case JsError(_)      =>
          throw new Exception
      }
    }

    "return Record object with Child object when name key is missing" in {
      val result = jsonMissingSubjectsKey.validate[Record](ReadsUtil.groReadRecord)
      result shouldBe a[JsSuccess[_]]
      result match {
        case JsSuccess(x, _) =>
          x                            shouldBe a[Record]
          x.child                      shouldBe a[Child]
          x.child.birthReferenceNumber shouldBe 500035710
          x.child.forenames            shouldBe ""
          x.child.lastName             shouldBe ""
          x.child.dateOfBirth          shouldBe None
          x.status                     shouldBe None
        case JsError(_)      =>
          throw new Exception
      }
    }

    "return Record object with Child object when dateOfBirth value is invalid format" in {
      val result = jsonInavlidDateOfBirthFormat.validate[Record](ReadsUtil.groReadRecord)
      result shouldBe a[JsSuccess[_]]
      result match {
        case JsSuccess(x, _) =>
          x                            shouldBe a[Record]
          x.child                      shouldBe a[Child]
          x.child.birthReferenceNumber shouldBe 500035710
          x.child.forenames            shouldBe "John"
          x.child.lastName             shouldBe "Jones"
          x.child.dateOfBirth          shouldBe None
          x.status                     shouldBe None
        case JsError(_)      =>
          throw new Exception
      }
    }

    "return Record object with Status object when all status flags exist" in {
      val result = jsonAllStatusFlags.validate[Record](ReadsUtil.groReadRecord)
      result shouldBe a[JsSuccess[_]]
      result match {
        case JsSuccess(x, _) =>
          x                                                               shouldBe a[Record]
          x.child                                                         shouldBe a[Child]
          x.child.birthReferenceNumber                                    shouldBe 500035710
          x.child.forenames                                               shouldBe "John"
          x.child.lastName                                                shouldBe "Jones"
          x.child.dateOfBirth.get.toString                                shouldBe "2007-02-18"
          x.child.dateOfBirth.get                                         shouldBe a[LocalDate]
          x.status.get                                                    shouldBe a[GROStatus]
          x.status.get.asInstanceOf[GROStatus].potentiallyFictitiousBirth shouldBe false
          x.status.get.asInstanceOf[GROStatus].correction.get             shouldBe "None"
          x.status.get.asInstanceOf[GROStatus].cancelled                  shouldBe false
          x.status.get.asInstanceOf[GROStatus].blockedRegistration        shouldBe false
          x.status.get.asInstanceOf[GROStatus].marginalNote.get           shouldBe "None"
          x.status.get.asInstanceOf[GROStatus].reRegistered.get           shouldBe "None"
          x.status.get.toJson                                             shouldBe
            Json.parse(s"""
               |{
               |  "potentiallyFictitiousBirth": "false",
               |  "correction": "None",
               |  "cancelled": "false",
               |  "blockedRegistration": "false",
               |  "marginalNote": "None",
               |  "reRegistered": "None"
               |}
             """.stripMargin)
        case JsError(_)      =>
          throw new Exception
      }
    }

    "return Record object with Status object when potentiallyFictitiousBirth key is excluded" in {
      val result = jsonStatusFlagsExcludingPotentiallyFicticiousBirth.validate[Record](ReadsUtil.groReadRecord)
      result shouldBe a[JsSuccess[_]]
      result match {
        case JsSuccess(x, _) =>
          x                                                               shouldBe a[Record]
          x.child                                                         shouldBe a[Child]
          x.child.birthReferenceNumber                                    shouldBe 500035710
          x.child.forenames                                               shouldBe "John"
          x.child.lastName                                                shouldBe "Jones"
          x.child.dateOfBirth.get.toString                                shouldBe "2007-02-18"
          x.child.dateOfBirth.get                                         shouldBe a[LocalDate]
          x.status.get                                                    shouldBe a[GROStatus]
          x.status.get.asInstanceOf[GROStatus].potentiallyFictitiousBirth shouldBe false
          x.status.get.asInstanceOf[GROStatus].correction.get             shouldBe "None"
          x.status.get.asInstanceOf[GROStatus].cancelled                  shouldBe false
          x.status.get.asInstanceOf[GROStatus].blockedRegistration        shouldBe false
          x.status.get.asInstanceOf[GROStatus].marginalNote.get           shouldBe "None"
          x.status.get.asInstanceOf[GROStatus].reRegistered.get           shouldBe "None"
        case JsError(_)      =>
          throw new Exception
      }
    }

    "return Record object with Status object when correction key is excluded" in {
      val result = jsonStatusFlagsExcludingCorrection.validate[Record](ReadsUtil.groReadRecord)
      result shouldBe a[JsSuccess[_]]
      result match {
        case JsSuccess(x, _) =>
          x                                                               shouldBe a[Record]
          x.child                                                         shouldBe a[Child]
          x.child.birthReferenceNumber                                    shouldBe 500035710
          x.child.forenames                                               shouldBe "John"
          x.child.lastName                                                shouldBe "Jones"
          x.child.dateOfBirth.get.toString                                shouldBe "2007-02-18"
          x.child.dateOfBirth.get                                         shouldBe a[LocalDate]
          x.status.get                                                    shouldBe a[GROStatus]
          x.status.get.asInstanceOf[GROStatus].potentiallyFictitiousBirth shouldBe false
          x.status.get.asInstanceOf[GROStatus].correction                 shouldBe None
          x.status.get.asInstanceOf[GROStatus].cancelled                  shouldBe false
          x.status.get.asInstanceOf[GROStatus].blockedRegistration        shouldBe false
          x.status.get.asInstanceOf[GROStatus].marginalNote.get           shouldBe "None"
          x.status.get.asInstanceOf[GROStatus].reRegistered.get           shouldBe "None"
        case JsError(_)      =>
          throw new Exception
      }
    }

    "return Record object with Status object when cancelled key is excluded" in {
      val result = jsonStatusFlagsExcludingCancelled.validate[Record](ReadsUtil.groReadRecord)
      result shouldBe a[JsSuccess[_]]
      result match {
        case JsSuccess(x, _) =>
          x                                                               shouldBe a[Record]
          x.child                                                         shouldBe a[Child]
          x.child.birthReferenceNumber                                    shouldBe 500035710
          x.child.forenames                                               shouldBe "John"
          x.child.lastName                                                shouldBe "Jones"
          x.child.dateOfBirth.get.toString                                shouldBe "2007-02-18"
          x.child.dateOfBirth.get                                         shouldBe a[LocalDate]
          x.status.get                                                    shouldBe a[GROStatus]
          x.status.get.asInstanceOf[GROStatus].potentiallyFictitiousBirth shouldBe false
          x.status.get.asInstanceOf[GROStatus].correction.get             shouldBe "None"
          x.status.get.asInstanceOf[GROStatus].cancelled                  shouldBe false
          x.status.get.asInstanceOf[GROStatus].blockedRegistration        shouldBe false
          x.status.get.asInstanceOf[GROStatus].marginalNote.get           shouldBe "None"
          x.status.get.asInstanceOf[GROStatus].reRegistered.get           shouldBe "None"
        case JsError(_)      =>
          throw new Exception
      }
    }

    "return Record object with Status object when blockedRegistration key is excluded" in {
      val result = jsonStatusFlagsExcludingBlockedRegistration.validate[Record](ReadsUtil.groReadRecord)
      result shouldBe a[JsSuccess[_]]
      result match {
        case JsSuccess(x, _) =>
          x                                                               shouldBe a[Record]
          x.child                                                         shouldBe a[Child]
          x.child.birthReferenceNumber                                    shouldBe 500035710
          x.child.forenames                                               shouldBe "John"
          x.child.lastName                                                shouldBe "Jones"
          x.child.dateOfBirth.get.toString                                shouldBe "2007-02-18"
          x.child.dateOfBirth.get                                         shouldBe a[LocalDate]
          x.status.get                                                    shouldBe a[GROStatus]
          x.status.get.asInstanceOf[GROStatus].potentiallyFictitiousBirth shouldBe false
          x.status.get.asInstanceOf[GROStatus].correction.get             shouldBe "None"
          x.status.get.asInstanceOf[GROStatus].cancelled                  shouldBe false
          x.status.get.asInstanceOf[GROStatus].blockedRegistration        shouldBe false
          x.status.get.asInstanceOf[GROStatus].marginalNote.get           shouldBe "None"
          x.status.get.asInstanceOf[GROStatus].reRegistered.get           shouldBe "None"
        case JsError(_)      =>
          throw new Exception
      }
    }

    "return Record object with Status object when marginalNote key is excluded" in {
      val result = jsonStatusFlagsExcludingMarginalNote.validate[Record](ReadsUtil.groReadRecord)
      result shouldBe a[JsSuccess[_]]
      result match {
        case JsSuccess(x, _) =>
          x                                                               shouldBe a[Record]
          x.child                                                         shouldBe a[Child]
          x.child.birthReferenceNumber                                    shouldBe 500035710
          x.child.forenames                                               shouldBe "John"
          x.child.lastName                                                shouldBe "Jones"
          x.child.dateOfBirth.get.toString                                shouldBe "2007-02-18"
          x.child.dateOfBirth.get                                         shouldBe a[LocalDate]
          x.status.get                                                    shouldBe a[GROStatus]
          x.status.get.asInstanceOf[GROStatus].potentiallyFictitiousBirth shouldBe false
          x.status.get.asInstanceOf[GROStatus].correction.get             shouldBe "None"
          x.status.get.asInstanceOf[GROStatus].cancelled                  shouldBe false
          x.status.get.asInstanceOf[GROStatus].blockedRegistration        shouldBe false
          x.status.get.asInstanceOf[GROStatus].marginalNote               shouldBe None
          x.status.get.asInstanceOf[GROStatus].reRegistered.get           shouldBe "None"
        case JsError(_)      =>
          throw new Exception
      }
    }

    "return Record object with Status object when reRegistered key is excluded" in {
      val result = jsonStatusFlagsExcludingReRegistered.validate[Record](ReadsUtil.groReadRecord)
      result shouldBe a[JsSuccess[_]]
      result match {
        case JsSuccess(x, _) =>
          x                                                               shouldBe a[Record]
          x.child                                                         shouldBe a[Child]
          x.child.birthReferenceNumber                                    shouldBe 500035710
          x.child.forenames                                               shouldBe "John"
          x.child.lastName                                                shouldBe "Jones"
          x.child.dateOfBirth.get.toString                                shouldBe "2007-02-18"
          x.child.dateOfBirth.get                                         shouldBe a[LocalDate]
          x.status.get                                                    shouldBe a[GROStatus]
          x.status.get.asInstanceOf[GROStatus].potentiallyFictitiousBirth shouldBe false
          x.status.get.asInstanceOf[GROStatus].correction.get             shouldBe "None"
          x.status.get.asInstanceOf[GROStatus].cancelled                  shouldBe false
          x.status.get.asInstanceOf[GROStatus].blockedRegistration        shouldBe false
          x.status.get.asInstanceOf[GROStatus].marginalNote.get           shouldBe "None"
          x.status.get.asInstanceOf[GROStatus].reRegistered               shouldBe None
        case JsError(x)      =>
          throw new Exception
      }
    }

    "return a JsonParseException from a broken json object" in
      intercept[com.fasterxml.jackson.core.JsonParseException] {
        jsonBrokenObject.validate[Record](ReadsUtil.groReadRecord)
      }

    "return a JsonMappingException from an invalid json object" in
      intercept[com.fasterxml.jackson.databind.JsonMappingException] {
        jsonNoObject.validate[Record](ReadsUtil.groReadRecord)
      }

  }

}
