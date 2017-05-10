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

package uk.gov.hmrc.brm.utils

import org.joda.time.LocalDate
import play.api.libs.json.{JsValue, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.brm.models.brm.Payload
import uk.gov.hmrc.brm.models.response.{Child, Record}
import uk.gov.hmrc.play.http.HttpResponse

object TestHelper {

  /**
    * GRO
   */

  val groJsonResponseObject = JsonUtils.getJsonFromFile("gro","500035710")
  val groJsonResponseObject400000001 = JsonUtils.getJsonFromFile("gro","400000001")
  val groJsonResponseObjectCollection = JsonUtils.getJsonFromFile("gro", "500035710-array")
  val groJsonResponseObjectCollection400000001 = JsonUtils.getJsonFromFile("gro", "400000001-array")
  val groJsonResponseObjectMultipleWithMatch = JsonUtils.getJsonFromFile("gro", "400000004-multiple-match")
  val groJsonResponseObject20120216 = JsonUtils.getJsonFromFile("gro", "2012-02-16")
  val groJsonResponseObject20090701 = JsonUtils.getJsonFromFile("gro", "2009-07-01")
  val groJsonResponseObject20090630 = JsonUtils.getJsonFromFile("gro", "2009-06-30")
  val groResponseWithAdditionalName = JsonUtils.getJsonFromFile("gro","with_additional_name")
  val groResponseWithoutAdditionalName = JsonUtils.getJsonFromFile("gro","without_additional_name")

  val payload = Payload(Some("500035710"), "Adam", "Wilson", new LocalDate("2006-11-12"), BirthRegisterCountry.ENGLAND)
  val payloadNoReference = Payload(None, "Adam", "Wilson", new LocalDate("2006-11-12"), BirthRegisterCountry.ENGLAND)

  /**
    * NRS
    */

  val validNrsJsonResponseObject = JsonUtils.getJsonFromFile("nrs", "2017734003")
  val validNrsJsonResponseObjectRCE = JsonUtils.getJsonFromFile("nrs", "2017350003")
  val validNrsJsonResponse2017350007 = JsonUtils.getJsonFromFile("nrs", "2017350007")
  val nrsResponseWithMultiple = JsonUtils.getJsonFromFile("nrs", "AdamTEST_multiple")
  val nrsRecord20090630 = JsonUtils.getJsonFromFile("nrs", "2017734100")
  val nrsRecord2017350001 = JsonUtils.getJsonFromFile("nrs", "2017350001")

  val nrsRequestPayload = Payload(Some("2017734003"), "Adam TEST", "SMITH", new LocalDate("2009-11-12"), BirthRegisterCountry.SCOTLAND)
  val nrsRequestPayload2017350001 = Payload(Some("2017350001"), "Adam TEST", "SMITH", new LocalDate("2009-11-12"), BirthRegisterCountry.SCOTLAND)

  val nrsRequestPayloadWithoutBrn = Payload(None, "Adam TEST", "SMITH", new LocalDate("2009-11-12"), BirthRegisterCountry.SCOTLAND)
  val nrsRequestPayloadWithSpecialChar = Payload(Some("2017350007"), "Gab'iœ-Äæy", "HaÐ0ÄœÄæes", new LocalDate("2011-10-01"), BirthRegisterCountry.SCOTLAND)
  val nrsRequestPayloadWithFirstNameWrong = Payload(Some("2017350007"), "firstNameWrong", "HaÐ0ÄœÄæes", new LocalDate("2011-10-01"), BirthRegisterCountry.SCOTLAND)

  val payloadNoReferenceScotland = Payload(None, "Adam", "Wilson", new LocalDate("2006-11-12"), BirthRegisterCountry.SCOTLAND)

  /**
    * GRO-NI
    */

  val payloadNoReferenceNorthernIreland = Payload(None, "Adam", "Wilson", new LocalDate("2006-11-12"), BirthRegisterCountry.NORTHERN_IRELAND)

  val groResponseValidJson = Json.parse(
    """
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
      |  "systemNumber": 123456789,
      |  "id": 123456789,
      |  "status": {
      |    "blockedRegistration": false
      |  },
      |  "previousRegistration": {}
      |
      |  }
    """.stripMargin)

  def postRequest(v: JsValue): FakeRequest[JsValue] = FakeRequest("POST", "/birth-registration-matching/match")
    .withHeaders((ACCEPT, "application/vnd.hmrc.1.0+json"), ("Audit-Source", "DFS"))
    .withBody(v)

  def httpResponse(responseCode: Int, js: JsValue) = HttpResponse.apply(responseCode, Some(js))

  def httpResponse(js: JsValue) = HttpResponse.apply(OK, Some(js))

  def httpResponse(responseCode: Int) = HttpResponse.apply(responseCode)

  def validRecord: Record ={
    val birthDate = new LocalDate("2012-02-16")
    val child =  Child(123456789, "Chris", "Jones",Some(birthDate))
    Record(child, None)
  }

  def validRecordSpecialCharactersFirstName: Record ={
    val birthDate = new LocalDate("2012-02-16")
    val child =  Child(123456789, "Chris-Jame's", "Jones",Some(birthDate))
    Record(child, None)
  }

  def validRecordMiddleNames: Record ={
    val birthDate = new LocalDate("2012-02-16")
    val child =  Child(123456789, "Adam David", "Jones",Some(birthDate))
    Record(child, None)
  }

  def validRecordMiddleNamesWithSpaces: Record ={
    val birthDate = new LocalDate("2012-02-16")
    val child =  Child(123456789, "  Adam     David ", "Jones",Some(birthDate))
    Record(child, None)
  }

  def validRecordMiddleNamesWithSpacesAndPunctuation: Record ={
    val birthDate = new LocalDate("2012-02-16")
    val child =  Child(123456789, "   Jamie  Mary-Ann'é    Earl ", "Jones",Some(birthDate))
    Record(child, None)
  }

  def validRecordSpecialCharactersLastName: Record ={
    val birthDate = new LocalDate("2012-02-16")
    val child =  Child(123456789, "Chris", "Jones--Smith",Some(birthDate))
    Record(child, None)
  }

  def validRecordFirstNameSpace: Record ={
    val birthDate = new LocalDate("2012-02-16")
    val child =  Child(123456789, "Chris James", "Jones",Some(birthDate))
    Record(child, None)
  }

  def validRecordLastNameSpace: Record ={
    val birthDate = new LocalDate("2012-02-16")
    val child =  Child(123456789, "Chris", "Jones Smith",Some(birthDate))
    Record(child, None)
  }

  def validRecordLastNameMultipleSpace: Record ={
    val birthDate = new LocalDate("2012-02-16")
    val child =  Child(123456789, "Chris", "Jones  Smith",Some(birthDate))
    Record(child, None)
  }

  def validRecordLastNameMultipleSpaceBeginningTrailing: Record ={
    val birthDate = new LocalDate("2012-02-16")
    val child =  Child(123456789, "Chris", "  Jones  Smith ",Some(birthDate))
    Record(child, None)
  }

  def validRecordUTF8FirstName : Record = {
    val birthDate = new LocalDate("2012-02-16")
    val child =  Child(123456789, "Chrîs", "Jones",Some(birthDate))
    Record(child, None)
  }

  def validRecordUTF8LastName : Record = {
    val birthDate = new LocalDate("2012-02-16")
    val child =  Child(123456789, "Chris", "Jonéş",Some(birthDate))
    Record(child, None)
  }

  def validRecordUppercase: Record ={
    val birthDate = new LocalDate("2012-02-16")
    val child =  Child(123456789, "CHRIS", "JONES",Some(birthDate))
    Record(child, None)
  }

  def wrongCaseFirstNameValidRecord : Record = {
    val birthDate = new LocalDate("2012-02-16")
    val child =  Child(1, "CHriS", "Jones", Some(birthDate))
    Record(child, None)
  }

  def wrongCaseLastNameValidRecord : Record = {
    val birthDate = new LocalDate("2012-02-16")
    val child =  Child(1, "Chris", "JOnES", Some(birthDate))
    Record(child, None)
  }

  def wrongCaseValidRecord : Record = {
    val birthDate = new LocalDate("2012-02-16")
    val child =  Child(1, "cHrIs", "JOnES", Some(birthDate))
    Record(child, None)
  }

  def invalidRecord: Record ={
    val birthDate = new LocalDate("2012-02-16")
    val child =  Child(1, "invalidfirstName", "invalidLastName",None)
    Record(child, None)
  }

  def firstNameNotMatchedRecord: Record ={
    val birthDate = new LocalDate("2012-02-16")
    val child =  Child(123456789, "Manish", "Jones",Some(birthDate))
    Record(child, None)
  }

  def lastNameNotMatchRecord: Record ={
    val birthDate = new LocalDate("2012-02-16")
    val child =  Child(123456789, "Chris", "lastName",Some(birthDate))
    Record(child, None)
  }

  def dobNotMatchRecord: Record ={
    val birthDate = new LocalDate("2012-02-16")
    val child =  Child(123456789, "Chris", "Jones",None)
    Record(child, None)
  }

  val invalidResponse = Json.parse(
    """
      |[]
    """.stripMargin)

  val noJson = Json.parse(
    s"""{
        }
    """.stripMargin)

  val userWhereBirthRegisteredNI = Json.parse(
    s"""
       |{
       | "birthReferenceNumber" : "123456789",
       | "firstName" : "Chris",
       | "lastName" : "Jones",
       | "dateOfBirth" : "2012-02-16",
       | "whereBirthRegistered" : "northern ireland"
       |}
    """.stripMargin)

  val userWhereBirthRegisteredScotland = Json.parse(
    s"""
       |{
       | "birthReferenceNumber" : "1234567891",
       | "firstName" : "Chris",
       | "lastName" : "Jones",
       | "dateOfBirth" : "2012-02-16",
       | "whereBirthRegistered" : "scotland"
       |}
    """.stripMargin)

  val userNoMatchExcludingReferenceKey = Json.parse(
    s"""
       |{
       | "firstName" : "Chris",
       | "lastName" : "Jones",
       | "dateOfBirth" : "2012-02-16",
       | "whereBirthRegistered" : "england"
       |}
    """.stripMargin)

  val userNoMatchUTF8SpecialCharacters = Json.parse(
    s"""
       |{
       | "firstName" : "Adàm TËST",
       | "lastName" : "SMÏTH",
       | "dateOfBirth" : "2006-08-12",
       | "whereBirthRegistered" : "england"
       |}
    """.stripMargin)

  val userNoMatchExcludingReferenceKeyScotland = Json.parse(
    s"""
       |{
       | "firstName" : "Chris",
       | "lastName" : "Jones",
       | "dateOfBirth" : "2012-02-16",
       | "whereBirthRegistered" : "scotland"
       |}
    """.stripMargin)

  val nrsRequestWithSpecialCharacters = Json.parse(
    s"""
       |{
       | "firstName" : "Gab'iœ-Äæy",
       | "lastName" : "HaÐ0ÄœÄæes",
       | "dateOfBirth" : "2011-10-01",
       | "whereBirthRegistered" : "scotland"
       |}
     """.stripMargin
  )

  val nrsReferenceRequestWithSpecialCharacters = Json.parse(
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

  val nrsDetailsRequestWithSingleMatch = Json.parse(
    s"""
       |{
       | "firstName" : "Adam TEST",
       | "lastName" : "SMITH",
       | "dateOfBirth" : "2009-11-12",
       | "whereBirthRegistered" : "scotland"
       |}
     """.stripMargin
  )

  val userNoMatchExcludingReferenceKeyNorthernIreland = Json.parse(
    s"""
       |{
       | "firstName" : "Chris",
       | "lastName" : "Jones",
       | "dateOfBirth" : "2012-02-16",
       | "whereBirthRegistered" : "northern ireland"
       |}
    """.stripMargin)

  val userMultipleMatchExcludingReferenceKey = Json.parse(
    s"""
       |{
       |
       | "firstName" : "Gibby",
       | "lastName" : "Haynes",
       | "dateOfBirth" : "2011-10-01",
       | "whereBirthRegistered" : "england"
       |}
    """.stripMargin)


  val user400000001 = Json.parse(
    s"""
       |{
       |  "birthReferenceNumber": "400000001",
       | "firstName" : "Gibby",
       | "lastName" : "Haynes",
       | "dateOfBirth" : "2011-10-01",
       | "whereBirthRegistered" : "england"
       |}
    """.stripMargin)

  val user400000001WithoutReferenceNumber = Json.parse(
    s"""
       |{
       | "firstName" : "Gibby",
       | "lastName" : "Haynes",
       | "dateOfBirth" : "2011-10-01",
       | "whereBirthRegistered" : "england"
       |}
    """.stripMargin)

  val userNoMatchExcludingReferenceValue = Json.parse(
    s"""
       |{
       | "firstName" : "Chris",
       | "lastName" : "Jones",
       | "dateOfBirth" : "2012-02-16",
       | "birthReferenceNumber" : "",
       | "whereBirthRegistered" : "england"
       |}
    """.stripMargin)

  val userNoMatchExcludingFirstNameKey = Json.parse(
    s"""
       |{
       |"lastName" : "Jones",
       |"dateOfBirth" : "2012-04-18",
       |"birthReferenceNumber" : "123456789"
       |}
     """.stripMargin)

  val userNoMatchExcludingReferenceNumber = Json.parse(
    s"""
       |{
       | "firstName" : "Chris",
       | "lastName" : "Jones",
       | "dateOfBirth" : "2012-02-16",
       | "birthReferenceNumber" : "",
       | "whereBirthRegistered" : "england"
       |}
    """.stripMargin)

  val userNoMatchIncludingReferenceNumber = Json.parse(
    s"""
       |{
       | "firstName" : "Chris",
       | "lastName" : "Jones",
       | "dateOfBirth" : "2012-08-03",
       | "birthReferenceNumber" : "123456789",
       | "whereBirthRegistered" : "wales"
       |}
    """.stripMargin)

  val userNoMatchIncludingReferenceNumberCamelCase = Json.parse(
    s"""
       |{
       | "firstName" : "Chris",
       | "lastName" : "Jones",
       | "dateOfBirth" : "2012-08-03",
       | "birthReferenceNumber" : "123456789",
       | "whereBirthRegistered" : "WalEs"
       |}
    """.stripMargin)

  val userNoMatchIncludingReferenceCharacters = Json.parse(
    s"""
       |{
       | "firstName" : "Chris",
       | "lastName" : "Jones",
       | "dateOfBirth" : "2012-08-03",
       | "birthReferenceNumber" : "ab1_-CD26",
       | "whereBirthRegistered" : "wales"
       |}
    """.stripMargin)

  val userNoMatchIncludingInvalidData = Json.parse(
    s"""
       |{
       | "firstName" : "Chris",
       | "lastName" : "Jones",
       | "dateOfBirth" : "2012-08-03",
       | "birthReferenceNumber" : "123*34)",
       | "whereBirthRegistered" : "wales"
       |}
    """.stripMargin)

  def userInvalidReference(country: String, referenceNumber: String) = Json.parse(
    s"""
       |{
       | "firstName" : "Chris",
       | "lastName" : "Jones",
       | "dateOfBirth" : "2012-08-03",
       | "birthReferenceNumber" : "$referenceNumber",
       | "whereBirthRegistered" : "$country"
       |}
    """.stripMargin)

  val userMatchExcludingReferenceNumber = Json.parse(
    s"""
       |{
       | "firstName" : "Adam TEST",
       | "lastName" : "SMITH",
       | "dateOfBirth" : "2012-02-16",
       | "birthReferenceNumber" : "",
       | "whereBirthRegistered" : "england"
       |}
    """.stripMargin)

  val userMatchIncludingReferenceNumber = Json.parse(
    s"""
       |{
       | "firstName" : "Adam TEST",
       | "lastName" : "SMITH",
       | "dateOfBirth" : "2006-11-12",
       | "birthReferenceNumber" : "500035710",
       | "whereBirthRegistered" : "england"
       |}
    """.stripMargin)

  val userMatchExcludingReferenceNumberKey = Json.parse(
    s"""
       |{
       | "firstName" : "Adam TEST",
       | "lastName" : "SMITH",
       | "dateOfBirth" : "2006-11-12",
       | "whereBirthRegistered" : "england"
       |}
    """.stripMargin)

  val userMatchExcludingReferenceNumberKeyForScotland = Json.parse(
    s"""
       |{
       | "firstName" : "Adam TEST",
       | "lastName" : "SMITH",
       | "dateOfBirth" : "2009-11-12",
       | "whereBirthRegistered" : "scotland"
       |}
    """.stripMargin)

  val userMatchIncludingReferenceNumberKeyForScotland = Json.parse(
    s"""
       |{
       | "birthReferenceNumber" : "2017734003",
       | "firstName" : "Adam TEST",
       | "lastName" : "SMITH",
       | "dateOfBirth" : "2009-11-12",
       | "whereBirthRegistered" : "scotland"
       |}
    """.stripMargin)

  val userDob20090630 = Json.parse(
    s"""
       |{
       | "birthReferenceNumber" : "2017734100",
       | "firstName" : "Adam TEST",
       | "lastName" : "SMITH",
       | "dateOfBirth" : "2009-06-30",
       | "whereBirthRegistered" : "scotland"
       |}
    """.stripMargin)

  val userMatchCountryNameInMixCase = Json.parse(
    s"""
       |{
       | "firstName" : "Adam TEST",
       | "lastName" : "SMITH",
       | "dateOfBirth" : "2006-11-12",
       | "birthReferenceNumber" : "500035710",
       | "whereBirthRegistered" : "EngLand"
       |}
    """.stripMargin)

  val userNoMatchExcludingDateOfBirthKey = Json.parse(
    s"""
       |{
       | "firstName" : "Adam TEST",
       | "lastName" : "SMITH",
       | "birthReferenceNumber" : "500035710",
       | "whereBirthRegistered" : "england"
       |}
    """.stripMargin)

  val userNoMatchExcludingDateOfBirthValue = Json.parse(
    s"""
       |{
       | "firstName" : "Adam TEST",
       | "lastName" : "SMITH",
       | "dateOfBirth" : "",
       | "birthReferenceNumber" : "500035710",
       | "whereBirthRegistered" : "england"
       |}
    """.stripMargin)

  val userNoMatchExcludingfirstNameKey = Json.parse(
    s"""
       |{
       |"lastName" : "Smith",
       |"dateOrBirth" : "2012-12-17",
       |"birthReferenceNumber" : "123456789",
       |"whereBirthRegistered" : "england"
       |}
     """.stripMargin)


  val userInvalidWhereBirthRegistered = Json.parse(
    s"""
       |{
       |"firstName" : "Adam TEST",
       |"lastName" : "SMITH",
       |"dateOfBirth" : "2012-11-16",
       |"birthReferenceNumber" : "500035710",
       |"whereBirthRegistered": "fiji"
       |}
     """.stripMargin)

  val userNoMatchExcludingfirstNameValue = Json.parse(
    s"""
       |{
       |"firstName" : "",
       |"lastName" : "Jones",
       |"dateOfBirth" : "2012-11-16",
       |"birthReferenceNumber" : "123456789",
       |"whereBirthRegistered" : "england"
       |}
     """.stripMargin)

  val firstNameWithSpecialCharacters = Json.parse(
    s"""
       |{
       |"firstName" : "../WEB-INF/web.xml",
       |"lastName" : "Jones",
       |"dateOfBirth" : "2012-11-16",
       |"birthReferenceNumber" : "123456789",
       |"whereBirthRegistered" : "england"
       |}
     """.stripMargin)

  val firstNameWithMoreThan250Characters = Json.parse(
    s"""
       |{
       |"firstName" : "RAdmUElSgUkBKGXKQMGXlBCBktIJK UBjpRuGGvswXBbIHIUNTquycNRdXyVftdnUJYid mRfjSbZJoNIIdXJraEAtGhdagNCyhMKHYocWLbVdwWWpYVbGkZYwelvvfIYhibZgbbpagN CyhMKHYocWLbVdwWWpYVbGkZYwelvvfIYhibZgbbptqEQEJYRWPKeELQYCUtteeaftfvvdjaQqnFMgwWWpYVbGkZYwelvvfIYhibZgbbptqEQEJYRWPKeELQYC UtteeaftfvvdjaQqnFMg",
       |"lastName" : "Jones",
       |"dateOfBirth" : "2012-11-16",
       |"birthReferenceNumber" : "123456789",
       |"whereBirthRegistered" : "england"
       |}
     """.stripMargin)

  val firstNameWithMoreThan100Characters = Json.parse(
    s"""
       |{
       |"firstName" : "RAdmUElSgUkBKGXKQMGXlBCBktIJKUBjpRuGGvswXBbIHIUNTquycNRdXyVftdnUJYidmRfjSbZJoNIIdXJraEAtGhdagNCyhMKHYocWL",
       |"lastName" : "Jones",
       |"dateOfBirth" : "2012-11-16",
       |"birthReferenceNumber" : "123456789",
       |"whereBirthRegistered" : "england"
       |}
     """.stripMargin)

  val lastNameWithMoreThan100Characters = Json.parse(
    s"""
       |{
       |"firstName" : "Adam",
       |"lastName" : "RAdmUElSgUkBKGXKQMGXlBCBktIJKUBjpRuGGvswXBbIHIUNTquycNRdXyVftdnUJYidmRfjSbZJoNIIdXJraEAtGhdagNCyhMKHYocWL",
       |"dateOfBirth" : "2012-11-16",
       |"birthReferenceNumber" : "123456789",
       |"whereBirthRegistered" : "england"
       |}
     """.stripMargin)

  val lastNameWithMoreThan250Characters = Json.parse(
    s"""
       |{
       |"firstName" : "Adam",
       |"lastName" : "RAdmUElSgUkBKGXKQMGXlBCBktIJK UBjpRuGGvswXBbIHIUNTquycNRdXyVftdnUJYid mRfjSbZJoNIIdXJraEAtGhdagNCyhMKHYocWLbVdwWWpYVbGkZYwelvvfIYhibZgbbpagN CyhMKHYocWLbVdwWWpYVbGkZYwelvvfIYhibZgbbptqEQEJYRWPKeELQYCUtteeaftfvvdjaQqnFMgwWWpYVbGkZYwelvvfIYhibZgbbptqEQEJYRWPKeELQYC UtteeaftfvvdjaQqnFMg",
       |"dateOfBirth" : "2012-11-16",
       |"birthReferenceNumber" : "123456789",
       |"whereBirthRegistered" : "england"
       |}
     """.stripMargin)

  val lastNameWithSpecialCharacters = Json.parse(
    s"""
       |{
       |"firstName" : "Adam TEST",
       |"lastName" : "Gibby&cat /etc/passwd&",
       |"dateOfBirth" : "2012-11-16",
       |"birthReferenceNumber" : "123456789",
       |"whereBirthRegistered" : "england"
       |}
     """.stripMargin)

  val userNoMatchExcludinglastNameKey = Json.parse(
    s"""
       |{
       |"firstName" : "John",
       |"dateOrBirth" : "2012-12-17",
       |"birthReferenceNumber" : "123456789",
       |"whereBirthRegistered" : "england"
       |}
     """.stripMargin)

  val userNoMatchExcludinglastNameValue = Json.parse(
    s"""
       |{
       |"firstName" : "John",
       |"lastName" : "",
       |"dateOfBirth" : "2012-11-16",
       |"birthReferenceNumber" : "123456789",
       |"whereBirthRegistered" : "england"
       |}
     """.stripMargin)


  val userNoMatchExcludingWhereBirthRegisteredKey = Json.parse(
    s"""
       |{
       |"firstName" : "Manish",
       |"lastName" : "Varma",
       |"dateOfBirth" : "2012-11-16",
       |"birthReferenceNumber" : "123456789"
       |}
     """.stripMargin)

  val userNoMatchExcludingWhereBirthRegisteredValue = Json.parse(
    s"""
       |{
       |"firstName" : "John",
       |"lastName" : "Jones",
       |"dateOfBirth" : "2012-11-16",
       |"birthReferenceNumber" : "123456789",
       |"whereBirthRegistered" : ""
       |}
     """.stripMargin)

  val userInvalidDOB = Json.parse(
    s"""
       |{
       | "firstName" : "Adam TEST",
       | "lastName" : "SMITH",
       | "dateOfBirth" : "2006-02-16",
       | "birthReferenceNumber" : "500035710",
       | "whereBirthRegistered" : "england"
       |}
    """.stripMargin)

  val userInvalidDOBFormat = Json.parse(
    s"""
       |{
       | "firstName" : "Adam TEST",
       | "lastName" : "SMITH",
       | "dateOfBirth" : "1234567890",
       | "birthReferenceNumber" : "500035710",
       | "whereBirthRegistered" : "england"
       |}
    """.stripMargin)

  val userValidDOB = Json.parse(
    s"""
       |{
       | "firstName" : "Adam TEST",
       | "lastName" : "SMITH",
       | "dateOfBirth" : "2012-02-16",
       | "birthReferenceNumber" : "500035710",
       | "whereBirthRegistered" : "england"
       |}
    """.stripMargin)

  val userValidDOB20090701 = Json.parse(
    s"""
       |{
       | "firstName" : "Adam TEST",
       | "lastName" : "SMITH",
       | "dateOfBirth" : "2009-07-01",
       | "birthReferenceNumber" : "500035710",
       | "whereBirthRegistered" : "england"
       |}
    """.stripMargin)

  val userValidDOB20090630 = Json.parse(
    s"""
       |{
       | "firstName" : "Adam TEST",
       | "lastName" : "SMITH",
       | "dateOfBirth" : "2009-06-30",
       | "birthReferenceNumber" : "500035710",
       | "whereBirthRegistered" : "england"
       |}
    """.stripMargin)

  val nrsNoRecordResponse = Json.parse(
    s"""
       |{
       |  "code": "BIRTH_REGISTRATION_NOT_FOUND",
       |  "reason": "No birth registration found that matched the search keys"
       |}
     """.stripMargin)

  val nrsInvalidPayload = Json.parse(
    s"""
       |{
       |  "code": "INVALID_PAYLOAD",
       |  "reason": "Submission has not passed validation. Invalid PAYLOAD"
       |}
     """.stripMargin)

  val nrsInvalidHeaderResponse = Json.parse(
    s"""
       |{
       |  "code": "INVALID_HEADER",
       |  "reason": "The HTTP header is invalid."
       |}
     """.stripMargin)

  val nrsInvalidDistrict = Json.parse(
    s"""
       |{
       |  "code": "INVALID_DISTRICT_NUMBER",
       |  "reason": "The Registration District number does not represent a number for the informed year."
       |}
     """.stripMargin)

  val nrsQueryLengthExcessive = Json.parse(
    s"""
       |{
       |  "code": "QUERY_LENGTH_EXCESSIVE",
       |  "reason": "Query message length is excessive."
       |}
     """.stripMargin)

  val nrsServerErrorResponse = Json.parse(
    s"""
       |{
       |  "code": "SERVER_ERROR",
       |  "reason": "DES is currently experiencing problems that require live service intervention"
       |}
     """.stripMargin)

  val nrsServiceUnavailableResponse = Json.parse(
    s"""
       |{
       |  "code": "SERVICE_UNAVAILABLE",
       |  "reason": "Dependent systems are currently not responding"
       |}
     """.stripMargin)

  val referenceNumberScenario = List(
    Map(
      "description" -> "return response code 400 if request contains birthReferenceNumber below minimum length for england",
      "responseCode" -> BAD_REQUEST,
      "country" -> "england",
      "referenceNumber" -> "12345678"
    ),
    Map(
      "description" -> "return response code 400 if request contains birthReferenceNumber above maximum length for england",
      "responseCode" -> BAD_REQUEST,
      "country" -> "england",
      "referenceNumber" -> "1234567891"
    ),
    Map(
      "description" -> "return response code 400 if request contains birthReferenceNumber below minimum length for scotland",
      "responseCode" -> BAD_REQUEST,
      "country" -> "scotland",
      "referenceNumber" -> "123456789"
    ),
    Map(
      "description" -> "return response code 400 if request contains birthReferenceNumber above maximum length for scotland",
      "responseCode" -> BAD_REQUEST,
      "country" -> "scotland",
      "referenceNumber" -> "12345678912"
    )
  )



  def getNrsResponse(fatherName:String="Asdf", fatherLastName:String="ASDF",
                     fatherBirthPlace:String="23 High Street, Perth, PA3 4TG",
                     informantName:String="Mother",qualification:String="J Smith") : JsValue = {

    def buildKey(key: String, value: String, append: Option[String] = Some(",")): String = {
      if(!value.isEmpty)
        s""" "$key": "${value}"${append.getOrElse("")}"""
      else
        """"""
    }

    val nrsResponse = Json.parse(
    s"""
      |{  "births": [
      |    {
      | "subjects" : {
      |    "father": {
      |          ${buildKey("firstName", fatherName)}
      |          ${buildKey("lastName", fatherLastName, if(fatherBirthPlace.isEmpty) None else Some(","))}
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
      |          "fullName": "${informantName}"
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
