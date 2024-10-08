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

package uk.gov.hmrc.brm.models

import play.api.Play.materializer
import play.api.libs.json.Json
import play.api.test.Helpers._
import uk.gov.hmrc.brm.models.brm._
import uk.gov.hmrc.brm.utils.BaseUnitSpec

class ErrorResponseSpec extends BaseUnitSpec {

  "ErrorResponses" should {

    "return BadRequest with generic body when key doesn't exist" in {
      val response = ErrorResponses.getHttpResponse("firstNameInvalid", "")
      (Json.parse(response.body.consumeData.futureValue.utf8String) \ "code").as[String]    shouldBe BadRequest.code
      (Json.parse(response.body.consumeData.futureValue.utf8String) \ "message").as[String] shouldBe BadRequest.message
      response.header.status                                                                shouldBe BAD_REQUEST
    }

    "return BadRequest with specific body when firstName is invalid" in {
      val response = ErrorResponses.getHttpResponse("firstName", "")
      (Json.parse(response.body.consumeData.futureValue.utf8String) \ "code").as[String] shouldBe InvalidFirstName.code
      (Json.parse(response.body.consumeData.futureValue.utf8String) \ "message")
        .as[String]                                                                      shouldBe InvalidFirstName.message
      response.header.status                                                             shouldBe BAD_REQUEST
    }

    "return BadRequest with generic body when firstName key is missing" in {
      val response = ErrorResponses.getHttpResponse("firstName", "error.path.missing")
      (Json.parse(response.body.consumeData.futureValue.utf8String) \ "code").as[String]    shouldBe BadRequest.code
      (Json.parse(response.body.consumeData.futureValue.utf8String) \ "message").as[String] shouldBe BadRequest.message
      response.header.status                                                                shouldBe BAD_REQUEST
    }

    "return BadRequest with specific body when lastName is invalid" in {
      val response = ErrorResponses.getHttpResponse("lastName", "")
      (Json.parse(response.body.consumeData.futureValue.utf8String) \ "code").as[String] shouldBe InvalidLastName.code
      (Json.parse(response.body.consumeData.futureValue.utf8String) \ "message")
        .as[String]                                                                      shouldBe InvalidLastName.message
      response.header.status                                                             shouldBe BAD_REQUEST
    }

    "return BadRequest with generic body when lastName key is missing" in {
      val response = ErrorResponses.getHttpResponse("lastName", "error.path.missing")
      (Json.parse(response.body.consumeData.futureValue.utf8String) \ "code").as[String]    shouldBe BadRequest.code
      (Json.parse(response.body.consumeData.futureValue.utf8String) \ "message").as[String] shouldBe BadRequest.message
      response.header.status                                                                shouldBe BAD_REQUEST
    }

    "return BadRequest with specific body when dateOfBirth is invalid" in {
      val response = ErrorResponses.getHttpResponse("dateOfBirth", "")
      (Json.parse(response.body.consumeData.futureValue.utf8String) \ "code")
        .as[String]          shouldBe InvalidDateOfBirth.code
      (Json.parse(response.body.consumeData.futureValue.utf8String) \ "message")
        .as[String]          shouldBe InvalidDateOfBirth.message
      response.header.status shouldBe BAD_REQUEST
    }

    "return BadRequest with generic body when dateOfBirth key is missing" in {
      val response = ErrorResponses.getHttpResponse("dateOfBirth", "error.path.missing")
      (Json.parse(response.body.consumeData.futureValue.utf8String) \ "code").as[String]    shouldBe BadRequest.code
      (Json.parse(response.body.consumeData.futureValue.utf8String) \ "message").as[String] shouldBe BadRequest.message
      response.header.status                                                                shouldBe BAD_REQUEST
    }

    "return BadRequest with specific body when birthReferenceNumber is invalid" in {
      val response = ErrorResponses.getHttpResponse("birthReferenceNumber", "")
      (Json.parse(response.body.consumeData.futureValue.utf8String) \ "code")
        .as[String]          shouldBe InvalidBirthReferenceNumber.code
      (Json.parse(response.body.consumeData.futureValue.utf8String) \ "message")
        .as[String]          shouldBe InvalidBirthReferenceNumber.message
      response.header.status shouldBe BAD_REQUEST
    }

    "return Forbidden with specific body when whereBirthRegistered is invalid" in {
      val response = ErrorResponses.getHttpResponse("whereBirthRegistered", "")
      (Json.parse(response.body.consumeData.futureValue.utf8String) \ "code")
        .as[String]          shouldBe InvalidWhereBirthRegistered.code
      (Json.parse(response.body.consumeData.futureValue.utf8String) \ "message")
        .as[String]          shouldBe InvalidWhereBirthRegistered.message
      response.header.status shouldBe FORBIDDEN
    }

    "return BadRequest with generic body when whereBirthRegistered key is missing" in {
      val response = ErrorResponses.getHttpResponse("whereBirthRegistered", "error.path.missing")
      (Json.parse(response.body.consumeData.futureValue.utf8String) \ "code").as[String]    shouldBe BadRequest.code
      (Json.parse(response.body.consumeData.futureValue.utf8String) \ "message").as[String] shouldBe BadRequest.message
      response.header.status                                                                shouldBe BAD_REQUEST
    }

  }

}
