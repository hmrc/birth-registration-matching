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
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

class ErrorResponseSpec extends UnitSpec with WithFakeApplication {

  /**
    * - Should
    * Return a JSON error response when an id is invalid
    * Return a JSON error response when given an error code of 5
    * Return a JSON error response when given an error code of 6
    * Return a default JSON error response when an invalid error code is given
    */

  "ErrorResponses" should {

    implicit lazy val materializer = Play.current.injector.instanceOf[Materializer]

    "return BadRequest with empty body when key doesn't exist" in {

      val response = await(ErrorResponses.handle("firstNameInvalid", ""))
      bodyOf(response) shouldBe empty
      response.header.status shouldBe BAD_REQUEST
    }

  }

}
