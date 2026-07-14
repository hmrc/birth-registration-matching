/*
 * Copyright 2026 HM Revenue & Customs
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
import uk.gov.hmrc.brm.config.BrmConfig
import uk.gov.hmrc.brm.models.response.StatusInterface
import uk.gov.hmrc.brm.filters.flags.{Green, Red, Severity}

trait FlagSeverity {
  def canProcessRecord(config: BrmConfig): Boolean
}

case class GROStatus(
  potentiallyFictitious: Boolean = false,
  correction: Option[String] = None,
  cancelled: Boolean = false,
  blocked: Boolean = false,
  marginalNote: Option[String] = None,
  reregistration: Option[String] = None
) extends StatusInterface {

  case class GROFlagSeverity(
    potentiallyFictitious: Severity,
    correction: Severity,
    cancelled: Severity,
    blocked: Severity,
    marginalNote: Severity,
    reregistration: Severity
  ) extends FlagSeverity {

    def canProcessRecord(config: BrmConfig): Boolean =
      isGreen(this.potentiallyFictitious, config.validateFlag("gro", "potentiallyFictitious")) &&
        isGreen(this.blocked, config.validateFlag("gro", "blocked")) &&
        isGreen(this.correction, config.validateFlag("gro", "correction")) &&
        isGreen(this.cancelled, config.validateFlag("gro", "cancelled")) &&
        isGreen(this.marginalNote, config.validateFlag("gro", "marginalNote")) &&
        isGreen(this.reregistration, config.validateFlag("gro", "reregistration"))

    private def isGreen(flag: Severity, turnedOn: Boolean): Boolean =
      if (turnedOn) {
        flag == Green
      } else {
        true
      }

  }

  private val invalidMarginalNote = List("other", "re-registered", "court order in place")
  private val invalidReRegistered = List("other")

  override def toJson: JsValue =
    Json.parse(s"""
         |{
         |"potentiallyFictitious": "$potentiallyFictitious",
         |"correction": "${correction.getOrElse("")}",
         |"cancelled": "$cancelled",
         |"blocked": "$blocked",
         |"marginalNote": "${marginalNote.getOrElse("")}",
         |"reregistration": "${reregistration.getOrElse("")}"
         |}
     """.stripMargin)

  override def flags: Map[String, String] = Map(
    "potentiallyFictitious" -> s"$potentiallyFictitious",
    "correction"            -> obfuscateReason(correction, "Correction on record"),
    "cancelled"             -> s"$cancelled",
    "blocked"               -> s"$blocked",
    "marginalNote"          -> obfuscateReason(marginalNote, "Marginal note on record"),
    "reregistration"        -> obfuscateReason(reregistration, "Re-registration on record")
  )

  def determineFlagSeverity: FlagSeverity =
    GROFlagSeverity(
      potentiallyFictitious = potentiallyFictitiousBirthP(this.potentiallyFictitious),
      correction = correctionP(this.correction),
      cancelled = cancelledP(this.cancelled),
      blocked = blockedRegistrationP(this.blocked),
      marginalNote = marginalNoteP(this.marginalNote),
      reregistration = reRegisteredP(this.reregistration)
    )

  private def potentiallyFictitiousBirthP: PartialFunction[Boolean, Severity] = {
    case true => Red
    case _    => Green
  }

  private def correctionP[A]: PartialFunction[Option[A], Severity] = {
    case Some(_) => Red
    case _       => Green
  }

  private def cancelledP: PartialFunction[Boolean, Severity] = {
    case true => Red
    case _    => Green
  }

  private def blockedRegistrationP: PartialFunction[Boolean, Severity] = {
    case true => Red
    case _    => Green
  }

  private def marginalNoteP[A]: PartialFunction[Option[A], Severity] = {
    case Some(x: String) if invalidMarginalNote.contains(x.trim.toLowerCase) => Red
    case _                                                                   => Green
  }

  private def reRegisteredP[A]: PartialFunction[Option[A], Severity] = {
    case Some(x: String) if invalidReRegistered.contains(x.trim.toLowerCase()) => Red
    case _                                                                     => Green
  }

}

object GROStatus {

  implicit val childReads: Reads[GROStatus] = (
    (JsPath \ "potentiallyFictitious").read[Boolean].orElse(Reads.pure(false)) and
      (JsPath \ "correction").readNullable[String] and
      (JsPath \ "cancelled").read[Boolean].orElse(Reads.pure(false)) and
      (JsPath \ "blocked").read[Boolean].orElse(Reads.pure(false)) and
      (JsPath \ "marginalNote").readNullable[String] and
      (JsPath \ "reregistration").readNullable[String]
  )(GROStatus.apply _)

}
