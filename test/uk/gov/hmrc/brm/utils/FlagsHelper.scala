/*
 * Copyright 2019 HM Revenue & Customs
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

import org.joda.time.LocalDate
import uk.gov.hmrc.brm.models.response.{Child, Record}
import uk.gov.hmrc.brm.models.response.gro.GROStatus

object FlagsHelper  {

  private val referenceNumber: Int = 123456789

  private val birthDate = new LocalDate("2012-02-16")

  def flaggedFictitiousBirth: Record = {

    val status = GROStatus(
      potentiallyFictitiousBirth = true,
      correction = None,
      cancelled = false,
      blockedRegistration = false,
      marginalNote = None,
      reRegistered = None)

    val child = Child(referenceNumber, "Chris", "Jones", Some(birthDate))
    Record(child, Some(status))
  }

  def flaggedBlockedRegistration: Record = {

    val status = GROStatus(
      potentiallyFictitiousBirth = false,
      correction = None,
      cancelled = false,
      blockedRegistration = true,
      marginalNote = None,
      reRegistered = None)

    val child = Child(referenceNumber, "Chris", "Jones", Some(birthDate))
    Record(child, Some(status))
  }

  def correction: Record = {

    val status = GROStatus(
      potentiallyFictitiousBirth = false,
      correction = Some("Simple clerical"),
      cancelled = false,
      blockedRegistration = false,
      marginalNote = None,
      reRegistered = None)

    val child = Child(referenceNumber, "Chris", "Jones", Some(birthDate))
    Record(child, Some(status))
  }

  def cancelled: Record = {

    val status = GROStatus(
      potentiallyFictitiousBirth = false,
      correction = None,
      cancelled = true,
      blockedRegistration = false,
      marginalNote = None,
      reRegistered = None)

    val child = Child(referenceNumber, "Chris", "Jones", Some(birthDate))
    Record(child, Some(status))
  }

  def marginalNote(value: String): Record = {

    val status = GROStatus(
      potentiallyFictitiousBirth = false,
      correction = None,
      cancelled = false,
      blockedRegistration = false,
      marginalNote = Some(value),
      reRegistered = None)

    val child = Child(referenceNumber, "Chris", "Jones", Some(birthDate))
    Record(child, Some(status))
  }

  def reRegistered(value: String): Record = {

    val status = GROStatus(
      potentiallyFictitiousBirth = false,
      correction = None,
      cancelled = false,
      blockedRegistration = false,
      marginalNote = None,
      reRegistered = Some(value))

    val child = Child(referenceNumber, "Chris", "Jones", Some(birthDate))
    Record(child, Some(status))
  }
}
