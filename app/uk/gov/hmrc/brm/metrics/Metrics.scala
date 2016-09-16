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

  Logger.debug(s"[ProxyMetrics][init]]")

  override val prefix = "proxy"
}

object NRSMetrics extends Metrics {

  Logger.debug(s"[NRSMetrics][init]]")

  override val prefix = "nrs"
}

object GRONIMetrics extends Metrics {

  Logger.debug(s"[GRONIMetrics][init]]")

  override val prefix = "gro-ni"
}

object MatchMetrics extends Metrics {

  Logger.debug(s"[MatchMetrics][init]]")

  override val prefix = "match"

  def matchCount() = MetricsRegistry.defaultRegistry.counter(s"$prefix-count").inc()
  def noMatchCount() = MetricsRegistry.defaultRegistry.counter(s"no-$prefix-count").inc()

}