/*
 * Copyright 2020 HM Revenue & Customs
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

package uk.gov.hmrc.brm.models.response

import play.api.Logger
import play.api.libs.json.Json
import uk.gov.hmrc.brm.config.BrmConfig
import uk.gov.hmrc.brm.models.brm.Payload
import uk.gov.hmrc.brm.services.parser.NameParser
import uk.gov.hmrc.brm.services.parser.NameParser._
import uk.gov.hmrc.brm.utils.BRMLogger

case class Record(child: Child, status: Option[StatusInterface] = None) {

  private def processFlags = true

  def isFlagged(implicit config: BrmConfig): Boolean = {
    val notFlagged = false
    if (processFlags) {

      status match {
        case Some(flags) => !flags.determineFlagSeverity.canProcessRecord(config)
        case None => notFlagged
      }
    }
    else {
      notFlagged
    }
  }
}

object Record {

  def audit(r: Record, p: Payload, c: Int)(implicit logger: BRMLogger, config: BrmConfig): Map[String, String] = {

    val recordNames = NameParser.parseNames(p, r, config.ignoreAdditionalNames)

    val words = wordCount(recordNames, c)
    val characters = characterCount(recordNames, c)
    val status = statusFlags(r.status, c)

    logNameCount(p, words)

    words ++ characters ++ status
  }


  private def wordCount(recordNames: Names, c: Int): Map[String, String] = {
    Map(
      s"records.record$c.numberOfForenames" -> s"${recordNames.firstNames.names.count(_.nonEmpty)}",
      s"records.record$c.numberOfAdditionalNames" -> s"${recordNames.additionalNames.names.count(_.nonEmpty)}",
      s"records.record$c.numberOfLastnames" -> s"${recordNames.lastNames.names.count(_.nonEmpty)}")
  }

  private def characterCount(recordNames: Names, c: Int): Map[String, String] = {
    Map(
      s"records.record$c.numberOfCharactersInFirstName" -> s"${recordNames.firstNames.length}",
      s"records.record$c.numberOfCharactersInAdditionalName" -> s"${recordNames.additionalNames.length}",
      s"records.record$c.numberOfCharactersInLastName" -> s"${recordNames.lastNames.length}"
    )
  }

  private def statusFlags(s: Option[StatusInterface], c: Int)(implicit config: BrmConfig) = {
		s match {
			case Some(x) if config.logFlags =>
				x.flags.map {
					case (key, value) => s"records.record$c.flags.$key" -> value
				}
			case _ => Map()
		}
	}

  private def logNameCount(p: Payload, auditWordsPerNameOnRecords: Map[String, String])(implicit logger: BRMLogger): Unit = {

    val payloadCount = Map(
      s"payload.numberOfForenames" -> s"${p.firstNames.names.count(_.nonEmpty) + p.additionalNames.names.count(_.nonEmpty)}",
      s"payload.numberOfLastnames" -> s"${p.lastName.names.count(_.nonEmpty)}"
    )

    logger.info("TransactionAuditor", "logNameCount", s"${Json.toJson(payloadCount ++ auditWordsPerNameOnRecords)}")
  }
}
