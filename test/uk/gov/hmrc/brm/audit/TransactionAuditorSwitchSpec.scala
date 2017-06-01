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
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterAll
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.OneAppPerSuite
import org.specs2.mock.mockito.ArgumentCapture
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers._
import uk.gov.hmrc.brm.BaseConfig
import uk.gov.hmrc.brm.models.brm.Payload
import uk.gov.hmrc.brm.models.matching.MatchingResult
import uk.gov.hmrc.brm.models.response.gro.GROStatus
import uk.gov.hmrc.brm.models.response.{Child, Record}
import uk.gov.hmrc.brm.services.matching.Bad
import uk.gov.hmrc.brm.utils.BirthRegisterCountry
import uk.gov.hmrc.play.audit.http.connector.{AuditConnector, AuditResult}
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.Future

/**
  * Created by anuja on 16/02/17.
  */
class TransactionAuditorSwitchSpec extends UnitSpec with MockitoSugar with OneAppPerSuite with BeforeAndAfterAll {

  val connector = mock[AuditConnector]
  val auditor = new TransactionAuditor(connector)
  implicit val hc = HeaderCarrier()

  val auditConfigOnForDefault: Map[String, _] = BaseConfig.config ++ Map(
    "microservice.services.birth-registration-matching.features.logFlags.enabled" -> true,
    "microservice.services.birth-registration-matching.matching.ignoreAdditionalNames" -> true
  )

  val auditConfigOnForAlternate: Map[String, _] = BaseConfig.config ++ Map(
    "microservice.services.birth-registration-matching.matching.firstName" -> false,
    "microservice.services.birth-registration-matching.matching.lastName" -> false,
    "microservice.services.birth-registration-matching.matching.dateOfBirth" -> false,
    "microservice.services.birth-registration-matching.matching.matchOnMultiple" -> true,
    "microservice.services.birth-registration-matching.matching.ignoreAdditionalNames" -> false,
    "microservice.services.birth-registration-matching.features.logFlags.enabled" -> false,
    "microservice.services.birth-registration-matching.features.gro.enabled" -> false,
    "microservice.services.birth-registration-matching.features.gro.reference.enabled" -> false,
    "microservice.services.birth-registration-matching.features.gro.details.enabled" -> false

  )

  val auditConfigOnAppForDefault = GuiceApplicationBuilder(disabled = Seq(classOf[com.kenshoo.play.metrics.PlayModule])).configure(auditConfigOnForDefault).build()
  val auditConfigOnAppForAlternate = GuiceApplicationBuilder(disabled = Seq(classOf[com.kenshoo.play.metrics.PlayModule])).configure(auditConfigOnForAlternate).build()

  val child: Child = Child(123456789: Int, "Adam", "Test1", Some(LocalDate.now()))

  val status  = GROStatus(
    potentiallyFictitiousBirth = false,
    correction = Some("Correction on record"),
    cancelled = false,
    blockedRegistration = true,
    marginalNote = None,
    reRegistered = None)

  val record = Record(child, Option(status))

  "RequestsAndResultsAudit" should {

    "return correct default settings for audit config" in running(
      auditConfigOnAppForDefault
    ) {

      val payload = Payload(Some("123456789"), "Adam", None, "Test1", LocalDate.now(), BirthRegisterCountry.ENGLAND)

      val argumentCapture = new ArgumentCapture[AuditEvent]
      when(connector.sendEvent(argumentCapture.capture)(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
      val event = await(auditor.transaction(payload, List(record), MatchingResult.noMatch))
      event shouldBe AuditResult.Success

      argumentCapture.value.detail("features.matchFirstName") shouldBe "true"
      argumentCapture.value.detail("features.matchLastName") shouldBe "true"
      argumentCapture.value.detail("features.matchDateOfBirth") shouldBe "true"
      argumentCapture.value.detail("features.matchOnMultiple") shouldBe "false"
      argumentCapture.value.detail("features.ignoreMiddleNames") shouldBe "true"
      argumentCapture.value.detail("features.details.enabled") shouldBe "true"
      argumentCapture.value.detail("features.reference.enabled") shouldBe "true"
      argumentCapture.value.detail("features.downstream.enabled") shouldBe "true"
      argumentCapture.value.detail("records.record1.flags.marginalNote") shouldBe "None"
      argumentCapture.value.detail("records.record1.flags.blockedRegistration") shouldBe "true"
      argumentCapture.value.detail("records.record1.flags.reRegistered") shouldBe "None"
      argumentCapture.value.detail("records.record1.flags.cancelled") shouldBe "false"
      argumentCapture.value.detail("records.record1.flags.potentiallyFictitiousBirth") shouldBe "false"
      argumentCapture.value.detail("records.record1.flags.correction") shouldBe "Correction on record"
    }


    "return correct settings when audit config is overridden" in running(
      auditConfigOnAppForAlternate
    ) {
      val payload = Payload(Some("123456789"), "Adam", None, "Test", LocalDate.now(), BirthRegisterCountry.ENGLAND)

      val argumentCapture = new ArgumentCapture[AuditEvent]
      when(connector.sendEvent(argumentCapture.capture)(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
      val event = await(auditor.transaction(payload, List(record), MatchingResult.noMatch))
      event shouldBe AuditResult.Success

      argumentCapture.value.detail("features.matchFirstName") shouldBe "false"
      argumentCapture.value.detail("features.matchLastName") shouldBe "false"
      argumentCapture.value.detail("features.matchDateOfBirth") shouldBe "false"
      argumentCapture.value.detail("features.matchOnMultiple") shouldBe "true"
      argumentCapture.value.detail("features.ignoreMiddleNames") shouldBe "false"
      argumentCapture.value.detail("features.details.enabled") shouldBe "false"
      argumentCapture.value.detail("features.reference.enabled") shouldBe "false"
      argumentCapture.value.detail("features.downstream.enabled") shouldBe "false"
      argumentCapture.value.detail.contains("records.record1.flags.marginalNote") shouldBe false
      argumentCapture.value.detail.contains("records.record1.flags.marginalNote") shouldBe false
      argumentCapture.value.detail.contains("records.record1.flags.blockedRegistration") shouldBe false
      argumentCapture.value.detail.contains("records.record1.flags.reRegistered") shouldBe false
      argumentCapture.value.detail.contains("records.record1.flags.cancelled") shouldBe false
      argumentCapture.value.detail.contains("records.record1.flags.potentiallyFictitiousBirth") shouldBe false
      argumentCapture.value.detail.contains("records.record1.flags.correction") shouldBe false
    }

  }
}
