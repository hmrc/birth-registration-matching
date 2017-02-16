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

package uk.gov.hmrc.brm.audit

import org.joda.time.LocalDate
import org.mockito.Matchers
import org.mockito.Mockito.when
import org.scalatest.BeforeAndAfterAll
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.OneAppPerSuite
import org.specs2.mock.mockito.ArgumentCapture
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.brm.BaseConfig
import uk.gov.hmrc.brm.models.brm.Payload
import uk.gov.hmrc.brm.utils.BirthRegisterCountry
import uk.gov.hmrc.play.audit.http.connector.{AuditConnector, AuditResult}
import uk.gov.hmrc.play.test.UnitSpec
import play.api.test.Helpers._
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future

/**
  * Created by anuja on 16/02/17.
  */
class ConfigSwitchToggleAuditSpec extends UnitSpec with MockitoSugar with OneAppPerSuite with BeforeAndAfterAll{

  val connector = mock[AuditConnector]
  val auditor = new RequestsAndResultsAudit(connector)
  implicit val hc = HeaderCarrier()

  val auditSwitchesOnForSuccess: Map[String, _] = BaseConfig.config ++ Map(
    "microservice.services.birth-registration-matching.firstName" -> true,
    "microservice.services.birth-registration-matching.lastName" -> true,
    "microservice.services.birth-registration-matching.dateOfBirth" -> true,
    "microservice.services.birth-registration-matching.matchOnMultiple" -> false,
    "microservice.services.birth-registration-matching.disableSearchByDetails" -> false,
    "microservice.services.birth-registration-matching.ignoreMiddleNames" -> true
  )

  val auditSwitchesOnForFailure: Map[String, _] = BaseConfig.config ++ Map(
    "microservice.services.birth-registration-matching.firstName" -> false,
    "microservice.services.birth-registration-matching.lastName" -> false,
    "microservice.services.birth-registration-matching.dateOfBirth" -> false,
    "microservice.services.birth-registration-matching.matchOnMultiple" -> true,
    "microservice.services.birth-registration-matching.disableSearchByDetails" -> true,
    "microservice.services.birth-registration-matching.ignoreMiddleNames" -> false
  )
  val auditSwitchOnAppForSuccess = GuiceApplicationBuilder(disabled = Seq(classOf[com.kenshoo.play.metrics.PlayModule])).configure(auditSwitchesOnForSuccess).build()
  val auditSwitchOnAppForFailure = GuiceApplicationBuilder(disabled = Seq(classOf[com.kenshoo.play.metrics.PlayModule])).configure(auditSwitchesOnForFailure).build()


  "RequestsAndResultsAudit" should {

    "return true to set all config related values as expected to audit" in running(
      auditSwitchOnAppForSuccess
    ) {
      val event = Map("match" -> "true")
      val payload = Payload(Some("123456789"), "Adam", "Test", LocalDate.now(), BirthRegisterCountry.ENGLAND)
      val argumentCapture = new ArgumentCapture[AuditEvent]
      when(connector.sendEvent(argumentCapture.capture)(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
      val result = await(auditor.audit(event, Some(payload)))
      result shouldBe AuditResult.Success

      println("*****************" + argumentCapture.values)
      argumentCapture.value.detail("features.matchFirstName") shouldBe "true"
      argumentCapture.value.detail("features.matchLastName") shouldBe "true"
      argumentCapture.value.detail("features.matchDateOfBirth") shouldBe "true"
      argumentCapture.value.detail("features.matchOnMultiple") shouldBe "false"
      argumentCapture.value.detail("features.disableSearchByDetails") shouldBe "false"
      argumentCapture.value.detail("features.ignoreMiddleNames") shouldBe "true"
    }


    "return false to set all config related values as unexpected to audit" in running(
      auditSwitchOnAppForFailure
    ) {
      val event = Map("match" -> "true")
      val payload = Payload(Some("123456789"), "Adam", "Test", LocalDate.now(), BirthRegisterCountry.ENGLAND)
      val argumentCapture = new ArgumentCapture[AuditEvent]
      when(connector.sendEvent(argumentCapture.capture)(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
      val result = await(auditor.audit(event, Some(payload)))
      println("*****************" + result.value)
      result shouldBe AuditResult.Success

      println("*****************" + argumentCapture.values)
      argumentCapture.value.detail("features.matchFirstName") shouldBe "false"
      argumentCapture.value.detail("features.matchLastName") shouldBe "false"
      argumentCapture.value.detail("features.matchDateOfBirth") shouldBe "false"
      //        argumentCapture.value.detail("features.matchOnMultiple") shouldBe "true"
      //        argumentCapture.value.detail("features.disableSearchByDetails") shouldBe "true"
      //        argumentCapture.value.detail("features.ignoreMiddleNames") shouldBe "false"
    }

  }
}
