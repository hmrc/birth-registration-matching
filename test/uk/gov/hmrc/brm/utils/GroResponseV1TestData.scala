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

package uk.gov.hmrc.brm.utils

import play.api.libs.json.{JsValue, Json}

object GroResponseV1TestData {

  lazy val maxLengthString: String =
    "XuLEjzWmZGzHbzVwxWhHjKBdGorAZNVxNdXHfwXemCXkfYPoeWbBJvtMrVuEfSfVZEkmNzhMQsscKFQLRXScwAhCWkndDQeAVRpTDbbkzDYxWHAMtYDBRDDHFHGwRQak"

  lazy val jsonFullRecord: JsValue = JsonUtils.getJsonFromFile("gro", "123456789")

  lazy val jsonFullRecordCollection: JsValue = Json.arr(jsonFullRecord)

  lazy val jsonValid: JsValue = Json.parse(
    """
      |{
      |  "id": 500035710,
      |  "child": {
      |    "forenames": "John",
      |    "surname": "Jones",
      |    "dateOfBirth": "2007-02-18"
      |  }
      |}
      |""".stripMargin
  )

  lazy val jsonValidWithASCIIExtended: JsValue = Json.parse(
    """
      |{
      |  "id": 500035710,
      |  "child": {
      |    "forenames": "Johnéë",
      |    "surname": "Jonésë",
      |    "dateOfBirth": "2007-02-18"
      |  }
      |}
      |""".stripMargin
  )

  lazy val jsonValidWithUTF8: JsValue = Json.parse(
    """
      |{
      |  "id": 500035710,
      |  "child": {
      |    "forenames": "JohͿͿŀŀŀnƷȸȸȸ- ƷġÊÊÊÊÊƂƂƂ' ÐÐġġġÐÐÐÐœœœÐÐÐ ÐÐÆġÆÆÅÅƼƼƼıııÅÅ",
      |    "surname": "JonesƷġÊÊÊÊÊƂƂƂ-'",
      |    "dateOfBirth": "2007-02-18"
      |  }
      |}
      |""".stripMargin
  )

  lazy val jsonValidMaxLength: JsValue = Json.parse(
    s"""
       |{
       |  "id": 500035710,
       |  "child": {
       |    "forenames": "$maxLengthString $maxLengthString $maxLengthString $maxLengthString",
       |    "surname": "$maxLengthString",
       |    "dateOfBirth": "2007-02-18"
       |  }
       |}
       |""".stripMargin
  )

  lazy val jsonMissingForenamesKey: JsValue = Json.parse(
    """
      |{
      |  "id": 500035710,
      |  "child": {
      |    "surname": "Jones",
      |    "dateOfBirth": "2007-02-18"
      |  }
      |}
      |""".stripMargin
  )

  lazy val jsonMissingSurnameKey: JsValue = Json.parse(
    """
      |{
      |  "id": 500035710,
      |  "child": {
      |    "forenames": "John",
      |    "dateOfBirth": "2007-02-18"
      |  }
      |}
      |""".stripMargin
  )

  lazy val jsonMissingDateOfBirthKey: JsValue = Json.parse(
    """
      |{
      |  "id": 500035710,
      |  "child": {
      |    "forenames": "John",
      |    "surname": "Jones"
      |  }
      |}
      |""".stripMargin
  )

  lazy val jsonMissingChildKey: JsValue = Json.parse(
    """
      |{
      |  "id": 500035710
      |}
      |""".stripMargin
  )

  lazy val jsonMissingIdKey: JsValue = Json.parse(
    """
      |{
      |  "child": {
      |    "forenames": "John",
      |    "surname": "Jones",
      |    "dateOfBirth": "2007-02-18"
      |  }
      |}
      |""".stripMargin
  )

  lazy val jsonMissingEmptyObject: JsValue =
    Json.obj()

  lazy val jsonInvalidDateOfBirthFormat: JsValue = Json.parse(
    """
      |{
      |  "id": 500035710,
      |  "child": {
      |    "forenames": "John",
      |    "surname": "Jones",
      |    "dateOfBirth": "20-02-207"
      |  }
      |}
      |""".stripMargin
  )

