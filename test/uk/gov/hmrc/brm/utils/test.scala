/*
 * Copyright 2016 HM Revenue & Customs
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

import play.api.libs.json.JsValue

/**
  * Created by user on 01/09/16.
  */
object test {

  def main(args: Array[String]): Unit = {
    var  jsonBuilder :JsonBuilder  = new JsonBuilder()
     //println(jsonBuilder.withKeyValue("a","b").appendMore().withKeyValue("b","c").buildToJson().validate[JsValue])


     val userWithNorthernIrelandBirthRegsitered  =  jsonBuilder.withKeyValue("firstName","Adam TEST").appendMore().
       withKeyValue("lastName","SMITH").appendMore().
       withKeyValue("dateOfBirth","2012-02-16").appendMore().
       withKeyValue("birthReferenceNumber","500035710").appendMore().
       withKeyValue("whereBirthRegistered",BirthRegisterCountry.NORTHERN_IRELAND.toString)
       .buildToJson()

    println(userWithNorthernIrelandBirthRegsitered.validate[JsValue])
  }

}
