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

package uk.gov.hmrc.brm.utils

import play.api.Logger

class BRMLogger(logger: org.slf4j.Logger) extends Logger(logger) {

  val BRM_KEY: String = s"BRM-Key:${KeyGenerator.getKey()}"

  def info(className: String, methodName: String, message: String): Unit = {
    logger.info(s"[$BRM_KEY], [$className][$methodName] : [$message]")
  }

  def warn(className: String, methodName: String, message: String): Unit = {
    logger.warn(s"[$BRM_KEY],[$className][$methodName] : [$message]")
  }

  def error(className: String, methodName: String, message: String): Unit = {
    logger.error(s"[$BRM_KEY],[$className][$methodName] : [$message]")
  }

  def debug(className: String, methodName: String, message: String): Unit = {
    logger.debug(s"[$BRM_KEY],[$className][$methodName] : [$message]")
  }

  def info(objectName: Object, methodName: String, message: String): Unit = {
    info(objectName.getClass.getCanonicalName, methodName: String, message: String)
  }

  def warn(objectName: Object, methodName: String, message: String): Unit = {
    warn(objectName.getClass.getCanonicalName, methodName: String, message: String)
  }

  def error(objectName: Object, methodName: String, message: String): Unit = {
    error(objectName.getClass.getCanonicalName, methodName: String, message: String)
  }

  def debug(objectName: Object, methodName: String, message: String): Unit = {
    debug(objectName.getClass.getCanonicalName, methodName: String, message: String)
  }
}

object BRMLogger extends BRMLogger(Logger.logger)
