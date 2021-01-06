/*
 * Copyright 2021 HM Revenue & Customs
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

import org.scalatest.BeforeAndAfter
import org.scalatest.concurrent.{Eventually, IntegrationPatience}
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import ch.qos.logback.classic.Level.{DEBUG, ERROR, INFO, WARN}
import play.api.Logger
import uk.gov.hmrc.play.test.{LogCapturing, UnitSpec}

class BrmLoggerSpec extends UnitSpec with MockitoSugar with BeforeAndAfter with GuiceOneAppPerSuite with LogCapturing with Eventually with IntegrationPatience {

  val keyGen: KeyGenerator = app.injector.instanceOf[KeyGenerator]
  val mockBrmLogger: BRMLogger = new BRMLogger(keyGen)

  val testLogger: Logger = mockBrmLogger.logger

  "BrmLogger" should {

    keyGen.setKey("somekey")
    val expected = List("methodName", "message", "somekey")

    "info call Logger info" in {
      withCaptureOfLoggingFrom(testLogger) { logs =>
        mockBrmLogger.info(this, "methodName", "message")

        logs.size shouldBe 1
        logs.head.getLevel shouldBe INFO
        expected.forall(logs.head.getMessage.contains) shouldBe true
      }
    }

    "warn call Logger warn" in {
      withCaptureOfLoggingFrom(testLogger) { logs =>
        mockBrmLogger.warn(this, "methodName", "message")

        logs.size shouldBe 1
        logs.head.getLevel shouldBe WARN
        expected.forall(logs.head.getMessage.contains) shouldBe true
      }
    }

    "debug call Logger debug" in {
      withCaptureOfLoggingFrom(testLogger) { logs =>
        mockBrmLogger.debug(this, "methodName", "message")

        logs.size shouldBe 1
        logs.head.getLevel shouldBe DEBUG
        expected.forall(logs.head.getMessage.contains) shouldBe true
      }
    }

    "error call Logger error" in {
      val expectedError = List("methodNameForError", "message", "somekey")
      withCaptureOfLoggingFrom(testLogger) { logs =>
        mockBrmLogger.error(this, "methodNameForError", "message")

        logs.size shouldBe 1
        logs.head.getLevel shouldBe ERROR
        expectedError.forall(logs.head.getMessage.contains) shouldBe true
      }
    }
  }
}
