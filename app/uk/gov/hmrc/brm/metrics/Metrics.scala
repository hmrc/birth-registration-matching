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

  def proxyConnectorTimer(diff: Long, unit: TimeUnit) : Unit
  def proxyConnectorStatus(code: Int) : Unit

  def matchCount() : Unit
  def noMatchCount() : Unit
}

object Metrics extends Metrics {

  val timer = (name: String) => MetricsRegistry.defaultRegistry.timer(name)
  val counter = (name: String) => MetricsRegistry.defaultRegistry.counter(name)

  Logger.info("[Metrics][constructor] Preloading metrics keys")

  // register metrics with timer and counter
  Seq(
    ("proxy-connector-timer", timer),
    ("proxy-connector-status-200", counter),
    ("proxy-connector-status-400", counter),
    ("proxy-connector-status-500", counter),
    ("match-count", counter),
    ("no-match-count", counter)
  ) foreach { t => t._2(t._1) }

  override def proxyConnectorTimer(diff: Long, unit: TimeUnit) =
    MetricsRegistry.defaultRegistry.timer("proxy-connector-timer").update(diff, unit)

  override def proxyConnectorStatus(code: Int) : Unit =
    MetricsRegistry.defaultRegistry.counter(s"proxy-connector-status-$code").inc()

  override def matchCount() = MetricsRegistry.defaultRegistry.counter("match-count").inc()
  override def noMatchCount() = MetricsRegistry.defaultRegistry.counter("no-match-count").inc()
}
