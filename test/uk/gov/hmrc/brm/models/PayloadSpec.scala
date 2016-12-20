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
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.brm.models.brm.Payload
import uk.gov.hmrc.brm.utils.BirthRegisterCountry
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

class PayloadSpec extends UnitSpec with WithFakeApplication {

  "Payload" should {

    /**
      * - Should
      * - instantiate an instance of payload
      * - convert to json
      */

    "instantiate an instance of payload" in {
      val payload = Payload(
        birthReferenceNumber = Some("123456789"),
        firstName = "John",
        lastName = "Smith",
        dateOfBirth = new LocalDate("1997-01-13"),
        whereBirthRegistered = BirthRegisterCountry.ENGLAND
      )

      payload shouldBe a[Payload]
      payload.birthReferenceNumber shouldBe Some("123456789")
      payload.firstName shouldBe "John"
      payload.lastName shouldBe "Smith"
      payload.whereBirthRegistered shouldBe BirthRegisterCountry.withName("england")
    }

    "convert to json" in {
      val payload = Payload(
        birthReferenceNumber = Some("123456789"),
        firstName = "John",
        lastName = "Smith",
        dateOfBirth = new LocalDate("1997-01-13"),
        whereBirthRegistered = BirthRegisterCountry.ENGLAND
      )

      Json.toJson(payload) shouldBe Json.parse(
        """
          |{
          | "birthReferenceNumber": "123456789",
          | "firstName" : "John",
          | "lastName" : "Smith",
          | "dateOfBirth" : "1997-01-13",
          | "whereBirthRegistered" : "england"
          |}
        """.stripMargin)
    }

    "return success when complete and valid record exists" in {
      val jsonObject: JsValue = Json.parse(
        """
          |{
          | "birthReferenceNumber": "123456789",
          | "firstName" : "John",
          | "lastName" : "Smith",
          | "dateOfBirth" : "1997-01-13",
          | "whereBirthRegistered" : "england"
          |}
        """.stripMargin)

      jsonObject.validate[Payload].isSuccess shouldBe true
    }

    "return error when firstName contains special character" in {
      val jsonObject: JsValue = Json.parse(
        """
          |{
          | "birthReferenceNumber": "123456789",
          | "firstName" : "John-->",
          | "lastName" : "Smith",
          | "dateOfBirth" : "1997-01-13",
          | "whereBirthRegistered" : "england"
          |}
        """.stripMargin)

      jsonObject.validate[Payload].isError shouldBe true
    }

    "return error when firstName contains newline character" in {
      val jsonObject: JsValue = Json.parse(
        """
          |{
          | "birthReferenceNumber": "123456789",
          | "firstName" : "John\n",
          | "lastName" : "Smith",
          | "dateOfBirth" : "1997-01-13",
          | "whereBirthRegistered" : "england"
          |}
        """.stripMargin)

      jsonObject.validate[Payload].isError shouldBe true
    }

    "return error when firstName contains double quote character." in {
      val jsonObject: JsValue = Json.parse(
        """
          |{
          | "birthReferenceNumber": "123456789",
          | "firstName" : "John\"",
          | "lastName" : "Smith",
          | "dateOfBirth" : "1997-01-13",
          | "whereBirthRegistered" : "england"
          |}
        """.stripMargin)

      jsonObject.validate[Payload].isError shouldBe true
    }


    "return error when lastName contains newline character" in {
      val jsonObject: JsValue = Json.parse(
        """
          |{
          | "birthReferenceNumber": "123456789",
          | "firstName" : "John",
          | "lastName" : "Sm\nith",
          | "dateOfBirth" : "1997-01-13",
          | "whereBirthRegistered" : "england"
          |}
        """.stripMargin)

      jsonObject.validate[Payload].isError shouldBe true
    }

    "return error when lastname contains special character" in {
      val jsonObject: JsValue = Json.parse(
        """
          |{
          | "birthReferenceNumber": "123456789",
          | "firstName" : "John",
          | "lastName" : "Smith/",
          | "dateOfBirth" : "1997-01-13",
          | "whereBirthRegistered" : "england"
          |}
        """.stripMargin)

      jsonObject.validate[Payload].isError shouldBe true
    }

    "return error when lastname contains invalid character" in {
      val jsonObject: JsValue = Json.parse(
        """
          |{
          | "birthReferenceNumber": "123456789",
          | "firstName" : "John",
          | "lastName" : "<!--#EXEC cmd=\"ls /\"--",
          | "dateOfBirth" : "1997-01-13",
          | "whereBirthRegistered" : "england"
          |}
        """.stripMargin)

      jsonObject.validate[Payload].isError shouldBe true
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

    "return success when birthReferenceNumber value exists and is alphanumeric" in {
      val jsonObject: JsValue = Json.parse(
        """
          |{
          | "birthReferenceNumber": "ab12CD263",
          | "firstName" : "John",
          | "lastName" : "Smith",
          | "dateOfBirth" : "1997-01-13",
          | "whereBirthRegistered" : "england"
          | }
        """.stripMargin)

      jsonObject.validate[Payload].isSuccess shouldBe true
    }

    "return success when birthReferenceNumber value exists and contains a hyphen and underscore" in {
      val jsonObject: JsValue = Json.parse(
        """
          |{
          | "birthReferenceNumber": "12_34-456",
          | "firstName" : "John",
          | "lastName" : "Smith",
          | "dateOfBirth" : "1997-01-13",
          | "whereBirthRegistered" : "england"
          | }
        """.stripMargin)

      jsonObject.validate[Payload].isSuccess shouldBe true
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
      val jsonObject: JsValue = Json.parse(
        """
          |{
          | "birthReferenceNumber": "",
          | "firstName" : "John",
          | "lastName" : "Smith",
          | "dateOfBirth" : "1997-01-13",
          | "whereBirthRegistered" : "england"
          | }
        """.stripMargin)

      jsonObject.validate[Payload].isError shouldBe true
    }

    "return error when birthReferenceNumber value exists but is an invalid format" in {
      val jsonObject: JsValue = Json.parse(
        """
          |{
          | "birthReferenceNumber": "1*3456789",
          | "firstName" : "John",
          | "lastName" : "Smith",
          | "dateOfBirth" : "1997-01-13",
          | "whereBirthRegistered" : "england"
          | }
        """.stripMargin)

      jsonObject.validate[Payload].isError shouldBe true
    }

    "return error when firstName key exists but value is empty" in {
      val jsonObject: JsValue = Json.parse(
        """
          |{
          | "birthReferenceNumber": "123456789",
          | "firstName" : "",
          | "lastName" : "Smith",
          | "dateOfBirth" : "1997-01-13",
          | "whereBirthRegistered" : "england"
          |}
        """.stripMargin)

      jsonObject.validate[Payload].isError shouldBe true
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

    "return error when lastName key exists but value is empty" in {
      val jsonObject: JsValue = Json.parse(
        """
          |{
          | "birthReferenceNumber": "123456789",
          | "firstName" : "John",
          | "lastName" : "",
          | "dateOfBirth" : "1997-01-13",
          | "whereBirthRegistered" : "england"
          |}
        """.stripMargin)

      jsonObject.validate[Payload].isError shouldBe true
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
  }

}
