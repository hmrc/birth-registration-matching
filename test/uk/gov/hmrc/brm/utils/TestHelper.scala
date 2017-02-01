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
import play.api.libs.json.Json
import uk.gov.hmrc.brm.models.brm.Payload
import uk.gov.hmrc.brm.models.response.Record
import uk.gov.hmrc.brm.models.response.gro.Child

object TestHelper {

  val groJsonResponseObject = JsonUtils.getJsonFromFile("500035710")
  val groJsonResponseObjectCollection = JsonUtils.getJsonFromFile("500035710-array")
  val groJsonResponseObjectMultipleWithMatch = JsonUtils.getJsonFromFile("400000004-multiple-match")
  val groJsonResponseObject20120216 = JsonUtils.getJsonFromFile("2012-02-16")
  val groJsonResponseObject20090701 = JsonUtils.getJsonFromFile("2009-07-01")
  val groJsonResponseObject20090630 = JsonUtils.getJsonFromFile("2009-06-30")

  val payload = Payload(Some("500035710"), "Adam", "Wilson", new LocalDate("2006-11-12"), BirthRegisterCountry.ENGLAND)

  val payloadNoReference = Payload(None, "Adam", "Wilson", new LocalDate("2006-11-12"), BirthRegisterCountry.ENGLAND)
  val payloadNoReferenceScotland = Payload(None, "Adam", "Wilson", new LocalDate("2006-11-12"), BirthRegisterCountry.SCOTLAND)
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
       | "birthReferenceNumber" : "123456789",
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
       | "birthReferenceNumber" : "ab1_-CD263",
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
       |"birthReferenceNumber" : "123456789"
       |}
     """.stripMargin)


  val userInvalidWhereBirthRegistered = Json.parse(
    s"""
       |{
       |"firstname" : "Adam TEST",
       |"lastName" : "SMITH",
       |"dateOfBirth" : "2012-11-16",
       |"birthReferenceNumber" : "500035710",
       |"whereBirthRegistered": "fiji"
       |}
     """.stripMargin)

  val userNoMatchExcludingfirstNameValue = Json.parse(
    s"""
       |{
       |"firstname" : "",
       |"lastName" : "Jones",
       |"dateOfBirth" : "2012-11-16",
       |"birthReferenceNumber" : "123456789"
       |}
     """.stripMargin)

  val firstNameWithSpecialCharacters = Json.parse(
    s"""
       |{
       |"firstname" : "../WEB-INF/web.xml",
       |"lastName" : "Jones",
       |"dateOfBirth" : "2012-11-16",
       |"birthReferenceNumber" : "123456789"
       |}
     """.stripMargin)

  val firstNameWithMoreThan250Characters = Json.parse(
    s"""
       |{
       |"firstname" : "RAdmUElSgUkBKGXKQMGXlBCBktIJK UBjpRuGGvswXBbIHIUNTquycNRdXyVftdnUJYid mRfjSbZJoNIIdXJraEAtGhdagNCyhMKHYocWLbVdwWWpYVbGkZYwelvvfIYhibZgbbpagN CyhMKHYocWLbVdwWWpYVbGkZYwelvvfIYhibZgbbptqEQEJYRWPKeELQYCUtteeaftfvvdjaQqnFMgwWWpYVbGkZYwelvvfIYhibZgbbptqEQEJYRWPKeELQYC UtteeaftfvvdjaQqnFMg",
       |"lastName" : "Jones",
       |"dateOfBirth" : "2012-11-16",
       |"birthReferenceNumber" : "123456789"
       |}
     """.stripMargin)

  val firstNameWithMoreThan100Characters = Json.parse(
    s"""
       |{
       |"firstname" : "RAdmUElSgUkBKGXKQMGXlBCBktIJKUBjpRuGGvswXBbIHIUNTquycNRdXyVftdnUJYidmRfjSbZJoNIIdXJraEAtGhdagNCyhMKHYocWL",
       |"lastName" : "Jones",
       |"dateOfBirth" : "2012-11-16",
       |"birthReferenceNumber" : "123456789"
       |}
     """.stripMargin)

  val lastNameWithMoreThan100Characters = Json.parse(
    s"""
       |{
       |"firstname" : "Adam",
       |"lastName" : "RAdmUElSgUkBKGXKQMGXlBCBktIJKUBjpRuGGvswXBbIHIUNTquycNRdXyVftdnUJYidmRfjSbZJoNIIdXJraEAtGhdagNCyhMKHYocWL",
       |"dateOfBirth" : "2012-11-16",
       |"birthReferenceNumber" : "123456789"
       |}
     """.stripMargin)

  val lastNameWithMoreThan250Characters = Json.parse(
    s"""
       |{
       |"firstname" : "Adam",
       |"lastName" : "RAdmUElSgUkBKGXKQMGXlBCBktIJK UBjpRuGGvswXBbIHIUNTquycNRdXyVftdnUJYid mRfjSbZJoNIIdXJraEAtGhdagNCyhMKHYocWLbVdwWWpYVbGkZYwelvvfIYhibZgbbpagN CyhMKHYocWLbVdwWWpYVbGkZYwelvvfIYhibZgbbptqEQEJYRWPKeELQYCUtteeaftfvvdjaQqnFMgwWWpYVbGkZYwelvvfIYhibZgbbptqEQEJYRWPKeELQYC UtteeaftfvvdjaQqnFMg",
       |"dateOfBirth" : "2012-11-16",
       |"birthReferenceNumber" : "123456789"
       |}
     """.stripMargin)

  val lastNameWithSpecialCharacters = Json.parse(
    s"""
       |{
       |"firstname" : "Adam TEST",
       |"lastName" : "Gibby&cat /etc/passwd&",
       |"dateOfBirth" : "2012-11-16",
       |"birthReferenceNumber" : "123456789"
       |}
     """.stripMargin)

  val userNoMatchExcludinglastNameKey = Json.parse(
    s"""
       |{
       |"firstname" : "John",
       |"dateOrBirth" : "2012-12-17",
       |"birthReferenceNumber" : "123456789"
       |}
     """.stripMargin)

  val userNoMatchExcludinglastNameValue = Json.parse(
    s"""
       |{
       |"firstname" : "John",
       |"lastName" : "",
       |"dateOfBirth" : "2012-11-16",
       |"birthReferenceNumber" : "123456789"
       |}
     """.stripMargin)


  val userNoMatchExcludingWhereBirthRegisteredKey = Json.parse(
    s"""
       |{
       |"firstname" : "Manish",
       |"lastName" : "Varma",
       |"dateOfBirth" : "2012-11-16",
       |"birthReferenceNumber" : "123456789"
       |
       |}
     """.stripMargin)

  val userNoMatchExcludingWhereBirthRegisteredValue = Json.parse(
    s"""
       |{
       |"firstname" : "John",
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



}
