/*
 * Copyright 2018 HM Revenue & Customs
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
import uk.gov.hmrc.brm.metrics.{InvalidBirthRegisteredCountMetrics, ScotlandBirthRegisteredCountMetrics, NorthernIrelandBirthRegisteredCountMetrics, EnglandAndWalesBirthRegisteredCountMetrics}

object BirthRegisterCountry extends Enumeration {

  type BirthRegisterCountry = Value
  val ENGLAND = Value("england")
  val WALES = Value("wales")
  val NORTHERN_IRELAND = Value("northern ireland")
  val SCOTLAND  = Value("scotland")

  def birthRegisterReads: Reads[BirthRegisterCountry] = new Reads[BirthRegisterCountry] {
    override def reads(json: JsValue): JsResult[BirthRegisterCountry.Value] =
      json match {
        case JsString(s) =>
          try {
            // increase count metrics
            val enum = BirthRegisterCountry.withName(s.trim.toLowerCase)
            enum match {
              case ENGLAND | WALES =>
                EnglandAndWalesBirthRegisteredCountMetrics.count()
              case NORTHERN_IRELAND =>
                NorthernIrelandBirthRegisteredCountMetrics.count()
              case SCOTLAND =>
                ScotlandBirthRegisteredCountMetrics.count()
            }

            JsSuccess(BirthRegisterCountry.withName(s.trim.toLowerCase))
          } catch {
            case _: NoSuchElementException =>
              InvalidBirthRegisteredCountMetrics.count()
              JsError(s"Enumeration expected of type: '${BirthRegisterCountry.getClass}', but it does not appear to contain the value:$s")
          }
        case _ => JsError("String value expected")
      }
  }

  def birthRegisterWrites = new Writes[BirthRegisterCountry] {
    def writes(d: BirthRegisterCountry): JsValue = JsString(d.toString)
  }

}
