package uk.gov.hmrc.brm.models.matching

import play.api.libs.json.Json
import uk.gov.hmrc.brm.models.brm.Payload

/**
  * Created by user on 28/09/16.
  */
case class ResultMatch (isMatch : Boolean)

object ResultMatch {
  implicit val formats = Json.format[ResultMatch]
}

