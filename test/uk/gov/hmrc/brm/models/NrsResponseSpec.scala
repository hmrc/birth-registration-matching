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

package uk.gov.hmrc.brm.models

import org.joda.time.LocalDate
import play.api.libs.json.{Json, Reads}
import uk.gov.hmrc.brm.models.response.{Record, StatusInterface}
import uk.gov.hmrc.brm.models.response.gro.{Child, Status}
import uk.gov.hmrc.brm.models.response.nrs.NRSStatus
import uk.gov.hmrc.brm.utils.{JsonUtils, ReadsUtil}
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.brm.utils.TestHelper._
/**
  * Created by user on 07/03/17.
  */
class NrsResponseSpec extends UnitSpec {

  lazy val emptyJson = Json.parse(
    """
      |{
      |}
    """.stripMargin)

  lazy val jsonValidWithUTF8 = Json.parse(
    """
      |{  "births": [
      |    {
      | "subjects" : {
      |  "child" : {
      |    "firstName" : "»¼½¾¿ÀÁÂÃÄÅÆÇÈÉÊËÌÍ ÏÐÑÒÓÔÕÖ×ØÙÚÛÜÝÞßàáâãäåæçèéêëìíîïðñòóôõö÷øùúûüýþÿ",
      |    "lastName" : "ÏÐÑÒÓÔÕÖ×ØÙÚÛÜÝÞßàáâãäåæçèéêëìíîïðñòóôõö÷øùúûüýþÿ''--",
      |    "dateOfBirth" : "2007-02-18"
      |  }
      | },
      | "id" : "2017734003",
      | "status": 1,
      | "deathCode": 0
      |}   ]
      |}
    """.stripMargin)





