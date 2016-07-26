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
