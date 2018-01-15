/*
 * Copyright 2018 HM Revenue & Customs
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

sealed protected trait Timer {
  this: MicroserviceMetrics =>

  val prefix : String

  private def time(diff: Long, unit: TimeUnit) =
    metrics.defaultRegistry.timer(s"$prefix-timer").update(diff, unit)

  def startTimer() : Long = System.currentTimeMillis()

  def endTimer(start: Long) = {
    val end = System.currentTimeMillis() - start
    time(end, TimeUnit.MILLISECONDS)
  }
}

sealed protected trait Connector {
  this: MicroserviceMetrics =>

  val prefix : String

  def status(code: Int) : Unit =
    metrics.defaultRegistry.counter(s"$prefix-connector-status-$code").inc()
}

sealed trait BRMMetrics extends MicroserviceMetrics
  with Timer
  with Connector {

  val prefix : String
}

/**
  * GRODetailsMetrics
  * Timer metric for GRO reference
  */

object GROReferenceMetrics extends BRMMetrics {
  override val prefix = "proxy"
}

/**
  * GRODetailsMetrics
  * Timer metric for GRO details
  */

object GRODetailsMetrics extends BRMMetrics {
  override val prefix : String = "proxy-details"
}

/**
  * NRSMetrics
  * Timer metric for NRS
  */

object NRSMetrics extends BRMMetrics {
  override val prefix = "nrs"
}

/**
  * GRONIMetrics
  * Timer metric for GRO-NI
  */

object GRONIMetrics extends BRMMetrics {
  override val prefix = "gro-ni"
}

/**
  * APIVersionMetrics
  * @param version api version used for this service
  */

case class APIVersionMetrics(version :String) extends BRMMetrics {
  override val prefix = version
  def count() = metrics.defaultRegistry.counter(s"api-version-$prefix").inc()
}

/**
  * AuditSourceMetrics
  * @param auditSource name of the client making the request to this service
  */

case class AuditSourceMetrics (auditSource :String) extends BRMMetrics {
  override val prefix = auditSource.toLowerCase
  def count() = metrics.defaultRegistry.counter(s"audit-source-$prefix").inc()
}

/**
  * CountingMetrics
  * @param name name of the counter
  */

sealed abstract class CountingMetric(name : String) extends BRMMetrics {

  override val prefix = name

  def count() = metrics.defaultRegistry.counter(s"$prefix-count").inc()
}


object MatchCountMetric extends CountingMetric("match")
object NoMatchCountMetric extends CountingMetric("no-match")

object EnglandAndWalesBirthRegisteredCountMetrics extends CountingMetric("england-and-wales")
object ScotlandBirthRegisteredCountMetrics extends CountingMetric("scotland")
object NorthernIrelandBirthRegisteredCountMetrics extends CountingMetric("northern-ireland")
object InvalidBirthRegisteredCountMetrics extends CountingMetric("invalid-birth-registered")
object DateofBirthFeatureCountMetric extends CountingMetric("feature-date-of-birth")
