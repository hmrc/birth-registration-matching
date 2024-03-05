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

package uk.gov.hmrc.brm.models.brm

import java.time.LocalDate
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json.Writes._
import play.api.libs.json._
import uk.gov.hmrc.brm.metrics.{EnglandAndWalesBirthRegisteredCountMetrics, InvalidBirthRegisteredCountMetrics, NorthernIrelandBirthRegisteredCountMetrics, ScotlandBirthRegisteredCountMetrics}
import uk.gov.hmrc.brm.utils.BirthRegisterCountry
import uk.gov.hmrc.brm.utils.ReadsUtil.validLocalDateReads
import uk.gov.hmrc.brm.utils.BirthRegisterCountry.{birthRegisterReads, birthRegisterWrites, apply => _}

case class Payload(
  birthReferenceNumber: Option[String] = None,
  private val _firstName: String,
  private val _additionalNames: Option[String] = None,
  private val _lastName: String,
  dateOfBirth: LocalDate,
  whereBirthRegistered: BirthRegisterCountry.Value
) {

  import uk.gov.hmrc.brm.services.parser.NameParser._

  def firstNames: String = _firstName.names.listToString

  def additionalNames: String = _additionalNames.fold("")(x => x.names.listToString)

  def lastName: String = _lastName.names.listToString

  def audit: Map[String, String] =
    Map(
      "payload.birthReferenceNumber" -> birthReferenceNumber.fold("No Birth Reference Number")(x => x),
      "payload.firstName"            -> firstNames,
      "payload.additionalNames"      -> additionalNames,
      "payload.lastName"             -> lastName,
      "payload.dateOfBirth"          -> dateOfBirth.toString,
      "payload.whereBirthRegistered" -> whereBirthRegistered.toString
    )

  def requestType: RequestType =
    this match {
      case input @ Payload(None, _, _, _, _, _)      => DetailsRequest()
      case payload @ Payload(Some(_), _, _, _, _, _) => ReferenceRequest()
    }
}

abstract class RequestType
case class ReferenceRequest() extends RequestType
case class DetailsRequest() extends RequestType

object Payload {

  val minimumDateOfBirthYear = 1900
  val nameMaxLength          = 250

  val birthReferenceNumber = "birthReferenceNumber"
  val firstName             = "firstName"
  val additionalNames      = "additionalNames"
  val lastName             = "lastName"
  val dateOfBirth          = "dateOfBirth"
  val whereBirthRegistered = "whereBirthRegistered"

  val datePattern = "yyyy-MM-dd"

  private val validBirthReferenceNumberRegEx    = """^[0-9]+$"""
  private val validBirthReferenceNumberGRORegEx = """^[0-9]{9}+$"""
  private val validBirthReferenceNumberNRSRegEx = """^[0-9]{10}+$"""
  private val invalidNameCharsRegEx             = "[;/\\\\()|*.=+@]|(<!)|(-->)|(\\n)|(\")|(\u0000)|(^\\s+$)".r

  private val validationError = JsonValidationError("Input does not match regex")

  private def validBirthReferenceNumber(country: BirthRegisterCountry.Value, referenceNumber: Option[String]): Boolean =
    (country, referenceNumber) match {
      case (BirthRegisterCountry.ENGLAND, Some(_))  => referenceNumber.get.matches(validBirthReferenceNumberGRORegEx)
      case (BirthRegisterCountry.WALES, Some(_))    => referenceNumber.get.matches(validBirthReferenceNumberGRORegEx)
      case (BirthRegisterCountry.SCOTLAND, Some(_)) => referenceNumber.get.matches(validBirthReferenceNumberNRSRegEx)
      case (_, Some(x))                             => x.matches(validBirthReferenceNumberRegEx)
      case (_, _)                                   => true
    }

  private val nameValidation: Reads[String] =
    Reads.StringReads.filter(validationError)(
      invalidNameCharsRegEx.findFirstIn(_).isEmpty
    )

  implicit val PayloadWrites: Writes[Payload] = (
    (JsPath \ birthReferenceNumber).writeNullable[String] and
      (JsPath \ firstName).write[String] and
      (JsPath \ additionalNames).writeNullable[String] and
      (JsPath \ lastName).write[String] and
      (JsPath \ dateOfBirth).write[LocalDate] and
      (JsPath \ whereBirthRegistered).write[BirthRegisterCountry.Value](birthRegisterWrites)
  )(unlift(Payload.unapply))

  implicit def requestFormat(implicit
    engAndWalesMetrics: EnglandAndWalesBirthRegisteredCountMetrics,
    northIreMetrics: NorthernIrelandBirthRegisteredCountMetrics,
    scotlandMetrics: ScotlandBirthRegisteredCountMetrics,
    invalidRegMetrics: InvalidBirthRegisteredCountMetrics
  ): Reads[Payload] = (
    (JsPath \ birthReferenceNumber).readNullable[String] and
      (JsPath \ firstName).read[String](
        nameValidation keepAnd minLength[String](1) keepAnd maxLength[String](nameMaxLength)
      ) and
      (JsPath \ additionalNames).readNullable[String](
        nameValidation keepAnd minLength[String](1) keepAnd maxLength[String](nameMaxLength)
      ) and
      (JsPath \ lastName).read[String](
        nameValidation keepAnd minLength[String](1) keepAnd maxLength[String](nameMaxLength)
      ) and
      (JsPath \ dateOfBirth).read[LocalDate](validLocalDateReads) and
      (JsPath \ whereBirthRegistered).read[BirthRegisterCountry.Value](birthRegisterReads)
  )(Payload.apply _)
    .filter(JsonValidationError(InvalidBirthReferenceNumber.message))(x =>
      validBirthReferenceNumber(x.whereBirthRegistered, x.birthReferenceNumber)
    )
}
