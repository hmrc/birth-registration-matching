package uk.gov.hmrc.brm.utils

import play.api.libs.json.{JsValue, Json}

import scala.collection.mutable.StringBuilder

/**
  * Created by user on 01/09/16.
  */
class JsonBuilder  {
  var jsonString : StringBuilder = new StringBuilder(" {")


  def withKeyValue (key: String, value:String ): JsonBuilder = {
    jsonString.append("\""+key + "\"").append(":").append("\""+value + "\"")
    this

  }


  def buildToJson():JsValue = {
    jsonString.append("}").toString()

    Json.parse(buildToJson.toString())
  }




}




