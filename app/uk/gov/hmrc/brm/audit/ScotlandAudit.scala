package uk.gov.hmrc.brm.audit

import uk.gov.hmrc.brm.config.MicroserviceGlobal
import uk.gov.hmrc.brm.models.brm.Payload
import uk.gov.hmrc.play.http.HeaderCarrier

/**
  * Created by adamconder on 08/02/2017.
  */
object ScotlandAudit extends BRMAudit {

  /**
    * ScotlandAuditEvent
    * Responsible for auditing when we find records on NRS
    * @param result map of key value results
    * @param path endpoint path
    * @param hc implicit headerCarrier
    */
  final class ScotlandAuditEvent(result : Map[String, String], path: String)(implicit hc: HeaderCarrier)
    extends AuditEvent(auditType = "BRM-NRSScotland-Results", detail = result, transactionName = "brm-scotland-match", path)

  override val connector = MicroserviceGlobal.auditConnector

  def audit(result : Map[String, String], payload: Payload)(implicit hc : HeaderCarrier) = {
    event(new ScotlandAuditEvent(result, path = "nrs"))
  }

}