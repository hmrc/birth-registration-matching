package uk.gov.hmrc.brm.utils

/**
  * Created by user on 01/09/16.
  */
object test {

  def main(args: Array[String]): Unit = {
    var  abc :JsonBuilder  = new JsonBuilder()
     println(abc.withKeyValue("a","b").buildToJson())
  }

}
