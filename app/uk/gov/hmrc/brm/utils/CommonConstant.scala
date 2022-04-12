/*
 * Copyright 2022 HM Revenue & Customs
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

/**
  * Created by user on 01/03/17.
  */
object CommonConstant {


  val JSON_ID_PATH : String = "id"
  val JSON_FIRSTNAME_PATH : String = "firstName"
  val JSON_LASTNAME_PATH : String = "lastName"
  val JSON_DATEOFBIRTH_PATH : String = "dateOfBirth"

  val ENVIRONMENT_HEADER = "Environment"
  val TOKEN_HEADER = "Authorization"
  val QUERY_ID_HEADER =  "QueryID"
  val DATETIME_HEADER =  "DateTime"
  val CONTENT_TYPE = "Content-Type"
  val CONTENT_TYPE_JSON = "application/json; charset=utf-8"
}
