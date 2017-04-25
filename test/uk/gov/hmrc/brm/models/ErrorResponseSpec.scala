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

package uk.gov.hmrc.brm.models

import akka.stream.Materializer
import play.api.Play
import play.api.http.Status._
import uk.gov.hmrc.brm.models.brm.ErrorResponses
import uk.gov.hmrc.brm.utils.MockErrorResponses
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

class ErrorResponseSpec extends UnitSpec with WithFakeApplication {

  "ErrorResponses" should {

    implicit lazy val materializer = Play.current.injector.instanceOf[Materializer]

    "return BadRequest with generic body when key doesn't exist" in {
      val response = await(ErrorResponses.getHttpResponse("firstNameInvalid", ""))
      bodyOf(response) shouldBe MockErrorResponses.BAD_REQUEST.json
      response.header.status shouldBe BAD_REQUEST
    }

    "return BadRequest with specific body when firstName is invalid" in {
      val response = await(ErrorResponses.getHttpResponse("firstName", ""))
      bodyOf(response) shouldBe MockErrorResponses.INVALID_FIRSTNAME.json
      response.header.status shouldBe BAD_REQUEST
    }

//    "return BadRequest with generic body when firstName key is missing" in {
//      val response = await(ErrorResponses.getHttpResponse("firstName", "error.path.missing"))
//      bodyOf(response) shouldBe MockErrorResponses.BAD_REQUEST.json
//      response.header.status shouldBe BAD_REQUEST
//    }
//
//    "return BadRequest with specific body when lastName is invalid" in {
//      val response = await(ErrorResponses.getHttpResponse("firstName", ""))
//      bodyOf(response) shouldBe MockErrorResponses.INVALID_LASTNAME.json
//      response.header.status shouldBe BAD_REQUEST
//    }
//
//    "return BadRequest with generic body when lastName key is missing" in {
//      val response = await(ErrorResponses.getHttpResponse("firstName", "error.path.missing"))
//      bodyOf(response) shouldBe MockErrorResponses.BAD_REQUEST.json
//      response.header.status shouldBe BAD_REQUEST
//    }

  }

}
