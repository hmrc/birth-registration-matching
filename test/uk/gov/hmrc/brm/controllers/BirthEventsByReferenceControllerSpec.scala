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

package uk.gov.hmrc.brm.controllers

import uk.gov.hmrc.play.test.UnitSpec

/**
  * Created by chrisianson on 26/07/16.
  */
class BirthEventsByReferenceControllerSpec extends UnitSpec {

  /**
    * - Should
    * - GET /api/v0/events/birth should return 200
    * - GET /api/v0/events/birth should return html
    * - Return valid JSON response on successful match
    * - Return valid JSON response on unsuccessful match
    * - Return response code 500 when API is down
    * - Return response code 404 when endpoint has been retired and is no longer in use
    * - Return response code 400 if request contains invalid/missing data
    * - Return response code 400 if authentication fails
    * - Return response code 403 if account (service account) is suspended
   **/
}
