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
import play.api.mvc.Results._
import play.api.mvc.{Result, Results}


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

  def status = {
    Results.Status(httpCode).apply(errorBody(code, message))
  }

}

trait HttpResponse {

  def getHttpResponse(key: String, error: String): Result

  def getErrorResponseByField(field: Seq[(JsPath, Seq[ValidationError])]): Result = {

    val fields = field.map { case (key, validationError) =>
      (key.toString.stripPrefix("/"), validationError.head.message)
    }

    getHttpResponse(fields.head._1, fields.head._2)
  }
}

object CustomErrorResponse extends HttpResponse {

  def getHttpResponse(key: String, error: String): Result = {
    ErrorResponses.handle(key, error)
  }

}

object InvalidFirstName extends HttpResponseBody {
  override val httpCode: Int = Status.BAD_REQUEST
  override val code: String = "INVALID_FIRSTNAME"
  override val message: String = "Provided firstName is invalid."
}

object InvalidLastName extends HttpResponseBody {
  override val httpCode: Int = Status.BAD_REQUEST
  override val code: String = "INVALID_LASTNAME"
  override val message: String = "Provided lastName is invalid."
}

object InvalidDateOfBirth extends HttpResponseBody {
  override val httpCode: Int = Status.BAD_REQUEST
  override val code: String = "INVALID_DATE_OF_BIRTH"
  override val message: String = "Provided dateOfBirth is invalid."
}

object InvalidBirthReferenceNumber extends HttpResponseBody {
  override val httpCode: Int = Status.BAD_REQUEST
  override val code: String = "INVALID_BIRTH_REFERENCE_NUMBER"
  override val message: String = "The birth reference number does not meet the required length"
}

object InvalidWhereBirthRegistered extends HttpResponseBody {
  override val httpCode: Int = Status.FORBIDDEN
  override val code: String = "INVALID_WHERE_BIRTH_REGISTERED"
  override val message: String = "Provided Country is invalid."
}

object InvalidAuditSource extends HttpResponseBody {
  override val httpCode: Int = Status.UNAUTHORIZED
  override val code: String = "INVALID_AUDITSOURCE"
  override val message: String = "Provided Audit-Source is invalid."
}

object InvalidAcceptHeader extends HttpResponseBody {
  override val httpCode: Int = Status.NOT_ACCEPTABLE
  override val code: String = "INVALID_ACCEPT_HEADER"
  override val message: String = "Accept header is invalid."
}

object InvalidContentType extends HttpResponseBody {
  override val httpCode: Int = Status.NOT_ACCEPTABLE
  override val code: String = "INVALID_CONTENT_TYPE"
  override val message: String = "Accept header is invalid."
}

object ErrorResponses {

  type ErrorResponses = List[(String, HttpResponseBody)]

  def handle(key: String, error: String) = {
    error match {
      case "error.path.missing" =>
        BadRequest("")
      case e =>
        errors.filter(x => x._1.equals(key)) match {
          case head :: tail =>
            head._2.status
          case _ =>
            BadRequest("")
        }
    }
  }

  protected val errors: ErrorResponses = List(
    ("birthReferenceNumber", InvalidBirthReferenceNumber),
    ("firstName", InvalidFirstName),
    ("lastName", InvalidLastName),
    ("dateOfBirth", InvalidDateOfBirth),
    ("whereBirthRegistered", InvalidWhereBirthRegistered)
  )

}
