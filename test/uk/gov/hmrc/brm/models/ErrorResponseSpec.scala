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

import play.api.libs.json.{JsObject, JsString, JsValue, Json}
import uk.gov.hmrc.brm.models.brm.ErrorResponse
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

class ErrorResponseSpec extends UnitSpec with WithFakeApplication {

  /**
    * - Should
    * Return a JSON error response when an id is invalid
    * Return a JSON error response when given an error code of 5
    * Return a JSON error response when given an error code of 6
    * Return a default JSON error response when an invalid error code is given
    */

  "ErrorResponse" should {
    "get the correct error when sent error code 5" in {

      val code = 5
      val response = ErrorResponse.getErrorResponseByErrorCode(code)

      response shouldBe a[JsObject]
      (response \ "code").as[JsString].value should be("5")
      (response \ "status").as[JsString].value should be("400")
      (response \ "title").as[JsString].value should be("ID invalid")
      (response \ "details").as[JsString].value should be("The id you supplied is invalid")
      (response \ "about").as[JsString].value should be("http://http://htmlpreview.github.io/?https://github.com/hmrc/birth-registration-matching/blob/master/api-documents/api.html")
    }

    "get the correct error when sent error code 6" in {

      val code = 6
      val response = ErrorResponse.getErrorResponseByErrorCode(code)

      response shouldBe a[JsObject]
      (response \ "code").as[JsString].value should be("6")
      (response \ "status").as[JsString].value should be("400")
      (response \ "title").as[JsString].value should be("ID value empty")
      (response \ "details").as[JsString].value should be("You must supply an id")
      (response \ "about").as[JsString].value should be("http://http://htmlpreview.github.io/?https://github.com/hmrc/birth-registration-matching/blob/master/api-documents/api.html")
    }

    "get the correct error when sent error code 145" in {

      val code = 145
      val response = ErrorResponse.getErrorResponseByErrorCode(code)

      response shouldBe a[JsObject]
      (response \ "code").as[JsString].value should be("145")
      (response \ "status").as[JsString].value should be("400")
      (response \ "title").as[JsString].value should be("Headers invalid")
      (response \ "details").as[JsString].value should be("The headers you supplied are invalid")
      (response \ "about").as[JsString].value should be("http://http://htmlpreview.github.io/?https://github.com/hmrc/birth-registration-matching/blob/master/api-documents/api.html")
    }

    "get default JSON error response when an invalid error code is given" in {

      val code = 123456789
      val response = ErrorResponse.getErrorResponseByErrorCode(code)

      response shouldBe a[JsObject]
      (response \ "details").as[JsString].value should be("something is wrong")
    }

  }

}
