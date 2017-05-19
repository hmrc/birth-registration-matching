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
import uk.gov.hmrc.brm.metrics.DateofBirthFeature
import uk.gov.hmrc.brm.models.brm.Payload
import uk.gov.hmrc.brm.utils.BirthRegisterCountry
import uk.gov.hmrc.brm.utils.BirthRegisterCountry.BirthRegisterCountry

abstract class Feature(key: String) {
  final def enabled(): Boolean = {
    try {
      if (!key.split("\\.").headOption.fold(false)(getConfig(_, "enabled", BrmConfig.getConfBool))) {
        false // If parent feature is false, all nested features are false
      }
      else {
        getConfig(key, "enabled", BrmConfig.getConfBool)
      }
    } catch {
      case _: Throwable =>
        throw new RuntimeException(s"problem obtaining valid config value")
    }
  }

  final def value: String = getConfig(key, "value", BrmConfig.getConfString)

  private def getConfig[A](key: String, attribute: String, f: (String, => A) => A) = {
    f(s"birth-registration-matching.features.$key.$attribute",
      throw BrmConfig.BirthConfigurationException(s"features.$key.$attribute")
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

trait DownstreamFeatureFactory {

  def feature: Boolean

  def isReferenceMatchingEnabled(implicit p: Payload): Boolean

  def isDetailsMatchingEnabled(implicit p: Payload): Boolean

  def validDateOfBirth(implicit p: Payload): Boolean = p match {
    case Payload(_, _, _, _, dob, _) =>
      val feature = DateOfBirthValidationFeature()
      if (feature.enabled()) {
        val configDate = LocalDate.parse(feature.value).toDate
        val isValid =  !dob.toDate.before(configDate)
        if(!isValid){
          DateofBirthFeature.count()
        }
        isValid
      } else {
        true
      }
  }

  def referenceFeatures(implicit p: Payload) = validDateOfBirth && isReferenceMatchingEnabled

  def detailsFeatures(implicit p: Payload) = validDateOfBirth && isDetailsMatchingEnabled

  def enabled()(implicit p: Payload): Boolean = p match {
    case Payload(Some(_), _, _, _, _, _) =>
      referenceFeatures
    case Payload(None, _, _, _, _, _) =>
      detailsFeatures
  }
}

object NRSConcreteFeature extends DownstreamFeatureFactory {

  def feature: Boolean = NRSFeature().enabled()

  override def isDetailsMatchingEnabled(implicit p: Payload): Boolean = {
    NRSDetailsFeature().enabled()
  }

  override def isReferenceMatchingEnabled(implicit p: Payload): Boolean = {
    NRSReferenceFeature().enabled()
  }

  override def referenceFeatures(implicit p: Payload) = validDateOfBirth && isReferenceMatchingEnabled

  override def detailsFeatures(implicit p: Payload) = validDateOfBirth && isDetailsMatchingEnabled
}

object GROConcreteFeature extends DownstreamFeatureFactory {

  def feature: Boolean = GROFeature().enabled()

  override def isDetailsMatchingEnabled(implicit p: Payload): Boolean = {
    GRODetailsFeature().enabled()
  }

  override def isReferenceMatchingEnabled(implicit p: Payload): Boolean = {
    GROReferenceFeature().enabled()
  }

  override def referenceFeatures(implicit p: Payload) = validDateOfBirth && isReferenceMatchingEnabled

  override def detailsFeatures(implicit p: Payload) = validDateOfBirth && isDetailsMatchingEnabled
}

case class GRONIFeature() extends Feature("groni")

case class GRONIReferenceFeature() extends Feature("groni.reference")

case class GRONIDetailsFeature() extends Feature("groni.details")

object GRONIConcreteFeature extends DownstreamFeatureFactory {
  def feature: Boolean = GRONIFeature().enabled()

  override def isDetailsMatchingEnabled(implicit p: Payload): Boolean = {
    GRONIDetailsFeature().enabled()
  }

  override def isReferenceMatchingEnabled(implicit p: Payload): Boolean = {
    GRONIReferenceFeature().enabled()
  }

  override def referenceFeatures(implicit p: Payload) = isReferenceMatchingEnabled

  override def detailsFeatures(implicit p: Payload) = isDetailsMatchingEnabled
}

object DownstreamFeatureFactory {

  private lazy val set: Map[BirthRegisterCountry, DownstreamFeatureFactory] = Map(
    BirthRegisterCountry.ENGLAND -> GROConcreteFeature,
    BirthRegisterCountry.WALES -> GROConcreteFeature,
    BirthRegisterCountry.SCOTLAND -> NRSConcreteFeature,
    BirthRegisterCountry.NORTHERN_IRELAND -> GRONIConcreteFeature
  )

  def apply()(implicit payload: Payload) = {
    set(payload.whereBirthRegistered)
  }
}
