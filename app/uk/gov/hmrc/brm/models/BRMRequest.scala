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

package uk.gov.hmrc.brm.models

import play.api.mvc.{Request, WrappedRequest}

/**
 * Created by adamconder on 18/08/2016.
 */

object BRMHeaderNames {
  val AuditSource = "Audit-Source"
}

case class APIVersion(value : Double)
case class AuditSource(value: String)

case class BRMHeaders(
                       apiVersion : Double,
                       auditSource : String
                       ) {
  require(apiVersion > 0.0, "Accept header application/vnd.hmrc.1.0+json must be 1.0 or greater")
  require(!auditSource.isEmpty, "Audit-Source must not be empty")
}

case class BRMRequest[A](
                     request: Request[A],
                     brmHeaders: BRMHeaders
                       ) extends WrappedRequest[A](request)
