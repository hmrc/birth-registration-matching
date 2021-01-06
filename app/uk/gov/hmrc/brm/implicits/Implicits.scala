/*
 * Copyright 2021 HM Revenue & Customs
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
import javax.inject.Inject
import play.api.libs.json.Reads
import uk.gov.hmrc.brm.audit._
import uk.gov.hmrc.brm.metrics._
import uk.gov.hmrc.brm.models.brm.Payload
import uk.gov.hmrc.brm.models.response.Record
import uk.gov.hmrc.brm.utils.{BirthRegisterCountry, ReadsUtil}



class MetricsFactory @Inject()(groMetrics: GROReferenceMetrics,
                               nrsMetrics: NRSMetrics,
                               groniMetrics: GRONIMetrics,
                               groDetailsMetrics: GRODetailsMetrics) {

  private lazy val referenceSet: Map[BirthRegisterCountry.Value, BRMMetrics] = Map(
    BirthRegisterCountry.ENGLAND -> groMetrics,
    BirthRegisterCountry.WALES -> groMetrics,
    BirthRegisterCountry.SCOTLAND -> nrsMetrics,
    BirthRegisterCountry.NORTHERN_IRELAND -> groniMetrics
  )

  private lazy val detailsSet: Map[BirthRegisterCountry.Value, BRMMetrics] = Map(
    BirthRegisterCountry.ENGLAND -> groDetailsMetrics,
    BirthRegisterCountry.WALES -> groDetailsMetrics,
    BirthRegisterCountry.SCOTLAND -> nrsMetrics,
    BirthRegisterCountry.NORTHERN_IRELAND -> groniMetrics
  )

  def getMetrics()(implicit payload: Payload): BRMMetrics = {
    payload.birthReferenceNumber match {
      case Some(_) =>
        referenceSet(payload.whereBirthRegistered)
      case None =>
        detailsSet(payload.whereBirthRegistered)
    }
  }

}

@Singleton
class AuditFactory @Inject()(engWalesAudit: EnglandAndWalesAudit,
                             scotAudit: ScotlandAudit,
                             northIreAudit: NorthernIrelandAudit) {

  private lazy val set: Map[BirthRegisterCountry.Value, BRMDownstreamAPIAudit] = Map(
    BirthRegisterCountry.ENGLAND -> engWalesAudit,
    BirthRegisterCountry.WALES -> engWalesAudit,
    BirthRegisterCountry.SCOTLAND -> scotAudit,
    BirthRegisterCountry.NORTHERN_IRELAND -> northIreAudit
  )

  def getAuditor()(implicit payload: Payload): BRMDownstreamAPIAudit = {
    set(payload.whereBirthRegistered)
  }
}


object ReadsFactory {
  private lazy val set: Map[BirthRegisterCountry.Value, (Reads[List[Record]], Reads[Record])] = Map(
    BirthRegisterCountry.ENGLAND -> (ReadsUtil.groRecordsListRead, ReadsUtil.groReadRecord),
    BirthRegisterCountry.WALES -> (ReadsUtil.groRecordsListRead, ReadsUtil.groReadRecord),
    BirthRegisterCountry.SCOTLAND -> (ReadsUtil.nrsRecordsListRead, ReadsUtil.nrsRecordsRead)
  )

  def getReads()(implicit payload: Payload): (Reads[List[Record]], Reads[Record]) = {
    set(payload.whereBirthRegistered)
  }
}