  lazy val jsonInvalidIdType: JsValue = Json.parse(
    """
      |{
      |  "id": "500035710",
      |  "child": {
      |    "forenames": "John",
      |    "surname": "Jones",
      |    "dateOfBirth": "2007-02-18"
      |  }
      |}
      |""".stripMargin
  )

  lazy val jsonMissingObjectsProperties: JsValue = Json.parse(
    """
      |{
      |  "id": 999999920,
      |  "registrar": {},
      |  "child": {},
      |  "mother": {},
      |  "father": {},
      |  "informant1": {},
      |  "informant2": {},
      |  "status": {
      |    "blocked": false
      |  },
      |  "previousRegistration": null,
      |  "nextRegistration": null
      |}
      |""".stripMargin
  )

  lazy val jsonRecordKeysNoValues: JsValue = Json.parse(
    """
      |{
      |  "id": 999999926,
      |  "child": {
      |    "forenames": null,
      |    "surname": null,
      |    "dateOfBirth": null
      |  },
      |  "status": {
      |    "potentiallyFictitious": null,
      |    "correction": null,
      |    "cancelled": null,
      |    "blocked": null,
      |    "marginalNote": null,
      |    "reregistration": null
      |  }
      |}
      |""".stripMargin
  )

  lazy val jsonAllStatusFlags: JsValue = Json.parse(
    """
      |{
      |  "id": 500035710,
      |  "child": {
      |    "forenames": "John",
      |    "surname": "Jones",
      |    "dateOfBirth": "2007-02-18"
      |  },
      |  "status": {
      |    "potentiallyFictitious": false,
      |    "correction": "None",
      |    "cancelled": false,
      |    "blocked": false,
      |    "marginalNote": "None",
      |    "reregistration": "None"
      |  }
      |}
      |""".stripMargin
  )

  lazy val jsonAllStatusFlagsPotentiallyFictitious: JsValue = Json.parse(
    """
      |{
      |  "id": 500035710,
      |  "child": {
      |    "forenames": "John",
      |    "surname": "Jones",
      |    "dateOfBirth": "2007-02-18"
      |  },
      |  "status": {
      |    "potentiallyFictitious": true,
      |    "correction": "None",
      |    "cancelled": false,
      |    "blocked": false,
      |    "marginalNote": "None",
      |    "reregistration": "None"
      |  }
      |}
      |""".stripMargin
  )

  lazy val jsonAllStatusFlagsCorrection: JsValue = Json.parse(
    """
      |{
      |  "id": 500035710,
      |  "child": {
      |    "forenames": "John",
      |    "surname": "Jones",
      |    "dateOfBirth": "2007-02-18"
      |  },
      |  "status": {
      |    "potentiallyFictitious": false,
      |    "correction": "Typographical",
      |    "cancelled": false,
      |    "blocked": false,
      |    "marginalNote": "None",
      |    "reregistration": "None"
      |  }
      |}
      |""".stripMargin
  )

  lazy val jsonAllStatusFlagsCancelled: JsValue = Json.parse(
    """
      |{
      |  "id": 500035710,
      |  "child": {
      |    "forenames": "John",
      |    "surname": "Jones",
      |    "dateOfBirth": "2007-02-18"
      |  },
      |  "status": {
      |    "potentiallyFictitious": false,
      |    "correction": "None",
      |    "cancelled": true,
      |    "blocked": false,
      |    "marginalNote": "None",
      |    "reregistration": "None"
      |  }
      |}
      |""".stripMargin
  )

  lazy val jsonAllStatusFlagsBlocked: JsValue = Json.parse(
    """
      |{
      |  "id": 500035710,
      |  "child": {
      |    "forenames": "John",
      |    "surname": "Jones",
      |    "dateOfBirth": "2007-02-18"
      |  },
      |  "status": {
      |    "potentiallyFictitious": false,
      |    "correction": "None",
      |    "cancelled": false,
      |    "blocked": true,
      |    "marginalNote": "None",
      |    "reregistration": "None"
      |  }
      |}
      |""".stripMargin
  )

