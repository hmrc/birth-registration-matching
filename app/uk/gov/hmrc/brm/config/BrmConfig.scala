/*
 * Copyright 2021 HM Revenue & Customs
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

package uk.gov.hmrc.brm.config

import javax.inject.Inject
import uk.gov.hmrc.brm.models.brm.Payload
import uk.gov.hmrc.brm.switches.SwitchException
import uk.gov.hmrc.brm.utils.BirthRegisterCountry
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig


class BrmConfig @Inject()(val conf: ServicesConfig) extends SwitchException {

  case class DesException(switch: String) extends RuntimeException(s"des.$switch configuration not found")
  def matchFirstName: Boolean = conf.getConfBool("birth-registration-matching.matching.firstName",  defBool = true)
  def matchLastName: Boolean = conf.getConfBool("birth-registration-matching.matching.lastName",  defBool = true)
  def matchDateOfBirth: Boolean = conf.getConfBool("birth-registration-matching.matching.dateOfBirth",  defBool = true)
  def matchOnMultiple: Boolean = conf.getConfBool("birth-registration-matching.matching.matchOnMultiple", defBool = false)

  def logFlags: Boolean = conf.getConfBool("birth-registration-matching.features.flags.logging", defBool = true)
  def processFlags: Boolean = conf.getConfBool("birth-registration-matching.features.flags.process", defBool = true)
  def validateFlag(api: String, flag: String): Boolean = conf.getConfBool(s"birth-registration-matching.features.$api.flags.$flag.process", defBool = false)

  def ignoreAdditionalNames: Boolean = conf.getConfBool("birth-registration-matching.matching.ignoreAdditionalNames",
    throw MatchingConfigurationException("ignoreAdditionalNames"))

  private def featureEnabled(api: String, requestType: Option[RequestType])  = {
    val path = requestType.fold(s"birth-registration-matching.features.$api.enabled") { x => s"birth-registration-matching.features.$api.${x.value}.enabled" }
    conf.getConfBool(path, throw FeatureSwitchException(api))
  }

  abstract class RequestType(val value: String)
  object ReferenceRequest extends RequestType("reference")
  object DetailsRequest extends RequestType("details")

  private def isDownstreamEnabled(payload: Option[Payload], requestType: Option[RequestType]): Boolean = payload match {
    case Some(p) =>
      p.whereBirthRegistered match {
        case BirthRegisterCountry.ENGLAND | BirthRegisterCountry.WALES =>
          featureEnabled("gro", requestType)
        case BirthRegisterCountry.SCOTLAND =>
          featureEnabled("nrs", requestType)
        case BirthRegisterCountry.NORTHERN_IRELAND =>
          featureEnabled("groni", requestType)
      }
    case None =>
      false
  }

  def audit(p: Option[Payload] = None): Map[String, String] = {
    val featuresPrefix = "features"

    Map(
      s"$featuresPrefix.matchFirstName" -> matchFirstName.toString,
      s"$featuresPrefix.matchLastName" -> matchLastName.toString,
      s"$featuresPrefix.matchDateOfBirth" -> matchDateOfBirth.toString,
      s"$featuresPrefix.matchOnMultiple" -> matchOnMultiple.toString,
      s"$featuresPrefix.ignoreMiddleNames" -> ignoreAdditionalNames.toString,
      s"$featuresPrefix.downstream.enabled" -> isDownstreamEnabled(p, None).toString,
      s"$featuresPrefix.reference.enabled" -> isDownstreamEnabled(p, Some(ReferenceRequest)).toString,
      s"$featuresPrefix.details.enabled" -> isDownstreamEnabled(p, Some(DetailsRequest)).toString,
      s"$featuresPrefix.flags.logging" -> logFlags.toString,
      s"$featuresPrefix.flags.process" -> processFlags.toString
    )
  }

  def desEnv: String = conf.getConfString("des.env", throw DesException("env"))
  def desToken: String = conf.getConfString("des.auth-token", throw DesException("auth-token"))

  val serviceUrl: String = conf.baseUrl("birth-registration-matching")
  val desUrl: String = conf.baseUrl("des")

}
