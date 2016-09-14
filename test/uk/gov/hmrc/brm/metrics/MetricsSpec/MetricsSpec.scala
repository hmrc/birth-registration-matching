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

package uk.gov.hmrc.brm.metrics.MetricsSpec

import java.util.concurrent.TimeUnit

import com.kenshoo.play.metrics.MetricsRegistry
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.brm.metrics.Metrics
import uk.gov.hmrc.play.test.{WithFakeApplication, UnitSpec}

/**
 * Created by adamconder on 14/09/2016.
 */
class MetricsSpec extends UnitSpec with WithFakeApplication with MockitoSugar {

  "metrics" should {

    "initialise" in {
      val metrics = Metrics
      metrics shouldBe a [Metrics]
    }

    "have a timer for the proxy connection" in {
      val metrics = Metrics
      metrics.proxyConnectorTimer(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
      MetricsRegistry.defaultRegistry.getTimers.get("proxy-connector-timer").getCount shouldBe 1
    }

    "have a 200 status count for proxy" in {
      val metrics = Metrics
      metrics.proxyConnectorStatus(200)
      MetricsRegistry.defaultRegistry.getCounters.get("proxy-connector-status-200").getCount shouldBe 1
    }

    "have a 400 status count for proxy" in {
      val metrics = Metrics
      metrics.proxyConnectorStatus(400)
      MetricsRegistry.defaultRegistry.getCounters.get("proxy-connector-status-400").getCount shouldBe 1
    }

    "have a 500 status count for proxy" in {
      val metrics = Metrics
      metrics.proxyConnectorStatus(500)
      MetricsRegistry.defaultRegistry.getCounters.get("proxy-connector-status-500").getCount shouldBe 1
    }

    "have a match counter" in {
      val metrics = Metrics
      metrics.matchCount()
      MetricsRegistry.defaultRegistry.getCounters.get("match-count").getCount shouldBe 1
    }

    "have a no match counter" in {
      val metrics = Metrics
      metrics.noMatchCount()
      MetricsRegistry.defaultRegistry.getCounters.get("no-match-count").getCount shouldBe 1
    }

  }

}
