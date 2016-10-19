/*
 * Copyright 2016 HM Revenue & Customs
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

import play.api.Logger
import uk.gov.hmrc.brm.metrics.{GRONIMetrics, Metrics, NRSMetrics, ProxyMetrics}
import uk.gov.hmrc.brm.models.brm.Payload
import uk.gov.hmrc.brm.utils.BirthRegisterCountry

object Implicits {

  implicit def getMetrics()(implicit payload : Payload) : Metrics = {
    payload.whereBirthRegistered match {
      case BirthRegisterCountry.ENGLAND | BirthRegisterCountry.WALES =>
        Logger.debug(s"[Implicits][Metrics][getMetrics] Proxy")
        ProxyMetrics
      case BirthRegisterCountry.NORTHERN_IRELAND  =>
        Logger.debug(s"[Implicits][Metrics][getMetrics] GRO-NI")
        GRONIMetrics
      case BirthRegisterCountry.SCOTLAND  =>
        Logger.debug(s"[Implicits][Metrics][getMetrics] NRS")
        NRSMetrics
    }
  }

}
