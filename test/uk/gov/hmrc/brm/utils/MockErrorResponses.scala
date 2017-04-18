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

import play.api.libs.json.Json

/**
  * Created by mew on 10/04/2017.
  */
object MockErrorResponses {

    case class ErrorResponse(code: String, message: String) {
      def json = Json.parse(
        s"""
           |{
           |  "code": "$code",
           |  "message": "$message"
           |}
     """.stripMargin).toString()
    }

    val CONNECTION_DOWN = ErrorResponse("GRO_CONNECTION_DOWN", "Connection to GRO is down")
    val NRS_CONNECTION_DOWN = ErrorResponse("SERVICE_UNAVAILABLE", "Dependent systems are currently not responding")
    val BAD_REQUEST = ErrorResponse("BAD_REQUEST", "Invalid payload provided")
    val TEAPOT = ErrorResponse("TEAPOT", "Invalid argument sent to GRO")
    val NOT_FOUND = ErrorResponse("NOT_FOUND", "Resource not found")
    val GATEWAY_TIMEOUT = ErrorResponse("GATEWAY_TIMEOUT", "Connection to GRO timed out")
    val CERTIFICATE_INVALID = ErrorResponse("INVALID_CERTIFICATE", "TLS certificate was either not provided or was invalid")
    val UNKNOWN_ERROR = ErrorResponse("UNKNOWN_ERROR", "An unknown exception has been thrown")
}
