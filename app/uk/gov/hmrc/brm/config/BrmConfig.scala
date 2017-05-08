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
import uk.gov.hmrc.brm.utils.{BRMLogger, BirthRegisterCountry}
import uk.gov.hmrc.play.config.ServicesConfig

trait BrmConfig extends ServicesConfig {

  case class BirthConfigurationException(switch: String) extends RuntimeException {
    override def toString: String = {
      val m = s"birth-registration-matching.matching.$switch configuration not found"
      m
    }
  }

  case class DesException(switch: String) extends RuntimeException {
    override def toString: String = {
      val m = s"des.$switch configuration not found"
      m
    }
  }

  val defaultDate: Int = 1900

  def minimumDateOfBirthYear: Int = getConfInt("birth-registration-matching.minimumDateOfBirthYear", defaultDate)

  def matchFirstName : Boolean = getConfBool("birth-registration-matching.matching.firstName",  defBool = true)
  def matchLastName : Boolean = getConfBool("birth-registration-matching.matching.lastName",  defBool = true)
  def matchDateOfBirth : Boolean = getConfBool("birth-registration-matching.matching.dateOfBirth",  defBool = true)

  def matchOnMultiple : Boolean = getConfBool("birth-registration-matching.matching.matchOnMultiple", defBool = false)

  def logFlags : Boolean = {
    val status = getConfBool("birth-registration-matching.features.logFlags.enabled", defBool = false)
    BRMLogger.info("Brmconfig", "logFlags", status.toString)
    status
  }

  def nameMaxLength : Int = getConfInt("birth-registration-matching.validation.maxNameLength", 250)

  val ignoreMiddleNamesRegex : String = getConfString("birth-registration-matching.matching.ignoreMiddleNamesRegex",
    throw BirthConfigurationException("ignoreMiddleNames"))

  def ignoreMiddleNames : Boolean = getConfBool("birth-registration-matching.matching.ignoreMiddleNames",
    throw BirthConfigurationException("ignoreMiddleNames"))

  def apiEnabled(api : String)  = {
    getConfBool(s"birth-registration-matching.features.$api.enabled",throw BirthConfigurationException(s"$api.enabled"))
  }
  def keyEnabled(api : String, requestType : RequestType)  = {
    getConfBool(s"birth-registration-matching.features.$api.${requestType.value}.enabled",throw BirthConfigurationException(s"$key.enabled"))
  }

  /**
    *
    * def isReferenceEnabled(payload: Payload) : Boolean = {
    *   payload.whereBirthRegistered match {
    *   case England | Wales =>
    *    // load this object from config for GRO
    *            gro {
          enabled = true
          reference.enabled = true
          details.enabled = true
        }
        then call {Object}.reference.enabled
    *   case Scotland =>
    *   // load nrs object then call reference.enabled
    *   case Northern Ireland =>
    *   case _ => false
    * }
    */

  abstract class RequestType(val value : String)
  object ReferenceRequest extends RequestType("reference")
  object DetailsRequest extends RequestType("details")


  def isDownstreamEnabled(payload: Payload, requestType: RequestType) : Boolean = {
    payload.whereBirthRegistered match {
      case BirthRegisterCountry.ENGLAND | BirthRegisterCountry.WALES =>
        keyEnabled("gro", requestType)
      case BirthRegisterCountry.SCOTLAND =>
//        apiEnabled("nrs")
        keyEnabled("nrs", requestType)
      case BirthRegisterCountry.NORTHERN_IRELAND =>
        keyEnabled("groni", requestType)
      case _=> false

    }
  }


  //  def referenceEnabled :Boolean = getConfBool("")

  /**
    * new methot only return Map() of refreence and details enabled
    * pass in payload
    *
    * then in where we call audit() we just concat the two Map()
    * @return
    */

  def audit : Map[String, String] = {
    val featuresPrefix = "features"
    val features : Map[String, String] = Map(
      s"$featuresPrefix.matchFirstName" -> BrmConfig.matchFirstName.toString,
      s"$featuresPrefix.matchLastName" -> BrmConfig.matchLastName.toString,
      s"$featuresPrefix.matchDateOfBirth" -> BrmConfig.matchDateOfBirth.toString,
      s"$featuresPrefix.matchOnMultiple" -> BrmConfig.matchOnMultiple.toString,
      s"$featuresPrefix.ignoreMiddleNames" -> BrmConfig.ignoreMiddleNames.toString,
      s"$featuresPrefix.reference.enabled" -> isDownstreamEnabled(null, ReferenceRequest).toString,
      s"$featuresPrefix.details.enabled" -> isDownstreamEnabled(null, DetailsRequest).toString
    )

    features
  }

  def desHost: String = getConfString("des.host", throw DesException("host"))
  def desPort: String = getConfString("des.port", throw DesException("port"))
  def desEnv: String = getConfString("des.env", throw DesException("env"))
  def desToken: String = getConfString("des.auth-token", throw DesException("auth-token"))

}

object BrmConfig extends BrmConfig
