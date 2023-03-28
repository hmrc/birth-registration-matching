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

package uk.gov.hmrc.brm.utils

import play.api.libs.json._
import uk.gov.hmrc.brm.metrics._

object BirthRegisterCountry extends Enumeration {

  val ENGLAND: BirthRegisterCountry.Value          = Value("england")
  val WALES: BirthRegisterCountry.Value            = Value("wales")
  val NORTHERN_IRELAND: BirthRegisterCountry.Value = Value("northern ireland")
  val SCOTLAND: BirthRegisterCountry.Value         = Value("scotland")

  def birthRegisterReads(implicit
    engAndWalesMetrics: EnglandAndWalesBirthRegisteredCountMetrics,
    northIreMetrics: NorthernIrelandBirthRegisteredCountMetrics,
    scotlandMetrics: ScotlandBirthRegisteredCountMetrics,
    invalidRegMetrics: InvalidBirthRegisteredCountMetrics
  ): Reads[BirthRegisterCountry.Value] = new Reads[BirthRegisterCountry.Value] {

    override def reads(json: JsValue): JsResult[BirthRegisterCountry.Value] =
      json match {
        case JsString(s) =>
          try {
            // increase count metrics
            BirthRegisterCountry.withName(s.trim.toLowerCase) match {
              case ENGLAND | WALES  =>
                engAndWalesMetrics.count()
              case NORTHERN_IRELAND =>
                northIreMetrics.count()
              case SCOTLAND         =>
                scotlandMetrics.count()
            }

            JsSuccess(BirthRegisterCountry.withName(s.trim.toLowerCase))
          } catch {
            case _: NoSuchElementException =>
              invalidRegMetrics.count()
              JsError(
                s"Enumeration expected of type: '${BirthRegisterCountry.getClass}', but it does not appear to contain the value:$s"
              )
          }
        case _           => JsError("String value expected")
      }
  }

  def birthRegisterWrites: Writes[BirthRegisterCountry.Value] = new Writes[BirthRegisterCountry.Value] {
    def writes(d: BirthRegisterCountry.Value): JsValue = JsString(d.toString)
  }

}
