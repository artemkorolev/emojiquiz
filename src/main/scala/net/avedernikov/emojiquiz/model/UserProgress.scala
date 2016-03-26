package net.avedernikov.emojiquiz.model

/**
  * @author Artem Vedernikov
  */

//TODO persist it somehow (mongo?)
case class UserProgress(userId: Int,
                        currentQuestion: Question,
                        level: Int = 0,
                        score: Int = 0,
                        helpersUsed: Int = 0,
                        totalHelpersUsed: Int = 0) {

  val questionString = currentQuestion.questionString

  val canUseHelpers: Boolean = helpersUsed < currentQuestion.helpersAvailable

}
