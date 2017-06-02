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

package uk.gov.hmrc.brm.services.parser

import org.joda.time.LocalDate
import org.scalatest.TestData
import org.scalatestplus.play.OneAppPerTest
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.brm.BRMFakeApplication
import uk.gov.hmrc.brm.models.brm.Payload
import uk.gov.hmrc.brm.models.response.{Child, Record}
import uk.gov.hmrc.brm.services.parser.NameParser._
import uk.gov.hmrc.brm.utils.BirthRegisterCountry
import uk.gov.hmrc.play.test.UnitSpec

/**
  * Created by adamconder on 02/02/2017.
  */
class NameParserSpec extends UnitSpec with OneAppPerTest {

  override def newAppForTest(testData: TestData) = new GuiceApplicationBuilder().configure(
    Map(
      "microservice.services.birth-registration-matching.matching.ignoreAdditionalNames" -> false
    )
  ).build()

  "NameParser" when {

    "Names" should {
      "return concatenated string of all the names" in {

        val names = Names(List("Adam", "David", "Test"), List("Charles"), List("Smith"))
        names.firstNames shouldBe "Adam David Test"
        names.additionalNames shouldBe "Charles"
        names.lastNames shouldBe "Smith"
      }
    }

    "filtering Middle Names" should {

      "split a string into words removing trailing space" in {
        val input = "    Adam David      Charles       Mary-Ann'é"
        val names: List[String] = input.names

        names.length shouldBe 4
        names.head shouldBe "Adam"
        names(1) shouldBe "David"
        names(2) shouldBe "Charles"
        names(3) shouldBe "Mary-Ann'é"
      }

      /*
        If left is less or equal then drop elements from right
        If left is greater then don't drop elements from right
       */

      "filter right hand side list when left has less elements" in {
        val left = List("Adam", "David", "Charles")
        val right = List("Adam", "David", "Charles", "Edward")

        // filter the list on the right (record) with the number of occurences in the left
        val names = left filter right
        names should not be Nil
        names shouldBe List("Adam", "David", "Charles")
      }

      "filter right hand side list when left has equal elements" in {
        val left = List("Adam", "David", "Charles", "Edward")
        val right = List("Adam", "David", "Charles", "Edward")

        // filter the list on the right (record) with the number of occurences in the left
        val names = left filter right
        names should not be Nil
        names shouldBe List("Adam", "David", "Charles", "Edward")
      }

      "not filter right hand side list when left has more elements and return right" in {
        val left = List("Adam", "David", "Charles", "Edward")
        val right = List("Adam", "David", "Charles")

        // filter the list on the right (record) with the number of occurences in the left
        val names = left filter right
        names should not be Nil
        names shouldBe List("Adam", "David", "Charles")
      }

      "not filter when left and right have zero items" in {
        val left = Nil
        val right = Nil

        // filter the list on the right (record) with the number of occurences in the left
        val names = left filter right
        names shouldBe Nil
      }

      "not filter when right has zero items" in {
        val left = List("Adam", "David")
        val right = Nil

        val names = left filter right
        names shouldBe Nil
      }

      "not filter when left has zero items" in {
        val left = Nil
        val right = List("Adam", "David")

        val names = left filter right
        names shouldBe List("Adam", "David")
      }

      "Nil should build up the names into a string" in {
        val list = Nil
        list.listToString shouldBe ""
      }

      "List(adam, david) should build up the names into a string" in {
        val list = List("Adam", "David")
        list.listToString shouldBe "Adam David"
      }

      "List(adam, david, smith, test) should build up the names into a string" in {
        val list = List("Adam", "David", "Smith", "Test")
        list.listToString shouldBe "Adam David Smith Test"
      }
    }

    "not filtering middle names" should {

      "return two lists of names where List 1 has 1 name and List 2 has 1 name due to firstName having 1 name" in {
        val date = LocalDate.now

        val payload = Payload(None,
          _firstName = "Adam",
          _additionalNames = Some("David"),
          _lastName = "Smith",
          dateOfBirth = date,
          whereBirthRegistered = BirthRegisterCountry.ENGLAND
        )

        val record = Record(child = Child(
          birthReferenceNumber = 123456789,
          _forenames = "Adam David",
          _lastName = "Smith",
          dateOfBirth = Some(date)
        ))

        NameParser.parseNames(payload, record) shouldBe Names(
          _firstNames = List("Adam"),
          _additionalNames = List("David"),
          _lastNames = List("Smith")
        )
      }

      "return two lists of names where List 1 has 2 name and List 2 has 1 name due to firstName having 2 names" in {
        val date = LocalDate.now

        val payload = Payload(None,
          _firstName = "Adam TEST",
          _additionalNames = Some("David"),
          _lastName = "Smith",
          dateOfBirth = date,
          whereBirthRegistered = BirthRegisterCountry.ENGLAND)

        val record = Record(child = Child(
          birthReferenceNumber = 123456789,
          _forenames = "Adam TEST David ",
          _lastName = "Smith",
          dateOfBirth = Some(date)
        ))

        NameParser.parseNames(payload, record) shouldBe Names(
          _firstNames = List("Adam", "TEST"),
          _additionalNames = List("David"),
          _lastNames = List("Smith")
        )

      }

      "return two lists of names where List 1 has 1 name and List 2 has 2 name due to firstName having 1 names" in {
        val date = LocalDate.now

        val payload = Payload(None,
          _firstName = "Adam",
          _additionalNames = Some("David"),
          _lastName = "Smith",
          dateOfBirth = date,
          whereBirthRegistered = BirthRegisterCountry.ENGLAND)

        val record = Record(child = Child(
          birthReferenceNumber = 123456789,
          _forenames = "Adam David TEST ",
          _lastName = "Smith",
          dateOfBirth = Some(date)
        ))

        NameParser.parseNames(payload, record) shouldBe Names(
          _firstNames = List("Adam"),
          _additionalNames = List("David", "TEST"),
          _lastNames = List("Smith")
        )

      }

      "return two lists of names where List 1 has 2 name and List 2 has 2 name due to firstName having 2 names" in {
        val date = LocalDate.now

        val payload = Payload(None,
          _firstName = "Adam TEST",
          _additionalNames = Some("David"),
          _lastName = "Smith",
          dateOfBirth = date,
          whereBirthRegistered = BirthRegisterCountry.ENGLAND)

        val record = Record(child = Child(
          birthReferenceNumber = 123456789,
          _forenames = "Adam TEST David test",
          _lastName = "Smith",
          dateOfBirth = Some(date)
        ))

        NameParser.parseNames(payload, record) shouldBe Names(
          _firstNames = List("Adam", "TEST"),
          _additionalNames = List("David", "test"),
          _lastNames = List("Smith")
        )

      }

      "return two list of names where the order is different from the record " in {
        val date = LocalDate.now

        val payload = Payload(None,
          _firstName = "Adam TEST",
          _additionalNames = Some("David"),
          _lastName = "Smith",
          dateOfBirth = date,
          whereBirthRegistered = BirthRegisterCountry.ENGLAND)

        val record = Record(child = Child(
          birthReferenceNumber = 123456789,
          _forenames = "Adam David TEST test",
          _lastName = "Smith",
          dateOfBirth = Some(date)
        ))

        NameParser.parseNames(payload, record) shouldBe Names(
          _firstNames = List("Adam", "David"),
          _additionalNames = List("TEST", "test"),
          _lastNames = List("Smith")
        )

      }

      "return two list of names where List 1 has 2 names and List 2 has 0 names" in {
        val date = LocalDate.now

        val payload = Payload(None,
          _firstName = "Adam TEST",
          _additionalNames = None,
          _lastName = "Smith",
          dateOfBirth = date,
          whereBirthRegistered = BirthRegisterCountry.ENGLAND)

        val record = Record(child = Child(
          birthReferenceNumber = 123456789,
          _forenames = "Adam Test",
          _lastName = "Smith",
          dateOfBirth = Some(date)
        ))

        NameParser.parseNames(payload, record) shouldBe Names(
          _firstNames = List("Adam", "Test"),
          _additionalNames = Nil,
          _lastNames = List("Smith")
        )
      }

      "return two list of names where the payload contains more names than on the record " in {
        val date = LocalDate.now

        val payload = Payload(None,
          _firstName = "Adam TEST David",
          _additionalNames = None,
          _lastName = "Smith",
          dateOfBirth = date,
          whereBirthRegistered = BirthRegisterCountry.ENGLAND)

        val record = Record(child = Child(
          birthReferenceNumber = 123456789,
          _forenames = "Adam Test",
          _lastName = "Smith",
          dateOfBirth = Some(date)
        ))

        NameParser.parseNames(payload, record) shouldBe Names(
          _firstNames = List("Adam", "Test"),
          _additionalNames = Nil,
          _lastNames = List("Smith")
        )
      }

      "return two list of names where the payload contains less names than on the record" in {
        val date = LocalDate.now

        val payload = Payload(None,
          _firstName = "Adam",
          _additionalNames = None,
          _lastName = "Smith",
          dateOfBirth = date,
          whereBirthRegistered = BirthRegisterCountry.ENGLAND)

        val record = Record(child = Child(
          birthReferenceNumber = 123456789,
          _forenames = "Adam Test",
          _lastName = "Smith",
          dateOfBirth = Some(date)
        ))

        NameParser.parseNames(payload, record) shouldBe Names(
          _firstNames = List("Adam"),
          _additionalNames = List("Test"),
          _lastNames = List("Smith")
        )
      }

      "return two list of names where the payload contains less names than on the record with middle names" in {
        val date = LocalDate.now

        val payload = Payload(None,
          _firstName = "Adam",
          _additionalNames = Some("David"),
          _lastName = "Smith",
          dateOfBirth = date,
          whereBirthRegistered = BirthRegisterCountry.ENGLAND)

        val record = Record(child = Child(
          birthReferenceNumber = 123456789,
          _forenames = "Adam Test",
          _lastName = "Smith",
          dateOfBirth = Some(date)
        ))

        NameParser.parseNames(payload, record) shouldBe Names(
          _firstNames = List("Adam"),
          _additionalNames = List("Test"),
          _lastNames = List("Smith")
        )
      }
    }
  }
}
