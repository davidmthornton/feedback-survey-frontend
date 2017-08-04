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

package uk.gov.hmrc.feedbacksurveyfrontend.controllers

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import controllers.FeedbackSurveyController
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.feedbacksurveyfrontend.services.{OriginConfigItem, OriginService}
import utils.FeedbackSurveySessionKeys._
import utils.UnitTestTraits


class FeedbackSurveyControllerSpec extends UnitTestTraits {

  trait SpecSetup {

    implicit val as = ActorSystem()
    implicit val mat = ActorMaterializer()
    val origin = "TOKEN1"

    def testRequest(page: String): FakeRequest[AnyContentAsEmpty.type] =
      FakeRequest(GET, "/feedback-survey/" + s"$page").withSession(sessionOriginService -> origin)
  }

  object TestFeedbackSurveyController extends FeedbackSurveyController {
    val originService = new OriginService {
      override lazy val originConfigItems = List(
        OriginConfigItem(Some("TOKEN1"), None),
        OriginConfigItem(Some("TOKEN2"), Some("http://example.com/custom-feedback-url"))
      )
    }
  }

  "FeedbackSurveyController" should {

    "Go to the ableToDo page" in new SpecSetup {
      val result = TestFeedbackSurveyController.ableToDo.apply(testRequest(""))
      status(await(result)) shouldBe OK
    }

    "redirect to the usingService page" in new SpecSetup {
      val result = TestFeedbackSurveyController.ableToDoContinue.apply(testRequest("")).run()
      status(result) shouldBe SEE_OTHER
      redirectLocation(result).get should include("/feedback-survey/usingService")
    }

    "Go to the usingService page" in new SpecSetup {
      val result = TestFeedbackSurveyController.usingService.apply(testRequest("usingService"))
      status(result) shouldBe OK
    }

    "redirect to the aboutService page" in new SpecSetup {
      val result = TestFeedbackSurveyController.usingServiceContinue.apply(testRequest("")).run()
      status(result) shouldBe SEE_OTHER
      redirectLocation(result).get should include("/feedback-survey/aboutService")
    }

    "Go to the recommendService page" in new SpecSetup {
      val result = TestFeedbackSurveyController.recommendService.apply(testRequest("recommendService"))
      status(result) shouldBe OK
    }

    "redirect to the Thank you page when this origin does not have a custom feedback url" in new SpecSetup {
      val result = TestFeedbackSurveyController.recommendServiceContinue.apply(testRequest("")).run()
      status(result) shouldBe SEE_OTHER
      redirectLocation(result).get should include("/feedback-survey/thankYou?origin=TOKEN1")
    }

    "redirect to the custom feedback url when this origin has one" in new SpecSetup {
      override val origin = "TOKEN2"
      val result = TestFeedbackSurveyController.recommendServiceContinue.apply(testRequest("")).run()
      status(result) shouldBe SEE_OTHER
      redirectLocation(result).get shouldBe "http://example.com/custom-feedback-url"
    }

    "Go to the Thank you page " in new SpecSetup {
      val result = TestFeedbackSurveyController.recommendService.apply(testRequest("thankYou"))
      status(result) shouldBe OK
    }
  }
}
