/*
 * Copyright 2020 HM Revenue & Customs
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
    val BAD_REQUEST = ErrorResponse("BAD_REQUEST", "Provided request is invalid.")
    val TEAPOT = ErrorResponse("TEAPOT", "Invalid argument sent to GRO")
    val NOT_FOUND = ErrorResponse("NOT_FOUND", "Resource not found")
    val GATEWAY_TIMEOUT = ErrorResponse("GATEWAY_TIMEOUT", "Connection to GRO timed out")
    val CERTIFICATE_INVALID = ErrorResponse("INVALID_CERTIFICATE", "TLS certificate was either not provided or was invalid")
    val UNKNOWN_ERROR = ErrorResponse("UNKNOWN_ERROR", "An unknown exception has been thrown")
    val INVALID_BIRTH_REFERENCE_NUMBER = ErrorResponse("INVALID_BIRTH_REFERENCE_NUMBER", "The birth reference number does not meet the required length")
    val INVALID_FIRSTNAME = ErrorResponse("INVALID_FIRSTNAME", "Provided firstName is invalid.")
    val INVALID_ADDITIONALNAMES = ErrorResponse("INVALID_ADDITIONALNAMES", "Provided additionalNames are invalid.")
    val INVALID_LASTNAME = ErrorResponse("INVALID_LASTNAME", "Provided lastName is invalid.")
    val INVALID_DATE_OF_BIRTH = ErrorResponse("INVALID_DATE_OF_BIRTH", "Provided dateOfBirth is invalid.")
    val INVALID_WHERE_BIRTH_REGISTERED = ErrorResponse("INVALID_WHERE_BIRTH_REGISTERED", "Provided Country is invalid.")
    val INVALID_AUDITSOURCE = ErrorResponse("INVALID_AUDITSOURCE", "Provided Audit-Source is invalid.")
    val INVALID_ACCEPT_HEADER = ErrorResponse("INVALID_ACCEPT_HEADER", "Accept header is invalid.")
    val INVALID_CONTENT_TYPE = ErrorResponse("INVALID_CONTENT_TYPE", "Accept header is invalid.")

}
