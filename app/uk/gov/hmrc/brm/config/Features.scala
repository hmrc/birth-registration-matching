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

import org.joda.time.LocalDate
import uk.gov.hmrc.brm.models.brm.Payload
import uk.gov.hmrc.brm.utils.BirthRegisterCountry

abstract class Feature(key : String) {
  final def enabled() : Boolean = {
    try {
      if(!key.split("\\.").headOption.fold(false) {
        case a => BrmConfig.getConfBool(s"birth-registration-matching.features.$a.enabled",
          throw BrmConfig.BirthConfigurationException(s"features.$a.enabled"))
      }) {
        // If parent feature is false, all nested features are false
        false
      }
      else
      {
        BrmConfig.getConfBool(s"birth-registration-matching.features.$key.enabled",
          throw BrmConfig.BirthConfigurationException(s"features.$key.enabled")
        )
      }
    } catch {
      case _ : Throwable =>
        throw new RuntimeException(s"problem obtaining valid config value")
    }
  }

  final def value : String = {
    BrmConfig.getConfString(s"birth-registration-matching.features.$key.value",
      throw BrmConfig.BirthConfigurationException(s"features.$key.value")
    )
  }
}

/*
  Main Features
 */
case class DateOfBirthValidationFeature() extends Feature("dobValidation")

/*
  GRO feature switches
*/
case class GROFeature() extends Feature("gro")
case class GROReferenceFeature() extends Feature("gro.reference")
case class GRODetailsFeature() extends Feature("gro.details")

/*
  NRS feature switches
*/
case class NRSFeature() extends Feature("nrs")
case class NRSReferenceFeature() extends Feature("nrs.reference")
case class NRSDetailsFeature() extends Feature("nrs.details")

trait FeatureFactory {

  def feature : Boolean
  def referenceFeature : Boolean
  def detailsFeature: Boolean

  def isReferenceMatchingEnabled(implicit p: Payload) : Boolean

  def isDetailsMatchingEnabled(implicit p: Payload) : Boolean

  def validateDateOfBirth(implicit p: Payload) : Boolean = p match {
    case Payload(_, _, _, dob, _) =>
      val feature = DateOfBirthValidationFeature()
      if(feature.enabled()) {
        val configDate = LocalDate.parse(feature.value).toDate
        !dob.toDate.before(configDate)
      } else { true }
  }

  def referenceFeatures(implicit p: Payload) = validateDateOfBirth && isReferenceMatchingEnabled
  def detailsFeatures(implicit p: Payload) = validateDateOfBirth && isDetailsMatchingEnabled

  def validate()(implicit p :Payload) : Boolean = p match {
    case Payload(Some(_), _, _, _, _) =>
      referenceFeatures
    case Payload(None, _, _, _, _) =>
      detailsFeatures
  }

}

object NRSConcreteFeature extends FeatureFactory {

  def feature: Boolean = NRSFeature().enabled()
  def referenceFeature: Boolean = NRSReferenceFeature().enabled()
  def detailsFeature: Boolean = NRSDetailsFeature().enabled()

  override def isDetailsMatchingEnabled(implicit p: Payload) : Boolean = {
    detailsFeature
  }

  override def isReferenceMatchingEnabled(implicit p: Payload) : Boolean = {
    referenceFeature
  }

  override def referenceFeatures(implicit p: Payload) = validateDateOfBirth && isReferenceMatchingEnabled
  override def detailsFeatures(implicit p: Payload) = validateDateOfBirth && isDetailsMatchingEnabled
}

object GROConcreteFeature extends FeatureFactory {

  def feature: Boolean = GROFeature().enabled()
  def referenceFeature: Boolean = GROReferenceFeature().enabled()
  def detailsFeature: Boolean = GRODetailsFeature().enabled()

  override def isDetailsMatchingEnabled(implicit p: Payload) : Boolean = {
    detailsFeature
  }

  override def isReferenceMatchingEnabled(implicit p: Payload) : Boolean = {
    referenceFeature
  }

  override def referenceFeatures(implicit p: Payload) = validateDateOfBirth && isReferenceMatchingEnabled
  override def detailsFeatures(implicit p: Payload) = validateDateOfBirth && isDetailsMatchingEnabled
}

object FeatureFactory {

  def apply()(implicit payload: Payload) = payload.whereBirthRegistered match {
    case BirthRegisterCountry.ENGLAND | BirthRegisterCountry.WALES =>
      GROConcreteFeature
    case BirthRegisterCountry.SCOTLAND =>
      NRSConcreteFeature
  }

}
