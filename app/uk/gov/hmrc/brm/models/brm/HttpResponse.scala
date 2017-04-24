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

package uk.gov.hmrc.brm.models.brm

import play.api.data.validation.ValidationError
import play.api.http.Status
import play.api.libs.json.{JsPath, Json}

trait HttpResponseBody {
  val httpCode: Int
  val code: String
  val message: String

  def errorBody(code: String, message: String) = Json.parse(
    s"""
       |{
       |  "code": "$code",
       |  "message": "$message"
       |}
     """.stripMargin).toString()

  def toJson(): String = {
    errorBody(code, message)
  }
}

trait HttpResponse {

  def getHttpResponse(key: String, error: String): String

  def getErrorResponseByField(field: Seq[(JsPath, Seq[ValidationError])]): String = {

    val fields = field.map { case (key, validationError) =>
      (key.toString.stripPrefix("/"), validationError.head.message)
    }

    getHttpResponse(fields.head._1, fields.head._2)
  }
}

object ErrorResponseBody extends HttpResponse {

  def getHttpResponse(key: String, error: String): String = (key, error) match {
    case ("firstName", _) if error != "error.path.missing" =>
      InvalidFirstName().toJson()
    case ("lastName", _) if error != "error.path.missing" =>
      InvalidLastName().toJson()
    case ("dateOfBirth", _) if error != "error.path.missing" =>
      InvalidDateOfBirth().toJson()
    case (_, _) =>
      ""
  }

  def getHttpResponse(response: HttpResponseBody): String = {
    response.toJson()
  }

}

case class DefaultResponse(
                            httpCode: Int = Status.BAD_REQUEST,
                            code: String = "",
                            message: String = ""
                          ) extends HttpResponseBody

case class InvalidFirstName(
                             httpCode: Int = Status.BAD_REQUEST,
                             code: String = "INVALID_FIRSTNAME",
                             message: String = "Provided firstName is invalid"
                           ) extends HttpResponseBody

case class InvalidLastName(
                            httpCode: Int = Status.BAD_REQUEST,
                            code: String = "INVALID_LASTNAME",
                            message: String = "Provided lastName is invalid"
                          ) extends HttpResponseBody

case class InvalidDateOfBirth(
                               httpCode: Int = Status.BAD_REQUEST,
                               code: String = "INVALID_DATE_OF_BIRTH",
                               message: String = "Provided dateOfBirth is invalid"
                             ) extends HttpResponseBody

case class InvalidBirthReferenceNumber(
                               httpCode: Int = Status.BAD_REQUEST,
                               code: String = "INVALID_BIRTH_REFERENCE_NUMBER",
                               message: String = "The birth reference number does not meet the required length"
                             ) extends HttpResponseBody

case class InvalidAuditSource(
                               httpCode: Int = Status.NOT_ACCEPTABLE,
                               code: String = "INVALID_AUDITSOURCE",
                               message: String = "Provided Audit-Source is invalid"
                             ) extends HttpResponseBody

case class InvalidAcceptHeader(
                               httpCode: Int = Status.NOT_ACCEPTABLE,
                               code: String = "INVALID_ACCEPT_HEADER",
                               message: String = "Accept header is invalid"
                             ) extends HttpResponseBody