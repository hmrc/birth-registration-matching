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

import org.joda.time.LocalDate
import play.api.data.validation.ValidationError
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json.Writes._
import play.api.libs.json._
import uk.gov.hmrc.brm.config.BrmConfig
import uk.gov.hmrc.brm.utils.BirthRegisterCountry
import uk.gov.hmrc.brm.utils.BirthRegisterCountry.{BirthRegisterCountry, apply => _, _}

case class Payload(
                    birthReferenceNumber: Option[String] = None,
                    private val _firstName: String,
                    private val _additionalNames: Option[String] = None,
                    private val _lastName: String,
                    dateOfBirth: LocalDate,
                    whereBirthRegistered : BirthRegisterCountry
                  ){

  import uk.gov.hmrc.brm.services.parser.NameParser._

  def firstNames : String = _firstName.names.listToString

  def additionalNames : String = _additionalNames.fold("")(x => x.names.listToString)

  def lastName : String = _lastName.names.listToString

  def audit : Map[String, String] = {
    Map(
    "payload.birthReferenceNumber" -> birthReferenceNumber.fold("No Birth Reference Number")(x => x),
    "payload.firstName" -> firstNames,
    "payload.additionalNames" -> additionalNames,
    "payload.lastName" -> lastName,
    "payload.dateOfBirth" -> dateOfBirth.toString(Payload.datePattern),
    "payload.whereBirthRegistered" -> whereBirthRegistered.toString
    )
  }

  def requestType: RequestType = {
    this match {
      case input@Payload(None, _, _, _, _, _) => DetailsRequest()
      case payload@Payload(Some(_), _, _, _, _, _) => ReferenceRequest()
    }
  }
}

abstract class RequestType
case class ReferenceRequest() extends RequestType
case class DetailsRequest() extends RequestType

object Payload {

  val birthReferenceNumber = "birthReferenceNumber"
  val firstName = "firstName"
  val additionalNames = "additionalNames"
  val lastName = "lastName"
  val dateOfBirth = "dateOfBirth"
  val whereBirthRegistered = "whereBirthRegistered"

  val datePattern = "yyyy-MM-dd"

  private val validBirthReferenceNumberRegEx = """^[0-9]+$"""
  private val validBirthReferenceNumberGRORegEx = """^[0-9]{9}+$"""
  private val validBirthReferenceNumberNRSRegEx = """^[0-9]{10}+$"""
  private val invalidNameCharsRegEx =  "[;/\\\\()|*]|(<!)|(-->)|(\\n)|(\")|(^\\s+$)".r

  private val validationError = ValidationError("")

  private def validBirthReferenceNumber(country: BirthRegisterCountry, referenceNumber: Option[String]): Boolean = (country, referenceNumber) match {
    case (BirthRegisterCountry.ENGLAND, Some(_)) => referenceNumber.get.matches(validBirthReferenceNumberGRORegEx)
    case (BirthRegisterCountry.WALES, Some(_)) => referenceNumber.get.matches(validBirthReferenceNumberGRORegEx)
    case (BirthRegisterCountry.SCOTLAND, Some(_)) => referenceNumber.get.matches(validBirthReferenceNumberNRSRegEx)
    case (_, Some(x)) => x.matches(validBirthReferenceNumberRegEx)
    case (_, _) => true
  }

  private val nameValidation: Reads[String] =
    Reads.StringReads.filter(validationError)(
      !invalidNameCharsRegEx.findFirstIn(_).isDefined
    )

  private val isAfterDate: Reads[LocalDate] =
    jodaLocalDateReads(datePattern).filter(validationError)(
      _.getYear >= BrmConfig.minimumDateOfBirthYear
    )

  implicit val PayloadWrites: Writes[Payload] = (
      (JsPath \ birthReferenceNumber).writeNullable[String] and
      (JsPath \ firstName).write[String] and
      (JsPath \ additionalNames).writeNullable[String] and
      (JsPath \ lastName).write[String] and
      (JsPath \ dateOfBirth).write[LocalDate](jodaLocalDateWrites(datePattern)) and
      (JsPath \ whereBirthRegistered).write[BirthRegisterCountry](birthRegisterWrites)
    )(unlift(Payload.unapply))

  implicit val requestFormat: Reads[Payload] = (
      (JsPath \ birthReferenceNumber).readNullable[String] and
      (JsPath \ firstName).read[String](nameValidation keepAnd minLength[String](1) keepAnd maxLength[String](BrmConfig.nameMaxLength)) and
      (JsPath \ additionalNames).readNullable[String](nameValidation keepAnd minLength[String](1) keepAnd maxLength[String](BrmConfig.nameMaxLength)) and
      (JsPath \ lastName).read[String](nameValidation keepAnd minLength[String](1) keepAnd maxLength[String](BrmConfig.nameMaxLength)) and
      (JsPath \ dateOfBirth).read[LocalDate](isAfterDate) and
      (JsPath \ whereBirthRegistered).read[BirthRegisterCountry](birthRegisterReads)
    ) (Payload.apply _)
    .filter(ValidationError(InvalidBirthReferenceNumber.message))(
      x =>
        validBirthReferenceNumber(x.whereBirthRegistered, x.birthReferenceNumber)
  )
}