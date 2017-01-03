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

  "ProxyMetrics" should {

    "initialise" in {
      val metrics = ProxyMetrics
      metrics shouldBe a [BRMMetrics]
    }

    "have a timer for the proxy connection" in {
      val metrics = ProxyMetrics
      metrics.time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
      metrics.metrics.defaultRegistry.getTimers.get("proxy-timer").getCount shouldBe 1
    }

    "have a 200 status count for proxy" in {
      val metrics = ProxyMetrics
      metrics.connectorStatus(200)
      metrics.metrics.defaultRegistry.getCounters.get("proxy-connector-status-200").getCount shouldBe 1
    }

    "have a 400 status count for proxy" in {
      val metrics = ProxyMetrics
      metrics.connectorStatus(400)
      metrics.metrics.defaultRegistry.getCounters.get("proxy-connector-status-400").getCount shouldBe 1
    }

    "have a 404 status count for proxy" in {
      val metrics = ProxyMetrics
      metrics.connectorStatus(404)
      metrics.metrics.defaultRegistry.getCounters.get("proxy-connector-status-404").getCount shouldBe 1
    }

    "have a 500 status count for proxy" in {
      val metrics = ProxyMetrics
      metrics.connectorStatus(500)
      metrics.metrics.defaultRegistry.getCounters.get("proxy-connector-status-500").getCount shouldBe 1
    }

    "have a 502 status count for proxy" in {
      val metrics = ProxyMetrics
      metrics.connectorStatus(502)
      metrics.metrics.defaultRegistry.getCounters.get("proxy-connector-status-502").getCount shouldBe 1
    }

    "have a 504 status count for proxy" in {
      val metrics = ProxyMetrics
      metrics.connectorStatus(504)
      metrics.metrics.defaultRegistry.getCounters.get("proxy-connector-status-504").getCount shouldBe 1
    }

    "accept a status code not registered" in {
      val metrics = ProxyMetrics
      for (i <- 1 to 5) yield metrics.connectorStatus(423)
      metrics.metrics.defaultRegistry.getCounters.get("proxy-connector-status-423").getCount shouldBe 5
    }

  }

  "NRSMetrics" should {

    "initialise" in {
      val metrics = NRSMetrics
      metrics shouldBe a[BRMMetrics]
    }

    "have a timer for the nrs connection" in {
      val metrics = NRSMetrics
      metrics.time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
      metrics.metrics.defaultRegistry.getTimers.get("nrs-timer").getCount shouldBe 1
    }

    "have a 200 status count for nrs" in {
      val metrics = NRSMetrics
      metrics.connectorStatus(200)
      metrics.metrics.defaultRegistry.getCounters.get("nrs-connector-status-200").getCount shouldBe 1
    }

    "have a 400 status count for nrs" in {
      val metrics = NRSMetrics
      metrics.connectorStatus(400)
      metrics.metrics.defaultRegistry.getCounters.get("nrs-connector-status-400").getCount shouldBe 1
    }

    "have a 404 status count for proxy" in {
      val metrics = NRSMetrics
      metrics.connectorStatus(404)
      metrics.metrics.defaultRegistry.getCounters.get("nrs-connector-status-404").getCount shouldBe 1
    }

    "have a 500 status count for nrs" in {
      val metrics = NRSMetrics
      metrics.connectorStatus(500)
      metrics.metrics.defaultRegistry.getCounters.get("nrs-connector-status-500").getCount shouldBe 1
    }

    "have a 502 status count for proxy" in {
      val metrics = NRSMetrics
      metrics.connectorStatus(502)
      metrics.metrics.defaultRegistry.getCounters.get("nrs-connector-status-502").getCount shouldBe 1
    }

    "have a 504 status count for proxy" in {
      val metrics = NRSMetrics
      metrics.connectorStatus(504)
      metrics.metrics.defaultRegistry.getCounters.get("nrs-connector-status-504").getCount shouldBe 1
    }

    "accept a status code not registered" in {
      val metrics = NRSMetrics
      for (i <- 1 to 5) yield metrics.connectorStatus(423)
      metrics.metrics.defaultRegistry.getCounters.get("nrs-connector-status-423").getCount shouldBe 5
    }

  }

  "GRONIMetrics" should {

    "initialise" in {
      val metrics = GRONIMetrics
      metrics shouldBe a [BRMMetrics]
    }

    "have a timer for the gro-ni connection" in {
      val metrics = GRONIMetrics
      metrics.time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
      metrics.metrics.defaultRegistry.getTimers.get("gro-ni-timer").getCount shouldBe 1
    }

    "have a 200 status count for gro-ni" in {
      val metrics = GRONIMetrics
      metrics.connectorStatus(200)
      metrics.metrics.defaultRegistry.getCounters.get("gro-ni-connector-status-200").getCount shouldBe 1
    }

    "have a 400 status count for gro-ni" in {
      val metrics = GRONIMetrics
      metrics.connectorStatus(400)
      metrics.metrics.defaultRegistry.getCounters.get("gro-ni-connector-status-400").getCount shouldBe 1
    }

    "have a 404 status count for proxy" in {
      val metrics = GRONIMetrics
      metrics.connectorStatus(404)
      metrics.metrics.defaultRegistry.getCounters.get("gro-ni-connector-status-404").getCount shouldBe 1
    }

    "have a 500 status count for gro-ni" in {
      val metrics = GRONIMetrics
      metrics.connectorStatus(500)
      metrics.metrics.defaultRegistry.getCounters.get("gro-ni-connector-status-500").getCount shouldBe 1
    }

    "have a 502 status count for proxy" in {
      val metrics = GRONIMetrics
      metrics.connectorStatus(502)
      metrics.metrics.defaultRegistry.getCounters.get("gro-ni-connector-status-502").getCount shouldBe 1
    }

    "have a 504 status count for proxy" in {
      val metrics = GRONIMetrics
      metrics.connectorStatus(504)
      metrics.metrics.defaultRegistry.getCounters.get("gro-ni-connector-status-504").getCount shouldBe 1
    }

    "accept a status code not registered" in {
      val metrics = GRONIMetrics
      for (i <- 1 to 5) yield metrics.connectorStatus(423)
      metrics.metrics.defaultRegistry.getCounters.get("gro-ni-connector-status-423").getCount shouldBe 5
    }

  }

  "MatchMetrics" should {

    "initialise" in {
      val metrics = MatchMetrics
      metrics shouldBe a [BRMMetrics]
    }


    "have a match counter" in {
      val metrics = MatchMetrics
      metrics.matchCount()
      metrics.metrics.defaultRegistry.getCounters.get("match-count").getCount shouldBe 1
    }

    "have a no match counter" in {
      val metrics = MatchMetrics
      metrics.noMatchCount()
      metrics.metrics.defaultRegistry.getCounters.get("no-match-count").getCount shouldBe 1
    }

  }

  "WhereBirthRegisteredMetrics" should {

    "increment for england and wales" in {
      val metrics = EnglandAndWalesBirthRegisteredMetrics
      metrics.count()
      metrics.metrics.defaultRegistry.getCounters.get("england-and-wales-count").getCount shouldBe 1
    }

    "increment for northern ireland" in {
      val metrics = NorthernIrelandBirthRegisteredMetrics
      metrics.count()
      metrics.metrics.defaultRegistry.getCounters.get("northern-ireland-count").getCount shouldBe 1
    }

    "increment for scotland" in {
      val metrics = ScotlandBirthRegisteredMetrics
      metrics.count()
      metrics.metrics.defaultRegistry.getCounters.get("scotland-count").getCount shouldBe 1
    }

    "increment for invalid register" in {
      val metrics = InvalidBirthRegisteredMetrics
      metrics.count()
      metrics.metrics.defaultRegistry.getCounters.get("invalid-birth-registered-count").getCount shouldBe 1
    }

  }

  "API version" should {

    "increment for version 1.0" in{
      val metrics = ProxyMetrics
      APIVersionMetrics("1.0").count()
      metrics.metrics.defaultRegistry.getCounters.get("api-version-1.0").getCount shouldBe 1
    }

    "increment for version 5.0" in {
      val metrics = ProxyMetrics
      APIVersionMetrics("5.0").count()
      metrics.metrics.defaultRegistry.getCounters.get("api-version-5.0").getCount shouldBe 1
    }

    "increment for version 3.0 older should be null" in {
      val metrics = ProxyMetrics
      APIVersionMetrics("3.0").count()
      intercept[NullPointerException] {
        metrics.metrics.defaultRegistry.getCounters.get("api-version-3.0").getCount shouldBe 1
        metrics.metrics.defaultRegistry.getCounters.get("api-version-2.0").getCount
      }
    }

    "Audit-Source" should {
      "increment for audit-source" in {
        val metrics = ProxyMetrics
        AuditSourceMetrics("DFS").count()
        metrics.metrics.defaultRegistry.getCounters.get("audit-source-dfs").getCount shouldBe 1
      }
    }

  }

}
