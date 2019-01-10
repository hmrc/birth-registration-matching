/*
 * Copyright 2019 HM Revenue & Customs
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

import akka.stream.Materializer
import org.scalatestplus.play.OneAppPerSuite
import play.api.Play
import play.api.test.Helpers.{contentAsJson, _}
import uk.gov.hmrc.brm.models.brm._
import uk.gov.hmrc.play.test.UnitSpec

class ErrorResponseSpec extends UnitSpec with OneAppPerSuite {

  "ErrorResponses" should {

    implicit lazy val materializer = Play.current.injector.instanceOf[Materializer]

    "return BadRequest with generic body when key doesn't exist" in {
      val response = await(ErrorResponses.getHttpResponse("firstNameInvalid", ""))
      (contentAsJson(response) \ "code").as[String] shouldBe BadRequest.code
      (contentAsJson(response) \ "message").as[String] shouldBe BadRequest.message
      response.header.status shouldBe BAD_REQUEST
    }

    "return BadRequest with specific body when firstName is invalid" in {
      val response = await(ErrorResponses.getHttpResponse("firstName", ""))
      (contentAsJson(response) \ "code").as[String] shouldBe InvalidFirstName.code
      (contentAsJson(response) \ "message").as[String] shouldBe InvalidFirstName.message
      response.header.status shouldBe BAD_REQUEST
    }

    "return BadRequest with generic body when firstName key is missing" in {
      val response = await(ErrorResponses.getHttpResponse("firstName", "error.path.missing"))
      (contentAsJson(response) \ "code").as[String] shouldBe BadRequest.code
      (contentAsJson(response) \ "message").as[String] shouldBe BadRequest.message
      response.header.status shouldBe BAD_REQUEST
    }

    "return BadRequest with specific body when lastName is invalid" in {
      val response = await(ErrorResponses.getHttpResponse("lastName", ""))
      (contentAsJson(response) \ "code").as[String] shouldBe InvalidLastName.code
      (contentAsJson(response) \ "message").as[String] shouldBe InvalidLastName.message
      response.header.status shouldBe BAD_REQUEST
    }

    "return BadRequest with generic body when lastName key is missing" in {
      val response = await(ErrorResponses.getHttpResponse("lastName", "error.path.missing"))
      (contentAsJson(response) \ "code").as[String] shouldBe BadRequest.code
      (contentAsJson(response) \ "message").as[String] shouldBe BadRequest.message
      response.header.status shouldBe BAD_REQUEST
    }

    "return BadRequest with specific body when dateOfBirth is invalid" in {
      val response = await(ErrorResponses.getHttpResponse("dateOfBirth", ""))
      (contentAsJson(response) \ "code").as[String] shouldBe InvalidDateOfBirth.code
      (contentAsJson(response) \ "message").as[String] shouldBe InvalidDateOfBirth.message
      response.header.status shouldBe BAD_REQUEST
    }

    "return BadRequest with generic body when dateOfBirth key is missing" in {
      val response = await(ErrorResponses.getHttpResponse("dateOfBirth", "error.path.missing"))
      (contentAsJson(response) \ "code").as[String] shouldBe BadRequest.code
      (contentAsJson(response) \ "message").as[String] shouldBe BadRequest.message
      response.header.status shouldBe BAD_REQUEST
    }

    "return BadRequest with specific body when birthReferenceNumber is invalid" in {
      val response = await(ErrorResponses.getHttpResponse("birthReferenceNumber", ""))
      (contentAsJson(response) \ "code").as[String] shouldBe InvalidBirthReferenceNumber.code
      (contentAsJson(response) \ "message").as[String] shouldBe InvalidBirthReferenceNumber.message
      response.header.status shouldBe BAD_REQUEST
    }

    "return Forbidden with specific body when whereBirthRegistered is invalid" in {
      val response = await(ErrorResponses.getHttpResponse("whereBirthRegistered", ""))
      (contentAsJson(response) \ "code").as[String] shouldBe InvalidWhereBirthRegistered.code
      (contentAsJson(response) \ "message").as[String] shouldBe InvalidWhereBirthRegistered.message
      response.header.status shouldBe FORBIDDEN
    }

    "return BadRequest with generic body when whereBirthRegistered key is missing" in {
      val response = await(ErrorResponses.getHttpResponse("whereBirthRegistered", "error.path.missing"))
      (contentAsJson(response) \ "code").as[String] shouldBe BadRequest.code
      (contentAsJson(response) \ "message").as[String] shouldBe BadRequest.message
      response.header.status shouldBe BAD_REQUEST
    }

  }

}
