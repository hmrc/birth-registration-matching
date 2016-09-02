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

import play.api.libs.json.{JsValue, Json}

import scala.collection.mutable.StringBuilder

/**
  * Created by user on 01/09/16.
  */
class JsonBuilder  {
  var jsonString : StringBuilder = new StringBuilder(" {")


  def withKeyValue (key: String, value:String ): JsonBuilder = {
    jsonString.append("\""+key + "\"").append(":").append("\""+value + "\"")
    this
  }
  def appendMore (): JsonBuilder = {
    jsonString.append(",")
    this
  }

  def buildToJson():JsValue = {
    jsonString.append("}")

    Json.parse(jsonString.toString())
  }




}