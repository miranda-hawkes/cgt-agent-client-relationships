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

package models

import org.apache.commons.lang3.RandomStringUtils
import play.api.libs.json.{JsValue, Json, OFormat}

case class RelationshipModel(arn: String, cgtRef: String) {
  def toEtmpRelationship: JsValue = {

    def getUniqueAckNo: String = {
      val length = 32
      val nanoTime = System.nanoTime()
      val restChars = length - nanoTime.toString.length
      val randomChars = RandomStringUtils.randomAlphanumeric(restChars)
      randomChars + nanoTime
    }

    Json.obj(
      "acknowledgmentReference" -> getUniqueAckNo,
      "refNumber" -> cgtRef,
      "agentReferenceNumber" -> arn,
      "regime" -> "CGT",
      "authorisation" -> Json.obj(
        "action" -> "Authorise",
        "isExclusiveAgent" -> false
      )
    )
  }
}

object RelationshipModel {
  implicit val formats: OFormat[RelationshipModel] = Json.format[RelationshipModel]
}
