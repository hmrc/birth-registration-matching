/*
 * Copyright 2022 HM Revenue & Customs
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

import com.kenshoo.play.metrics.Metrics
import javax.inject.Inject

sealed protected trait Timer {
  this: BRMMetrics =>

  val prefix : String

  private def time(diff: Long): Unit =
    metrics.defaultRegistry.timer(s"$prefix-timer").update(diff, TimeUnit.MILLISECONDS)

  def startTimer(): Long = System.currentTimeMillis()

  def endTimer(start: Long): Unit = {
    val end = System.currentTimeMillis() - start
    time(end)
  }
}

sealed protected trait Connector {
  this: BRMMetrics =>

  val prefix: String

  def status(code: Int) : Unit =
    metrics.defaultRegistry.counter(s"$prefix-connector-status-$code").inc()
}

sealed trait BRMMetrics extends Timer with Connector {
  val metrics: Metrics
  val prefix : String
}

/**
  * GRODetailsMetrics
  * Timer metric for GRO reference
  */

class GROReferenceMetrics @Inject()(val metrics: Metrics) extends BRMMetrics {
  override val prefix = "proxy"
}

/**
  * GRODetailsMetrics
  * Timer metric for GRO details
  */

class GRODetailsMetrics @Inject()(val metrics: Metrics) extends BRMMetrics {
  override val prefix : String = "proxy-details"
}

/**
  * NRSMetrics
  * Timer metric for NRS
  */

class NRSMetrics @Inject()(val metrics: Metrics) extends BRMMetrics {
  override val prefix = "nrs"
}

/**
  * GRONIMetrics
  * Timer metric for GRO-NI
  */

class GRONIMetrics @Inject()(val metrics: Metrics) extends BRMMetrics {
  override val prefix = "gro-ni"
}


class APIVersionMetrics @Inject()(val metrics: Metrics) extends BRMMetrics {
  override val prefix = "1.0"
  def count(): Unit = metrics.defaultRegistry.counter(s"api-version-$prefix").inc()
}


class AuditSourceMetrics @Inject()(val metrics: Metrics) extends BRMMetrics {
  override val prefix = "unused"
  def count(source: String): Unit = metrics.defaultRegistry.counter(s"audit-source-$source").inc()
}



trait CountingMetric extends BRMMetrics {
  def count(): Unit = metrics.defaultRegistry.counter(s"$prefix-count").inc()
}


class MatchCountMetric @Inject()(val metrics: Metrics) extends CountingMetric {
  val prefix = "match"
}

class NoMatchCountMetric @Inject()(val metrics: Metrics) extends CountingMetric{
  val prefix = "no-match"
}

class EnglandAndWalesBirthRegisteredCountMetrics @Inject()(val metrics: Metrics) extends CountingMetric {
  val prefix = "england-and-wales"
}

class ScotlandBirthRegisteredCountMetrics @Inject()(val metrics: Metrics) extends CountingMetric {
  val prefix = "scotland"
}

class NorthernIrelandBirthRegisteredCountMetrics @Inject()(val metrics: Metrics) extends CountingMetric {
  val prefix = "northern-ireland"
}

class InvalidBirthRegisteredCountMetrics @Inject()(val metrics: Metrics) extends CountingMetric {
  val prefix = "invalid-birth-registered"
}

class DateofBirthFeatureCountMetric @Inject()(val metrics: Metrics) extends CountingMetric {
  val prefix = "feature-date-of-birth"
}
