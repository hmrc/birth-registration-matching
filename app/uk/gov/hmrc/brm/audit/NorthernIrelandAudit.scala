package uk.gov.hmrc.brm.audit

import uk.gov.hmrc.brm.config.MicroserviceGlobal
import uk.gov.hmrc.brm.models.brm.Payload
import uk.gov.hmrc.play.http.HeaderCarrier

/**
  * Created by adamconder on 08/02/2017.
  */
object NorthernIrelandAudit extends BRMAudit {

  /**
    * NorthernIrelandAuditEvent
    * Responsible for auditing when we find records on GRO-NI
    * @param result map of key value results
    * @param path endpoint path
    * @param hc implicit headerCarrier
    */
  final private class NorthernIrelandAuditEvent(result : Map[String, String], path: String)(implicit hc: HeaderCarrier)
    extends AuditEvent(auditType = "BRM-GRONorthernIreland-Results", detail = result, transactionName = "brm-northern-ireland-match", path)

  override val connector = MicroserviceGlobal.auditConnector

  def audit(result : Map[String, String], payload: Payload)(implicit hc : HeaderCarrier) = {
    event(new NorthernIrelandAuditEvent(result, path = "gro-ni"))
  }


}
