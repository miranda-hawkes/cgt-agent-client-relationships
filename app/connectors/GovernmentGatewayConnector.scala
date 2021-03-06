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

package connectors

import javax.inject.{Inject, Singleton}

import config.{ApplicationConfig, WSHttp}
import models.SubmissionModel
import play.api.Logger
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.play.http._
import play.api.http.Status._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class GovernmentGatewayConnector @Inject()(config: ApplicationConfig) {

  val serviceUrl: String = config.governmentGatewayServiceUrl
  val http: HttpGet with HttpPost with HttpPut = WSHttp

  private def modelToSubmissionPayload(submissionModel: SubmissionModel): JsValue = {
    Json.obj(
      "clientAllocation" -> Json.obj(
        "serviceName" -> submissionModel.serviceName,
        "identifiers" -> Json.arr(
          Json.obj(
            "identifierType" -> "cgtReference",
            "value" -> submissionModel.relationshipModel.cgtRef
          ),
          Json.obj(
            "identifierType" -> "cgtReference1",
            "value" -> submissionModel.relationshipModel.cgtRef
          )
        )
      )
    )
  }

  def createClientRelationship(submissionModel: SubmissionModel)(implicit hc: HeaderCarrier): Future[Int] = {
    val url: String = s"$serviceUrl/agent/${submissionModel.relationshipModel.arn}/client-list"

    http.POST[JsValue, HttpResponse](url, modelToSubmissionPayload(submissionModel)).map {
      response => response.status match {
        case NO_CONTENT =>
          Logger.info("Agent-client relationship created in Gateway")
          NO_CONTENT
        case _ =>
          val error = s"Invalid Gateway response code:${response.status} message:${response.body}"
          Logger.warn(error)
          throw new Exception(error)
      }
    }
  }
}
