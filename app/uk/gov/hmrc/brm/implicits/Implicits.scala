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

package uk.gov.hmrc.brm.implicits

import com.google.inject.Singleton
import uk.gov.hmrc.brm.audit.{BRMAudit, EnglandAndWalesAudit, NorthernIrelandAudit, ScotlandAudit}
import uk.gov.hmrc.brm.metrics._
import uk.gov.hmrc.brm.models.brm.Payload
import uk.gov.hmrc.brm.utils.BirthRegisterCountry

object Implicits {

  object MetricsFactory {

    private lazy val referenceSet : Map[BirthRegisterCountry.Value, BRMMetrics] = Map(
      BirthRegisterCountry.ENGLAND -> GROReferenceMetrics,
      BirthRegisterCountry.WALES -> GROReferenceMetrics,
      BirthRegisterCountry.SCOTLAND -> NRSMetrics,
      BirthRegisterCountry.NORTHERN_IRELAND -> GRONIMetrics
    )

    private lazy val detailsSet : Map[BirthRegisterCountry.Value, BRMMetrics] = Map(
      BirthRegisterCountry.ENGLAND -> GRODetailsMetrics,
      BirthRegisterCountry.WALES -> GRODetailsMetrics,
      BirthRegisterCountry.SCOTLAND -> NRSMetrics,
      BirthRegisterCountry.NORTHERN_IRELAND -> GRONIMetrics
    )

    def getMetrics()(implicit payload : Payload) : BRMMetrics = {
      payload.birthReferenceNumber match {
        case Some(x) =>
          referenceSet(payload.whereBirthRegistered)
        case None =>
          detailsSet(payload.whereBirthRegistered)
      }
    }

  }

  @Singleton
  class AuditFactory() {

    private lazy val set : Map[BirthRegisterCountry.Value, BRMAudit] = Map(
      BirthRegisterCountry.ENGLAND -> new EnglandAndWalesAudit(),
      BirthRegisterCountry.WALES -> new EnglandAndWalesAudit(),
      BirthRegisterCountry.SCOTLAND -> new ScotlandAudit(),
      BirthRegisterCountry.NORTHERN_IRELAND -> new NorthernIrelandAudit()
    )

    def getAuditor()(implicit payload : Payload) : BRMAudit = {
      set(payload.whereBirthRegistered)
    }
  }

}
