package uk.gov.hmrc.utility

/**
  * Created by chrisianson on 26/07/16.
  */


object Format {

}

trait Trim {
  def trim(v: String): String = {
    v
  }
}

trait LowerCase {
  def lowercase(v: String): String = {
    v
  }
}

object NameFormat extends Trim with LowerCase {

  def format(v: String): String = {
    v
  }
}

