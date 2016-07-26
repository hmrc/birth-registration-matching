package uk.gov.hmrc.birthregistrationmatching.utility

import uk.gov.hmrc.play.test.UnitSpec

import uk.gov.hmrc.utility._

/**
  * Created by chrisianson on 26/07/16.
  */
class FormatSpec extends UnitSpec {

  "Format" should {

    "remove whitespace from the start of a string" in {

      val input: String = "chris "
      NameFormat.format(input) shouldBe "chris"
    }
  }

}
