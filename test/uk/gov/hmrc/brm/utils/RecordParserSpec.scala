/*
 * Copyright 2026 HM Revenue & Customs
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

import org.mockito.ArgumentMatchers.{anyString, argThat, contains}
import org.mockito.Mockito.*
import play.api.libs.json.{JsValue, Json, Reads}

class RecordParserSpec extends BaseUnitSpec {

  private case class Dummy(a: String)

  private object Dummy {
    given Reads[Dummy] = Json.reads[Dummy]
  }

  private given Manifest[Dummy] = scala.reflect.ManifestFactory.classType(classOf[Dummy])

  private val readsTuple: (Reads[List[Dummy]], Reads[Dummy]) =
    (Reads.list(Dummy.given_Reads_Dummy), Dummy.given_Reads_Dummy)

  "RecordParser.parse" should {

    "return a list when JSON validates as List[T] and log list success" in {
      val logger = mock[BRMLogger]
      val parser = new RecordParser(logger)

      val json: JsValue = Json.arr(Json.obj("a" -> "one"), Json.obj("a" -> "two"))

      val result = parser.parse[Dummy](json, readsTuple)

      result.map(_.a) shouldBe List("one", "two")

      verify(logger, times(1))
        .info(contains("RecordParser"), contains("parse()"), contains("Successfully validated as[List["))

      verify(logger, never()).warn(anyString(), anyString(), anyString())
    }

    "return empty list when JSON validates as empty List[T] and log final failure warning" in {
      val logger = mock[BRMLogger]
      val parser = new RecordParser(logger)

      val json: JsValue = Json.arr()

      val result = parser.parse[Dummy](json, readsTuple)

      result shouldBe Nil

      verify(logger, times(1))
        .info(contains("RecordParser"), contains("parse()"), contains("Successfully validated as[List["))

      verify(logger, times(1))
        .warn(contains("RecordParser"), contains("parse()"), contains("Failed to parse response"))

      verify(logger, never())
        .warn(contains("RecordParser"), contains("parse()"), contains("Failed to validate"))
    }

    "fall back to single-record Reads when list validation fails and single succeeds" in {
      val logger = mock[BRMLogger]
      val parser = new RecordParser(logger)

      val json: JsValue = Json.obj("a" -> "solo") // not an array => list read fails, single read succeeds

      val result = parser.parse[Dummy](json, readsTuple)

      result.map(_.a) shouldBe List("solo")

      verify(logger, times(1))
        .warn(contains("RecordParser"), contains("parse()"), contains("Failed to validate as[List["))

      verify(logger, times(1))
        .info(contains("RecordParser"), contains("parse()"), contains("Successfully validated as["))

      verify(logger, never())
        .warn(contains("RecordParser"), contains("parse()"), contains("Failed to parse response"))
    }

    "return empty list when both list and single validation fail (2 validate warnings + final warning)" in {
      val logger = mock[BRMLogger]
      val parser = new RecordParser(logger)

      val json: JsValue = Json.obj("a" -> 123)

      val result = parser.parse[Dummy](json, readsTuple)

      result shouldBe Nil

      verify(logger, times(1))
        .warn(contains("RecordParser"), contains("parse()"), contains("Failed to validate as[List["))

      verify(logger, times(1))
        .warn(
          contains("RecordParser"),
          contains("parse()"),
          argThat((msg: String) =>
            msg.contains("Failed to validate as[") && !msg.contains("Failed to validate as[List[")
          )
        )

      verify(logger, times(1))
        .warn(contains("RecordParser"), contains("parse()"), contains("Failed to parse response"))

      verify(logger, times(3)).warn(anyString(), anyString(), anyString())
    }

    "return empty list when JSON is not an array and not a valid object for Dummy" in {
      val logger = mock[BRMLogger]
      val parser = new RecordParser(logger)

      val json: JsValue = Json.toJson("not-an-array-or-object")

      val result = parser.parse[Dummy](json, readsTuple)

      result shouldBe Nil

      verify(logger, times(1))
        .warn(contains("RecordParser"), contains("parse()"), contains("Failed to validate as[List["))

      verify(logger, times(1))
        .warn(
          contains("RecordParser"),
          contains("parse()"),
          argThat((msg: String) =>
            msg.contains("Failed to validate as[") && !msg.contains("Failed to validate as[List[")
          )
        )

      verify(logger, times(1))
        .warn(contains("RecordParser"), contains("parse()"), contains("Failed to parse response"))

      verify(logger, times(3)).warn(anyString(), anyString(), anyString())
    }
  }

}
