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

import org.joda.time.format.DateTimeFormat
import org.joda.time.{DateTime, DateTimeUtils}
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfter
import org.scalatest.mock.MockitoSugar
import play.api.libs.json.JsValue
import play.api.mvc.{Headers, Request}
import uk.gov.hmrc.brm.connectors.BirthConnector
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.brm.utils._

import scala.concurrent.Future


/**
  * Created by user on 15/09/16.
  */
class KeyGeneratorSpec extends UnitSpec with MockitoSugar  with BeforeAndAfter {
  val mockRequest = mock[Request[JsValue]]
  val headers = mock[Headers]


  before {
    reset(mockRequest)
    reset(headers)
  }


  "KeyGenerator" should {
    "return key" in {
      when(mockRequest.id).thenReturn(10)
      when(mockRequest.headers).thenReturn(headers)
      when(headers.get(Matchers.any())).thenReturn(Some("dfs"))
      val date = new DateTime(2009, 10, 10, 5, 10, 10)
      DateTimeUtils.setCurrentMillisFixed(date.getMillis)
      Keygenerator.generateKey(mockRequest).isEmpty shouldBe false
    }


    "return key in 20091010:051010-0-dfs" in {

      when(mockRequest.headers).thenReturn(headers)
      when(headers.get("Audit-Source")).thenReturn(Some("dfs"))
      val date = new DateTime(2016, 9, 15, 5, 10, 10)
      DateTimeUtils.setCurrentMillisFixed(date.getMillis)
      val key = Keygenerator.generateKey(mockRequest)
      key shouldBe "20160915:051010-0-dfs"
      key.contains("dfs") shouldBe true
      key.contains("20160915:051010") shouldBe true
    }

    "return key when audio source is empty" in {

      when(mockRequest.headers).thenReturn(headers)
      when(headers.get("Audit-Source")).thenReturn(None)
      val date = new DateTime(2016, 9, 15, 5, 10, 10)
      DateTimeUtils.setCurrentMillisFixed(date.getMillis)
      val key = Keygenerator.generateKey(mockRequest)
      key shouldBe "20160915:051010-0-"
      key.contains("dfs") shouldBe false
    }

    "return key when request id is empty" in {

      when(mockRequest.headers).thenReturn(headers)
      when(headers.get("Audit-Source")).thenReturn(Some("dfs"))
      val date = new DateTime(2016, 9, 15, 5, 10, 10)
      DateTimeUtils.setCurrentMillisFixed(date.getMillis)
      val key = Keygenerator.generateKey(mockRequest)
      key shouldBe "20160915:051010-0-dfs"

    }
  }

}
