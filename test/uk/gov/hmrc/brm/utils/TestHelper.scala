/*
 * Copyright 2023 HM Revenue & Customs
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

import java.time.LocalDate
import play.api.libs.json.Json._
import play.api.libs.json.JsValue
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.brm.models.brm.Payload
import uk.gov.hmrc.brm.models.response.{Child, Record}
import uk.gov.hmrc.http.HttpResponse

object TestHelper {

  /** GRO
    */

  val groJsonResponseObject: JsValue                    = JsonUtils.getJsonFromFile("gro", "500035710")
  val groJsonResponseObject400000001: JsValue           = JsonUtils.getJsonFromFile("gro", "400000001")
  val groJsonResponseObjectCollection: JsValue          = JsonUtils.getJsonFromFile("gro", "500035710-array")
  val groJsonResponseObjectCollection400000001: JsValue = JsonUtils.getJsonFromFile("gro", "400000001-array")
  val groJsonResponseObjectMultipleWithMatch: JsValue   = JsonUtils.getJsonFromFile("gro", "400000004-multiple-match")
  val groJsonResponseObject20120216: JsValue            = JsonUtils.getJsonFromFile("gro", "2012-02-16")
  val groJsonResponseObject20090701: JsValue            = JsonUtils.getJsonFromFile("gro", "2009-07-01")
  val groJsonResponseObject20090630: JsValue            = JsonUtils.getJsonFromFile("gro", "2009-06-30")
  val groResponseWithAdditionalName: JsValue            = JsonUtils.getJsonFromFile("gro", "with_additional_name")
  val groResponseWithoutAdditionalName: JsValue         = JsonUtils.getJsonFromFile("gro", "without_additional_name")
  val groResponseWithMoreAdditionalName: JsValue        = JsonUtils.getJsonFromFile("gro", "with_more_additional_name")
  val groResponseWithSpecialCharacter: JsValue          = JsonUtils.getJsonFromFile("gro", "with_special_character")
  val groResponse500036682: JsValue                     = JsonUtils.getJsonFromFile("gro", "500036682")

  val dateOfBirth: LocalDate    = LocalDate.of(2006, 11, 12)
  val altDateOfBirth: LocalDate = LocalDate.of(2009, 11, 12)

  val payload: Payload            =
    Payload(Some("500035710"), "Adam", None, "Wilson", LocalDate.of(2006, 11, 12), BirthRegisterCountry.ENGLAND)
  val payloadNoReference: Payload =
    Payload(None, "Adam", None, "Wilson", dateOfBirth, BirthRegisterCountry.ENGLAND)

  /** NRS
    */

  val validNrsJsonResponseObject: JsValue     = JsonUtils.getJsonFromFile("nrs", "2017734003")
  val validNrsJsonResponseObjectRCE: JsValue  = JsonUtils.getJsonFromFile("nrs", "2017350003")
  val validNrsJsonResponse2017350007: JsValue = JsonUtils.getJsonFromFile("nrs", "2017350007")
  val nrsResponseWithMultiple: JsValue        = JsonUtils.getJsonFromFile("nrs", "AdamTEST_multiple")
  val nrsRecord20090630: JsValue              = JsonUtils.getJsonFromFile("nrs", "2017734100")
  val nrsRecord2017350001: JsValue            = JsonUtils.getJsonFromFile("nrs", "2017350001")

  val nrsRequestPayload: Payload           =
    Payload(Some("2017734003"), "Adam TEST", None, "SMITH", altDateOfBirth, BirthRegisterCountry.SCOTLAND)
  val nrsRequestPayload2017350001: Payload =
    Payload(Some("2017350001"), "Adam TEST", None, "SMITH", altDateOfBirth, BirthRegisterCountry.SCOTLAND)

  val nrsRequestPayloadWithoutBrn: Payload         =
    Payload(None, "Adam TEST", None, "SMITH", altDateOfBirth, BirthRegisterCountry.SCOTLAND)
  val nrsRequestPayloadWithSpecialChar: Payload    =
    Payload(
      Some("2017350007"),
      "Gab'iœ-Äæy",
      None,
      "HaÐ0ÄœÄæes",
      LocalDate.of(2011, 10, 1),
      BirthRegisterCountry.SCOTLAND
    )
  val nrsRequestPayloadWithFirstNameWrong: Payload =
    Payload(
      Some("2017350007"),
      "firstNameWrong",
      None,
      "HaÐ0ÄœÄæes",
      LocalDate.of(2011, 10, 1),
      BirthRegisterCountry.SCOTLAND
    )

  val payloadNoReferenceScotland: Payload =
    Payload(None, "Adam", None, "Wilson", dateOfBirth, BirthRegisterCountry.SCOTLAND)

  /** GRO-NI
    */

  val payloadNoReferenceNorthernIreland: Payload =
    Payload(None, "Adam", None, "Wilson", dateOfBirth, BirthRegisterCountry.NORTHERN_IRELAND)

  private val referenceNumber: Int  = 123456789
  val groResponseValidJson: JsValue = parse(s"""
      |{
      |  "location": {
      |
      |  },
      |  "subjects": {
      |    "child" : {
      |   "name" : {
      |    "givenName" : "Chris",
      |    "surname" : "Jones"
      |   },
      |   "dateOfBirth" : "2012-02-16"
      |  },
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
      |  "systemNumber": $referenceNumber,
      |  "id": $referenceNumber,
      |  "status": {
      |    "blockedRegistration": false
      |  },
      |  "previousRegistration": {}
      |
      |  }
    """.stripMargin)

  private val birthDate = LocalDate.of(2012, 2, 16)

  def postRequest(v: JsValue): FakeRequest[JsValue] = FakeRequest("POST", "/birth-registration-matching/match")
    .withHeaders((ACCEPT, "application/vnd.hmrc.1.0+json"), ("Audit-Source", "DFS"))
    .withBody(v)

  def httpResponse(responseCode: Int, js: JsValue): HttpResponse =
    HttpResponse(responseCode, js, Map.empty[String, Seq[String]])

  def httpResponse(js: JsValue): HttpResponse = HttpResponse(OK, js, Map.empty[String, Seq[String]])

  def httpResponse(responseCode: Int): HttpResponse = HttpResponse(responseCode, "")

  def getRecord(foreNames: String, lastName: String): Record = {
    val child = Child(referenceNumber, foreNames, lastName, Some(birthDate))
    Record(child, None)
  }

  def validRecord: Record = {
    val child = Child(referenceNumber, "Chris", "Jones", Some(birthDate))
    Record(child, None)
  }

  def validRecordSpecialCharactersFirstName: Record = {

    val child = Child(referenceNumber, "Chris-Jame's", "Jones", Some(birthDate))
    Record(child, None)
  }

  def validRecordMiddleNames: Record = {
    val child = Child(referenceNumber, "Adam David", "Jones", Some(birthDate))
    Record(child, None)
  }

  def adamTestTestJonesRecord: Record = {
    val child = Child(referenceNumber, "Adam test test", "Jones", Some(birthDate))
    Record(child, None)
  }

  def validRecordMiddleNamesWithSpaces: Record = {
    val child = Child(referenceNumber, "  Adam     David ", "Jones", Some(birthDate))
    Record(child, None)
  }

  def validRecordMiddleNamesWithSpacesAndPunctuation: Record = {
    val child = Child(referenceNumber, "   Jamie  Mary-Ann'é    Earl ", "Jones", Some(birthDate))
    Record(child, None)
  }

  def validRecordSpecialCharactersLastName: Record = {
    val child = Child(referenceNumber, "Chris", "Jones--Smith", Some(birthDate))
    Record(child, None)
  }

  def validRecordFirstNameSpace: Record = {
    val child = Child(referenceNumber, "Chris James", "Jones", Some(birthDate))
    Record(child, None)
  }

  def validRecordLastNameSpace: Record = {
    val child = Child(referenceNumber, "Chris", "Jones Smith", Some(birthDate))
    Record(child, None)
  }

  def validRecordLastNameMultipleSpace: Record = {
    val child = Child(referenceNumber, "Chris", "Jones  Smith", Some(birthDate))
    Record(child, None)
  }

  def validRecordLastNameMultipleSpaceBeginningTrailing: Record = {
    val child = Child(referenceNumber, "Chris", "  Jones  Smith ", Some(birthDate))
    Record(child, None)
  }

  def validRecordUTF8FirstName: Record = {
    val child = Child(referenceNumber, "Chrîs", "Jones", Some(birthDate))
    Record(child, None)
  }

  def validRecordUTF8LastName: Record = {
    val child = Child(referenceNumber, "Chris", "Jonéş", Some(birthDate))
    Record(child, None)
  }

  def validRecordUppercase: Record = {
    val child = Child(referenceNumber, "CHRIS", "JONES", Some(birthDate))
    Record(child, None)
  }

  def wrongCaseFirstNameValidRecord: Record = {
    val child = Child(1, "CHriS", "Jones", Some(birthDate))
    Record(child, None)
  }

  def wrongCaseLastNameValidRecord: Record = {
    val child = Child(1, "Chris", "JOnES", Some(birthDate))
    Record(child, None)
  }

  def wrongCaseValidRecord: Record = {
    val child = Child(1, "cHrIs", "JOnES", Some(birthDate))
    Record(child, None)
  }

  def invalidRecordFirstName: Record = {
    val child = Child(referenceNumber, "", "", None)
    Record(child, None)
  }

  def invalidRecord: Record = {
    val child = Child(1, "invalidfirstName", "invalidLastName", None)
    Record(child, None)
  }

  def firstNameNotMatchedRecord: Record = {
    val child = Child(referenceNumber, "Manish", "Jones", Some(birthDate))
    Record(child, None)
  }

  def lastNameNotMatchRecord: Record = {
    val child = Child(referenceNumber, "Chris", "lastName", Some(birthDate))
    Record(child, None)
  }

  def dobNotMatchRecord: Record = {
    val child = Child(referenceNumber, "Chris", "Jones", None)
    Record(child, None)
  }

  val invalidResponse: JsValue = parse("""
      |[]
    """.stripMargin)

  val noJson: JsValue = parse(s"""{
        }
    """.stripMargin)

  val userWhereBirthRegisteredNI: JsValue = parse(s"""
       |{
       | "birthReferenceNumber" : "$referenceNumber",
       | "firstName" : "Chris",
       | "lastName" : "Jones",
       | "dateOfBirth" : "2012-02-16",
       | "whereBirthRegistered" : "northern ireland"
       |}
    """.stripMargin)

  val userWhereBirthRegisteredScotland: JsValue = parse(s"""
       |{
       | "birthReferenceNumber" : "1234567891",
       | "firstName" : "Chris",
       | "lastName" : "Jones",
       | "dateOfBirth" : "2012-02-16",
       | "whereBirthRegistered" : "scotland"
       |}
    """.stripMargin)

  val userNoMatchExcludingReferenceKey: JsValue = parse(s"""
       |{
       | "firstName" : "Chris",
       | "lastName" : "Jones",
       | "dateOfBirth" : "2012-02-16",
       | "whereBirthRegistered" : "england"
       |}
    """.stripMargin)

  val userNoMatchScotlandExcludingReferenceKey: JsValue = parse(s"""
       |{
       | "firstName" : "Chris",
       | "lastName" : "Jones",
       | "dateOfBirth" : "2012-02-16",
       | "whereBirthRegistered" : "scotland"
       |}
    """.stripMargin)

  val userNoMatchUTF8SpecialCharacters: JsValue = parse(s"""
       |{
       | "firstName" : "Adàm TËST",
       | "lastName" : "SMÏTH",
       | "dateOfBirth" : "2006-08-12",
       | "whereBirthRegistered" : "england"
       |}
    """.stripMargin)

  val additionalNamesKeyNoValue: JsValue = parse(s"""
       |{
       | "firstName" : "Adàm TËST",
       | "additionalNames" : "",
       | "lastName" : "SMÏTH",
       | "dateOfBirth" : "2006-08-12",
       | "whereBirthRegistered" : "england"
       |}
    """.stripMargin)

  val userNoMatchExcludingAdditionalNameKey: JsValue = parse(s"""
       |{
       | "firstName" : "Adàm TËST",
       |
       | "lastName" : "SMÏTH",
       | "dateOfBirth" : "2006-08-12",
       | "whereBirthRegistered" : "england"
       |}
    """.stripMargin)

  val additionalNameWithSpecialCharacters: JsValue = parse(s"""
       |{
       | "firstName" : "Adàm TËST",
       | "additionalNames" : ",../WEB-INF/web.xml",
       | "lastName" : "SMÏTH",
       | "dateOfBirth" : "2006-08-12",
       | "whereBirthRegistered" : "england"
       |}
    """.stripMargin)

  val additionalNameWithMoreThan250Characters: JsValue = parse(s"""
       |{
       |"firstName" : "Adàm TËST",
       |"additionalNames" : "RAdmUElSgUkBKGXKQMGXlBCBktIJK UBjpRuGGvswXBbIHIUNTquycNRdXyVftdnUJYid mRfjSbZJoNIIdXJraEAtGhdagNCyhMKHYocWLbVdwWWpYVbGkZYwelvvfIYhibZgbbpagN CyhMKHYocWLbVdwWWpYVbGkZYwelvvfIYhibZgbbptqEQEJYRWPKeELQYCUtteeaftfvvdjaQqnFMgwWWpYVbGkZYwelvvfIYhibZgbbptqEQEJYRWPKeELQYC UtteeaftfvvdjaQqnFMg",
       |"lastName" : "Jones",
       |"dateOfBirth" : "2012-11-16",
       |"birthReferenceNumber" : "$referenceNumber",
       |"whereBirthRegistered" : "england"
       |}
     """.stripMargin)

  val additionalNameWithASingleSpace: JsValue = parse(s"""
       |{
       |"firstName" : "Ronan",
       |"additionalNames" : " ",
       |"lastName" : "Test",
       |"dateOfBirth" : "2012-11-16",
       |"birthReferenceNumber" : "$referenceNumber",
       |"whereBirthRegistered" : "england"
       |}
     """.stripMargin)

  val additionalNameWithMultipleSpaces: JsValue = parse(s"""
       |{
       |"firstName" : "Ronan",
       |"additionalNames" : "     ",
       |"lastName" : "Test",
       |"dateOfBirth" : "2012-11-16",
       |"birthReferenceNumber" : "$referenceNumber",
       |"whereBirthRegistered" : "england"
       |}
     """.stripMargin)

  val userNoMatchExcludingReferenceKeyScotland: JsValue = parse(s"""
       |{
       | "firstName" : "Chris",
       | "lastName" : "Jones",
       | "dateOfBirth" : "2012-02-16",
       | "whereBirthRegistered" : "scotland"
       |}
    """.stripMargin)

  val nrsRequestWithSpecialCharacters: JsValue = parse(
    s"""
       |{
       | "firstName" : "Gab'iœ-Äæy",
       | "lastName" : "HaÐ0ÄœÄæes",
       | "dateOfBirth" : "2011-10-01",
       | "whereBirthRegistered" : "scotland"
       |}
     """.stripMargin
  )

  val nrsReferenceRequestWithSpecialCharacters: JsValue = parse(
    s"""
       |{
       | "birthReferenceNumber": "2017350007",
       | "firstName" : "Gab'iœ-Äæy",
       | "lastName" : "HaÐ0ÄœÄæes",
       | "dateOfBirth" : "2011-10-01",
       | "whereBirthRegistered" : "scotland"
       |}
     """.stripMargin
  )

  val nrsDetailsRequestWithSingleMatch: JsValue = parse(
    s"""
       |{
       | "firstName" : "Adam TEST",
       | "lastName" : "SMITH",
       | "dateOfBirth" : "2009-11-12",
       | "whereBirthRegistered" : "scotland"
       |}
     """.stripMargin
  )

  val userNoMatchExcludingReferenceKeyNorthernIreland: JsValue = parse(s"""
       |{
       | "firstName" : "Chris",
       | "lastName" : "Jones",
       | "dateOfBirth" : "2012-02-16",
       | "whereBirthRegistered" : "northern ireland"
       |}
    """.stripMargin)

  val userMultipleMatchExcludingReferenceKey: JsValue = parse(s"""
       |{
       |
       | "firstName" : "Gibby",
       | "lastName" : "Haynes",
       | "dateOfBirth" : "2011-10-01",
       | "whereBirthRegistered" : "england"
       |}
    """.stripMargin)

  val user400000001: JsValue = parse(s"""
       |{
       |  "birthReferenceNumber": "400000001",
       | "firstName" : "Gibby",
       | "lastName" : "Haynes",
       | "dateOfBirth" : "2011-10-01",
       | "whereBirthRegistered" : "england"
       |}
    """.stripMargin)

  val user400000001WithoutReferenceNumber: JsValue = parse(s"""
       |{
       | "firstName" : "Gibby",
       | "lastName" : "Haynes",
       | "dateOfBirth" : "2011-10-01",
       | "whereBirthRegistered" : "england"
       |}
    """.stripMargin)

  val userNoMatchExcludingReferenceValue: JsValue = parse(s"""
       |{
       | "firstName" : "Chris",
       | "lastName" : "Jones",
       | "dateOfBirth" : "2012-02-16",
       | "birthReferenceNumber" : "",
       | "whereBirthRegistered" : "england"
       |}
    """.stripMargin)

  val userNoMatchExcludingFirstNameKey: JsValue = parse(s"""
       |{
       |"lastName" : "Jones",
       |"dateOfBirth" : "2012-04-18",
       |"birthReferenceNumber" : "$referenceNumber"
       |}
     """.stripMargin)

  val userNoMatchExcludingReferenceNumber: JsValue = parse(s"""
       |{
       | "firstName" : "Chris",
       | "lastName" : "Jones",
       | "dateOfBirth" : "2012-02-16",
       | "birthReferenceNumber" : "",
       | "whereBirthRegistered" : "england"
       |}
    """.stripMargin)

  val userNoMatchIncludingReferenceNumber: JsValue = parse(s"""
       |{
       | "firstName" : "Chris",
       | "lastName" : "Jones",
       | "dateOfBirth" : "2012-08-03",
       | "birthReferenceNumber" : "$referenceNumber",
       | "whereBirthRegistered" : "wales"
       |}
    """.stripMargin)

  val userNoMatchIncludingReferenceNumberCamelCase: JsValue = parse(s"""
       |{
       | "firstName" : "Chris",
       | "lastName" : "Jones",
       | "dateOfBirth" : "2012-08-03",
       | "birthReferenceNumber" : "$referenceNumber",
       | "whereBirthRegistered" : "WalEs"
       |}
    """.stripMargin)

  val userNoMatchIncludingReferenceCharacters: JsValue = parse(s"""
       |{
       | "firstName" : "Chris",
       | "lastName" : "Jones",
       | "dateOfBirth" : "2012-08-03",
       | "birthReferenceNumber" : "ab1_-CD26",
       | "whereBirthRegistered" : "wales"
       |}
    """.stripMargin)

  val userNoMatchIncludingInvalidData: JsValue = parse(s"""
       |{
       | "firstName" : "Chris",
       | "lastName" : "Jones",
       | "dateOfBirth" : "2012-08-03",
       | "birthReferenceNumber" : "123*34)",
       | "whereBirthRegistered" : "wales"
       |}
    """.stripMargin)

  def userInvalidReference(country: String, referenceNumber: String): JsValue = parse(s"""
       |{
       | "firstName" : "Chris",
       | "lastName" : "Jones",
       | "dateOfBirth" : "2012-08-03",
       | "birthReferenceNumber" : "$referenceNumber",
       | "whereBirthRegistered" : "$country"
       |}
    """.stripMargin)

  val userMatchExcludingReferenceNumber: JsValue = parse(s"""
       |{
       | "firstName" : "Adam TEST",
       | "lastName" : "SMITH",
       | "dateOfBirth" : "2012-02-16",
       | "birthReferenceNumber" : "",
       | "whereBirthRegistered" : "england"
       |}
    """.stripMargin)

  val userMatchIncludingReferenceNumber: JsValue = parse(s"""
       |{
       | "firstName" : "Adam TEST",
       | "lastName" : "SMITH",
       | "dateOfBirth" : "2006-11-12",
       | "birthReferenceNumber" : "500035710",
       | "whereBirthRegistered" : "england"
       |}
    """.stripMargin)

  val userMatchExcludingReferenceNumberKey: JsValue = parse(s"""
       |{
       | "firstName" : "Adam TEST",
       | "lastName" : "SMITH",
       | "dateOfBirth" : "2006-11-12",
       | "whereBirthRegistered" : "england"
       |}
    """.stripMargin)

  val userMatchExcludingReferenceNumberKeyForScotland: JsValue = parse(s"""
       |{
       | "firstName" : "Adam TEST",
       | "lastName" : "SMITH",
       | "dateOfBirth" : "2009-11-12",
       | "whereBirthRegistered" : "scotland"
       |}
    """.stripMargin)

  val userMatchIncludingReferenceNumberKeyForScotland: JsValue = parse(s"""
       |{
       | "birthReferenceNumber" : "2017734003",
       | "firstName" : "Adam TEST",
       | "lastName" : "SMITH",
       | "dateOfBirth" : "2009-11-12",
       | "whereBirthRegistered" : "scotland"
       |}
    """.stripMargin)

  val userDob20090630: JsValue = parse(s"""
       |{
       | "birthReferenceNumber" : "2017734100",
       | "firstName" : "Adam TEST",
       | "lastName" : "SMITH",
       | "dateOfBirth" : "2009-06-30",
       | "whereBirthRegistered" : "scotland"
       |}
    """.stripMargin)

  val userMatchCountryNameInMixCase: JsValue = parse(s"""
       |{
       | "firstName" : "Adam TEST",
       | "lastName" : "SMITH",
       | "dateOfBirth" : "2006-11-12",
       | "birthReferenceNumber" : "500035710",
       | "whereBirthRegistered" : "EngLand"
       |}
    """.stripMargin)

  val userNoMatchExcludingDateOfBirthKey: JsValue = parse(s"""
       |{
       | "firstName" : "Adam TEST",
       | "lastName" : "SMITH",
       | "birthReferenceNumber" : "500035710",
       | "whereBirthRegistered" : "england"
       |}
    """.stripMargin)

  val userNoMatchExcludingDateOfBirthValue: JsValue = parse(s"""
       |{
       | "firstName" : "Adam TEST",
       | "lastName" : "SMITH",
       | "dateOfBirth" : "",
       | "birthReferenceNumber" : "500035710",
       | "whereBirthRegistered" : "england"
       |}
    """.stripMargin)

  val userNoMatchExcludingfirstNameKey: JsValue = parse(s"""
       |{
       |"lastName" : "Smith",
       |"dateOrBirth" : "2012-12-17",
       |"birthReferenceNumber" : "$referenceNumber",
       |"whereBirthRegistered" : "england"
       |}
     """.stripMargin)

  val userInvalidWhereBirthRegistered: JsValue = parse(s"""
       |{
       |"firstName" : "Adam TEST",
       |"lastName" : "SMITH",
       |"dateOfBirth" : "2012-11-16",
       |"birthReferenceNumber" : "500035710",
       |"whereBirthRegistered": "fiji"
       |}
     """.stripMargin)

  val userNoMatchExcludingfirstNameValue: JsValue = parse(s"""
       |{
       |"firstName" : "",
       |"lastName" : "Jones",
       |"dateOfBirth" : "2012-11-16",
       |"birthReferenceNumber" : "$referenceNumber",
       |"whereBirthRegistered" : "england"
       |}
     """.stripMargin)

  val firstNameWithSpecialCharacters: JsValue = parse(s"""
       |{
       |"firstName" : "../WEB-INF/web.xml",
       |"lastName" : "Jones",
       |"dateOfBirth" : "2012-11-16",
       |"birthReferenceNumber" : "$referenceNumber",
       |"whereBirthRegistered" : "england"
       |}
     """.stripMargin)

  val firstNameWithMoreThan250Characters: JsValue = parse(s"""
       |{
       |"firstName" : "RAdmUElSgUkBKGXKQMGXlBCBktIJK UBjpRuGGvswXBbIHIUNTquycNRdXyVftdnUJYid mRfjSbZJoNIIdXJraEAtGhdagNCyhMKHYocWLbVdwWWpYVbGkZYwelvvfIYhibZgbbpagN CyhMKHYocWLbVdwWWpYVbGkZYwelvvfIYhibZgbbptqEQEJYRWPKeELQYCUtteeaftfvvdjaQqnFMgwWWpYVbGkZYwelvvfIYhibZgbbptqEQEJYRWPKeELQYC UtteeaftfvvdjaQqnFMg",
       |"lastName" : "Jones",
       |"dateOfBirth" : "2012-11-16",
       |"birthReferenceNumber" : "$referenceNumber",
       |"whereBirthRegistered" : "england"
       |}
     """.stripMargin)

  val firstNameWithEqualsCharacter: JsValue = parse(s"""
        |{
        |"firstName" : "=",
        |"lastName" : "Jones",
        |"dateOfBirth" : "2012-11-16",
        |"birthReferenceNumber" : "$referenceNumber",
        |"whereBirthRegistered" : "england"
        |}
      """.stripMargin)

  val firstNameWithPlusCharacter: JsValue = parse(s"""
       |{
       |"firstName" : "+",
       |"lastName" : "Jones",
       |"dateOfBirth" : "2012-11-16",
       |"birthReferenceNumber" : "$referenceNumber",
       |"whereBirthRegistered" : "england"
       |}
      """.stripMargin)

  val firstNameWithAtCharacter: JsValue = parse(s"""
       |{
       |"firstName" : "@",
       |"lastName" : "Jones",
       |"dateOfBirth" : "2012-11-16",
       |"birthReferenceNumber" : "$referenceNumber",
       |"whereBirthRegistered" : "england"
       |}
      """.stripMargin)

  val firstNameWithNullCharacter: JsValue = parse(s"""
       |{
       |"firstName" : "\\u0000",
       |"lastName" : "Jones",
       |"dateOfBirth" : "2012-11-16",
       |"birthReferenceNumber" : "$referenceNumber",
       |"whereBirthRegistered" : "england"
       |}
      """.stripMargin)

  val firstNameWithMoreThan100Characters: JsValue = parse(s"""
       |{
       |"firstName" : "RAdmUElSgUkBKGXKQMGXlBCBktIJKUBjpRuGGvswXBbIHIUNTquycNRdXyVftdnUJYidmRfjSbZJoNIIdXJraEAtGhdagNCyhMKHYocWL",
       |"lastName" : "Jones",
       |"dateOfBirth" : "2012-11-16",
       |"birthReferenceNumber" : "$referenceNumber",
       |"whereBirthRegistered" : "england"
       |}
     """.stripMargin)

  val firstNameWithASingleSpace: JsValue = parse(s"""
       |{
       |"firstName" : " ",
       |"lastName" : "Jones",
       |"dateOfBirth" : "2012-11-16",
       |"birthReferenceNumber" : "$referenceNumber",
       |"whereBirthRegistered" : "england"
       |}
     """.stripMargin)

  val firstNameWithMultipleSpaces: JsValue = parse(s"""
       |{
       |"firstName" : "      ",
       |"lastName" : "Jones",
       |"dateOfBirth" : "2012-11-16",
       |"birthReferenceNumber" : "$referenceNumber",
       |"whereBirthRegistered" : "england"
       |}
     """.stripMargin)

  val lastNameWithMoreThan100Characters: JsValue = parse(s"""
       |{
       |"firstName" : "Adam",
       |"lastName" : "RAdmUElSgUkBKGXKQMGXlBCBktIJKUBjpRuGGvswXBbIHIUNTquycNRdXyVftdnUJYidmRfjSbZJoNIIdXJraEAtGhdagNCyhMKHYocWL",
       |"dateOfBirth" : "2012-11-16",
       |"birthReferenceNumber" : "$referenceNumber",
       |"whereBirthRegistered" : "england"
       |}
     """.stripMargin)

  val lastNameWithMoreThan250Characters: JsValue = parse(s"""
       |{
       |"firstName" : "Adam",
       |"lastName" : "RAdmUElSgUkBKGXKQMGXlBCBktIJK UBjpRuGGvswXBbIHIUNTquycNRdXyVftdnUJYid mRfjSbZJoNIIdXJraEAtGhdagNCyhMKHYocWLbVdwWWpYVbGkZYwelvvfIYhibZgbbpagN CyhMKHYocWLbVdwWWpYVbGkZYwelvvfIYhibZgbbptqEQEJYRWPKeELQYCUtteeaftfvvdjaQqnFMgwWWpYVbGkZYwelvvfIYhibZgbbptqEQEJYRWPKeELQYC UtteeaftfvvdjaQqnFMg",
       |"dateOfBirth" : "2012-11-16",
       |"birthReferenceNumber" : "$referenceNumber",
       |"whereBirthRegistered" : "england"
       |}
     """.stripMargin)

  val lastNameWithSpecialCharacters: JsValue = parse(s"""
       |{
       |"firstName" : "Adam TEST",
       |"lastName" : "Gibby&cat /etc/passwd&",
       |"dateOfBirth" : "2012-11-16",
       |"birthReferenceNumber" : "$referenceNumber",
       |"whereBirthRegistered" : "england"
       |}
     """.stripMargin)

  val lastNameWithASingleSpace: JsValue = parse(s"""
       |{
       |"firstName" : "Ronan",
       |"lastName" : " ",
       |"dateOfBirth" : "2012-11-16",
       |"birthReferenceNumber" : "$referenceNumber",
       |"whereBirthRegistered" : "england"
       |}
     """.stripMargin)

  val lastNameWithMultipleSpaces: JsValue = parse(s"""
       |{
       |"firstName" : "Ronan",
       |"lastName" : "      ",
       |"dateOfBirth" : "2012-11-16",
       |"birthReferenceNumber" : "$referenceNumber",
       |"whereBirthRegistered" : "england"
       |}
     """.stripMargin)

  val userNoMatchExcludinglastNameKey: JsValue = parse(s"""
       |{
       |"firstName" : "John",
       |"dateOrBirth" : "2012-12-17",
       |"birthReferenceNumber" : "$referenceNumber",
       |"whereBirthRegistered" : "england"
       |}
     """.stripMargin)

  val userNoMatchExcludinglastNameValue: JsValue = parse(s"""
       |{
       |"firstName" : "John",
       |"lastName" : "",
       |"dateOfBirth" : "2012-11-16",
       |"birthReferenceNumber" : "$referenceNumber",
       |"whereBirthRegistered" : "england"
       |}
     """.stripMargin)

  val userNoMatchExcludingWhereBirthRegisteredKey: JsValue = parse(s"""
       |{
       |"firstName" : "Manish",
       |"lastName" : "Varma",
       |"dateOfBirth" : "2012-11-16",
       |"birthReferenceNumber" : "$referenceNumber"
       |}
     """.stripMargin)

  val userNoMatchExcludingWhereBirthRegisteredValue: JsValue = parse(s"""
       |{
       |"firstName" : "John",
       |"lastName" : "Jones",
       |"dateOfBirth" : "2012-11-16",
       |"birthReferenceNumber" : "$referenceNumber",
       |"whereBirthRegistered" : ""
       |}
     """.stripMargin)

  val userInvalidDOB: JsValue = parse(s"""
       |{
       | "firstName" : "Adam TEST",
       | "lastName" : "SMITH",
       | "dateOfBirth" : "2006-02-16",
       | "birthReferenceNumber" : "500035710",
       | "whereBirthRegistered" : "england"
       |}
    """.stripMargin)

  val userInvalidDOBFormat: JsValue = parse(s"""
       |{
       | "firstName" : "Adam TEST",
       | "lastName" : "SMITH",
       | "dateOfBirth" : "1234567890",
       | "birthReferenceNumber" : "500035710",
       | "whereBirthRegistered" : "england"
       |}
    """.stripMargin)

  val userValidDOB: JsValue = parse(s"""
       |{
       | "firstName" : "Adam TEST",
       | "lastName" : "SMITH",
       | "dateOfBirth" : "2012-02-16",
       | "birthReferenceNumber" : "500035710",
       | "whereBirthRegistered" : "england"
       |}
    """.stripMargin)

  val userValidDOB20090701: JsValue = parse(s"""
       |{
       | "firstName" : "Adam TEST",
       | "lastName" : "SMITH",
       | "dateOfBirth" : "2009-07-01",
       | "birthReferenceNumber" : "500035710",
       | "whereBirthRegistered" : "england"
       |}
    """.stripMargin)

  val userValidDOB20090630: JsValue = parse(s"""
       |{
       | "firstName" : "Adam TEST",
       | "lastName" : "SMITH",
       | "dateOfBirth" : "2009-06-30",
       | "birthReferenceNumber" : "500035710",
       | "whereBirthRegistered" : "england"
       |}
    """.stripMargin)

  val nrsNoRecordResponse: JsValue = parse(s"""
       |{
       |  "code": "BIRTH_REGISTRATION_NOT_FOUND",
       |  "reason": "No birth registration found that matched the search keys"
       |}
     """.stripMargin)

  val nrsInvalidPayload: JsValue = parse(s"""
       |{
       |  "code": "INVALID_PAYLOAD",
       |  "reason": "Submission has not passed validation. Invalid PAYLOAD"
       |}
     """.stripMargin)

  val nrsInvalidHeaderResponse: JsValue = parse(s"""
       |{
       |  "code": "INVALID_HEADER",
       |  "reason": "The HTTP header is invalid."
       |}
     """.stripMargin)

  val nrsInvalidDistrict: JsValue = parse(s"""
       |{
       |  "code": "INVALID_DISTRICT_NUMBER",
       |  "reason": "The Registration District number does not represent a number for the informed year."
       |}
     """.stripMargin)

  val nrsQueryLengthExcessive: JsValue = parse(s"""
       |{
       |  "code": "QUERY_LENGTH_EXCESSIVE",
       |  "reason": "Query message length is excessive."
       |}
     """.stripMargin)

  val nrsServerErrorResponse: JsValue = parse(s"""
       |{
       |  "code": "SERVER_ERROR",
       |  "reason": "DES is currently experiencing problems that require live service intervention"
       |}
     """.stripMargin)

  val nrsServiceUnavailableResponse: JsValue = parse(s"""
       |{
       |  "code": "SERVICE_UNAVAILABLE",
       |  "reason": "Dependent systems are currently not responding"
       |}
     """.stripMargin)

  val referenceNumberScenario = List(
    Map(
      "description"     -> "return response code 400 if request contains birthReferenceNumber below minimum length for england",
      "responseCode"    -> BAD_REQUEST,
      "country"         -> "england",
      "referenceNumber" -> "12345678"
    ),
    Map(
      "description"     -> "return response code 400 if request contains birthReferenceNumber above maximum length for england",
      "responseCode"    -> BAD_REQUEST,
      "country"         -> "england",
      "referenceNumber" -> "1234567891"
    ),
    Map(
      "description"     -> "return response code 400 if request contains birthReferenceNumber below minimum length for scotland",
      "responseCode"    -> BAD_REQUEST,
      "country"         -> "scotland",
      "referenceNumber" -> s"$referenceNumber"
    ),
    Map(
      "description"     -> "return response code 400 if request contains birthReferenceNumber above maximum length for scotland",
      "responseCode"    -> BAD_REQUEST,
      "country"         -> "scotland",
      "referenceNumber" -> "12345678912"
    )
  )

  def getNrsResponse(
    fatherName: String = "Asdf",
    fatherLastName: String = "ASDF",
    fatherBirthPlace: String = "23 High Street, Perth, PA3 4TG",
    informantName: String = "Mother",
    qualification: String = "J Smith"
  ): JsValue = {

    def buildKey(key: String, value: String, append: Option[String] = Some(",")): String =
      if (value.nonEmpty) {
        s""" "$key": "$value"${append.getOrElse("")}"""
      } else {
        """"""
      }

    val nrsResponse: JsValue = parse(s"""
         |{  "births": [
         |    {
         | "subjects" : {
         |    "father": {
         |          ${buildKey("firstName", fatherName)}
         |          ${buildKey("lastName", fatherLastName, if (fatherBirthPlace.isEmpty) None else Some(","))}
         |          ${buildKey("address", fatherBirthPlace, None)}
         |       },
         |    "mother": {
         |          "firstName": "Joan",
         |          "lastName": "SMITH",
         |          "address": "24 Church Road Edinburgh",
         |          "maidenSurname": "SMITH"
         |       },
         |   "child" : {
         |          "firstName": "Adam TEST",
         |          "lastName": "SMITH",
         |          "birthPlace": "Edinburgh",
         |          "sex": "M",
         |          "dateOfBirth": "2009-11-12"
         |    },
         |   "informant": {
         |          ${buildKey("qualification", qualification)}
         |          "fullName": "$informantName"
         |        }
         | },
         | "id" : "2017734003",
         | "status": 1,
         | "deathCode": 0
         |}   ]
         |}
    """.stripMargin)

    nrsResponse
  }

}
