/*
 * Copyright 2023 HM Revenue & Customs
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

//import com.kenshoo.play.metrics.Metrics
import org.apache.pekko.http.scaladsl.model.StatusCodes._
import com.codahale.metrics.MetricRegistry
import java.time.LocalDate
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.brm.implicits.MetricsFactory
import uk.gov.hmrc.brm.metrics._
import uk.gov.hmrc.brm.models.brm.Payload
import uk.gov.hmrc.brm.utils.BirthRegisterCountry
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatest.OptionValues
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.inject.guice.GuiceApplicationBuilder

class MetricsSpec extends AnyWordSpecLike with Matchers with OptionValues with GuiceOneAppPerSuite with MockitoSugar {
  override lazy val app = new GuiceApplicationBuilder()
    .configure("metrics.enabled" -> true)
    .build()

  "MetricsFactory" should {

    "return England and Wales metrics for reference" in {
      implicit val payload =
        Payload(Some("123456789"), "Adam", None, "Wilson", LocalDate.now(), BirthRegisterCountry.ENGLAND)
      app.injector.instanceOf[MetricsFactory].getMetrics()
    }

  }

  "GROReferenceMetrics" should {

    val metrics = app.injector.instanceOf[GROReferenceMetrics]

    "initialise" in {
      metrics        shouldBe a[BRMMetrics]
      metrics.prefix shouldBe "proxy"
    }

    "have a timer for the proxy connection" in {
      val time = metrics.startTimer()
      metrics.endTimer(time)
      metrics.metrics.getTimers.get("proxy-timer").getCount shouldBe 1
    }

    "have a 200 status count for proxy" in {
      metrics.status(OK.intValue)
      metrics.metrics.getCounters.get("proxy-connector-status-200").getCount shouldBe 1
    }

    "have a 400 status count for proxy" in {
      metrics.status(BadRequest.intValue)
      metrics.metrics.getCounters.get("proxy-connector-status-400").getCount shouldBe 1
    }

    "have a 404 status count for proxy" in {
      metrics.status(NotFound.intValue)
      metrics.metrics.getCounters.get("proxy-connector-status-404").getCount shouldBe 1
    }

    "have a 500 status count for proxy" in {
      metrics.status(InternalServerError.intValue)
      metrics.metrics.getCounters.get("proxy-connector-status-500").getCount shouldBe 1
    }

    "have a 502 status count for proxy" in {
      metrics.status(BadGateway.intValue)
      metrics.metrics.getCounters.get("proxy-connector-status-502").getCount shouldBe 1
    }

    "have a 504 status count for proxy" in {
      metrics.status(GatewayTimeout.intValue)
      metrics.metrics.getCounters.get("proxy-connector-status-504").getCount shouldBe 1
    }

    "accept a status code not registered" in {
      for (i <- 1 to 5) yield metrics.status(Locked.intValue)
      metrics.metrics.getCounters.get("proxy-connector-status-423").getCount shouldBe 5
    }

  }

  "GRODetailsMetrics" should {

    val metrics = app.injector.instanceOf[GRODetailsMetrics]

    "initialise" in {
      metrics        shouldBe a[BRMMetrics]
      metrics.prefix shouldBe "proxy-details"
    }
    "have a timer for the proxy-details connection" in {
      val time = metrics.startTimer()
      metrics.endTimer(time)
      metrics.metrics.getTimers.get("proxy-details-timer").getCount shouldBe 1
    }
    "have a 200 status count for proxy-details" in {
      metrics.status(OK.intValue)
      metrics.metrics.getCounters.get("proxy-details-connector-status-200").getCount shouldBe 1
    }

    "have a 400 status count for proxy-details" in {
      metrics.status(BadRequest.intValue)
      metrics.metrics.getCounters.get("proxy-details-connector-status-400").getCount shouldBe 1
    }

    "have a 404 status count for proxy-details" in {
      metrics.status(NotFound.intValue)
      metrics.metrics.getCounters.get("proxy-details-connector-status-404").getCount shouldBe 1
    }

    "have a 500 status count for proxy-details" in {
      metrics.status(InternalServerError.intValue)
      metrics.metrics.getCounters.get("proxy-details-connector-status-500").getCount shouldBe 1
    }

    "have a 502 status count for proxy-details" in {
      metrics.status(BadGateway.intValue)
      metrics.metrics.getCounters.get("proxy-details-connector-status-502").getCount shouldBe 1
    }

    "have a 504 status count for proxy-details" in {
      metrics.status(GatewayTimeout.intValue)
      metrics.metrics.getCounters.get("proxy-details-connector-status-504").getCount shouldBe 1
    }

    "accept a status code not registered" in {
      for (i <- 1 to 5) yield metrics.status(Locked.intValue)
      metrics.metrics.getCounters.get("proxy-details-connector-status-423").getCount shouldBe 5
    }

  }

  "NRSMetrics" should {

    val metrics = app.injector.instanceOf[NRSMetrics]

    "initialise" in {
      metrics        shouldBe a[BRMMetrics]
      metrics.prefix shouldBe "nrs"
    }

    "have a timer for the nrs connection" in {
      val time = metrics.startTimer()
      metrics.endTimer(time)
      metrics.metrics.getTimers.get("nrs-timer").getCount shouldBe 1
    }

    "have a 200 status count for nrs" in {
      metrics.status(OK.intValue)
      metrics.metrics.getCounters.get("nrs-connector-status-200").getCount shouldBe 1
    }

    "have a 400 status count for nrs" in {
      metrics.status(BadRequest.intValue)
      metrics.metrics.getCounters.get("nrs-connector-status-400").getCount shouldBe 1
    }

    "have a 404 status count for proxy" in {
      metrics.status(NotFound.intValue)
      metrics.metrics.getCounters.get("nrs-connector-status-404").getCount shouldBe 1
    }

    "have a 500 status count for nrs" in {
      metrics.status(InternalServerError.intValue)
      metrics.metrics.getCounters.get("nrs-connector-status-500").getCount shouldBe 1
    }

    "have a 502 status count for proxy" in {
      metrics.status(BadGateway.intValue)
      metrics.metrics.getCounters.get("nrs-connector-status-502").getCount shouldBe 1
    }

    "have a 504 status count for proxy" in {
      metrics.status(GatewayTimeout.intValue)
      metrics.metrics.getCounters.get("nrs-connector-status-504").getCount shouldBe 1
    }

    "accept a status code not registered" in {
      for (i <- 1 to 5) yield metrics.status(Locked.intValue)
      metrics.metrics.getCounters.get("nrs-connector-status-423").getCount shouldBe 5
    }

  }

  "GRONIMetrics" should {

    val metrics = app.injector.instanceOf[GRONIMetrics]

    "initialise" in {
      metrics        shouldBe a[BRMMetrics]
      metrics.prefix shouldBe "gro-ni"
    }

    "have a timer for the gro-ni connection" in {
      val time = metrics.startTimer()
      metrics.endTimer(time)
      metrics.metrics.getTimers.get("gro-ni-timer").getCount shouldBe 1
    }

    "have a 200 status count for gro-ni" in {
      metrics.status(OK.intValue)
      metrics.metrics.getCounters.get("gro-ni-connector-status-200").getCount shouldBe 1
    }

    "have a 400 status count for gro-ni" in {
      metrics.status(BadRequest.intValue)
      metrics.metrics.getCounters.get("gro-ni-connector-status-400").getCount shouldBe 1
    }

    "have a 404 status count for proxy" in {
      metrics.status(NotFound.intValue)
      metrics.metrics.getCounters.get("gro-ni-connector-status-404").getCount shouldBe 1
    }

    "have a 500 status count for gro-ni" in {
      metrics.status(InternalServerError.intValue)
      metrics.metrics.getCounters.get("gro-ni-connector-status-500").getCount shouldBe 1
    }

    "have a 502 status count for proxy" in {
      metrics.status(BadGateway.intValue)
      metrics.metrics.getCounters.get("gro-ni-connector-status-502").getCount shouldBe 1
    }

    "have a 504 status count for proxy" in {
      metrics.status(GatewayTimeout.intValue)
      metrics.metrics.getCounters.get("gro-ni-connector-status-504").getCount shouldBe 1
    }

    "accept a status code not registered" in {
      for (i <- 1 to 5) yield metrics.status(Locked.intValue)
      metrics.metrics.getCounters.get("gro-ni-connector-status-423").getCount shouldBe 5
    }

  }

  "MatchMetrics" should {

    val metrics = app.injector.instanceOf[MatchCountMetric]

    "initialise" in {
      metrics        shouldBe a[BRMMetrics]
      metrics.prefix shouldBe "match"
    }

    "count" in {
      metrics.count()
      metrics.metrics.getCounters.get("match-count").getCount shouldBe 1
    }

  }

  "NoMatchMetrics" should {

    val metrics = app.injector.instanceOf[NoMatchCountMetric]

    "initialise" in {
      metrics        shouldBe a[BRMMetrics]
      metrics.prefix shouldBe "no-match"
    }

    "count" in {
      metrics.count()
      metrics.metrics.getCounters.get("no-match-count").getCount shouldBe 1
      metrics.count()
      metrics.metrics.getCounters.get("no-match-count").getCount shouldBe 2
      metrics.metrics.getCounters.get("match-count").getCount      should not be 2
    }

  }

  "WhereBirthRegisteredMetrics" should {

    "increment for england and wales" in {
      val metrics = app.injector.instanceOf[EnglandAndWalesBirthRegisteredCountMetrics]
      metrics.count()
      metrics.metrics.getCounters.get("england-and-wales-count").getCount shouldBe 1
    }

    "increment for northern ireland" in {
      val metrics = app.injector.instanceOf[NorthernIrelandBirthRegisteredCountMetrics]
      metrics.count()
      metrics.metrics.getCounters.get("northern-ireland-count").getCount shouldBe 1
    }

    "increment for scotland" in {
      val metrics = app.injector.instanceOf[ScotlandBirthRegisteredCountMetrics]
      metrics.count()
      metrics.metrics.getCounters.get("scotland-count").getCount shouldBe 1
    }

    "increment for invalid register" in {
      val metrics = app.injector.instanceOf[InvalidBirthRegisteredCountMetrics]
      metrics.count()
      metrics.metrics.getCounters.get("invalid-birth-registered-count").getCount shouldBe 1
    }

  }

  "API version" should {

    val metrics = app.injector.instanceOf[GROReferenceMetrics]

    "increment for version 1.0" in {
      new APIVersionMetrics(app.injector.instanceOf[MetricRegistry]).count()
      metrics.metrics.getCounters.get("api-version-1.0").getCount shouldBe 1
    }

    "Audit-Source" should {
      "increment for audit-source" in {
        new AuditSourceMetrics(app.injector.instanceOf[MetricRegistry]).count("dfs")
        metrics.metrics.getCounters.get("audit-source-dfs").getCount shouldBe 1
      }
    }

  }

}
