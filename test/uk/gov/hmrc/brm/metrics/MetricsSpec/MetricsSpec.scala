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

package uk.gov.hmrc.brm.metrics.MetricsSpec

import java.util.concurrent.TimeUnit

import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.brm.metrics._
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

class MetricsSpec extends UnitSpec with WithFakeApplication with MockitoSugar {

  "GROReferenceMetrics" should {

    "initialise" in {
      val metrics = GROReferenceMetrics
      metrics shouldBe a[BRMMetrics]
      metrics.prefix shouldBe "proxy"
    }

    "have a timer for the proxy connection" in {
      val metrics = GROReferenceMetrics
      val time = metrics.startTimer()
      metrics.endTimer(time)
      metrics.metrics.defaultRegistry.getTimers.get("proxy-timer").getCount shouldBe 1
    }

    "have a 200 status count for proxy" in {
      val metrics = GROReferenceMetrics
      metrics.status(200)
      metrics.metrics.defaultRegistry.getCounters.get("proxy-connector-status-200").getCount shouldBe 1
    }

    "have a 400 status count for proxy" in {
      val metrics = GROReferenceMetrics
      metrics.status(400)
      metrics.metrics.defaultRegistry.getCounters.get("proxy-connector-status-400").getCount shouldBe 1
    }

    "have a 404 status count for proxy" in {
      val metrics = GROReferenceMetrics
      metrics.status(404)
      metrics.metrics.defaultRegistry.getCounters.get("proxy-connector-status-404").getCount shouldBe 1
    }

    "have a 500 status count for proxy" in {
      val metrics = GROReferenceMetrics
      metrics.status(500)
      metrics.metrics.defaultRegistry.getCounters.get("proxy-connector-status-500").getCount shouldBe 1
    }

    "have a 502 status count for proxy" in {
      val metrics = GROReferenceMetrics
      metrics.status(502)
      metrics.metrics.defaultRegistry.getCounters.get("proxy-connector-status-502").getCount shouldBe 1
    }

    "have a 504 status count for proxy" in {
      val metrics = GROReferenceMetrics
      metrics.status(504)
      metrics.metrics.defaultRegistry.getCounters.get("proxy-connector-status-504").getCount shouldBe 1
    }

    "accept a status code not registered" in {
      val metrics = GROReferenceMetrics
      for (i <- 1 to 5) yield metrics.status(423)
      metrics.metrics.defaultRegistry.getCounters.get("proxy-connector-status-423").getCount shouldBe 5
    }

  }

  "GRODetailsMetrics" should {

    "initialise" in {
      val metrics = GRODetailsMetrics
      metrics shouldBe a[BRMMetrics]
      metrics.prefix shouldBe "proxy-details"
    }

  }

  "NRSMetrics" should {

    "initialise" in {
      val metrics = NRSMetrics
      metrics shouldBe a[BRMMetrics]
      metrics.prefix shouldBe "nrs"
    }

    "have a timer for the nrs connection" in {
      val metrics = NRSMetrics
      val time = metrics.startTimer()
      metrics.endTimer(time)
      metrics.metrics.defaultRegistry.getTimers.get("nrs-timer").getCount shouldBe 1
    }

    "have a 200 status count for nrs" in {
      val metrics = NRSMetrics
      metrics.status(200)
      metrics.metrics.defaultRegistry.getCounters.get("nrs-connector-status-200").getCount shouldBe 1
    }

    "have a 400 status count for nrs" in {
      val metrics = NRSMetrics
      metrics.status(400)
      metrics.metrics.defaultRegistry.getCounters.get("nrs-connector-status-400").getCount shouldBe 1
    }

    "have a 404 status count for proxy" in {
      val metrics = NRSMetrics
      metrics.status(404)
      metrics.metrics.defaultRegistry.getCounters.get("nrs-connector-status-404").getCount shouldBe 1
    }

    "have a 500 status count for nrs" in {
      val metrics = NRSMetrics
      metrics.status(500)
      metrics.metrics.defaultRegistry.getCounters.get("nrs-connector-status-500").getCount shouldBe 1
    }

    "have a 502 status count for proxy" in {
      val metrics = NRSMetrics
      metrics.status(502)
      metrics.metrics.defaultRegistry.getCounters.get("nrs-connector-status-502").getCount shouldBe 1
    }

    "have a 504 status count for proxy" in {
      val metrics = NRSMetrics
      metrics.status(504)
      metrics.metrics.defaultRegistry.getCounters.get("nrs-connector-status-504").getCount shouldBe 1
    }

    "accept a status code not registered" in {
      val metrics = NRSMetrics
      for (i <- 1 to 5) yield metrics.status(423)
      metrics.metrics.defaultRegistry.getCounters.get("nrs-connector-status-423").getCount shouldBe 5
    }

  }

