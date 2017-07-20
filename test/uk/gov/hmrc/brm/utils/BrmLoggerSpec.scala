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

package uk.gov.hmrc.brm.utils

import org.mockito.Mockito._
import org.scalatest.BeforeAndAfter
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.OneAppPerSuite
import org.specs2.mock.mockito.ArgumentCapture
import uk.gov.hmrc.play.test.UnitSpec

class BrmLoggerSpec extends UnitSpec with MockitoSugar with BeforeAndAfter with OneAppPerSuite {

  import uk.gov.hmrc.brm.utils.Mocks._

  before {
    reset(mockLogger)
  }

  "BrmLogger" should {
    "info call Logger info" in {
      KeyGenerator.setKey("somekey")
      MockBRMLogger.info(this, "methodName", "message")
      val argumentCapture = new ArgumentCapture[String]
      verify(mockLogger, times(1)).info(argumentCapture.capture)
      argumentCapture.value.contains("methodName") shouldBe true
      argumentCapture.value.contains("message") shouldBe true
      argumentCapture.value.contains("somekey") shouldBe true
    }

    "warn call Logger warn" in {
      MockBRMLogger.warn(this, "methodName", "message")
      val argumentCapture = new ArgumentCapture[String]
      verify(mockLogger, times(1)).warn(argumentCapture.capture)

      argumentCapture.value.contains("methodName") shouldBe true
      argumentCapture.value.contains("message") shouldBe true
      argumentCapture.value.contains("somekey") shouldBe true
    }

    "debug call Logger debug" in {
      MockBRMLogger.debug(this, "methodName", "message")
      val argumentCapture = new ArgumentCapture[String]
      verify(mockLogger, times(1)).debug(argumentCapture.capture)

      argumentCapture.value.contains("methodName") shouldBe true
      argumentCapture.value.contains("message") shouldBe true
      argumentCapture.value.contains("somekey") shouldBe true
    }

    "error call Logger error" in {
      MockBRMLogger.error(this, "methodNameForError", "errorMessage")
      val argumentCapture = new ArgumentCapture[String]
      verify(mockLogger, times(1)).error(argumentCapture.capture)

      argumentCapture.value.contains("methodNameForError") shouldBe true
      argumentCapture.value.contains("errorMessage") shouldBe true
      argumentCapture.value.contains("somekey") shouldBe true
    }

  }
}
