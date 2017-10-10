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

package uk.gov.hmrc.brm.models.response.gro

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, JsValue, Json, Reads}
import play.api.libs.json.Reads._
import uk.gov.hmrc.brm.models.response.StatusInterface
import uk.gov.hmrc.brm.filters.flags.{Severity, Green, Red}

trait FlagSeverity {
  def canProcessRecord() : Boolean
}

case class GROStatus(
  potentiallyFictitiousBirth: Boolean = false,
  correction: Option[String] = None,
  cancelled: Boolean = false,
  blockedRegistration: Boolean = false,
  marginalNote: Option[String] = None,
  reRegistered: Option[String] = None
) extends StatusInterface {

  case class GROFlagSeverity(
                            potentiallyFictitiousBirth: Severity,
                            correction: Severity,
                            cancelled: Severity,
                            blockedRegistration: Severity,
                            marginalNote: Severity,
                            reRegistered: Severity
                          ) extends FlagSeverity {
    def canProcessRecord = {
      this.potentiallyFictitiousBirth == Green && this.blockedRegistration == Green
    }

  }

  override def toJson: JsValue = {
    Json.parse(s"""
       |{
       |"potentiallyFictitiousBirth": "$potentiallyFictitiousBirth",
       |"correction": "${correction.getOrElse("")}",
       |"cancelled": "$cancelled",
       |"blockedRegistration": "$blockedRegistration",
       |"marginalNote": "${marginalNote.getOrElse("")}",
       |"reRegistered": "${reRegistered.getOrElse("")}"
       |}
     """.stripMargin)
  }

  override def flags: Map[String, String] = Map(
    "potentiallyFictitiousBirth" -> s"$potentiallyFictitiousBirth",
    "correction" -> obfuscateReason(correction, "Correction on record"),
    "cancelled" -> s"$cancelled",
    "blockedRegistration" -> s"$blockedRegistration",
    "marginalNote" -> obfuscateReason(marginalNote, "Marginal note on record"),
    "reRegistered" -> obfuscateReason(reRegistered, "Re-registration on record")
  )

  def determineFlagSeverity() : FlagSeverity = {
    GROFlagSeverity(
      potentiallyFictitiousBirth = potentiallyFictitiousBirthP(this.potentiallyFictitiousBirth),
      correction = correctionP(this.correction),
      cancelled = cancelledP(this.cancelled),
      blockedRegistration = blockedRegistrationP(this.blockedRegistration),
      marginalNote = marginalNoteP(this.marginalNote),
      reRegistered = reRegisteredP(this.reRegistered)
    )
  }

  private def potentiallyFictitiousBirthP: PartialFunction[Boolean, Severity] = {
    case true => Red
    case _ => Green
  }

  private def correctionP[A]: PartialFunction[Option[A], Severity] = {
    case Some(_) => Red
    case _ => Green
  }

  private def cancelledP: PartialFunction[Boolean, Severity] = {
    case true => Red
    case _ => Green
  }

  private def blockedRegistrationP: PartialFunction[Boolean, Severity] = {
    case true => Red
    case _ => Green
  }

  private def marginalNoteP[A]: PartialFunction[Option[A], Severity] = {
    case Some(_) => Red
    case _ => Green
  }

  private def reRegisteredP[A]: PartialFunction[Option[A], Severity] = {
    case Some(_) => Red
    case _ => Green
  }

}

object GROStatus {

  implicit val childReads: Reads[GROStatus] = (
    (JsPath \ "potentiallyFictitiousBirth").read[Boolean].orElse(Reads.pure(false)) and
    (JsPath \ "correction").readNullable[String] and
    (JsPath \ "cancelled").read[Boolean].orElse(Reads.pure(false)) and
    (JsPath \ "blockedRegistration").read[Boolean].orElse(Reads.pure(false)) and
    (JsPath \ "marginalNote").readNullable[String] and
    (JsPath \ "reRegistered").readNullable[String]
  )(GROStatus.apply _)
}
