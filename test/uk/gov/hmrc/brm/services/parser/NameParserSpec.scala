/*
 * Copyright 2023 HM Revenue & Customs
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

import java.time.LocalDate
import org.scalatest.{BeforeAndAfterEachTestData, Tag, TestData}
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.brm.models.brm.Payload
import uk.gov.hmrc.brm.models.response.{Child, Record}
import uk.gov.hmrc.brm.services.parser.NameParser._
import uk.gov.hmrc.brm.utils.BirthRegisterCountry
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatest.OptionValues

/** Created by adamconder on 02/02/2017.
  */
trait NameParserSpec
    extends AnyWordSpecLike
    with Matchers
    with OptionValues
    with GuiceOneAppPerTest
    with BeforeAndAfterEachTestData {

  lazy val ignoreAdditionalNamesFalse: Map[String, _] = Map(
    "microservice.services.birth-registration-matching.matching.ignoreAdditionalNames" -> false
  )

  lazy val ignoreAdditionalNamesTrue: Map[String, _] = Map(
    "microservice.services.birth-registration-matching.matching.ignoreAdditionalNames" -> true
  )

  override def newAppForTest(testData: TestData): Application = {
    val config = if (testData.tags.contains("ignoreAdditionalNames")) {
      ignoreAdditionalNamesTrue
    } else if (testData.tags.contains("dontIgnoreAdditionalNames")) {
      ignoreAdditionalNamesFalse
    } else { Map("" -> "") }

    new GuiceApplicationBuilder()
      .disable[com.kenshoo.play.metrics.PlayModule]
      .configure(config)
      .build()
  }

  "NameParser" when {

    "Names" should {
      "return concatenated string of all the names" taggedAs Tag("dontIgnoreAdditionalNames") in {

        val names = Names(List("Adam", "David", "Test"), List("Charles"), List("Smith"))
        names.firstNames      shouldBe "Adam David Test"
        names.additionalNames shouldBe "Charles"
        names.lastNames       shouldBe "Smith"
      }
    }

    "filtering Middle Names" should {

      "split a string into words removing trailing space" taggedAs Tag("dontIgnoreAdditionalNames") in {
        val input               = "    Adam David      Charles       Mary-Ann'é"
        val names: List[String] = input.names

        names.length shouldBe 4
        names.head   shouldBe "Adam"
        names(1)     shouldBe "David"
        names(2)     shouldBe "Charles"
        names(3)     shouldBe "Mary-Ann'é"
      }

      /*
        If left is less or equal then drop elements from right
        If left is greater then don't drop elements from right
       */

      "filter right hand side list when left has less elements" taggedAs Tag("dontIgnoreAdditionalNames") in {
        val left  = List("Adam", "David", "Charles")
        val right = List("Adam", "David", "Charles", "Edward")

        // filter the list on the right (record) with the number of occurences in the left
        val names = left filter right
        names   should not be Nil
        names shouldBe List("Adam", "David", "Charles")
      }

      "filter right hand side list when left has equal elements" taggedAs Tag("dontIgnoreAdditionalNames") in {
        val left  = List("Adam", "David", "Charles", "Edward")
        val right = List("Adam", "David", "Charles", "Edward")

        // filter the list on the right (record) with the number of occurences in the left
        val names = left filter right
        names   should not be Nil
        names shouldBe List("Adam", "David", "Charles", "Edward")
      }

      "not filter right hand side list when left has more elements and return right" taggedAs Tag(
        "dontIgnoreAdditionalNames"
      ) in {
        val left  = List("Adam", "David", "Charles", "Edward")
        val right = List("Adam", "David", "Charles")

        // filter the list on the right (record) with the number of occurences in the left
        val names = left filter right
        names   should not be Nil
        names shouldBe List("Adam", "David", "Charles")
      }

      "not filter when left and right have zero items" taggedAs Tag("dontIgnoreAdditionalNames") in {
        val left  = Nil
        val right = Nil

        // filter the list on the right (record) with the number of occurences in the left
        val names = left filter right
        names shouldBe Nil
      }

      "not filter when right has zero items" taggedAs Tag("dontIgnoreAdditionalNames") in {
        val left  = List("Adam", "David")
        val right = Nil

        val names = left filter right
        names shouldBe Nil
      }

      "not filter when left has zero items" taggedAs Tag("dontIgnoreAdditionalNames") in {
        val left  = Nil
        val right = List("Adam", "David")

        val names = left filter right
        names shouldBe List("Adam", "David")
      }

      "Nil should build up the names into a string" taggedAs Tag("dontIgnoreAdditionalNames") in {
        val list = Nil
        list.listToString shouldBe ""
      }

      "List(adam, david) should build up the names into a string" taggedAs Tag("dontIgnoreAdditionalNames") in {
        val list = List("Adam", "David")
        list.listToString shouldBe "Adam David"
      }

      "List(adam, david, smith, test) should build up the names into a string" taggedAs Tag(
        "dontIgnoreAdditionalNames"
      ) in {
        val list = List("Adam", "David", "Smith", "Test")
        list.listToString shouldBe "Adam David Smith Test"
      }
    }

    "not filtering middle names" should {

      val birthRefNumber = 123456789

      "return two lists of names where List 1 has 1 name and List 2 has 1 name due to firstName having 1 name" taggedAs Tag(
        "dontIgnoreAdditionalNames"
      ) in {
        val date = LocalDate.now

        val payload = Payload(
          None,
          _firstName = "Adam",
          _additionalNames = Some("David"),
          _lastName = "Smith",
          dateOfBirth = date,
          whereBirthRegistered = BirthRegisterCountry.ENGLAND
        )

        val record = Record(child =
          Child(
            birthReferenceNumber = birthRefNumber,
            forenames = "Adam David",
            lastName = "Smith",
            dateOfBirth = Some(date)
          )
        )

        NameParser.parseNames(payload, record) shouldBe Names(
          _firstNames = List("Adam"),
          _additionalNames = List("David"),
          _lastNames = List("Smith")
        )
      }

      "return two lists of names where List 1 has 2 name and List 2 has 1 name due to firstName having 2 names" taggedAs Tag(
        "dontIgnoreAdditionalNames"
      ) in {
        val date = LocalDate.now

        val payload = Payload(
          None,
          _firstName = "Adam TEST",
          _additionalNames = Some("David"),
          _lastName = "Smith",
          dateOfBirth = date,
          whereBirthRegistered = BirthRegisterCountry.ENGLAND
        )

        val record = Record(child =
          Child(
            birthReferenceNumber = birthRefNumber,
            forenames = "Adam TEST David ",
            lastName = "Smith",
            dateOfBirth = Some(date)
          )
        )

        NameParser.parseNames(payload, record) shouldBe Names(
          _firstNames = List("Adam", "TEST"),
          _additionalNames = List("David"),
          _lastNames = List("Smith")
        )

      }

      "return two lists of names where List 1 has 1 name and List 2 has 2 name due to firstName having 1 names" taggedAs Tag(
        "dontIgnoreAdditionalNames"
      ) in {
        val date = LocalDate.now

        val payload = Payload(
          None,
          _firstName = "Adam",
          _additionalNames = Some("David"),
          _lastName = "Smith",
          dateOfBirth = date,
          whereBirthRegistered = BirthRegisterCountry.ENGLAND
        )

        val record = Record(child =
          Child(
            birthReferenceNumber = birthRefNumber,
            forenames = "Adam David TEST ",
            lastName = "Smith",
            dateOfBirth = Some(date)
          )
        )

        NameParser.parseNames(payload, record) shouldBe Names(
          _firstNames = List("Adam"),
          _additionalNames = List("David", "TEST"),
          _lastNames = List("Smith")
        )

      }

      "return two lists of names where List 1 has 2 name and List 2 has 2 name due to firstName having 2 names" taggedAs Tag(
        "dontIgnoreAdditionalNames"
      ) in {
        val date = LocalDate.now

        val payload = Payload(
          None,
          _firstName = "Adam TEST",
          _additionalNames = Some("David"),
          _lastName = "Smith",
          dateOfBirth = date,
          whereBirthRegistered = BirthRegisterCountry.ENGLAND
        )

        val record = Record(child =
          Child(
            birthReferenceNumber = birthRefNumber,
            forenames = "Adam TEST David test",
            lastName = "Smith",
            dateOfBirth = Some(date)
          )
        )

        NameParser.parseNames(payload, record) shouldBe Names(
          _firstNames = List("Adam", "TEST"),
          _additionalNames = List("David", "test"),
          _lastNames = List("Smith")
        )

      }

      "return two list of names where the order is different from the record " taggedAs Tag(
        "dontIgnoreAdditionalNames"
      ) in {
        val date = LocalDate.now

        val payload = Payload(
          None,
          _firstName = "Adam TEST",
          _additionalNames = Some("David"),
          _lastName = "Smith",
          dateOfBirth = date,
          whereBirthRegistered = BirthRegisterCountry.ENGLAND
        )

        val record = Record(child =
          Child(
            birthReferenceNumber = birthRefNumber,
            forenames = "Adam David TEST test",
            lastName = "Smith",
            dateOfBirth = Some(date)
          )
        )

        NameParser.parseNames(payload, record) shouldBe Names(
          _firstNames = List("Adam", "David"),
          _additionalNames = List("TEST", "test"),
          _lastNames = List("Smith")
        )

      }

      "return two list of names where List 1 has 2 names and List 2 has 0 names" taggedAs Tag(
        "dontIgnoreAdditionalNames"
      ) in {
        val date = LocalDate.now

        val payload = Payload(
          None,
          _firstName = "Adam TEST",
          _additionalNames = None,
          _lastName = "Smith",
          dateOfBirth = date,
          whereBirthRegistered = BirthRegisterCountry.ENGLAND
        )

        val record = Record(child =
          Child(
            birthReferenceNumber = birthRefNumber,
            forenames = "Adam Test",
            lastName = "Smith",
            dateOfBirth = Some(date)
          )
        )

        NameParser.parseNames(payload, record) shouldBe Names(
          _firstNames = List("Adam", "Test"),
          _additionalNames = Nil,
          _lastNames = List("Smith")
        )
      }

      "return two list of names where the payload contains more names than on the record " taggedAs Tag(
        "dontIgnoreAdditionalNames"
      ) in {
        val date = LocalDate.now

        val payload = Payload(
          None,
          _firstName = "Adam TEST David",
          _additionalNames = None,
          _lastName = "Smith",
          dateOfBirth = date,
          whereBirthRegistered = BirthRegisterCountry.ENGLAND
        )

        val record = Record(child =
          Child(
            birthReferenceNumber = birthRefNumber,
            forenames = "Adam Test",
            lastName = "Smith",
            dateOfBirth = Some(date)
          )
        )

        NameParser.parseNames(payload, record) shouldBe Names(
          _firstNames = List("Adam", "Test"),
          _additionalNames = Nil,
          _lastNames = List("Smith")
        )
      }

      "return two list of names where the payload contains less names than on the record" taggedAs Tag(
        "dontIgnoreAdditionalNames"
      ) in {
        val date = LocalDate.now

        val payload = Payload(
          None,
          _firstName = "Adam",
          _additionalNames = None,
          _lastName = "Smith",
          dateOfBirth = date,
          whereBirthRegistered = BirthRegisterCountry.ENGLAND
        )

        val record = Record(child =
          Child(
            birthReferenceNumber = birthRefNumber,
            forenames = "Adam Test",
            lastName = "Smith",
            dateOfBirth = Some(date)
          )
        )

        NameParser.parseNames(payload, record) shouldBe Names(
          _firstNames = List("Adam"),
          _additionalNames = List("Test"),
          _lastNames = List("Smith")
        )
      }

      "return two list of names where the payload contains less names than on the record with middle names" taggedAs Tag(
        "dontIgnoreAdditionalNames"
      ) in {
        val date = LocalDate.now

        val payload = Payload(
          None,
          _firstName = "Adam",
          _additionalNames = Some("David"),
          _lastName = "Smith",
          dateOfBirth = date,
          whereBirthRegistered = BirthRegisterCountry.ENGLAND
        )

        val record = Record(child =
          Child(
            birthReferenceNumber = 123456789,
            forenames = "Adam Test",
            lastName = "Smith",
            dateOfBirth = Some(date)
          )
        )

        NameParser.parseNames(payload, record) shouldBe Names(
          _firstNames = List("Adam"),
          _additionalNames = List("Test"),
          _lastNames = List("Smith")
        )
      }

      "return additionalName = Nil when additionalNames exists in both lists but ignoreAdditionalNames is true" taggedAs Tag(
        "ignoreAdditionalNames"
      ) in {
        val date = LocalDate.now

        val payload = Payload(
          None,
          _firstName = "Adam",
          _additionalNames = Some("David"),
          _lastName = "Smith",
          dateOfBirth = date,
          whereBirthRegistered = BirthRegisterCountry.ENGLAND
        )

        val record = Record(child =
          Child(
            birthReferenceNumber = birthRefNumber,
            forenames = "Adam Test",
            lastName = "Smith",
            dateOfBirth = Some(date)
          )
        )

        NameParser.parseNames(payload, record) shouldBe Names(
          _firstNames = List("Adam"),
          _additionalNames = Nil,
          _lastNames = List("Smith")
        )
      }
    }
  }
}
