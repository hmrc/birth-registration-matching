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

package uk.gov.hmrc.brm.audit

import com.google.inject.Singleton
import uk.gov.hmrc.brm.config.{BrmConfig, MicroserviceGlobal}
import uk.gov.hmrc.brm.models.brm.Payload
import uk.gov.hmrc.brm.models.response.Record
import uk.gov.hmrc.brm.utils.{BRMFormat, CommonUtil, Keygenerator}
import uk.gov.hmrc.brm.utils.CommonUtil.{DetailsRequest, ReferenceRequest}
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.brm.services.parser.NameParser._

import scala.annotation.tailrec

/**
  * Created by adamconder on 15/02/2017.
  */
@Singleton
class RequestsAndResultsAudit(
                               connector : AuditConnector = MicroserviceGlobal.auditConnector
                             ) extends BRMAudit(connector) {

  /**
    * Audit event for the result of MatchingService and data submitted to the API
    * @param result map of key value results
    * @param hc implicit headerCarrier
    */
  final private class RequestsAndResultsAuditEvent(result : Map[String, String], path : String)
                                                  (implicit hc : HeaderCarrier)
    extends AuditEvent(
      auditType = "BRM-RequestsAndResults",
      detail = result,
      transactionName = "brm-customer-information-and-results",
      path = path
    )

  def audit(result : Map[String, String], payload: Option[Payload])(implicit hc : HeaderCarrier) = {
    payload match {
      case Some(p) =>

        val features : Map[String, String] = BrmConfig.audit

        // TODO UNIT TEST THIS INDIVIDUALLY
        val input : Map[String, String] = Map(
          "brmKey" -> Keygenerator.geKey(),
          "payload.birthReferenceNumber" -> p.birthReferenceNumber.toString,
          "payload.firstName" -> p.firstName,
          "payload.lastName" -> p.lastName,
          "payload.dateOfBirth" -> p.dateOfBirth.toString(BRMFormat.datePattern),
          "payload.whereBirthRegistered" -> p.whereBirthRegistered.toString
        )

        // concat the match result
        val data = input ++ result ++ features

        CommonUtil.getOperationType(p) match {
          case DetailsRequest() =>
            event(new RequestsAndResultsAuditEvent(data, "birth-registration-matching/match/details"))
          case ReferenceRequest() =>
            event(new RequestsAndResultsAuditEvent(data, "birth-registration-matching/match/reference"))
        }
      case _ =>
        throw new IllegalArgumentException("payload argument not specified")
    }
  }

  def responseWordCount(record: List[Record]): Map[String, Int] = {
    responseDetail(record, length)
  }

  def responseCharacterCount(record: List[Record]): Map[String, Int] = {
    responseDetail(record, characterCount)
  }

  private def length(r: Record, c: Int): Map[String, Int] = {
    Map(
      s"records.record$c.numberOfForenames" -> r.child.firstName.names.length,
      s"records.record$c.numberOfLastnames" -> r.child.lastName.names.length
    )
  }

  def characterCount(r: Record, c: Int): Map[String, Int] = {
    Map(
      s"records.record$c.numberOfCharactersInFirstName" -> r.child.firstName.names.listToString.size,
      s"records.record$c.numberOfCharactersInLastName" -> r.child.lastName.names.listToString.size
    )
  }

  private def responseDetail(record: List[Record], f: (Record, Int) => Map[String, Int]): Map[String, Int] = {

    @tailrec
    def build(c: Int, l: List[Record], m: List[Map[String, Int]], f: (Record, Int) => Map[String, Int]): List[Map[String, Int]] = l match {
      case Nil => m
      case _ => {
        val newMap = f(l.head, c)
        build(c + 1, l.tail,  newMap :: m, f)
      }
    }

    val responseMap = build(1, record, Nil, f)

    responseMap.reduceLeft((k, v) => k ++ v)
  }

}
