/*
 * Copyright 2025 HM Revenue & Customs
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

import scala.io.Source

object JsonUtils {

  def getJsonFromFile[A](path: String, filename: A) = loadResource(s"/resources/$path/$filename.json")

  def loadResource(path: String) = {

    def resourceAsString(resourcePath: String): Option[String] =
      Option(getClass.getResourceAsStream(resourcePath)) map { is =>
        Source.fromInputStream(is).getLines().mkString("\n")
      }

    resourceAsString(path) match {
      case Some(x) =>
        val json: JsValue = Json.parse(x)
        json
      case _       =>
        throw new RuntimeException("cannot load json")
    }
  }
}
