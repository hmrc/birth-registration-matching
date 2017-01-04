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

package uk.gov.hmrc.brm.metrics

import java.util.concurrent.TimeUnit

import play.api.Logger

import uk.gov.hmrc.play.graphite.MicroserviceMetrics

trait BRMMetrics extends MicroserviceMetrics {
  Logger.info(s"[${super.getClass}][constructor] Initialising metrics interface")

  val prefix : String

  def time(diff: Long, unit: TimeUnit) =
    metrics.defaultRegistry.timer(s"$prefix-timer").update(diff, unit)

  def connectorStatus(code: Int) : Unit =
    metrics.defaultRegistry.counter(s"$prefix-connector-status-$code").inc()

  def startTimer() : Long = System.currentTimeMillis()

  def endTimer(start: Long) = {
    val end = System.currentTimeMillis() - start
    time(end, TimeUnit.MILLISECONDS)
  }
}

object ProxyMetrics extends BRMMetrics {
  Logger.info(s"[ProxyMetrics][init]")
  override val prefix = "proxy"
}

object NRSMetrics extends BRMMetrics {
  Logger.info(s"[NRSMetrics][init]")
  override val prefix = "nrs"
}

object GRONIMetrics extends BRMMetrics {
  Logger.info(s"[GRONIMetrics][init]")
  override val prefix = "gro-ni"
}
case class APIVersionMetrics(version :String) extends BRMMetrics {
  Logger.info(s"[APIVersionMetrics][init]")
  override val prefix = version
  def count() = metrics.defaultRegistry.counter(s"api-version-$prefix").inc()
}

case class AuditSourceMetrics (auditSource :String) extends BRMMetrics {
  Logger.info(s"[AuditSourceMetrics][init]")
  override val prefix = auditSource.toLowerCase
  def count() = metrics.defaultRegistry.counter(s"audit-source-$prefix").inc()
}

abstract class WhereBirthRegisteredMetrics (location: String) extends BRMMetrics {
  Logger.info(s"[WhereBirthRegisteredMetrics][init]")
  override val prefix = location

  def count() = metrics.defaultRegistry.counter(s"$prefix-count").inc()
}

object EnglandAndWalesBirthRegisteredMetrics extends WhereBirthRegisteredMetrics("england-and-wales")
object ScotlandBirthRegisteredMetrics extends WhereBirthRegisteredMetrics("scotland")
object NorthernIrelandBirthRegisteredMetrics extends WhereBirthRegisteredMetrics("northern-ireland")
object InvalidBirthRegisteredMetrics extends WhereBirthRegisteredMetrics("invalid-birth-registered")

object MatchMetrics extends BRMMetrics {
  Logger.info(s"[MatchMetrics][init]")
  override val prefix = "match"

  def matchCount() = metrics.defaultRegistry.counter(s"$prefix-count").inc()
  def noMatchCount() = metrics.defaultRegistry.counter(s"no-$prefix-count").inc()
}
