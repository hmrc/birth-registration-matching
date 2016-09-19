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

package uk.gov.hmrc.brm.metrics

import java.util.concurrent.TimeUnit

import com.kenshoo.play.metrics.MetricsRegistry
import play.api.Logger

trait Metrics {

  Logger.info(s"[${super.getClass}][constructor] Initialising metrics interface")

  val prefix : String

  def time(diff: Long, unit: TimeUnit) =
    MetricsRegistry.defaultRegistry.timer(s"$prefix-timer").update(diff, unit)

  def connectorStatus(code: Int) : Unit =
    MetricsRegistry.defaultRegistry.counter(s"$prefix-connector-status-$code").inc()

  def startTimer() : Long = System.currentTimeMillis()

  def endTimer(start: Long) = {
    val end = System.currentTimeMillis() - start
    time(end, TimeUnit.MILLISECONDS)
  }

}

object ProxyMetrics extends Metrics {

  Logger.debug(s"[ProxyMetrics][init]")

  override val prefix = "proxy"
}

object NRSMetrics extends Metrics {

  Logger.debug(s"[NRSMetrics][init]")

  override val prefix = "nrs"
}

object GRONIMetrics extends Metrics {

  Logger.debug(s"[GRONIMetrics][init]")

  override val prefix = "gro-ni"
}
case class APIVersionMetrics(version :String) extends Metrics{
  Logger.debug(s"[APIVersionMetrics][init]")
  override val prefix = version
  def count() = MetricsRegistry.defaultRegistry.counter(s"api-version-$prefix").inc()
}

case class AuditSourceMetrics(auditSource :String) extends Metrics{
  Logger.debug(s"[AuditSourceMetrics][init]")
  override val prefix = auditSource.toLowerCase
  def count() = MetricsRegistry.defaultRegistry.counter(s"audit-source-$prefix").inc()
}


abstract class WhereBirthRegisteredMetrics(location: String) extends Metrics {

  Logger.debug(s"[WhereBirthRegisteredMetrics][init]")

  override val prefix = location

  def count() = MetricsRegistry.defaultRegistry.counter(s"$prefix-count").inc()
}

object EnglandAndWalesBirthRegisteredMetrics extends WhereBirthRegisteredMetrics("england-and-wales")
object ScotlandBirthRegisteredMetrics extends WhereBirthRegisteredMetrics("scotland")
object NorthernIrelandBirthRegisteredMetrics extends WhereBirthRegisteredMetrics("northern-ireland")
object InvalidBirthRegisteredMetrics extends WhereBirthRegisteredMetrics("invalid-birth-registered")

object MatchMetrics extends Metrics {

  Logger.debug(s"[MatchMetrics][init]")

  override val prefix = "match"

  def matchCount() = MetricsRegistry.defaultRegistry.counter(s"$prefix-count").inc()
  def noMatchCount() = MetricsRegistry.defaultRegistry.counter(s"no-$prefix-count").inc()

}