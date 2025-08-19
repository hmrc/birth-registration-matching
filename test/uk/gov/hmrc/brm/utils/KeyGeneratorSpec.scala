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

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfter
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import org.scalatest.matchers.should.Matchers
import java.time.LocalDateTime
import java.time.Month._
import java.time.format.DateTimeFormatter

class KeyGeneratorSpec extends AnyWordSpecLike with Matchers with BeforeAndAfter with GuiceOneAppPerSuite {

  import uk.gov.hmrc.brm.utils.Mocks._

  override lazy val app: Application = new GuiceApplicationBuilder().build()

  val mockKeyGen: KeyGenerator = app.injector.instanceOf[KeyGenerator]

  before {
    reset(mockRequest)
    reset(headers)
  }

  val dateString: String = LocalDateTime
    .of(2016, SEPTEMBER.getValue, 15, 5, 10, 10)
    .format(DateTimeFormatter.ofPattern("yyyyMMdd:HHmmssSS"))

  "KeyGenerator" should {

    "return key" in {
      when(mockRequest.id).thenReturn(10)
      when(mockRequest.headers).thenReturn(headers)
      when(headers.get(any())).thenReturn(Some("dfs"))
      mockKeyGen.generateKey(mockRequest, "1.0").isEmpty shouldBe false
    }

    "return key as 20160915:05101000-0-dfs-1.0 for audio source dfs and version 1.0" in {
      when(mockRequest.headers).thenReturn(headers)
      when(headers.get("Audit-Source")).thenReturn(Some("dfs"))
      when(headers.get(HeaderNames.ACCEPT)).thenReturn(Some("application/vnd.hmrc.1.0+json"))

      mockKeyGen.generateKey(mockRequest, "1.0")
    }

    "return key when audio source is empty" in {
      when(mockRequest.headers).thenReturn(headers)
      when(headers.get("Audit-Source")).thenReturn(None)
      when(headers.get(HeaderNames.ACCEPT)).thenReturn(Some("application/vnd.hmrc.1.0+json"))
      val key = mockKeyGen.generateKey(mockRequest, "1.0")
      key shouldBe DateUtil.getCurrentDateString("yyyyMMdd:HHmmssSS") + "-0--1.0"
    }

    "return key when request id is empty" in {
      when(mockRequest.headers).thenReturn(headers)
      when(headers.get("Audit-Source")).thenReturn(Some("dfs"))
      when(headers.get(HeaderNames.ACCEPT)).thenReturn(Some("application/vnd.hmrc.1.0+json"))
      val key = mockKeyGen.generateKey(mockRequest, "1.0")
      key shouldBe DateUtil.getCurrentDateString("yyyyMMdd:HHmmssSS") + "-0-dfs-1.0"

    }

    "return key when version is empty" in {
      when(mockRequest.headers).thenReturn(headers)
      when(headers.get("Audit-Source")).thenReturn(Some("dfs"))
      when(headers.get(HeaderNames.ACCEPT)).thenReturn(None)
      val key = mockKeyGen.generateKey(mockRequest, "")
      key shouldBe DateUtil.getCurrentDateString("yyyyMMdd:HHmmssSS") + "-0-dfs-"

    }

    "return key when audit source length is more than 20" in {
      when(mockRequest.headers).thenReturn(headers)
      when(mockRequest.id).thenReturn(123456)
      when(headers.get("Audit-Source")).thenReturn(Some("this--is--30--character--long"))
      when(headers.get(HeaderNames.ACCEPT)).thenReturn(Some("application/vnd.hmrc.10.0+json"))
      val key = mockKeyGen.generateKey(mockRequest, "2.0")
      key.contains("this--is--30--charac")  shouldBe true
      key.contains("this--is--30--charact") shouldBe false
      key.length <= 50                      shouldBe true

    }

    "return key contains version 2.0 for version 2.0." in {
      when(mockRequest.headers).thenReturn(headers)
      when(headers.get("Audit-Source")).thenReturn(Some("dfs"))
      when(headers.get(HeaderNames.ACCEPT)).thenReturn(Some("application/vnd.hmrc.2.0+json"))
      val key = mockKeyGen.generateKey(mockRequest, "2.0")
      key.contains("2.0") shouldBe true

    }
  }

}
