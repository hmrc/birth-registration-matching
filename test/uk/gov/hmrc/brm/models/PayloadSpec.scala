/*
 * Copyright 2022 HM Revenue & Customs
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
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.brm.metrics._
import uk.gov.hmrc.brm.models.brm.Payload
import uk.gov.hmrc.brm.models.brm.Payload.{PayloadWrites, _}
import uk.gov.hmrc.brm.utils.BirthRegisterCountry
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatest.OptionValues

class PayloadSpec extends AnyWordSpecLike with Matchers with OptionValues with GuiceOneAppPerSuite {

  implicit val engAndWalesMetrics: EnglandAndWalesBirthRegisteredCountMetrics = app.injector.instanceOf[EnglandAndWalesBirthRegisteredCountMetrics]
  implicit val northIreMetrics: NorthernIrelandBirthRegisteredCountMetrics = app.injector.instanceOf[NorthernIrelandBirthRegisteredCountMetrics]
  implicit val scotlandMetrics: ScotlandBirthRegisteredCountMetrics = app.injector.instanceOf[ScotlandBirthRegisteredCountMetrics]
  implicit val invalidRegMetrics: InvalidBirthRegisteredCountMetrics = app.injector.instanceOf[InvalidBirthRegisteredCountMetrics]

  private val unicode = "ÀÁÂÃÄÅÆÇÈÉÊËÌÍ ÏÐÑÒÓÔÕÖ×ØÙÚÛÜÝÞßàáâãäåæçèéêëìíîïðñòóôõö÷øùúûüýþÿÀÁÂÃÄÅÆÇÈÉÊËÌÍ" +
    " ÏÐÑÒÓÔÕÖ×ØÙÚÛÜÝÞßàáâãäåæçèéêëìíîïðñòóôõö÷øùú111111ÀÁÂÃÄÅÆÇÈÉÊËÌÍ ÏÐÑÒÓÔÕÖ×ØÙÚÛÜÝÞßàáâãäåæçèéêëìíî" +
    "ïðñòóôõö÷øùúûüýþÿÀÁÂÃÄÅÆÇÈÉÊËÌÍ ÏÐÑÒÓÔÕÖ×ØÙÚÛÜÝÞßàáâãäåæçèéêëìíîïðñòóôõö÷ø"
  private val maxCharacterLength = "RAdmUElSgUkBKGXKQMGXlBCBktIJKUBjpRuGGvswXBbIHIUNTquycNRdXyVftdnUJYi" +
    "dmRfjSbZJoNIIdXJraEAtGhdagNCyhMKHYocWLbVdwWWpYVbGkZYwelvvfIYhibZgbbpagNCyhMKHYocWLbVdwWWpYVbGkZYwe" +
    "lvvfIYhibZgbbptqEQEJYRWPKeELQYCUtteeaftfvvdjaQqnFMgwWWpYVbGkZYwelvvfIYhibZgbbptqEQEJYY"

  "Payload" when {

    "NRS" should {

       "return error when birthReferenceNumber value exceeds maximum length" ignore {
         val payload = Payload(Some("12345678911"), "Test", None, "Test", LocalDate.now, BirthRegisterCountry.SCOTLAND)
         Json.toJson(payload).validate[Payload].isError shouldBe true
       }

      "return error when birthReferenceNumber value is under minimum length" ignore {
        val payload = Payload(Some("123456789"), "Test", None, "Test", LocalDate.now, BirthRegisterCountry.SCOTLAND)
        Json.toJson(payload).validate[Payload].isError shouldBe true
      }
    }

    "GRO" should {

      "return error when birthReferenceNumber key exceeds maximum length" ignore {
        val payload = Payload(Some("1234567891"), "Test", None, "Test", LocalDate.now, BirthRegisterCountry.ENGLAND)
        Json.toJson(payload).validate[Payload].isError shouldBe true
      }

      "return error when birthReferenceNumber key is under minimum length" ignore {
        val payload = Payload(Some("12345678"), "Test", None, "Test", LocalDate.now, BirthRegisterCountry.ENGLAND)
        Json.toJson(payload).validate[Payload].isError shouldBe true
      }

      "return success when birthReferenceNumber key is valid length" ignore {
        val payload = Payload(Some("123456789"), "Test", None, "Test", LocalDate.now, BirthRegisterCountry.ENGLAND)
        Json.toJson(payload).validate[Payload].isSuccess shouldBe true
      }

      "instantiate an instance of payload" in {
        val payload = Payload(
          birthReferenceNumber = Some("123456789"),
          _firstName = "John",
          _lastName = "Smith",
          dateOfBirth = new LocalDate("1997-01-13"),
          whereBirthRegistered = BirthRegisterCountry.ENGLAND
        )

        payload shouldBe a[Payload]
        payload.birthReferenceNumber shouldBe Some("123456789")
        payload.firstNames shouldBe "John"
        payload.lastName shouldBe "Smith"
        payload.whereBirthRegistered shouldBe BirthRegisterCountry.withName("england")
      }

      "convert to json" in {
        val payload = Payload(
          birthReferenceNumber = Some("123456789"),
          _firstName = "John",
          _additionalNames = Some("Jones"),
          _lastName = "Smith",
          dateOfBirth = new LocalDate("1997-01-13"),
          whereBirthRegistered = BirthRegisterCountry.ENGLAND
        )

        Json.toJson(payload) shouldBe Json.parse(
          """
            |{
            | "birthReferenceNumber": "123456789",
            | "firstName" : "John",
            | "additionalNames" : "Jones",
            | "lastName" : "Smith",
            | "dateOfBirth" : "1997-01-13",
            | "whereBirthRegistered" : "england"
            |}
          """.stripMargin)
      }

      "return success when complete and valid record exists" in {
        val payload = Payload(Some("123456789"), "John", Some("Jones"), "Smith", LocalDate.now, BirthRegisterCountry.ENGLAND)
        Json.toJson(payload).validate[Payload].isSuccess shouldBe true
      }

      "return success when firstName contains & character" in {
        val payload = Payload(None, "&", None, "Test", LocalDate.now, BirthRegisterCountry.ENGLAND)
        Json.toJson(payload).validate[Payload].isSuccess shouldBe true
      }

      "return success when firstName contains valid special characters" in {
        val payload = Payload(Some("123456789"), "&`-'^", Some("Jones"), "Smith", LocalDate.now, BirthRegisterCountry.ENGLAND)
        Json.toJson(payload).validate[Payload].isSuccess shouldBe true
      }

      "return success when firstName contains unicode characters." in {
        val payload = Payload(None, unicode, None, "Test", LocalDate.now, BirthRegisterCountry.SCOTLAND)
        Json.toJson(payload).validate[Payload].isSuccess shouldBe true
      }

      "return error when firstName contains special character" in {
        val payload = Payload(None, "-->", None, "Test", LocalDate.now, BirthRegisterCountry.ENGLAND)
        Json.toJson(payload).validate[Payload].isError shouldBe true
      }

      "return error when firstName contains special character |" in {
        val payload = Payload(None, "|", None, "Test", LocalDate.now, BirthRegisterCountry.ENGLAND)
        Json.toJson(payload).validate[Payload].isError shouldBe true
      }

      "return error when firstName contains . character" in {
        val payload = Payload(Some("123456789"), ".", Some("Jones"), "Smith", LocalDate.now, BirthRegisterCountry.ENGLAND)
        Json.toJson(payload).validate[Payload].isSuccess shouldBe false
      }

      "return error when firstName contains = character" in {
        val payload = Payload(Some("123456789"), "=", Some("Jones"), "Smith", LocalDate.now, BirthRegisterCountry.ENGLAND)
        Json.toJson(payload).validate[Payload].isSuccess shouldBe false
      }

      "return error when firstName contains @ character" in {
        val payload = Payload(Some("123456789"), "@", Some("Jones"), "Smith", LocalDate.now, BirthRegisterCountry.ENGLAND)
        Json.toJson(payload).validate[Payload].isSuccess shouldBe false
      }

      "return error when firstName contains + character" in {
        val payload = Payload(Some("123456789"), "+", Some("Jones"), "Smith", LocalDate.now, BirthRegisterCountry.ENGLAND)
        Json.toJson(payload).validate[Payload].isSuccess shouldBe false
      }

      "return error when firstName contains \u0000 (NULL) character" in {
        val payload = Payload(Some("123456789"), "\u0000", Some("Jones"), "Smith", LocalDate.now, BirthRegisterCountry.ENGLAND)
        Json.toJson(payload).validate[Payload].isSuccess shouldBe false
      }

      "return error when firstName contains newline character" in {
        val payload = Payload(None, "John\n", None, "Test", LocalDate.now, BirthRegisterCountry.ENGLAND)
        Json.toJson(payload).validate[Payload].isError shouldBe true
      }

      "return error when firstName contains double quote character." in {
        val payload = Payload(None, "John\"", None, "Test", LocalDate.now, BirthRegisterCountry.ENGLAND)
        Json.toJson(payload).validate[Payload].isError shouldBe true
      }

      "return error when firstName contains characters more than 250 ." in {
        val payload = Payload(None, maxCharacterLength, None, "Test", LocalDate.now, BirthRegisterCountry.ENGLAND)
        Json.toJson(payload).validate[Payload].isError shouldBe true
      }

      "return error when firstName contains * characters" in {
        val payload = Payload(None, "*", None, "Test", LocalDate.now, BirthRegisterCountry.ENGLAND)
        Json.toJson(payload).validate[Payload].isError shouldBe true
      }

      "return error when firstName key exists but value is empty" in {
        val payload = Payload(None, "", None, "Test", LocalDate.now, BirthRegisterCountry.ENGLAND)
        Json.toJson(payload).validate[Payload].isError shouldBe true
      }

      "return error when firstName contains a single white space only" in {
        val payload = Payload(None, " ", None, "Test", LocalDate.now, BirthRegisterCountry.ENGLAND)
        Json.toJson(payload).validate[Payload].isError shouldBe true
      }

      "return error when firstName contains multiple white spaces only" in {
        val payload = Payload(None, "     ", None, "Test", LocalDate.now, BirthRegisterCountry.ENGLAND)
        Json.toJson(payload).validate[Payload].isError shouldBe true
      }

      "return error when firstName value is an int" in {
        val jsonObject: JsValue = Json.parse(
          """
            |{
            | "birthReferenceNumber": "123456789",
            | "firstName" : 123,
            | "lastName" : "Smith",
            | "dateOfBirth" : "1997-01-13",
            | "whereBirthRegistered" : "england"
            |}
          """.stripMargin)

        jsonObject.validate[Payload].isError shouldBe true
      }

      "return success when additionalNames contains multiple space separated strings" in {
        val payload = Payload(None, "Test", Some("Shane Seamus McShane "), "Test", LocalDate.now, BirthRegisterCountry.ENGLAND)
        Json.toJson(payload).validate[Payload].isSuccess shouldBe true
      }

      "return success when additionalNames contains valid special characters" in {
        val payload = Payload(None, "Test", Some("&`-'^"), "Test", LocalDate.now, BirthRegisterCountry.ENGLAND)
        Json.toJson(payload).validate[Payload].isSuccess shouldBe true
      }

      "return success when additionalNames contains unicode characters" in {
        val payload = Payload(None, "Test", Some(unicode), "Test", LocalDate.now, BirthRegisterCountry.ENGLAND)
        Json.toJson(payload).validate[Payload].isSuccess shouldBe true
      }

      "return success when additionalNames contains & character" in {
        val payload = Payload(None, "Test", Some("&"), "Test", LocalDate.now, BirthRegisterCountry.ENGLAND)
        Json.toJson(payload).validate[Payload].isSuccess shouldBe true
      }

      "return error when additionalNames value is an int" in {
        val jsonObject: JsValue = Json.parse(
          """
            |{
            | "birthReferenceNumber": "123456789",
            | "firstName" : "John",
            | "additionalNames" : 123,
            | "lastName" : "Smith",
            | "dateOfBirth" : "1997-01-13",
            | "whereBirthRegistered" : "england"
            |}
          """.stripMargin)

        jsonObject.validate[Payload].isError shouldBe true
      }

      "return error when additionalNames contains * characters" in {
        val payload = Payload(None, "Test", Some("*"), "Test", LocalDate.now, BirthRegisterCountry.ENGLAND)
        Json.toJson(payload).validate[Payload].isError shouldBe true
      }

      "return error when additionalNames contains = character" in {
        val payload = Payload(Some("123456789"), "Test", Some("="), "Smith", LocalDate.now, BirthRegisterCountry.ENGLAND)
        Json.toJson(payload).validate[Payload].isSuccess shouldBe false
      }

      "return error when additionalNames contains @ character" in {
        val payload = Payload(Some("123456789"), "John", Some("@"), "Smith", LocalDate.now, BirthRegisterCountry.ENGLAND)
        Json.toJson(payload).validate[Payload].isSuccess shouldBe false
      }

      "return error when additionalNames contains + character" in {
        val payload = Payload(Some("123456789"), "John", Some("+"), "Smith", LocalDate.now, BirthRegisterCountry.ENGLAND)
        Json.toJson(payload).validate[Payload].isSuccess shouldBe false
      }

      "return error when additionalNames contains \u0000 (NULL) character" in {
        val payload = Payload(Some("123456789"), "John", Some("\u0000"), "Smith", LocalDate.now, BirthRegisterCountry.ENGLAND)
        Json.toJson(payload).validate[Payload].isSuccess shouldBe false
      }

      "return error when additionalNames contains double quote character" in {
        val payload = Payload(None, "Test", Some("Johnny\""), "Test", LocalDate.now, BirthRegisterCountry.ENGLAND)
        Json.toJson(payload).validate[Payload].isError shouldBe true
      }

      "return error when additionalNames contains . character" in {
        val payload = Payload(Some("123456789"), "Adam", Some("."), "Smith", LocalDate.now, BirthRegisterCountry.ENGLAND)
        Json.toJson(payload).validate[Payload].isSuccess shouldBe false
      }

      "return error when additionalNames contains special character |" in {
        val payload = Payload(None, "Test", Some("|"), "Test", LocalDate.now, BirthRegisterCountry.ENGLAND)
        Json.toJson(payload).validate[Payload].isError shouldBe true
      }

      "return error when additionalNames contains special character" in {
        val payload = Payload(None, "Test", Some("Johnny-->"), "Test", LocalDate.now, BirthRegisterCountry.ENGLAND)
        Json.toJson(payload).validate[Payload].isError shouldBe true
      }

      "return error when additionalNames contains newline character" in {
        val payload = Payload(None, "Test", Some("Johnny\n"), "Test", LocalDate.now, BirthRegisterCountry.ENGLAND)
        Json.toJson(payload).validate[Payload].isError shouldBe true
      }

      "return error when additionalNames key exists but value is empty" in {
        val payload = Payload(None, "Test", Some(""), "Test", LocalDate.now, BirthRegisterCountry.ENGLAND)
        Json.toJson(payload).validate[Payload].isError shouldBe true
      }

      "return error when additionalNames contains character more than 250" in {
        val payload = Payload(None, "Test", Some(maxCharacterLength), "Test",  LocalDate.now, BirthRegisterCountry.ENGLAND)
        Json.toJson(payload).validate[Payload].isError shouldBe true
      }

      "return error when additionalNames contains a single white space only" in {
        val payload = Payload(None, "Test", Some(" "), "Test", LocalDate.now, BirthRegisterCountry.ENGLAND)
        Json.toJson(payload).validate[Payload].isError shouldBe true
      }

      "return error when additionalNames contains multiple white spaces only" in {
        val payload = Payload(None, "Test", Some("     "), "Test", LocalDate.now, BirthRegisterCountry.ENGLAND)
        Json.toJson(payload).validate[Payload].isError shouldBe true
      }

      "return success when lastName contains unicode characters" in {
        val payload = Payload(None, "Test", None, unicode,  LocalDate.now, BirthRegisterCountry.ENGLAND)
        Json.toJson(payload).validate[Payload].isSuccess shouldBe true
      }

      "return success when lastName contains valid special characters" in {
        val payload = Payload(None, "Test", None, "&`-'^", LocalDate.now, BirthRegisterCountry.ENGLAND)
        Json.toJson(payload).validate[Payload].isSuccess shouldBe true
      }

      "return success when lastName contains & character" in {
        val payload = Payload(None, "Test", None, "Smith&", LocalDate.now, BirthRegisterCountry.ENGLAND)
        Json.toJson(payload).validate[Payload].isSuccess shouldBe true
      }

      "return error when lastName contains * characters" in {
        val payload = Payload(None, "test", None, "*", LocalDate.now, BirthRegisterCountry.SCOTLAND)
        Json.toJson(payload).validate[Payload].isError shouldBe true
      }

      "return error when lastName contains . character" in {
        val payload = Payload(Some("123456789"), "Adam", None, ".", LocalDate.now, BirthRegisterCountry.ENGLAND)
        Json.toJson(payload).validate[Payload].isSuccess shouldBe false
      }

      "return error when lastName contains = character" in {
        val payload = Payload(Some("123456789"), "Test", Some("Jones"), "=", LocalDate.now, BirthRegisterCountry.ENGLAND)
        Json.toJson(payload).validate[Payload].isSuccess shouldBe false
      }

      "return error when lastName contains @ character" in {
        val payload = Payload(Some("123456789"), "John", Some("Jones"), "@", LocalDate.now, BirthRegisterCountry.ENGLAND)
        Json.toJson(payload).validate[Payload].isSuccess shouldBe false
      }

      "return error when lastName contains + character" in {
        val payload = Payload(Some("123456789"), "John", Some("Jones"), "+", LocalDate.now, BirthRegisterCountry.ENGLAND)
        Json.toJson(payload).validate[Payload].isSuccess shouldBe false
      }

      "return error when lastName contains \u0000 (NULL) character" in {
        val payload = Payload(Some("123456789"), "John", Some("Jones"), "\u0000", LocalDate.now, BirthRegisterCountry.ENGLAND)
        Json.toJson(payload).validate[Payload].isSuccess shouldBe false
      }

      "return error when lastName contains newline character" in {
        val payload = Payload(None, "Test", None, "Sm\nith", LocalDate.now, BirthRegisterCountry.ENGLAND)
        Json.toJson(payload).validate[Payload].isError shouldBe true
      }

      "return error when lastName contains character more than 250" in {
        val payload = Payload(None, "Test", None, maxCharacterLength, LocalDate.now, BirthRegisterCountry.ENGLAND)
        Json.toJson(payload).validate[Payload].isError shouldBe true
      }

      "return error when lastName contains invalid special character" in {
        val payload = Payload(None, "Test", None, "Smith/", LocalDate.now, BirthRegisterCountry.ENGLAND)
        Json.toJson(payload).validate[Payload].isError shouldBe true
      }

      "return error when lastName contains invalid character" in {
        val payload = Payload(None, "Test", None, "<!--#EXEC cmd=\\\"ls /\\\"--", LocalDate.now, BirthRegisterCountry.ENGLAND)
        Json.toJson(payload).validate[Payload].isError shouldBe true
      }

      "return error when lastName key exists but value is empty" in {
        val payload = Payload(None, "Test", None, "", LocalDate.now, BirthRegisterCountry.ENGLAND)
        Json.toJson(payload).validate[Payload].isError shouldBe true
      }

      "return error when lastName value is an int" in {
        val jsonObject: JsValue = Json.parse(
          """
            |{
            | "birthReferenceNumber": "123456789",
            | "firstName" : "John",
            | "lastName" : 123,
            | "dateOfBirth" : "1997-01-13",
            | "whereBirthRegistered" : "england"
            |}
          """.stripMargin)

        jsonObject.validate[Payload].isError shouldBe true
      }

      "return error when lastName contains a single white space only" in {
        val payload = Payload(None, "Test", None, " ", LocalDate.now, BirthRegisterCountry.ENGLAND)
        Json.toJson(payload).validate[Payload].isError shouldBe true
      }

      "return error when lastName contains multiple white spaces only" in {
        val payload = Payload(None, "Test", None, "      ", LocalDate.now, BirthRegisterCountry.ENGLAND)
        Json.toJson(payload).validate[Payload].isError shouldBe true
      }

      "return success when birthReferenceNumber key doesn't exist" in {
        val jsonObject: JsValue = Json.parse(
          """
            |{
            | "firstName" : "John",
            | "lastName" : "Smith",
            | "dateOfBirth" : "1997-01-13",
            | "whereBirthRegistered" : "england"
            | }
          """.stripMargin)

        jsonObject.validate[Payload].isSuccess shouldBe true
      }

      "return error when birthReferenceNumber value contains characters" in {
        val payload = Payload(Some("12345678a"), "Test", None, "Test", LocalDate.now, BirthRegisterCountry.ENGLAND)
        Json.toJson(payload).validate[Payload].isError shouldBe true
      }

      "return error when birthReferenceNumber value exists and is alphanumeric" in {
        val payload = Payload(Some("ab12CD263"), "Test", None, "Test", LocalDate.now, BirthRegisterCountry.ENGLAND)
        Json.toJson(payload).validate[Payload].isError shouldBe true
      }

      "return error when birthReferenceNumber value exists and contains a hyphen and underscore" in {
        val payload = Payload(Some("12_34-456"), "Test", None, "Test", LocalDate.now, BirthRegisterCountry.ENGLAND)
        Json.toJson(payload).validate[Payload].isError shouldBe true
      }

      "return error when whereBirthRegistered is number" in {
        val jsonObject: JsValue = Json.parse(
          """
            |{
            | "birthReferenceNumber": "123456789",
            | "firstName" : "John",
            | "lastName" : "Smith",
            | "dateOfBirth" : "1997-01-13",
            | "whereBirthRegistered" : 123
            |}
          """.stripMargin)

        jsonObject.validate[Payload].isError shouldBe true
      }

      "return error when whereBirthRegistered value is not from valid enum values" in {
        val jsonObject: JsValue = Json.parse(
          """
            |{
            | "birthReferenceNumber": "123456789",
            | "firstName" : "John",
            | "lastName" : "Smith",
            | "dateOfBirth" : "1997-01-13",
            | "whereBirthRegistered" : "notvalid"
            |}
          """.stripMargin)

        jsonObject.validate[Payload].isError shouldBe true
      }

      "return error when birthReferenceNumber key exists but value is empty" in {
        val payload = Payload(Some(""), "Test", None, "Test", LocalDate.now, BirthRegisterCountry.ENGLAND)
        Json.toJson(payload).validate[Payload].isError shouldBe true
      }

      "return error when birthReferenceNumber value exists but is an invalid format" in {
        val payload = Payload(Some("1*3456789"), "Test", None, "Test", LocalDate.now, BirthRegisterCountry.ENGLAND)
        Json.toJson(payload).validate[Payload].isError shouldBe true
      }

      "return error when dateOfBirth key exists but value is empty" in {
        val jsonObject: JsValue = Json.parse(
          """
            |{
            | "birthReferenceNumber": "123456789",
            | "firstName" : "John",
            | "lastName" : "Smith",
            | "dateOfBirth" : "",
            | "whereBirthRegistered" : "england"
            |}
          """.stripMargin)

        jsonObject.validate[Payload].isError shouldBe true
      }

      "return error when dateOfBirth value is invalid" in {
        val jsonObject: JsValue = Json.parse(
          """
            |{
            | "birthReferenceNumber": "123456789",
            | "firstName" : "John",
            | "lastName" : "Smith",
            | "dateOfBirth" : "1234567890",
            | "whereBirthRegistered" : "england"
            |}
          """.stripMargin)

        jsonObject.validate[Payload].isError shouldBe true
      }

      "return error when dateOfBirth has 0000 for year" in {
        val jsonObject: JsValue = Json.parse(
          """
            |{
            | "birthReferenceNumber": "123456789",
            | "firstName" : "John",
            | "lastName" : "Smith",
            | "dateOfBirth" : "0000-01-10",
            | "whereBirthRegistered" : "england"
            |}
          """.stripMargin)

        jsonObject.validate[Payload].isError shouldBe true
      }

      "return error when dateOfBirth only has a year" in {
        val jsonObject: JsValue = Json.parse(
          """
            |{
            | "birthReferenceNumber": "123456789",
            | "firstName" : "John",
            | "lastName" : "Smith",
            | "dateOfBirth" : "2016",
            | "whereBirthRegistered" : "england"
            |}
          """.stripMargin)

        jsonObject.validate[Payload].isError shouldBe true
      }
    }
  }

}
