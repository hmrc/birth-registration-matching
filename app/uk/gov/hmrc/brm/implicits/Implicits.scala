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

package uk.gov.hmrc.brm.implicits

import com.google.inject.Singleton
import play.api.libs.json.Reads
import uk.gov.hmrc.brm.audit.*
import uk.gov.hmrc.brm.metrics.*
import uk.gov.hmrc.brm.models.brm.Payload
import uk.gov.hmrc.brm.models.response.Record
import uk.gov.hmrc.brm.utils.{BirthRegisterCountry, ReadsUtil}

import javax.inject.Inject

class MetricsFactory @Inject() (
  groMetrics: GROReferenceMetrics,
  nrsMetrics: NRSMetrics,
  groniMetrics: GRONIMetrics,
  groDetailsMetrics: GRODetailsMetrics
) {

  private lazy val referenceSet: Map[BirthRegisterCountry.Value, BRMMetrics] = Map(
    BirthRegisterCountry.ENGLAND          -> groMetrics,
    BirthRegisterCountry.WALES            -> groMetrics,
    BirthRegisterCountry.SCOTLAND         -> nrsMetrics,
    BirthRegisterCountry.NORTHERN_IRELAND -> groniMetrics
  )

  private lazy val detailsSet: Map[BirthRegisterCountry.Value, BRMMetrics] = Map(
    BirthRegisterCountry.ENGLAND          -> groDetailsMetrics,
    BirthRegisterCountry.WALES            -> groDetailsMetrics,
    BirthRegisterCountry.SCOTLAND         -> nrsMetrics,
    BirthRegisterCountry.NORTHERN_IRELAND -> groniMetrics
  )

  def getMetrics()(using payload: Payload): BRMMetrics =
    payload.birthReferenceNumber match {
      case Some(_) =>
        referenceSet(payload.whereBirthRegistered)
      case None    =>
        detailsSet(payload.whereBirthRegistered)
    }

}

@Singleton
class AuditFactory @Inject() (
  engWalesAudit: EnglandAndWalesAudit,
  scotAudit: ScotlandAudit,
  northIreAudit: NorthernIrelandAudit
) {

  private lazy val set: Map[BirthRegisterCountry.Value, BRMDownstreamAPIAudit] = Map(
    BirthRegisterCountry.ENGLAND          -> engWalesAudit,
    BirthRegisterCountry.WALES            -> engWalesAudit,
    BirthRegisterCountry.SCOTLAND         -> scotAudit,
    BirthRegisterCountry.NORTHERN_IRELAND -> northIreAudit
  )

  def getAuditor()(using payload: Payload): BRMDownstreamAPIAudit =
    set(payload.whereBirthRegistered)

}

object ReadsFactory {

  private val groV0Reads: (Reads[List[Record]], Reads[Record]) =
    (ReadsUtil.groRecordsListRead, ReadsUtil.groReadRecord)

  private val groV1Reads: (Reads[List[Record]], Reads[Record]) =
    (ReadsUtil.groRecordsListReadV1, ReadsUtil.groReadRecordV1)

  private val nrsReads: (Reads[List[Record]], Reads[Record]) =
    (ReadsUtil.nrsRecordsListRead, ReadsUtil.nrsRecordsRead)

  def getReads(enableV1Version: Boolean)(using payload: Payload): (Reads[List[Record]], Reads[Record]) =
    payload.whereBirthRegistered match {
      case BirthRegisterCountry.ENGLAND | BirthRegisterCountry.WALES =>
        if (enableV1Version) groV1Reads
        else groV0Reads

      case BirthRegisterCountry.SCOTLAND =>
        nrsReads

    }

}