  lazy val jsonAllStatusFlagsMarginalNote: JsValue = Json.parse(
    """
      |{
      |  "id": 500035710,
      |  "child": {
      |    "forenames": "John",
      |    "surname": "Jones",
      |    "dateOfBirth": "2007-02-18"
      |  },
      |  "status": {
      |    "potentiallyFictitious": false,
      |    "correction": "None",
      |    "cancelled": false,
      |    "blocked": false,
      |    "marginalNote": "Re-registered",
      |    "reregistration": "None"
      |  }
      |}
      |""".stripMargin
  )

  lazy val jsonAllStatusFlagsReregistered: JsValue = Json.parse(
    """
      |{
      |  "id": 500035710,
      |  "child": {
      |    "forenames": "John",
      |    "surname": "Jones",
      |    "dateOfBirth": "2007-02-18"
      |  },
      |  "status": {
      |    "potentiallyFictitious": false,
      |    "correction": "None",
      |    "cancelled": false,
      |    "blocked": false,
      |    "marginalNote": "None",
      |    "reregistration": "Re-registered"
      |  }
      |}
      |""".stripMargin
  )

  lazy val jsonStatusFlagsExcludingPotentiallyFictitious: JsValue = Json.parse(
    """
      |{
      |  "id": 500035710,
      |  "child": {
      |    "forenames": "John",
      |    "surname": "Jones",
      |    "dateOfBirth": "2007-02-18"
      |  },
      |  "status": {
      |    "correction": "None",
      |    "cancelled": false,
      |    "blocked": false,
      |    "marginalNote": "None",
      |    "reregistration": "None"
      |  }
      |}
      |""".stripMargin
  )

  lazy val jsonStatusFlagsExcludingCorrection: JsValue = Json.parse(
    """
      |{
      |  "id": 500035710,
      |  "child": {
      |    "forenames": "John",
      |    "surname": "Jones",
      |    "dateOfBirth": "2007-02-18"
      |  },
      |  "status": {
      |    "potentiallyFictitious": false,
      |    "cancelled": false,
      |    "blocked": false,
      |    "marginalNote": "None",
      |    "reregistration": "None"
      |  }
      |}
      |""".stripMargin
  )

  lazy val jsonStatusFlagsExcludingCancelled: JsValue = Json.parse(
    """
      |{
      |  "id": 500035710,
      |  "child": {
      |    "forenames": "John",
      |    "surname": "Jones",
      |    "dateOfBirth": "2007-02-18"
      |  },
      |  "status": {
      |    "potentiallyFictitious": false,
      |    "correction": "None",
      |    "blocked": false,
      |    "marginalNote": "None",
      |    "reregistration": "None"
      |  }
      |}
      |""".stripMargin
  )

  lazy val jsonStatusFlagsExcludingBlocked: JsValue = Json.parse(
    """
      |{
      |  "id": 500035710,
      |  "child": {
      |    "forenames": "John",
      |    "surname": "Jones",
      |    "dateOfBirth": "2007-02-18"
      |  },
      |  "status": {
      |    "potentiallyFictitious": false,
      |    "correction": "None",
      |    "cancelled": false,
      |    "marginalNote": "None",
      |    "reregistration": "None"
      |  }
      |}
      |""".stripMargin
  )

  lazy val jsonStatusFlagsExcludingMarginalNote: JsValue = Json.parse(
    """
      |{
      |  "id": 500035710,
      |  "child": {
      |    "forenames": "John",
      |    "surname": "Jones",
      |    "dateOfBirth": "2007-02-18"
      |  },
      |  "status": {
      |    "potentiallyFictitious": false,
      |    "correction": "None",
      |    "cancelled": false,
      |    "blocked": false,
      |    "reregistration": "None"
      |  }
      |}
      |""".stripMargin
  )

  lazy val jsonStatusFlagsExcludingReregistration: JsValue = Json.parse(
    """
      |{
      |  "id": 500035710,
      |  "child": {
      |    "forenames": "John",
      |    "surname": "Jones",
      |    "dateOfBirth": "2007-02-18"
      |  },
      |  "status": {
      |    "potentiallyFictitious": false,
      |    "correction": "None",
      |    "cancelled": false,
      |    "blocked": false,
      |    "marginalNote": "None"
      |  }
      |}
      |""".stripMargin
  )

  lazy val jsonNoObject: JsValue = Json.parse("")

  lazy val jsonBrokenObject: JsValue = Json.parse("{")
}
