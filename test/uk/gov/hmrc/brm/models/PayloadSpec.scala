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
import play.api.libs.json.Json
import uk.gov.hmrc.brm.utils.BirthRegisterCountry
import uk.gov.hmrc.brm.utils.BirthRegisterCountry._
import uk.gov.hmrc.play.test.UnitSpec

/**
  * Created by chrisianson on 02/08/16.
  */
class PayloadSpec extends UnitSpec {

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
  }

}
