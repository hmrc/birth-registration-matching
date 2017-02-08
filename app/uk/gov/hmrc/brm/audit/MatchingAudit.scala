package uk.gov.hmrc.brm.audit

import uk.gov.hmrc.brm.config.MicroserviceGlobal
import uk.gov.hmrc.brm.models.brm.Payload
import uk.gov.hmrc.brm.utils.CommonUtil
import uk.gov.hmrc.brm.utils.CommonUtil.{DetailsRequest, ReferenceRequest}
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future

/**
  * Created by adamconder on 08/02/2017.
  */
object MatchingAudit extends BRMAudit {

  /**
    * MatchingEvent
    * Audit event for the result of MatchingService, how did the matching perform
    * @param result map of key value results
    * @param hc implicit headerCarrier
    */
  final class MatchingEvent(result: Map[String, String], path : String)
                           (implicit hc : HeaderCarrier)
    extends AuditEvent("BRM-Matching-Results", detail = result, transactionName = "brm-match", path)

  override val connector = MicroserviceGlobal.auditConnector

  def audit(result : Map[String, String], payload : Payload)(implicit hc : HeaderCarrier) = {
    CommonUtil.getOperationType(payload) match {
      case DetailsRequest() =>
        event(new MatchingEvent(result, "birth-registration-matching/match/details"))
      case ReferenceRequest() =>
        event(new MatchingEvent(result, "birth-registration-matching/match/reference"))
    }

  }


}
