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

import uk.gov.hmrc.play.config.ServicesConfig

trait BrmConfig extends ServicesConfig {

  case class BirthConfigurationException(switch: String) extends RuntimeException {
    override def toString: String = {
      val m = s"birth-registration-matching.matching.$switch configuration not found"
      m
    }
  }

  def validateDobForGro: Boolean = getConfBool("birth-registration-matching.validateDobForGro", defBool = false)
  def minimumDateValueForGroValidation: String = getConfString("birth-registration-matching.validDateForGro", "1900-01-01")

  val defaultDate: Int = 1900

  def minimumDateOfBirthYear: Int = getConfInt("birth-registration-matching.minimumDateOfBirthYear", defaultDate)

  def matchFirstName : Boolean = getConfBool("birth-registration-matching.matching.firstName",  defBool = true)
  def matchLastName : Boolean = getConfBool("birth-registration-matching.matching.lastName",  defBool = true)
  def matchDateOfBirth : Boolean = getConfBool("birth-registration-matching.matching.dateOfBirth",  defBool = true)

  def matchOnMultiple : Boolean = getConfBool("birth-registration-matching.matching.matchOnMultiple", defBool = false)

  def disableSearchByDetails : Boolean = getConfBool("birth-registration-matching.matching.disableSearchByDetails", defBool = false)

  def nameMaxLength : Int = getConfInt("birth-registration-matching.validation.maxNameLength", 250)

  val ignoreMiddleNamesRegex : String = getConfString("birth-registration-matching.matching.ignoreMiddleNamesRegex",
    throw BirthConfigurationException("ignoreMiddleNames"))

  def ignoreMiddleNames : Boolean = getConfBool("birth-registration-matching.matching.ignoreMiddleNames",
    throw BirthConfigurationException("ignoreMiddleNames"))

  def audit : Map[String, String] = {
    val featuresPrefix = "features"
    val features : Map[String, String] = Map(
      s"$featuresPrefix.matchFirstName" -> BrmConfig.matchFirstName.toString,
      s"$featuresPrefix.matchLastName" -> BrmConfig.matchLastName.toString,
      s"$featuresPrefix.matchDateOfBirth" -> BrmConfig.matchDateOfBirth.toString,
      s"$featuresPrefix.matchOnMultiple" -> BrmConfig.matchOnMultiple.toString,
      s"$featuresPrefix.disableSearchByDetails" -> BrmConfig.disableSearchByDetails.toString,
      s"$featuresPrefix.ignoreMiddleNames" -> BrmConfig.ignoreMiddleNames.toString
    )

    features
  }

}
//
//abstract class Feature(key : String) {
//  final def enabled() : Boolean = {
//    try {
//      if(!key.split("\\.").headOption.fold(false) {
//        case a => BrmConfig.getConfBool(s"birth-registration-matching.features.$a.enabled",
//          throw BrmConfig.BirthConfigurationException(s"features.$a.enabled"))
//      }) {
//        // If parent feature is false, all nested features are false
//        false
//      }
//      else
//      {
//        BrmConfig.getConfBool(s"birth-registration-matching.features.$key.enabled",
//          throw BrmConfig.BirthConfigurationException(s"features.$key.enabled")
//        )
//      }
//    } catch {
//      case _ : Throwable =>
//        throw new RuntimeException(s"problem obtaining valid config value")
//    }
//  }
//
//  final def value : String = {
//    BrmConfig.getConfString(s"birth-registration-matching.features.$key.value",
//      throw BrmConfig.BirthConfigurationException(s"features.$key.value")
//    )
//  }
//}
//
///*
//  Main Features
// */
//case class DateOfBirthValidationFeature() extends Feature("dobValidation")
//
///*
//  GRO feature switches
//*/
//case class GROFeature() extends Feature("gro")
//case class GROReferenceFeature() extends Feature("gro.reference")
//case class GRODetailsFeature() extends Feature("gro.details")
//
///*
//  NRS feature switches
//*/
//case class NRSFeature() extends Feature("nrs")
//case class NRSReferenceFeature() extends Feature("nrs.reference")
//case class NRSDetailsFeature() extends Feature("nrs.details")
//
//
//trait FeatureFactory {
//
//  val feature : Boolean
//  val referenceFeature : Boolean
//  val detailsFeature: Boolean
//
//  private def isReferenceMatchingEnabled(implicit p: Payload) : Boolean = p match {
//    case Payload(Some(_), _, _,_, country) =>
//      country match {
//        case BirthRegisterCountry.ENGLAND | BirthRegisterCountry.WALES =>
//          GROReferenceFeature().enabled()
//        case BirthRegisterCountry.SCOTLAND =>
//          NRSReferenceFeature().enabled()
//      }
//  }
//
//  private def isDetailsMatchingEnabled(implicit p: Payload) : Boolean = p match {
//    case Payload(None, _, _,_, country) =>
//      country match {
//        case BirthRegisterCountry.ENGLAND | BirthRegisterCountry.WALES =>
//          GRODetailsFeature().enabled()
//        case BirthRegisterCountry.SCOTLAND =>
//          NRSDetailsFeature().enabled()
//      }
//  }
//
//  private def validateDateOfBirth(implicit p: Payload) : Boolean = p match {
//    case Payload(_, _, _, dob, _) =>
//      val feature = DateOfBirthValidationFeature()
//      if(feature.enabled()) {
//        val configDate = LocalDate.parse(feature.value).toDate
//        !dob.toDate.before(configDate)
//      } else { true }
//  }
//
//  def referenceFeatures(implicit p: Payload) = validateDateOfBirth && isReferenceMatchingEnabled
//  def detailsFeatures(implicit p: Payload) = validateDateOfBirth && isDetailsMatchingEnabled
//
//  def validate()(implicit p :Payload) : Boolean = p match {
//    case Payload(Some(_), _, _, dob, _) =>
//      referenceFeatures
//    case Payload(None, _, _, dob, _) =>
//      detailsFeatures
//  }
//
//}
//
//case class GROConcreteFeature(
//                               feature: Boolean = GROFeature().enabled(),
//                               referenceFeature: Boolean = GROReferenceFeature().enabled(),
//                               detailsFeature: Boolean = GRODetailsFeature().enabled()) extends FeatureFactory
//
//case class NRSConcreteFeature(
//                               feature: Boolean = NRSFeature().enabled(),
//                               referenceFeature: Boolean = NRSReferenceFeature().enabled(),
//                               detailsFeature: Boolean = NRSDetailsFeature().enabled()) extends FeatureFactory
//
//object FeatureFactory {
//
//  def apply()(implicit payload: Payload) = payload.whereBirthRegistered match {
//    case BirthRegisterCountry.ENGLAND | BirthRegisterCountry.WALES =>
//      GROConcreteFeature()
//    case BirthRegisterCountry.SCOTLAND =>
//      NRSConcreteFeature()
//  }
//
//}


object BrmConfig extends BrmConfig