  "Record" should {

    "return Record object with all Child attributes when json is a full record as NRS record" in {
      val nrsJsonResponseObject = JsonUtils.getJsonFromFile("nrs", "2017734003")

      var response = nrsJsonResponseObject.validate[List[Record]](ReadsUtil.nrsRecordsListRead)
      response.isSuccess shouldBe true
      var listOfRecords = nrsJsonResponseObject.as[List[Record]](ReadsUtil.nrsRecordsListRead)

      val record = listOfRecords.head
      listOfRecords.length shouldBe 1
      record shouldBe a[Record]
      record.child shouldBe a[Child]
      record.child.birthReferenceNumber shouldBe 2017734003
      record.child.firstName shouldBe "Adam TEST"
      record.child.lastName shouldBe "SMITH"
      record.child.dateOfBirth.get.toString shouldBe "2009-11-12"
      record.child.dateOfBirth.get shouldBe a[LocalDate]
      record.status.get shouldBe a[StatusInterface]
      record.status.get.asInstanceOf[NRSStatus].status shouldBe 1
      record.status.get.asInstanceOf[NRSStatus].deathCode shouldBe 0
    }



    "return Record object with all Child attributes when json is does not have father details" in {
      val nrsJsonResponseObject = JsonUtils.getJsonFromFile("nrs", "2017734003_withoutFatherDetails")

      var response = nrsJsonResponseObject.validate[List[Record]](ReadsUtil.nrsRecordsListRead)
      response.isSuccess shouldBe true
      var listOfRecords = nrsJsonResponseObject.as[List[Record]](ReadsUtil.nrsRecordsListRead)

      val record = listOfRecords.head
      listOfRecords.length shouldBe 1
      record shouldBe a[Record]
      record.child shouldBe a[Child]
      record.child.birthReferenceNumber shouldBe 2017734003
      record.child.firstName shouldBe "Adam TEST"
      record.child.lastName shouldBe "SMITH"
      record.child.dateOfBirth.get.toString shouldBe "2009-11-12"
      record.child.dateOfBirth.get shouldBe a[LocalDate]
      record.status.get shouldBe a[StatusInterface]
      record.status.get.asInstanceOf[NRSStatus].status shouldBe 1
      record.status.get.asInstanceOf[NRSStatus].deathCode shouldBe 0
    }




    "return Record object with all Child attributes when json is does not have mother details" in {
      val nrsJsonResponseObject = JsonUtils.getJsonFromFile("nrs", "2017734003_withoutMotherDetails")

      var response = nrsJsonResponseObject.validate[List[Record]](ReadsUtil.nrsRecordsListRead)
      response.isSuccess shouldBe true
      var listOfRecords = nrsJsonResponseObject.as[List[Record]](ReadsUtil.nrsRecordsListRead)

      val record = listOfRecords.head
      listOfRecords.length shouldBe 1
      record shouldBe a[Record]
      record.child shouldBe a[Child]
      record.child.birthReferenceNumber shouldBe 2017734003
      record.child.firstName shouldBe "Adam TEST"
      record.child.lastName shouldBe "SMITH"
      record.child.dateOfBirth.get.toString shouldBe "2009-11-12"
      record.child.dateOfBirth.get shouldBe a[LocalDate]
      record.status.get shouldBe a[StatusInterface]
      record.status.get.asInstanceOf[NRSStatus].status shouldBe 1
      record.status.get.asInstanceOf[NRSStatus].deathCode shouldBe 0
    }


    "return Record object with all Child attributes when json is does not have only informant details" in {
      val nrsJsonResponseObject = JsonUtils.getJsonFromFile("nrs", "2017734003_withoutInformant")

      var response = nrsJsonResponseObject.validate[List[Record]](ReadsUtil.nrsRecordsListRead)
      response.isSuccess shouldBe true
      var listOfRecords = nrsJsonResponseObject.as[List[Record]](ReadsUtil.nrsRecordsListRead)

      val record = listOfRecords.head
      listOfRecords.length shouldBe 1
      record shouldBe a[Record]
      record.child shouldBe a[Child]
      record.child.birthReferenceNumber shouldBe 2017734003
      record.child.firstName shouldBe "Adam TEST"
      record.child.lastName shouldBe "SMITH"
      record.child.dateOfBirth.get.toString shouldBe "2009-11-12"
      record.child.dateOfBirth.get shouldBe a[LocalDate]
      record.status.get shouldBe a[StatusInterface]
      record.status.get.asInstanceOf[NRSStatus].status shouldBe 1
      record.status.get.asInstanceOf[NRSStatus].deathCode shouldBe 0
    }


    "return Record object with all Child attributes when record does not have mother First Name only." in {
      val nrsJsonResponseObject = JsonUtils.getJsonFromFile("nrs", "2017734003_withoutMotherFirstName")

      var response = nrsJsonResponseObject.validate[List[Record]](ReadsUtil.nrsRecordsListRead)
      response.isSuccess shouldBe true
      var listOfRecords = nrsJsonResponseObject.as[List[Record]](ReadsUtil.nrsRecordsListRead)

      val record = listOfRecords.head
      listOfRecords.length shouldBe 1
      record shouldBe a[Record]
      record.child shouldBe a[Child]
      record.child.birthReferenceNumber shouldBe 2017734003
      record.child.firstName shouldBe "Adam TEST"
      record.child.lastName shouldBe "SMITH"
      record.child.dateOfBirth.get.toString shouldBe "2009-11-12"
      record.child.dateOfBirth.get shouldBe a[LocalDate]
      record.status.get shouldBe a[StatusInterface]
      record.status.get.asInstanceOf[NRSStatus].status shouldBe 1
      record.status.get.asInstanceOf[NRSStatus].deathCode shouldBe 0
    }



    "return Record object with all Child attributes when record does not have mother Last Name only." in {
      val nrsJsonResponseObject = JsonUtils.getJsonFromFile("nrs", "2017734003_withoutMotherLastName")

      var response = nrsJsonResponseObject.validate[List[Record]](ReadsUtil.nrsRecordsListRead)
      response.isSuccess shouldBe true
      var listOfRecords = nrsJsonResponseObject.as[List[Record]](ReadsUtil.nrsRecordsListRead)

      val record = listOfRecords.head
      listOfRecords.length shouldBe 1
      record shouldBe a[Record]
      record.child shouldBe a[Child]
      record.child.birthReferenceNumber shouldBe 2017734003
      record.child.firstName shouldBe "Adam TEST"
      record.child.lastName shouldBe "SMITH"
      record.child.dateOfBirth.get.toString shouldBe "2009-11-12"
      record.child.dateOfBirth.get shouldBe a[LocalDate]
      record.status.get shouldBe a[StatusInterface]
      record.status.get.asInstanceOf[NRSStatus].status shouldBe 1
      record.status.get.asInstanceOf[NRSStatus].deathCode shouldBe 0
    }


    "return Record object with all Child attributes when record does not have mothers birth place." in {
      val nrsJsonResponseObject = JsonUtils.getJsonFromFile("nrs", "2017734003_withoutMotherBirthPlace")

      var response = nrsJsonResponseObject.validate[List[Record]](ReadsUtil.nrsRecordsListRead)
      response.isSuccess shouldBe true
      var listOfRecords = nrsJsonResponseObject.as[List[Record]](ReadsUtil.nrsRecordsListRead)

      val record = listOfRecords.head
      listOfRecords.length shouldBe 1
      record shouldBe a[Record]
      record.child shouldBe a[Child]
      record.child.birthReferenceNumber shouldBe 2017734003
      record.child.firstName shouldBe "Adam TEST"
      record.child.lastName shouldBe "SMITH"
      record.child.dateOfBirth.get.toString shouldBe "2009-11-12"
      record.child.dateOfBirth.get shouldBe a[LocalDate]
      record.status.get shouldBe a[StatusInterface]
      record.status.get.asInstanceOf[NRSStatus].status shouldBe 1
      record.status.get.asInstanceOf[NRSStatus].deathCode shouldBe 0
    }


    "return Record object with all Child attributes as empty when there are not child details value." in {
      val nrsJsonResponseObject = JsonUtils.getJsonFromFile("nrs", "2017734003_withoutChildDetails")

      var response = nrsJsonResponseObject.validate[List[Record]](ReadsUtil.nrsRecordsListRead)
      response.isSuccess shouldBe true
      var listOfRecords = nrsJsonResponseObject.as[List[Record]](ReadsUtil.nrsRecordsListRead)

      val record = listOfRecords.head
      listOfRecords.length shouldBe 1
      record shouldBe a[Record]
      record.child shouldBe a[Child]
      record.child.birthReferenceNumber shouldBe 2017734003
      record.child.firstName shouldBe ""
      record.child.lastName shouldBe ""
      record.child.dateOfBirth shouldBe None
      record.status.get shouldBe a[StatusInterface]
      record.status.get.asInstanceOf[NRSStatus].status shouldBe 1
      record.status.get.asInstanceOf[NRSStatus].deathCode shouldBe 0
    }

    "return Record object with all Child attributes as empty when there are not child details object." in {
      val nrsJsonResponseObject = JsonUtils.getJsonFromFile("nrs", "2017734003_withoutChildDetailsObject")

      var response = nrsJsonResponseObject.validate[List[Record]](ReadsUtil.nrsRecordsListRead)
      response.isSuccess shouldBe true
      var listOfRecords = nrsJsonResponseObject.as[List[Record]](ReadsUtil.nrsRecordsListRead)

      val record = listOfRecords.head
      listOfRecords.length shouldBe 1
      record shouldBe a[Record]
      record.child shouldBe a[Child]
      record.child.birthReferenceNumber shouldBe 2017734003
      record.child.firstName shouldBe ""
      record.child.lastName shouldBe ""
      record.child.dateOfBirth shouldBe None
      record.status.get shouldBe a[StatusInterface]
      record.status.get.asInstanceOf[NRSStatus].status shouldBe 1
      record.status.get.asInstanceOf[NRSStatus].deathCode shouldBe 0
    }


    "return Record object with all Child attributes when record does not have father First Name only." in {
      val nrsJsonResponseObject = getNrsResponse(fatherName = "")

      var response = nrsJsonResponseObject.validate[List[Record]](ReadsUtil.nrsRecordsListRead)
      response.isSuccess shouldBe true
      var listOfRecords = nrsJsonResponseObject.as[List[Record]](ReadsUtil.nrsRecordsListRead)

      val record = listOfRecords.head
      listOfRecords.length shouldBe 1
      record shouldBe a[Record]
      record.child shouldBe a[Child]
      record.child.birthReferenceNumber shouldBe 2017734003
      record.child.firstName shouldBe "Adam TEST"
      record.child.lastName shouldBe "SMITH"
      record.child.dateOfBirth.get.toString shouldBe "2009-11-12"
      record.child.dateOfBirth.get shouldBe a[LocalDate]
      record.status.get shouldBe a[StatusInterface]
      record.status.get.asInstanceOf[NRSStatus].status shouldBe 1
      record.status.get.asInstanceOf[NRSStatus].deathCode shouldBe 0
    }


    "return Record object with all Child attributes when record does not have father Last Name only." in {
      val nrsJsonResponseObject = getNrsResponse(fatherLastName = "")

      var response = nrsJsonResponseObject.validate[List[Record]](ReadsUtil.nrsRecordsListRead)
      response.isSuccess shouldBe true
      var listOfRecords = nrsJsonResponseObject.as[List[Record]](ReadsUtil.nrsRecordsListRead)

      val record = listOfRecords.head
      listOfRecords.length shouldBe 1
      record shouldBe a[Record]
      record.child shouldBe a[Child]
      record.child.birthReferenceNumber shouldBe 2017734003
      record.child.firstName shouldBe "Adam TEST"
      record.child.lastName shouldBe "SMITH"
      record.child.dateOfBirth.get.toString shouldBe "2009-11-12"
      record.child.dateOfBirth.get shouldBe a[LocalDate]
      record.status.get shouldBe a[StatusInterface]
      record.status.get.asInstanceOf[NRSStatus].status shouldBe 1
      record.status.get.asInstanceOf[NRSStatus].deathCode shouldBe 0
    }


    "return Record object with all Child attributes when record does not have father's birth place." in {
      val nrsJsonResponseObject = getNrsResponse(fatherBirthPlace = "")

      var response = nrsJsonResponseObject.validate[List[Record]](ReadsUtil.nrsRecordsListRead)
      response.isSuccess shouldBe true
      var listOfRecords = nrsJsonResponseObject.as[List[Record]](ReadsUtil.nrsRecordsListRead)

      val record = listOfRecords.head
      listOfRecords.length shouldBe 1
      record shouldBe a[Record]
      record.child shouldBe a[Child]
      record.child.birthReferenceNumber shouldBe 2017734003
      record.child.firstName shouldBe "Adam TEST"
      record.child.lastName shouldBe "SMITH"
      record.child.dateOfBirth.get.toString shouldBe "2009-11-12"
      record.child.dateOfBirth.get shouldBe a[LocalDate]
      record.status.get shouldBe a[StatusInterface]
      record.status.get.asInstanceOf[NRSStatus].status shouldBe 1
      record.status.get.asInstanceOf[NRSStatus].deathCode shouldBe 0
    }

    "return Record object with all Child attributes when record does not have informant's qualification." in {
      val nrsJsonResponseObject = getNrsResponse(qualification = "")

      var response = nrsJsonResponseObject.validate[List[Record]](ReadsUtil.nrsRecordsListRead)
      response.isSuccess shouldBe true
      var listOfRecords = nrsJsonResponseObject.as[List[Record]](ReadsUtil.nrsRecordsListRead)

      val record = listOfRecords.head
      listOfRecords.length shouldBe 1
      record shouldBe a[Record]
      record.child shouldBe a[Child]
      record.child.birthReferenceNumber shouldBe 2017734003
      record.child.firstName shouldBe "Adam TEST"
      record.child.lastName shouldBe "SMITH"
      record.child.dateOfBirth.get.toString shouldBe "2009-11-12"
      record.child.dateOfBirth.get shouldBe a[LocalDate]
      record.status.get shouldBe a[StatusInterface]
      record.status.get.asInstanceOf[NRSStatus].status shouldBe 1
      record.status.get.asInstanceOf[NRSStatus].deathCode shouldBe 0
    }


    "return Record object without child details like firstname, lastname, dob when json does not have child details" in {
      val nrsJsonResponseObject = JsonUtils.getJsonFromFile("nrs", "2017350003")
      var listOfRecords = nrsJsonResponseObject.as[List[Record]](ReadsUtil.nrsRecordsListRead)
      val record = listOfRecords.head
      listOfRecords.length shouldBe 1
      record shouldBe a[Record]
      record.child shouldBe a[Child]
      record.child.birthReferenceNumber shouldBe 2017350003
      record.child.firstName shouldBe ""
      record.child.lastName shouldBe ""
      record.child.dateOfBirth shouldBe None
      record.status.get shouldBe a[StatusInterface]
      record.status.get.asInstanceOf[NRSStatus].status shouldBe -4
      record.status.get.asInstanceOf[NRSStatus].deathCode shouldBe 0
    }




    "return two Record object with all Child attributes when json has two records" in {
      val nrsJsonResponseObject = JsonUtils.getJsonFromFile("nrs", "AdamTEST_multiple")

      var response = nrsJsonResponseObject.validate[List[Record]](ReadsUtil.nrsRecordsListRead)
      response.isSuccess shouldBe true
      var listOfRecords = nrsJsonResponseObject.as[List[Record]](ReadsUtil.nrsRecordsListRead)
      listOfRecords.length shouldBe 2

      val record = listOfRecords.head

      record shouldBe a[Record]
      record.child shouldBe a[Child]
      record.child.birthReferenceNumber shouldBe 2017734003
      record.child.firstName shouldBe "Adam TEST"
      record.child.lastName shouldBe "SMITH"
      record.child.dateOfBirth.get.toString shouldBe "2009-11-12"
      record.child.dateOfBirth.get shouldBe a[LocalDate]
      record.status.get shouldBe a[StatusInterface]
      record.status.get.asInstanceOf[NRSStatus].status shouldBe 1
      record.status.get.asInstanceOf[NRSStatus].deathCode shouldBe 0

      val recordTwo = listOfRecords(1)

      recordTwo shouldBe a[Record]
      recordTwo.child shouldBe a[Child]
      recordTwo.child.birthReferenceNumber shouldBe 2017734004
      recordTwo.child.firstName shouldBe "Adam TEST"
      recordTwo.child.lastName shouldBe "SMITH"
      recordTwo.child.dateOfBirth.get.toString shouldBe "2009-11-12"
      recordTwo.child.dateOfBirth.get shouldBe a[LocalDate]
      record.status.get shouldBe a[StatusInterface]
      record.status.get.asInstanceOf[NRSStatus].status shouldBe 1
      record.status.get.asInstanceOf[NRSStatus].deathCode shouldBe 0
    }

    "return error when json is empty" in {
      var response = emptyJson.validate[List[Record]](ReadsUtil.nrsRecordsListRead)
      response.isError shouldBe true
      var singleRecordRead = emptyJson.validate[Record](ReadsUtil.nrsRecordsRead)
      singleRecordRead.isError shouldBe true
    }

    "return Record object with all Child attributes when json is a full record and with UTF ASCII extendted characters." in {

      var response = jsonValidWithUTF8.validate[List[Record]](ReadsUtil.nrsRecordsListRead)
      response.isSuccess shouldBe true
      var listOfRecords = jsonValidWithUTF8.as[List[Record]](ReadsUtil.nrsRecordsListRead)

      val record = listOfRecords.head
      listOfRecords.length shouldBe 1
      record shouldBe a[Record]
      record.child shouldBe a[Child]
      record.child.birthReferenceNumber shouldBe 2017734003
      record.child.firstName shouldBe "»¼½¾¿ÀÁÂÃÄÅÆÇÈÉÊËÌÍ ÏÐÑÒÓÔÕÖ×ØÙÚÛÜÝÞßàáâãäåæçèéêëìíîïðñòóôõö÷øùúûüýþÿ"
      record.child.lastName shouldBe "ÏÐÑÒÓÔÕÖ×ØÙÚÛÜÝÞßàáâãäåæçèéêëìíîïðñòóôõö÷øùúûüýþÿ''--"
      record.child.dateOfBirth.get.toString shouldBe "2007-02-18"
      record.child.dateOfBirth.get shouldBe a[LocalDate]
      record.status.get shouldBe a[StatusInterface]
      record.status.get.asInstanceOf[NRSStatus].status shouldBe 1
      record.status.get.asInstanceOf[NRSStatus].deathCode shouldBe 0
    }

  }

}
