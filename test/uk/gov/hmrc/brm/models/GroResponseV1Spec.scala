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

package uk.gov.hmrc.brm.models

import org.scalatest.OptionValues
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import play.api.libs.json.*
import uk.gov.hmrc.brm.models.response.gro.GROStatusV1
import uk.gov.hmrc.brm.models.response.{Child, Record}
import uk.gov.hmrc.brm.utils.GroResponseV1TestData.*
import uk.gov.hmrc.brm.utils.ReadsUtil

import java.time.LocalDate

class GroResponseV1Spec extends AnyWordSpecLike with Matchers with OptionValues {

  "Record" should {

    "return a Map of flags where potentiallyFictitious is true" in {

      val result = jsonAllStatusFlagsPotentiallyFictitious.validate[Record](ReadsUtil.groReadRecordV1).get

      result.status.get.flags shouldBe Map(
        "potentiallyFictitious" -> "true",
        "correction"            -> "None",
        "cancelled"             -> "false",
        "blocked"               -> "false",
        "marginalNote"          -> "None",
        "reregistration"        -> "None"
      )
    }

    "return a Map of flags where correction exists" in {

      val result = jsonAllStatusFlagsCorrection.validate[Record](ReadsUtil.groReadRecordV1).get

      result.status.get.flags shouldBe Map(
        "potentiallyFictitious" -> "false",
        "correction"            -> "Correction on record",
        "cancelled"             -> "false",
        "blocked"               -> "false",
        "marginalNote"          -> "None",
        "reregistration"        -> "None"
      )
    }

    "return a Map of flags where cancelled is true" in {

      val result = jsonAllStatusFlagsCancelled.validate[Record](ReadsUtil.groReadRecordV1).get

      result.status.get.flags shouldBe Map(
        "potentiallyFictitious" -> "false",
        "correction"            -> "None",
        "cancelled"             -> "true",
        "blocked"               -> "false",
        "marginalNote"          -> "None",
        "reregistration"        -> "None"
      )
    }

    "return a Map of flags where blocked is true" in {

      val result = jsonAllStatusFlagsBlocked.validate[Record](ReadsUtil.groReadRecordV1).get

      result.status.get.flags shouldBe Map(
        "potentiallyFictitious" -> "false",
        "correction"            -> "None",
        "cancelled"             -> "false",
        "blocked"               -> "true",
        "marginalNote"          -> "None",
        "reregistration"        -> "None"
      )
    }

    "return a Map of flags where marginalNote exists" in {

      val result = jsonAllStatusFlagsMarginalNote.validate[Record](ReadsUtil.groReadRecordV1).get

      result.status.get.flags shouldBe Map(
        "potentiallyFictitious" -> "false",
        "correction"            -> "None",
        "cancelled"             -> "false",
        "blocked"               -> "false",
        "marginalNote"          -> "Marginal note on record",
        "reregistration"        -> "None"
      )
    }

    "return a Map of flags where reregistration exists" in {

      val result = jsonAllStatusFlagsReregistered.validate[Record](ReadsUtil.groReadRecordV1).get

      result.status.get.flags shouldBe Map(
        "potentiallyFictitious" -> "false",
        "correction"            -> "None",
        "cancelled"             -> "false",
        "blocked"               -> "false",
        "marginalNote"          -> "None",
        "reregistration"        -> "Re-registration on record"
      )
    }

    "return Record with all Child attributes when JSON is an array" in {

      val listOfRecords = jsonFullRecordCollection.as[List[Record]](ReadsUtil.groRecordsListReadV1)

      val record = listOfRecords.head

      listOfRecords.length shouldBe 1
      record               shouldBe a[Record]
      record.child         shouldBe a[Child]

      record.child.birthReferenceNumber shouldBe 123456789
      record.child.forenames            shouldBe "Joan Narcissus Ouroboros"
      record.child.lastName             shouldBe "SMITH"
      record.child.dateOfBirth.get      shouldBe LocalDate.parse("2008-08-08")

      record.status.get shouldBe a[GROStatusV1]

      val status = record.status.get.asInstanceOf[GROStatusV1]

      status.potentiallyFictitious shouldBe false
      status.correction.get        shouldBe "None"
      status.cancelled             shouldBe false
      status.blocked               shouldBe false
      status.marginalNote.get      shouldBe "None"
      status.reregistration.get    shouldBe "None"
    }

    "return Record when JSON is valid" in {

      val result = jsonValid.validate[Record](ReadsUtil.groReadRecordV1)

      result match {

        case JsSuccess(record, _) =>
          record       shouldBe a[Record]
          record.child shouldBe a[Child]

          record.child.birthReferenceNumber shouldBe 500035710
          record.child.forenames            shouldBe "John"
          record.child.lastName             shouldBe "Jones"
          record.child.dateOfBirth.get      shouldBe LocalDate.parse("2007-02-18")
          record.status                     shouldBe None

        case JsError(_) =>
          throw new Exception
      }
    }

    "return Record when JSON contains ASCII-Extended characters" in {

      val result = jsonValidWithASCIIExtended.validate[Record](ReadsUtil.groReadRecordV1)

      result match {

        case JsSuccess(record, _) =>
          record.child.birthReferenceNumber shouldBe 500035710
          record.child.forenames            shouldBe "Johnéë"
          record.child.lastName             shouldBe "Jonésë"
          record.child.dateOfBirth.get      shouldBe LocalDate.parse("2007-02-18")
          record.status                     shouldBe None

        case JsError(_) =>
          throw new Exception
      }
    }

    "return Record when JSON contains UTF-8 characters" in {

      val result = jsonValidWithUTF8.validate[Record](ReadsUtil.groReadRecordV1)

      result match {

        case JsSuccess(record, _) =>
          record.child.birthReferenceNumber shouldBe 500035710

          record.child.forenames shouldBe
            "JohͿͿŀŀŀnƷȸȸȸ- ƷġÊÊÊÊÊƂƂƂ' ÐÐġġġÐÐÐÐœœœÐÐÐ ÐÐÆġÆÆÅÅƼƼƼıııÅÅ"

          record.child.lastName shouldBe
            "JonesƷġÊÊÊÊÊƂƂƂ-'"

          record.child.dateOfBirth.get shouldBe
            LocalDate.parse("2007-02-18")

          record.status shouldBe None

        case JsError(_) =>
          throw new Exception
      }
    }

    "return Record when JSON contains maximum-length names" in {

      val result = jsonValidMaxLength.validate[Record](ReadsUtil.groReadRecordV1)

      result match {

        case JsSuccess(record, _) =>
          record.child.birthReferenceNumber shouldBe 500035710

          record.child.forenames shouldBe
            s"$maxLengthString $maxLengthString $maxLengthString $maxLengthString"

          record.child.lastName shouldBe maxLengthString

          record.child.dateOfBirth.get shouldBe
            LocalDate.parse("2007-02-18")

          record.status shouldBe None

        case JsError(_) =>
          throw new Exception
      }
    }

    "return error when JSON object is empty" in {

      val result = jsonMissingEmptyObject.validate[Record](ReadsUtil.groReadRecordV1)

      result match {

        case JsSuccess(_, _) =>
          throw new Exception

        case JsError(errors) =>
          errors.length           shouldBe 1
          errors.head._1.toString shouldBe "/id"
      }
    }

    "return Record when child and status contain null values" in {

      val result = jsonRecordKeysNoValues.validate[Record](ReadsUtil.groReadRecordV1)

      result match {

        case JsSuccess(record, _) =>
          record.child.birthReferenceNumber shouldBe 999999926
          record.child.forenames            shouldBe ""
          record.child.lastName             shouldBe ""
          record.child.dateOfBirth          shouldBe None

          val status =
            record.status.get.asInstanceOf[GROStatusV1]

          status.potentiallyFictitious shouldBe false
          status.correction            shouldBe None
          status.cancelled             shouldBe false
          status.blocked               shouldBe false
          status.marginalNote          shouldBe None
          status.reregistration        shouldBe None

        case JsError(_) =>
          throw new Exception
      }
    }

    "return error when id is a String" in {

      val result = jsonInvalidIdType.validate[Record](ReadsUtil.groReadRecordV1)

      result match {

        case JsSuccess(_, _) =>
          throw new Exception

        case JsError(errors) =>
          errors.length           shouldBe 1
          errors.head._1.toString shouldBe "/id"
      }
    }

    "return error when id is missing" in {

      val result = jsonMissingIdKey.validate[Record](ReadsUtil.groReadRecordV1)

      result should not be a[JsSuccess[_]]

      result match {

        case JsSuccess(_, _) =>
          throw new Exception

        case JsError(errors) =>
          errors.length           shouldBe 1
          errors.head._1.toString shouldBe "/id"
      }
    }

    "return Record when optional object properties are missing" in {

      val result = jsonMissingObjectsProperties.validate[Record](ReadsUtil.groReadRecordV1)

      result shouldBe a[JsSuccess[_]]

      result match {

        case JsSuccess(record, _) =>
          record.child.birthReferenceNumber shouldBe 999999920
          record.child.forenames            shouldBe empty
          record.child.lastName             shouldBe empty
          record.child.dateOfBirth          shouldBe None

          record.status shouldBe Some(
            GROStatusV1(
              potentiallyFictitious = false,
              correction = None,
              cancelled = false,
              blocked = false,
              marginalNote = None,
              reregistration = None
            )
          )

        case JsError(_) =>
          throw new Exception
      }
    }

    "return empty forenames when forenames key is missing" in {

      val result = jsonMissingForenamesKey.validate[Record](ReadsUtil.groReadRecordV1)

      result shouldBe a[JsSuccess[_]]

      result match {

        case JsSuccess(record, _) =>
          record.child.birthReferenceNumber shouldBe 500035710
          record.child.forenames            shouldBe ""
          record.child.lastName             shouldBe "Jones"
          record.child.dateOfBirth.get      shouldBe LocalDate.parse("2007-02-18")
          record.status                     shouldBe None

        case JsError(_) =>
          throw new Exception
      }
    }

    "return empty surname when surname key is missing" in {

      val result = jsonMissingSurnameKey.validate[Record](ReadsUtil.groReadRecordV1)

      result shouldBe a[JsSuccess[_]]

      result match {

        case JsSuccess(record, _) =>
          record.child.birthReferenceNumber shouldBe 500035710
          record.child.forenames            shouldBe "John"
          record.child.lastName             shouldBe ""
          record.child.dateOfBirth.get      shouldBe LocalDate.parse("2007-02-18")
          record.status                     shouldBe None

        case JsError(_) =>
          throw new Exception
      }
    }

    "return None when dateOfBirth key is missing" in {

      val result = jsonMissingDateOfBirthKey.validate[Record](ReadsUtil.groReadRecordV1)

      result shouldBe a[JsSuccess[_]]

      result match {

        case JsSuccess(record, _) =>
          record.child.birthReferenceNumber shouldBe 500035710
          record.child.forenames            shouldBe "John"
          record.child.lastName             shouldBe "Jones"
          record.child.dateOfBirth          shouldBe None
          record.status                     shouldBe None

        case JsError(_) =>
          throw new Exception
      }
    }

    "return empty Child fields when child key is missing" in {

      val result = jsonMissingChildKey.validate[Record](ReadsUtil.groReadRecordV1)

      result shouldBe a[JsSuccess[_]]

      result match {

        case JsSuccess(record, _) =>
          record.child.birthReferenceNumber shouldBe 500035710
          record.child.forenames            shouldBe ""
          record.child.lastName             shouldBe ""
          record.child.dateOfBirth          shouldBe None
          record.status                     shouldBe None

        case JsError(_) =>
          throw new Exception
      }
    }

    "return None when dateOfBirth has invalid format" in {

      val result = jsonInvalidDateOfBirthFormat.validate[Record](ReadsUtil.groReadRecordV1)

      result shouldBe a[JsSuccess[_]]

      result match {

        case JsSuccess(record, _) =>
          record.child.birthReferenceNumber shouldBe 500035710
          record.child.forenames            shouldBe "John"
          record.child.lastName             shouldBe "Jones"
          record.child.dateOfBirth          shouldBe None
          record.status                     shouldBe None

        case JsError(_) =>
          throw new Exception
      }
    }

    "return Record with all status fields" in {

      val result = jsonAllStatusFlags.validate[Record](ReadsUtil.groReadRecordV1)

      result shouldBe a[JsSuccess[_]]

      result match {

        case JsSuccess(record, _) =>
          record.child.birthReferenceNumber shouldBe 500035710
          record.child.forenames            shouldBe "John"
          record.child.lastName             shouldBe "Jones"
          record.child.dateOfBirth.get      shouldBe LocalDate.parse("2007-02-18")

          val status = record.status.get.asInstanceOf[GROStatusV1]

          status.potentiallyFictitious shouldBe false
          status.correction.get        shouldBe "None"
          status.cancelled             shouldBe false
          status.blocked               shouldBe false
          status.marginalNote.get      shouldBe "None"
          status.reregistration.get    shouldBe "None"

          status.toJson shouldBe Json.parse(
            """
              |{
              |  "potentiallyFictitious": "false",
              |  "correction": "None",
              |  "cancelled": "false",
              |  "blocked": "false",
              |  "marginalNote": "None",
              |  "reregistration": "None"
              |}
              |""".stripMargin
          )

        case JsError(_) =>
          throw new Exception
      }
    }

    "use false when potentiallyFictitious key is missing" in {

      val result = jsonStatusFlagsExcludingPotentiallyFictitious.validate[Record](ReadsUtil.groReadRecordV1)

      result shouldBe a[JsSuccess[_]]

      val status = result.get.status.get.asInstanceOf[GROStatusV1]

      status.potentiallyFictitious shouldBe false
      status.correction.get        shouldBe "None"
      status.cancelled             shouldBe false
      status.blocked               shouldBe false
      status.marginalNote.get      shouldBe "None"
      status.reregistration.get    shouldBe "None"
    }

    "use None when correction key is missing" in {

      val result = jsonStatusFlagsExcludingCorrection.validate[Record](ReadsUtil.groReadRecordV1)

      result shouldBe a[JsSuccess[_]]

      val status = result.get.status.get.asInstanceOf[GROStatusV1]

      status.potentiallyFictitious shouldBe false
      status.correction            shouldBe None
      status.cancelled             shouldBe false
      status.blocked               shouldBe false
      status.marginalNote.get      shouldBe "None"
      status.reregistration.get    shouldBe "None"
    }

    "use false when cancelled key is missing" in {

      val result = jsonStatusFlagsExcludingCancelled.validate[Record](ReadsUtil.groReadRecordV1)

      result shouldBe a[JsSuccess[_]]

      val status = result.get.status.get.asInstanceOf[GROStatusV1]

      status.potentiallyFictitious shouldBe false
      status.correction.get        shouldBe "None"
      status.cancelled             shouldBe false
      status.blocked               shouldBe false
      status.marginalNote.get      shouldBe "None"
      status.reregistration.get    shouldBe "None"
    }

    "use false when blocked key is missing" in {

      val result = jsonStatusFlagsExcludingBlocked.validate[Record](ReadsUtil.groReadRecordV1)

      result shouldBe a[JsSuccess[_]]

      val status = result.get.status.get.asInstanceOf[GROStatusV1]

      status.potentiallyFictitious shouldBe false
      status.correction.get        shouldBe "None"
      status.cancelled             shouldBe false
      status.blocked               shouldBe false
      status.marginalNote.get      shouldBe "None"
      status.reregistration.get    shouldBe "None"
    }

    "use None when marginalNote key is missing" in {

      val result = jsonStatusFlagsExcludingMarginalNote.validate[Record](ReadsUtil.groReadRecordV1)

      result shouldBe a[JsSuccess[_]]

      val status = result.get.status.get.asInstanceOf[GROStatusV1]

      status.potentiallyFictitious shouldBe false
      status.correction.get        shouldBe "None"
      status.cancelled             shouldBe false
      status.blocked               shouldBe false
      status.marginalNote          shouldBe None
      status.reregistration.get    shouldBe "None"
    }

    "use None when reregistration key is missing" in {

      val result = jsonStatusFlagsExcludingReregistration.validate[Record](ReadsUtil.groReadRecordV1)

      result shouldBe a[JsSuccess[_]]

      val status = result.get.status.get.asInstanceOf[GROStatusV1]

      status.potentiallyFictitious shouldBe false
      status.correction.get        shouldBe "None"
      status.cancelled             shouldBe false
      status.blocked               shouldBe false
      status.marginalNote.get      shouldBe "None"
      status.reregistration        shouldBe None
    }

    "return JsonParseException from broken JSON" in
      intercept[com.fasterxml.jackson.core.JsonParseException] {
        jsonBrokenObject.validate[Record](ReadsUtil.groReadRecordV1)
      }

    "return JsonMappingException from empty JSON input" in
      intercept[com.fasterxml.jackson.databind.JsonMappingException] {
        jsonNoObject.validate[Record](ReadsUtil.groReadRecordV1)
      }
  }

}
