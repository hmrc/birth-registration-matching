package uk.gov.hmrc.brm.services

import uk.gov.hmrc.brm.models.brm.Payload

/**
  * Created by user on 28/09/16.
  */
trait MatchingAlgorithm {

  def performMatch(payload: Payload, responsePayload: Payload): Match


  protected def firstNamesMatch(brmsFirstname: Option[String], groFirstName: Option[String]): Match =
    matching[String](brmsFirstname, groFirstName, (a, b) => a.equalsIgnoreCase(b)

    )

  protected def matching[T](input: Option[T], other: Option[T], matchFunction: (T, T) => Boolean): Match = {
    (input, other) match {
      case (Some(input), Some(other)) =>
        if (matchFunction(input, other)) Good()
        else Bad()

    }

  }



}


object FullMatching extends MatchingAlgorithm {
  def performMatch(payload: Payload, responsePayload: Payload): Match = {
    val firstNames = firstNamesMatch(Some(payload.firstName), Some(responsePayload.firstName))
    firstNames
  }
}

sealed abstract class Match {

}

case class Good() extends Match

case class Bad() extends Match

case object Good extends () {
  def apply(): Good = Good()
}

case object Bad extends () {
  def apply(): Bad = Bad()
}
