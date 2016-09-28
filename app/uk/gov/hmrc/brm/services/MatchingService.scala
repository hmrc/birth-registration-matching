package uk.gov.hmrc.brm.services

import uk.gov.hmrc.brm.models.brm.Payload
import uk.gov.hmrc.brm.models.gro.GroResponse
import uk.gov.hmrc.brm.models.matching.ResultMatch
import uk.gov.hmrc.brm.utils.MatchingType
import uk.gov.hmrc.brm.services.MatchingAlgorithm

/**
  * Created by user on 28/09/16.
  */
trait MatchingService {
  def performMatch(input: Payload, response: GroResponse, matchingType: MatchingType.Value): ResultMatch = {


    val algorithm = matchingType match {
      case MatchingType.FULL => FullMatching
    }

    algorithm.performMatch(input, response) match {
      case Good() => ResultMatch(true)
      case Bad() => ResultMatch(false)
    }


  }
}


object MatchingService extends MatchingService

