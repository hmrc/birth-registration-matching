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

import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.words.{EmptyWord, MatcherWords}
import play.api.http.Status
import play.api.libs.json.JsValue
import play.api.mvc.Result
import play.api.test.Helpers._
import uk.gov.hmrc.brm.utils.Mocks.MockController
import uk.gov.hmrc.play.audit.http.connector.AuditResult
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.Future
import uk.gov.hmrc.brm.utils.TestHelper._
import uk.gov.hmrc.brm.utils.Mocks._
/**
  * Created by user on 04/05/17.
  */
trait BaseUnitSpec extends UnitSpec  {


  def checkResponse(result: Result, responseStatus:Int , matchResonse:Boolean): Unit = {
    status(result) shouldBe responseStatus
    contentType(result).get shouldBe "application/json"
    (contentAsJson(result) \ "matched").as[Boolean] shouldBe matchResonse
    header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
  }

  def checkResponse(result: Result, responseStatus:Int , responseString:String): Unit = {
    status(result) shouldBe responseStatus
    contentType(result).get shouldBe "application/json"
    jsonBodyOf(result).toString() shouldBe responseString
    header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
  }

  def checkResponse(result: Result, responseStatus:Int , responseBody:EmptyWord): Unit = {
    status(result) shouldBe responseStatus
    contentType(result).get shouldBe "application/json"
    bodyOf(result) shouldBe responseBody
    header(ACCEPT, result).get shouldBe "application/vnd.hmrc.1.0+json"
  }



  def mockReferenceResponse(jsonResponse:JsValue) = {
    when(MockController.service.groConnector.getReference(Matchers.any())(Matchers.any())).thenReturn(Future.successful(httpResponse(jsonResponse)))
  }

  def mockAuditSuccess = {
    when(mockAuditConnector.sendEvent(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(AuditResult.Success))
  }

}
