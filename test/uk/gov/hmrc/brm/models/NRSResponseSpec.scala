/*
 * Copyright 2021 HM Revenue & Customs
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
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.brm.models.response.nrs.NRSStatus
import uk.gov.hmrc.brm.models.response.{Child, Record, StatusInterface}
import uk.gov.hmrc.brm.utils.TestHelper._
import uk.gov.hmrc.brm.utils.{JsonUtils, ReadsUtil}
import org.scalatest.{Matchers, OptionValues, WordSpecLike}

class NRSResponseSpec extends WordSpecLike with Matchers with OptionValues with GuiceOneAppPerSuite {

  lazy val emptyJson: JsValue = Json.parse(
    """
      |{
      |}
    """.stripMargin
  )

  lazy val jsonValidWithUTF8: JsValue = Json.parse(
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
    """.stripMargin
  )

  lazy val jsonValidWithUTF8Deceased: JsValue = Json.parse(
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
      | "deathCode": 1
      |}   ]
      |}
    """.stripMargin
  )

  lazy val jsonValidWithUTF8Corrections: JsValue = Json.parse(
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
      | "status": -4,
      | "deathCode": 1
      |}   ]
      |}
    """.stripMargin
  )

  lazy val jsonValidWithUTF8Incomplete: JsValue = Json.parse(
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
      | "status": -5,
      | "deathCode": 1
      |}   ]
      |}
    """.stripMargin
  )

  lazy val jsonValidWithUTF8Cancelled: JsValue = Json.parse(
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
      | "status": -6,
      | "deathCode": 1
      |}   ]
      |}
    """.stripMargin
  )

  lazy val jsonValidWithUTF8Unknown: JsValue = Json.parse(
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
      | "status": 0,
      | "deathCode": 1
      |}   ]
      |}
    """.stripMargin
  )

  "Record" should {

    "return Record object with all Child attributes when json is a full record as NRS record" in {
      val nrsJsonResponseObject = JsonUtils.getJsonFromFile("nrs", "2017734003")

      val response = nrsJsonResponseObject.validate[List[Record]](ReadsUtil.nrsRecordsListRead)
      response.isSuccess shouldBe true
      val listOfRecords = nrsJsonResponseObject.as[List[Record]](ReadsUtil.nrsRecordsListRead)

      val record = listOfRecords.head
      listOfRecords.length shouldBe 1
      record shouldBe a[Record]
      record.child shouldBe a[Child]
      record.child.birthReferenceNumber shouldBe 2017734003
      record.child.forenames shouldBe "Adam TEST"
      record.child.lastName shouldBe "SMITH"
      record.child.dateOfBirth.get.toString shouldBe "2009-11-12"
      record.child.dateOfBirth.get shouldBe a[LocalDate]
      record.status.get shouldBe a[StatusInterface]
      record.status.get.asInstanceOf[NRSStatus].status shouldBe 1
      record.status.get.asInstanceOf[NRSStatus].deathCode shouldBe 0
      record.status.get.toJson shouldBe
        Json.parse(s"""
           |{
           |  "status": "1",
           |  "deathCode": "0"
           |}
         """.stripMargin)
    }

    "return a Map() of flags where found and not deceased" in {
      val response = jsonValidWithUTF8.validate[List[Record]](ReadsUtil.nrsRecordsListRead).get
      response.head.status.get.flags shouldBe Map("status" -> "Valid", "deathCode" -> "Not deceased")
    }

    "return a Map() of flags where found and deceased" in {
      val response = jsonValidWithUTF8Deceased.validate[List[Record]](ReadsUtil.nrsRecordsListRead).get
      response.head.status.get.flags shouldBe Map("status" -> "Valid", "deathCode" -> "Potentially deceased")
    }

    "return a Map() of flags where status is Corrections" in {
      val response = jsonValidWithUTF8Corrections.validate[List[Record]](ReadsUtil.nrsRecordsListRead).get
      response.head.status.get.flags shouldBe Map("status" -> "Corrections", "deathCode" -> "Potentially deceased")

    }

    "return a Map() of flags where status is Not completed" in {
      val response = jsonValidWithUTF8Incomplete.validate[List[Record]](ReadsUtil.nrsRecordsListRead).get
      response.head.status.get.flags shouldBe Map("status" -> "Incomplete", "deathCode" -> "Potentially deceased")

    }

    "return a Map() of flags where status is Cancelled" in {
      val response = jsonValidWithUTF8Cancelled.validate[List[Record]](ReadsUtil.nrsRecordsListRead).get
      response.head.status.get.flags shouldBe Map("status" -> "Cancelled", "deathCode" -> "Potentially deceased")

    }

    "return a Map() of flags where status is Unknown" in {
      val response = jsonValidWithUTF8Unknown.validate[List[Record]](ReadsUtil.nrsRecordsListRead).get
      response.head.status.get.flags shouldBe Map("status" -> "Unknown", "deathCode" -> "Potentially deceased")

    }

    "return Record object with all Child attributes when json is does not have father details" in {
      val nrsJsonResponseObject = JsonUtils.getJsonFromFile("nrs", "2017734003_withoutFatherDetails")

      val response = nrsJsonResponseObject.validate[List[Record]](ReadsUtil.nrsRecordsListRead)
      response.isSuccess shouldBe true
      val listOfRecords = nrsJsonResponseObject.as[List[Record]](ReadsUtil.nrsRecordsListRead)

      val record = listOfRecords.head
      listOfRecords.length shouldBe 1
      record shouldBe a[Record]
      record.child shouldBe a[Child]
      record.child.birthReferenceNumber shouldBe 2017734003
      record.child.forenames shouldBe "Adam TEST"
      record.child.lastName shouldBe "SMITH"
      record.child.dateOfBirth.get.toString shouldBe "2009-11-12"
      record.child.dateOfBirth.get shouldBe a[LocalDate]
      record.status.get shouldBe a[StatusInterface]
      record.status.get.asInstanceOf[NRSStatus].status shouldBe 1
      record.status.get.asInstanceOf[NRSStatus].deathCode shouldBe 0
    }

    "return Record object with all Child attributes when json is does not have mother details" in {
      val nrsJsonResponseObject = JsonUtils.getJsonFromFile("nrs", "2017734003_withoutMotherDetails")

      val response = nrsJsonResponseObject.validate[List[Record]](ReadsUtil.nrsRecordsListRead)
      response.isSuccess shouldBe true
      val listOfRecords = nrsJsonResponseObject.as[List[Record]](ReadsUtil.nrsRecordsListRead)

      val record = listOfRecords.head
      listOfRecords.length shouldBe 1
      record shouldBe a[Record]
      record.child shouldBe a[Child]
      record.child.birthReferenceNumber shouldBe 2017734003
      record.child.forenames shouldBe "Adam TEST"
      record.child.lastName shouldBe "SMITH"
      record.child.dateOfBirth.get.toString shouldBe "2009-11-12"
      record.child.dateOfBirth.get shouldBe a[LocalDate]
      record.status.get shouldBe a[StatusInterface]
      record.status.get.asInstanceOf[NRSStatus].status shouldBe 1
      record.status.get.asInstanceOf[NRSStatus].deathCode shouldBe 0
    }

    "return Record object with all Child attributes when json is does not have only informant details" in {
      val nrsJsonResponseObject = JsonUtils.getJsonFromFile("nrs", "2017734003_withoutInformant")

      val response = nrsJsonResponseObject.validate[List[Record]](ReadsUtil.nrsRecordsListRead)
      response.isSuccess shouldBe true
      val listOfRecords = nrsJsonResponseObject.as[List[Record]](ReadsUtil.nrsRecordsListRead)

      val record = listOfRecords.head
      listOfRecords.length shouldBe 1
      record shouldBe a[Record]
      record.child shouldBe a[Child]
      record.child.birthReferenceNumber shouldBe 2017734003
      record.child.forenames shouldBe "Adam TEST"
      record.child.lastName shouldBe "SMITH"
      record.child.dateOfBirth.get.toString shouldBe "2009-11-12"
      record.child.dateOfBirth.get shouldBe a[LocalDate]
      record.status.get shouldBe a[StatusInterface]
      record.status.get.asInstanceOf[NRSStatus].status shouldBe 1
      record.status.get.asInstanceOf[NRSStatus].deathCode shouldBe 0
    }

    "return Record object with all Child attributes when record does not have mother First Name only." in {
      val nrsJsonResponseObject = JsonUtils.getJsonFromFile("nrs", "2017734003_withoutMotherFirstName")

      val response = nrsJsonResponseObject.validate[List[Record]](ReadsUtil.nrsRecordsListRead)
      response.isSuccess shouldBe true
      val listOfRecords = nrsJsonResponseObject.as[List[Record]](ReadsUtil.nrsRecordsListRead)

      val record = listOfRecords.head
      listOfRecords.length shouldBe 1
      record shouldBe a[Record]
      record.child shouldBe a[Child]
      record.child.birthReferenceNumber shouldBe 2017734003
      record.child.forenames shouldBe "Adam TEST"
      record.child.lastName shouldBe "SMITH"
      record.child.dateOfBirth.get.toString shouldBe "2009-11-12"
      record.child.dateOfBirth.get shouldBe a[LocalDate]
      record.status.get shouldBe a[StatusInterface]
      record.status.get.asInstanceOf[NRSStatus].status shouldBe 1
      record.status.get.asInstanceOf[NRSStatus].deathCode shouldBe 0
    }

    "return Record object with all Child attributes when record does not have mother Last Name only." in {
      val nrsJsonResponseObject = JsonUtils.getJsonFromFile("nrs", "2017734003_withoutMotherLastName")

      val response = nrsJsonResponseObject.validate[List[Record]](ReadsUtil.nrsRecordsListRead)
      response.isSuccess shouldBe true
      val listOfRecords = nrsJsonResponseObject.as[List[Record]](ReadsUtil.nrsRecordsListRead)

      val record = listOfRecords.head
      listOfRecords.length shouldBe 1
      record shouldBe a[Record]
      record.child shouldBe a[Child]
      record.child.birthReferenceNumber shouldBe 2017734003
      record.child.forenames shouldBe "Adam TEST"
      record.child.lastName shouldBe "SMITH"
      record.child.dateOfBirth.get.toString shouldBe "2009-11-12"
      record.child.dateOfBirth.get shouldBe a[LocalDate]
      record.status.get shouldBe a[StatusInterface]
      record.status.get.asInstanceOf[NRSStatus].status shouldBe 1
      record.status.get.asInstanceOf[NRSStatus].deathCode shouldBe 0
    }

    "return Record object with all Child attributes when record does not have mothers birth place." in {
      val nrsJsonResponseObject = JsonUtils.getJsonFromFile("nrs", "2017734003_withoutMotherBirthPlace")

      val response = nrsJsonResponseObject.validate[List[Record]](ReadsUtil.nrsRecordsListRead)
      response.isSuccess shouldBe true
      val listOfRecords = nrsJsonResponseObject.as[List[Record]](ReadsUtil.nrsRecordsListRead)

      val record = listOfRecords.head
      listOfRecords.length shouldBe 1
      record shouldBe a[Record]
      record.child shouldBe a[Child]
      record.child.birthReferenceNumber shouldBe 2017734003
      record.child.forenames shouldBe "Adam TEST"
      record.child.lastName shouldBe "SMITH"
      record.child.dateOfBirth.get.toString shouldBe "2009-11-12"
      record.child.dateOfBirth.get shouldBe a[LocalDate]
      record.status.get shouldBe a[StatusInterface]
      record.status.get.asInstanceOf[NRSStatus].status shouldBe 1
      record.status.get.asInstanceOf[NRSStatus].deathCode shouldBe 0
    }

    "return Record object with all Child attributes as empty when there are not child details value." in {
      val nrsJsonResponseObject = JsonUtils.getJsonFromFile("nrs", "2017734003_withoutChildDetails")

      val response = nrsJsonResponseObject.validate[List[Record]](ReadsUtil.nrsRecordsListRead)
      response.isSuccess shouldBe true
      val listOfRecords = nrsJsonResponseObject.as[List[Record]](ReadsUtil.nrsRecordsListRead)

      val record = listOfRecords.head
      listOfRecords.length shouldBe 1
      record shouldBe a[Record]
      record.child shouldBe a[Child]
      record.child.birthReferenceNumber shouldBe 2017734003
      record.child.forenames shouldBe ""
      record.child.lastName shouldBe ""
      record.child.dateOfBirth shouldBe None
      record.status.get shouldBe a[StatusInterface]
      record.status.get.asInstanceOf[NRSStatus].status shouldBe 1
      record.status.get.asInstanceOf[NRSStatus].deathCode shouldBe 0
    }

    "return Record object with all Child attributes as empty when there are not child details object." in {
      val nrsJsonResponseObject = JsonUtils.getJsonFromFile("nrs", "2017734003_withoutChildDetailsObject")

      val response = nrsJsonResponseObject.validate[List[Record]](ReadsUtil.nrsRecordsListRead)
      response.isSuccess shouldBe true
      val listOfRecords = nrsJsonResponseObject.as[List[Record]](ReadsUtil.nrsRecordsListRead)

      val record = listOfRecords.head
      listOfRecords.length shouldBe 1
      record shouldBe a[Record]
      record.child shouldBe a[Child]
      record.child.birthReferenceNumber shouldBe 2017734003
      record.child.forenames shouldBe ""
      record.child.lastName shouldBe ""
      record.child.dateOfBirth shouldBe None
      record.status.get shouldBe a[StatusInterface]
      record.status.get.asInstanceOf[NRSStatus].status shouldBe 1
      record.status.get.asInstanceOf[NRSStatus].deathCode shouldBe 0
    }

    "return Record object with all Child attributes when record does not have father value firstName only." in {
      val nrsJsonResponseObject = getNrsResponse(fatherName = "")

      val response = nrsJsonResponseObject.validate[List[Record]](ReadsUtil.nrsRecordsListRead)
      response.isSuccess shouldBe true
      val listOfRecords = nrsJsonResponseObject.as[List[Record]](ReadsUtil.nrsRecordsListRead)

      val record = listOfRecords.head
      listOfRecords.length shouldBe 1
      record shouldBe a[Record]
      record.child shouldBe a[Child]
      record.child.birthReferenceNumber shouldBe 2017734003
      record.child.forenames shouldBe "Adam TEST"
      record.child.lastName shouldBe "SMITH"
      record.child.dateOfBirth.get.toString shouldBe "2009-11-12"
      record.child.dateOfBirth.get shouldBe a[LocalDate]
      record.status.get shouldBe a[StatusInterface]
      record.status.get.asInstanceOf[NRSStatus].status shouldBe 1
      record.status.get.asInstanceOf[NRSStatus].deathCode shouldBe 0
    }

    "return Record object with all Child attributes when record does not have father value lastName only." in {
      val nrsJsonResponseObject = getNrsResponse(fatherLastName = "")

      val response = nrsJsonResponseObject.validate[List[Record]](ReadsUtil.nrsRecordsListRead)
      response.isSuccess shouldBe true
      val listOfRecords = nrsJsonResponseObject.as[List[Record]](ReadsUtil.nrsRecordsListRead)

      val record = listOfRecords.head
      listOfRecords.length shouldBe 1
      record shouldBe a[Record]
      record.child shouldBe a[Child]
      record.child.birthReferenceNumber shouldBe 2017734003
      record.child.forenames shouldBe "Adam TEST"
      record.child.lastName shouldBe "SMITH"
      record.child.dateOfBirth.get.toString shouldBe "2009-11-12"
      record.child.dateOfBirth.get shouldBe a[LocalDate]
      record.status.get shouldBe a[StatusInterface]
      record.status.get.asInstanceOf[NRSStatus].status shouldBe 1
      record.status.get.asInstanceOf[NRSStatus].deathCode shouldBe 0
    }

    "return Record object with all Child attributes when record does not have father's birth place." in {
      val nrsJsonResponseObject = getNrsResponse(fatherBirthPlace = "")

      val response = nrsJsonResponseObject.validate[List[Record]](ReadsUtil.nrsRecordsListRead)
      response.isSuccess shouldBe true
      val listOfRecords = nrsJsonResponseObject.as[List[Record]](ReadsUtil.nrsRecordsListRead)

      val record = listOfRecords.head
      listOfRecords.length shouldBe 1
      record shouldBe a[Record]
      record.child shouldBe a[Child]
      record.child.birthReferenceNumber shouldBe 2017734003
      record.child.forenames shouldBe "Adam TEST"
      record.child.lastName shouldBe "SMITH"
      record.child.dateOfBirth.get.toString shouldBe "2009-11-12"
      record.child.dateOfBirth.get shouldBe a[LocalDate]
      record.status.get shouldBe a[StatusInterface]
      record.status.get.asInstanceOf[NRSStatus].status shouldBe 1
      record.status.get.asInstanceOf[NRSStatus].deathCode shouldBe 0
    }

    "return Record object with all Child attributes when record does not have informant's qualification." in {
      val nrsJsonResponseObject = getNrsResponse(qualification = "")

      val response = nrsJsonResponseObject.validate[List[Record]](ReadsUtil.nrsRecordsListRead)
      response.isSuccess shouldBe true
      val listOfRecords = nrsJsonResponseObject.as[List[Record]](ReadsUtil.nrsRecordsListRead)

      val record = listOfRecords.head
      listOfRecords.length shouldBe 1
      record shouldBe a[Record]
      record.child shouldBe a[Child]
      record.child.birthReferenceNumber shouldBe 2017734003
      record.child.forenames shouldBe "Adam TEST"
      record.child.lastName shouldBe "SMITH"
      record.child.dateOfBirth.get.toString shouldBe "2009-11-12"
      record.child.dateOfBirth.get shouldBe a[LocalDate]
      record.status.get shouldBe a[StatusInterface]
      record.status.get.asInstanceOf[NRSStatus].status shouldBe 1
      record.status.get.asInstanceOf[NRSStatus].deathCode shouldBe 0
    }

    "return Record object without child details like firstname, lastname, dob when json does not have child details" in {
      val nrsJsonResponseObject = JsonUtils.getJsonFromFile("nrs", "2017350003")
      val listOfRecords = nrsJsonResponseObject.as[List[Record]](ReadsUtil.nrsRecordsListRead)
      val record = listOfRecords.head
      listOfRecords.length shouldBe 1
      record shouldBe a[Record]
      record.child shouldBe a[Child]
      record.child.birthReferenceNumber shouldBe 2017350003
      record.child.forenames shouldBe ""
      record.child.lastName shouldBe ""
      record.child.dateOfBirth shouldBe None
      record.status.get shouldBe a[StatusInterface]
      record.status.get.asInstanceOf[NRSStatus].status shouldBe -4
      record.status.get.asInstanceOf[NRSStatus].deathCode shouldBe 0
    }

    "return two Record object with all Child attributes when json has two records" in {
      val nrsJsonResponseObject = JsonUtils.getJsonFromFile("nrs", "AdamTEST_multiple")

      val response = nrsJsonResponseObject.validate[List[Record]](ReadsUtil.nrsRecordsListRead)
      response.isSuccess shouldBe true
      val listOfRecords = nrsJsonResponseObject.as[List[Record]](ReadsUtil.nrsRecordsListRead)
      listOfRecords.length shouldBe 2

      val record = listOfRecords.head

      record shouldBe a[Record]
      record.child shouldBe a[Child]
      record.child.birthReferenceNumber shouldBe 2017734003
      record.child.forenames shouldBe "Adam TEST"
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
      recordTwo.child.forenames shouldBe "Adam TEST"
      recordTwo.child.lastName shouldBe "SMITH"
      recordTwo.child.dateOfBirth.get.toString shouldBe "2009-11-12"
      recordTwo.child.dateOfBirth.get shouldBe a[LocalDate]
      record.status.get shouldBe a[StatusInterface]
      record.status.get.asInstanceOf[NRSStatus].status shouldBe 1
      record.status.get.asInstanceOf[NRSStatus].deathCode shouldBe 0
    }

    "return error when json is empty" in {
      val response = emptyJson.validate[List[Record]](ReadsUtil.nrsRecordsListRead)
      response.isError shouldBe true
      val singleRecordRead = emptyJson.validate[Record](ReadsUtil.nrsRecordsRead)
      singleRecordRead.isError shouldBe true
    }

    "return Record object with all Child attributes when json is a full record and with UTF ASCII extendted characters." in {

      val response = jsonValidWithUTF8.validate[List[Record]](ReadsUtil.nrsRecordsListRead)
      response.isSuccess shouldBe true
      val listOfRecords = jsonValidWithUTF8.as[List[Record]](ReadsUtil.nrsRecordsListRead)

      val record = listOfRecords.head
      listOfRecords.length shouldBe 1
      record shouldBe a[Record]
      record.child shouldBe a[Child]
      record.child.birthReferenceNumber shouldBe 2017734003
      record.child.forenames shouldBe "»¼½¾¿ÀÁÂÃÄÅÆÇÈÉÊËÌÍ ÏÐÑÒÓÔÕÖ×ØÙÚÛÜÝÞßàáâãäåæçèéêëìíîïðñòóôõö÷øùúûüýþÿ"
      record.child.lastName shouldBe "ÏÐÑÒÓÔÕÖ×ØÙÚÛÜÝÞßàáâãäåæçèéêëìíîïðñòóôõö÷øùúûüýþÿ''--"
      record.child.dateOfBirth.get.toString shouldBe "2007-02-18"
      record.child.dateOfBirth.get shouldBe a[LocalDate]
      record.status.get shouldBe a[StatusInterface]
      record.status.get.asInstanceOf[NRSStatus].status shouldBe 1
      record.status.get.asInstanceOf[NRSStatus].deathCode shouldBe 0
    }

    "return Record object with all Child attributes when json is a full record with maximum length value." in {
      val nrsJsonResponseObject = JsonUtils.getJsonFromFile("nrs", "2017734003_withMaxLength")

      val response = nrsJsonResponseObject.validate[List[Record]](ReadsUtil.nrsRecordsListRead)
      response.isSuccess shouldBe true
      val listOfRecords = nrsJsonResponseObject.as[List[Record]](ReadsUtil.nrsRecordsListRead)

      val record = listOfRecords.head
      listOfRecords.length shouldBe 1
      record shouldBe a[Record]
      record.child shouldBe a[Child]
      record.child.birthReferenceNumber shouldBe 2017734003
      record.child.forenames.length shouldBe 250
      record.child.lastName.length shouldBe 250
      record.child.dateOfBirth.get.toString shouldBe "2009-11-12"
      record.child.dateOfBirth.get shouldBe a[LocalDate]
      record.status.get shouldBe a[StatusInterface]
      record.status.get.asInstanceOf[NRSStatus].status shouldBe 1
      record.status.get.asInstanceOf[NRSStatus].deathCode shouldBe 0
    }

    "return Record object with all Child attributes when json is a full record with minimum length value." in {

      val nrsJsonResponseObject = JsonUtils.getJsonFromFile("nrs", "2017734003_withMinLength")

      val response = nrsJsonResponseObject.validate[List[Record]](ReadsUtil.nrsRecordsListRead)
      response.isSuccess shouldBe true
      val listOfRecords = nrsJsonResponseObject.as[List[Record]](ReadsUtil.nrsRecordsListRead)

      val record = listOfRecords.head
      listOfRecords.length shouldBe 1
      record shouldBe a[Record]
      record.child shouldBe a[Child]
      record.child.birthReferenceNumber shouldBe 2017734003
      record.child.forenames.length shouldBe 1
      record.child.lastName.length shouldBe 1
      record.child.dateOfBirth.get.toString shouldBe "2009-11-12"
      record.child.dateOfBirth.get shouldBe a[LocalDate]
      record.status.get shouldBe a[StatusInterface]
      record.status.get.asInstanceOf[NRSStatus].status shouldBe 1
      record.status.get.asInstanceOf[NRSStatus].deathCode shouldBe 0
    }

    "return Record object with all Child attributes when json is a full record with minimum required fields." in {

      val nrsJsonResponseObject = JsonUtils.getJsonFromFile("nrs", "2017734003_withMinRequiredField")

      val response = nrsJsonResponseObject.validate[List[Record]](ReadsUtil.nrsRecordsListRead)
      response.isSuccess shouldBe true
      val listOfRecords = nrsJsonResponseObject.as[List[Record]](ReadsUtil.nrsRecordsListRead)

      val record = listOfRecords.head
      listOfRecords.length shouldBe 1
      record shouldBe a[Record]
      record.child shouldBe a[Child]
      record.child.birthReferenceNumber shouldBe 2017734003
      record.child.forenames shouldBe ""
      record.child.lastName shouldBe ""
      record.child.dateOfBirth shouldBe None
      record.status.get shouldBe a[StatusInterface]
      record.status.get.asInstanceOf[NRSStatus].status shouldBe 1
      record.status.get.asInstanceOf[NRSStatus].deathCode shouldBe 0
    }

  }

}