  "GRONIMetrics" should {

    "initialise" in {
      val metrics = GRONIMetrics
      metrics shouldBe a[BRMMetrics]
      metrics.prefix shouldBe "gro-ni"
    }

    "have a timer for the gro-ni connection" in {
      val metrics = GRONIMetrics
      val time = metrics.startTimer()
      metrics.endTimer(time)
      metrics.metrics.defaultRegistry.getTimers.get("gro-ni-timer").getCount shouldBe 1
    }

    "have a 200 status count for gro-ni" in {
      val metrics = GRONIMetrics
      metrics.status(200)
      metrics.metrics.defaultRegistry.getCounters.get("gro-ni-connector-status-200").getCount shouldBe 1
    }

    "have a 400 status count for gro-ni" in {
      val metrics = GRONIMetrics
      metrics.status(400)
      metrics.metrics.defaultRegistry.getCounters.get("gro-ni-connector-status-400").getCount shouldBe 1
    }

    "have a 404 status count for proxy" in {
      val metrics = GRONIMetrics
      metrics.status(404)
      metrics.metrics.defaultRegistry.getCounters.get("gro-ni-connector-status-404").getCount shouldBe 1
    }

    "have a 500 status count for gro-ni" in {
      val metrics = GRONIMetrics
      metrics.status(500)
      metrics.metrics.defaultRegistry.getCounters.get("gro-ni-connector-status-500").getCount shouldBe 1
    }

    "have a 502 status count for proxy" in {
      val metrics = GRONIMetrics
      metrics.status(502)
      metrics.metrics.defaultRegistry.getCounters.get("gro-ni-connector-status-502").getCount shouldBe 1
    }

    "have a 504 status count for proxy" in {
      val metrics = GRONIMetrics
      metrics.status(504)
      metrics.metrics.defaultRegistry.getCounters.get("gro-ni-connector-status-504").getCount shouldBe 1
    }

    "accept a status code not registered" in {
      val metrics = GRONIMetrics
      for (i <- 1 to 5) yield metrics.status(423)
      metrics.metrics.defaultRegistry.getCounters.get("gro-ni-connector-status-423").getCount shouldBe 5
    }

  }

  "MatchMetrics" should {

    "initialise" in {
      val metrics = MatchCountMetric
      metrics shouldBe a[BRMMetrics]
      metrics.prefix shouldBe "match"
    }

    "count" in {
      val metrics = MatchCountMetric
      metrics.count()
      metrics.metrics.defaultRegistry.getCounters.get("match-count").getCount shouldBe 1
    }

  }

  "NoMatchMetrics" should {

    "initialise" in {
      val metrics = NoMatchCountMetric
      metrics shouldBe a[BRMMetrics]
      metrics.prefix shouldBe "no-match"
    }

    "count" in {
      val metrics = NoMatchCountMetric
      metrics.count()
      metrics.metrics.defaultRegistry.getCounters.get("no-match-count").getCount shouldBe 1
      metrics.count()
      metrics.metrics.defaultRegistry.getCounters.get("no-match-count").getCount shouldBe 2
      metrics.metrics.defaultRegistry.getCounters.get("match-count").getCount should not be 2
    }

  }

  "WhereBirthRegisteredMetrics" should {

    "increment for england and wales" in {
      val metrics = EnglandAndWalesBirthRegisteredCountMetrics
      metrics.count()
      metrics.metrics.defaultRegistry.getCounters.get("england-and-wales-count").getCount shouldBe 1
    }

    "increment for northern ireland" in {
      val metrics = NorthernIrelandBirthRegisteredCountMetrics
      metrics.count()
      metrics.metrics.defaultRegistry.getCounters.get("northern-ireland-count").getCount shouldBe 1
    }

    "increment for scotland" in {
      val metrics = ScotlandBirthRegisteredCountMetrics
      metrics.count()
      metrics.metrics.defaultRegistry.getCounters.get("scotland-count").getCount shouldBe 1
    }

    "increment for invalid register" in {
      val metrics = InvalidBirthRegisteredCountMetrics
      metrics.count()
      metrics.metrics.defaultRegistry.getCounters.get("invalid-birth-registered-count").getCount shouldBe 1
    }

  }

  "API version" should {

    "increment for version 1.0" in{
      val metrics = GROReferenceMetrics
      APIVersionMetrics("1.0").count()
      metrics.metrics.defaultRegistry.getCounters.get("api-version-1.0").getCount shouldBe 1
    }

    "increment for version 5.0" in {
      val metrics = GROReferenceMetrics
      APIVersionMetrics("5.0").count()
      metrics.metrics.defaultRegistry.getCounters.get("api-version-5.0").getCount shouldBe 1
    }

    "increment for version 3.0 older should be null" in {
      val metrics = GROReferenceMetrics
      APIVersionMetrics("3.0").count()
      intercept[NullPointerException] {
        metrics.metrics.defaultRegistry.getCounters.get("api-version-3.0").getCount shouldBe 1
        metrics.metrics.defaultRegistry.getCounters.get("api-version-2.0").getCount
      }
    }

    "Audit-Source" should {
      "increment for audit-source" in {
        val metrics = GROReferenceMetrics
        AuditSourceMetrics("DFS").count()
        metrics.metrics.defaultRegistry.getCounters.get("audit-source-dfs").getCount shouldBe 1
      }
    }

  }

}
