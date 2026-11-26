/*
 * Copyright 2026 HM Revenue & Customs
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

package uk.gov.hmrc.brm.utils

import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.JsValue
import uk.gov.hmrc.brm.config.BrmConfig
import uk.gov.hmrc.brm.connectors.{GROConnector, GRONIConnector, NRSConnector}

import java.time.LocalDate
import scala.concurrent.ExecutionContext

trait WireMockHelperTrait extends BaseUnitSpec with WireMockHelper {

  given ex: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  def ignoreAdditionalNames: Boolean

  override lazy val app: Application =
    new GuiceApplicationBuilder()
      .configure(
        Map(
          "microservice.services.birth-registration-matching.matching.ignoreAdditionalNames" -> ignoreAdditionalNames,
          "microservice.services.birth-registration-matching.host"                           -> "localhost",
          "microservice.services.birth-registration-matching.port"                           -> server.port(),
          "microservice.services.des.host"                                                   -> "localhost",
          "microservice.services.des.port"                                                   -> server.port(),
          "auditing.enabled"                                                                 -> false
        )
      )
      .build()

  // JSON property names
  protected val FORNAMES: String      = "forenames"
  protected val LASTNAME: String      = "lastname"
  protected val DATE_OF_BIRTH: String = "dateofbirth"

  // Common test data
  protected val dateOfBirth: LocalDate = LocalDate.of(2009, 7, 1)

  protected val nrsJsonResponseObject: JsValue = JsonUtils.getJsonFromFile("nrs", "2017734003")

  protected val nrsJsonResponseObjectWithoutAdditionalName: JsValue = JsonUtils.getJsonFromFile("nrs", "2017350006")

  protected val groJsonResponseObject = JsonUtils.getJsonFromFile("gro", "500035710")

  // Connectors
  protected lazy val groConnector: GROConnector = app.injector.instanceOf[GROConnector]

  protected lazy val nrsConnector: NRSConnector = app.injector.instanceOf[NRSConnector]

  protected lazy val groniConnector: GRONIConnector = app.injector.instanceOf[GRONIConnector]

  protected lazy val brmConfig: BrmConfig = app.injector.instanceOf[BrmConfig]

  // WireMock URLs
  protected val groReferenceUrl = "/birth-registration-matching-proxy/match/reference"

  protected val groDetailsUrl = "/birth-registration-matching-proxy/match/details"

  protected val nrsUrl = "/national-records/births"

}
