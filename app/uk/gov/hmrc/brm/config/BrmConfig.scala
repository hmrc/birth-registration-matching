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

package uk.gov.hmrc.brm.config

import uk.gov.hmrc.brm.models.brm.Payload
import uk.gov.hmrc.brm.utils.BirthRegisterCountry
import uk.gov.hmrc.play.config.ServicesConfig

trait BrmConfig extends ServicesConfig {

  case class BirthConfigurationException(switch: String) extends RuntimeException {
    override def toString: String = s"birth-registration-matching.matching.$switch configuration not found"
  }

  case class DesException(switch: String) extends RuntimeException {
    override def toString: String = s"des.$switch configuration not found"
  }

  private val defaultDate: Int = 1900
  private val characterMaxLength: Int = 250

  def minimumDateOfBirthYear: Int = getConfInt("birth-registration-matching.minimumDateOfBirthYear", defaultDate)
  def nameMaxLength : Int = getConfInt("birth-registration-matching.validation.maxNameLength", characterMaxLength)

  def matchFirstName : Boolean = getConfBool("birth-registration-matching.matching.firstName",  defBool = true)
  def matchLastName : Boolean = getConfBool("birth-registration-matching.matching.lastName",  defBool = true)
  def matchDateOfBirth : Boolean = getConfBool("birth-registration-matching.matching.dateOfBirth",  defBool = true)
  def matchOnMultiple : Boolean = getConfBool("birth-registration-matching.matching.matchOnMultiple", defBool = false)
  def logFlags : Boolean = getConfBool("birth-registration-matching.features.logFlags.enabled", defBool = false)

  val ignoreMiddleNamesRegex : String = getConfString("birth-registration-matching.matching.ignoreMiddleNamesRegex",
    throw BirthConfigurationException("ignoreMiddleNames"))

  def ignoreAdditionalNames : Boolean = getConfBool("birth-registration-matching.matching.ignoreAdditionalNames",
    throw BirthConfigurationException("ignoreAdditionalNames"))


  private def featureEnabled(api : String, requestType : Option[RequestType] = None)  = {
    val path = requestType.fold(s"birth-registration-matching.features.$api.enabled") { x => s"birth-registration-matching.features.$api.${x.value}.enabled" }
    getConfBool(path,
      throw BirthConfigurationException(
        s"birth-registration-matching.features.$api.enabled"
      ))
  }

  abstract class RequestType(val value : String)
  object ReferenceRequest extends RequestType("reference")
  object DetailsRequest extends RequestType("details")

  private def isDownstreamEnabled(payload: Option[Payload] = None, requestType: Option[RequestType] = None) : Boolean = payload match {
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

  def audit(p: Option[Payload] = None) : Map[String, String] = {
    val featuresPrefix = "features"

    Map(
      s"$featuresPrefix.matchFirstName" -> BrmConfig.matchFirstName.toString,
      s"$featuresPrefix.matchLastName" -> BrmConfig.matchLastName.toString,
      s"$featuresPrefix.matchDateOfBirth" -> BrmConfig.matchDateOfBirth.toString,
      s"$featuresPrefix.matchOnMultiple" -> BrmConfig.matchOnMultiple.toString,
      s"$featuresPrefix.ignoreMiddleNames" -> BrmConfig.ignoreAdditionalNames.toString,
      s"$featuresPrefix.downstream.enabled" -> isDownstreamEnabled(p, None).toString,
      s"$featuresPrefix.reference.enabled" -> isDownstreamEnabled(p, Some(ReferenceRequest)).toString,
      s"$featuresPrefix.details.enabled" -> isDownstreamEnabled(p, Some(DetailsRequest)).toString
    )
  }

  def desHost: String = getConfString("des.host", throw DesException("host"))
  def desPort: String = getConfString("des.port", throw DesException("port"))
  def desEnv: String = getConfString("des.env", throw DesException("env"))
  def desToken: String = getConfString("des.auth-token", throw DesException("auth-token"))

}

object BrmConfig extends BrmConfig
