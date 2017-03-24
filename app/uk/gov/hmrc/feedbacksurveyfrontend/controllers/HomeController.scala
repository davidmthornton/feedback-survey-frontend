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

package controllers

import play.api.Play.current
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.mvc._
import controllers.bindable.Origin
import uk.gov.hmrc.play.frontend.controller.{FrontendController}

import scala.concurrent.Future

object HomeController extends HomeController

trait HomeController extends FrontendController  {

  def start(originService : Origin ): Action[AnyContent] = Action.async {
    implicit request =>
    Origin(originService.value).isValid match {
    case true =>  Future.successful(Redirect(routes.FeedbackSurveyController.ableToDo).withSession(request.session + ("OriginService",originService.value)))
    case false => Future.successful(Ok(uk.gov.hmrc.feedbacksurveyfrontend.views.html.error_template(Messages("global_errors.title"), Messages("global_errors.heading"), Messages("global_errors.message"))))
    }
  }
}
