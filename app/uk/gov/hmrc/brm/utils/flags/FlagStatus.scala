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

package uk.gov.hmrc.brm.utils.flags

import uk.gov.hmrc.brm.models.response.gro.GROStatus

//sealed abstract class FlagLevels[StatusInterface] {
//  def apply(flags: GROStatus) : FlagStatus
//}

//sealed trait FlagStatus
//case class GROFlagStatus(
//                          potentiallyFictitiousBirth: FlagCategory,
//                          correction: FlagCategory,
//                          cancelled: FlagCategory,
//                          blockedRegistration: FlagCategory,
//                          marginalNote: FlagCategory,
//                          reRegistered: FlagCategory
//                        ) extends FlagStatus

//object GROFlagLevels {
//
//  def apply(flags : GROStatus) : GROFlagStatus = {
//    GROFlagStatus(
//      potentiallyFictitiousBirth = potentiallyFictitiousBirthP(flags.potentiallyFictitiousBirth),
//      correction = correctionP(flags.correction),
//      cancelled = cancelledP(flags.cancelled),
//      blockedRegistration = blockedRegistrationP(flags.blockedRegistration),
//      marginalNote = marginalNoteP(flags.marginalNote),
//      reRegistered = reRegisteredP(flags.reRegistered)
//    )
//  }
//
//
//}
