package net.avedernikov.emojiquiz.bots

import java.util.concurrent.ConcurrentHashMap

import com.typesafe.emoji.Emoji
import info.mukel.telegram.bots.{Polling, TelegramBot}
import net.avedernikov.emojiquiz.model.{Question, UserProgress}
import net.avedernikov.emojiquiz.utils.{CommandsWithDefault, StringUtils}
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * @author Artem Vedernikov
  */

object QuizBot {
  val ActionOnGameComple = "You have already won the game. \n If you want to try again please type /refresh"
  val MessageOnGameNotStarted = "You need to start game. \n Type /start"
}

class QuizBot(token: String, questions: List[Question]) extends TelegramBot(token) with Polling with CommandsWithDefault {

  val log = LoggerFactory.getLogger(classOf[QuizBot])

  val gameProgress = new ConcurrentHashMap[Int, UserProgress]().asScala

  val firstQuestion = questions.head

  on("start") { (sender, args) => Future {
      replyTo(sender) {
        gameProgress.get(sender) match {
          case Some(userProgress) if userProgress.level == questions.size =>
            "You have already won the game. \n If you want to try again please type /refresh"

          case Some(userProgress) =>
            val defaultResponse = s"""
               |You're now on level ${userProgress.level}
               |Question:
               |${userProgress.questionString}
             """.stripMargin

            if (userProgress.helpersUsed > 0) {
              s"""
                 |$defaultResponse
                 |Helpers:
                 |${userProgress.currentQuestion.helpersString(userProgress.helpersUsed)}
               """.stripMargin
            } else {
              defaultResponse
            }

          case None =>
            val newProgress = UserProgress(sender, firstQuestion)
            gameProgress.put(sender, newProgress)
            s"""
               |New game started!
               |Question:
               |${firstQuestion.questionString}
             """.stripMargin
        }
      }
    }
  }

  on("refresh") { (sender, args) => Future {
      replyTo(sender) {
        gameProgress.remove(sender)
        "Your progress successfully removed"
      }
    }
  }

  on("help") { (sender, args) => Future {
      replyTo(sender) {
        s"""
           |/start - start game
           |/stat - see stat
           |/refresh - clean stats
           |/use_help - ask for help for question
           |/help - see help
         """.stripMargin
      }
    }
  }

//TODO make pretty stat
  on("stat") { (sender, args) => Future {
      replyTo(sender) {
        s"Your level is ${gameProgress(sender)}"
      }
    }
  }

  on("/use_help") { (sender, args) => Future {
    withUserProgress(sender, args) { (sender, args, userProgress) =>
      if (userProgress.currentQuestion.helpEmojis.isEmpty) {
        replyTo(sender) {
          s"""No helpers for current level are available.
             |Question:
             |${userProgress.questionString}""".stripMargin
        }
      } else if (userProgress.canUseHelpers) {
        val updatedHelpersUsed = userProgress.helpersUsed + 1
        val updatedTotalHelpersUsed = userProgress.totalHelpersUsed + 1
        val updatedProgress = userProgress.copy(helpersUsed = updatedHelpersUsed, totalHelpersUsed = updatedTotalHelpersUsed)
        gameProgress.put(sender, updatedProgress)
        replyTo(sender) {
          s"""
             |Using $updatedHelpersUsed of ${userProgress.currentQuestion.helpersAvailable}
             |Question:
             |${userProgress.questionString}
             |Helpers:
             |${userProgress.currentQuestion.helpersString(updatedHelpersUsed)}""".stripMargin
        }
      } else {
        replyTo(sender) {
          s"""
             |You can not use more helpers on this level.
             |Question:
             |${userProgress.questionString}
             |Helpers:
             |${userProgress.currentQuestion.helpersString(userProgress.helpersUsed)}""".stripMargin
        }
      }
    }
  }

  }

  override def onNonCommand(msg: String, sender: Int): Unit = {
    Future {
      println(s"Processing non command input $msg")
      gameProgress.get(sender) match {
        case Some(userProgress) if userProgress.level == questions.size =>
          replyTo(sender) {
            "You have already won the game. \n If you want to try again please type /refresh"
          }
        case Some(userProgress) =>
          if (StringUtils.answerCorrect(msg, userProgress.currentQuestion.answers)) {
            val newLevel = userProgress.level + 1

            if (newLevel == questions.size) {
              val updatedProgress = userProgress.copy(currentQuestion = null, helpersUsed = 0)
              gameProgress.put(sender, updatedProgress)
              replyTo(sender) {
                """Correct!
                  |Congratulations! You won the game!
                  |If you want to refresh your progress plese send /refresh """.stripMargin
              }
            } else {
              val nextQuestion = questions(newLevel)
              val updatedProgress = userProgress.copy(currentQuestion = nextQuestion, helpersUsed = 0)
              gameProgress.put(sender, updatedProgress)
              replyTo(
                sender) {
                s"""
                   |Correct!
                   |Your current level is now $newLevel
                   |Next question:
                   |${nextQuestion.questionString}""".stripMargin
              }
            }
          } else {
            replyTo(sender) {
              "Incorrect answer"
            }
          }
        case None =>
          replyTo(sender) {
            """
              |You need to start game.
              |Type /start""".stripMargin
          }
      }
    }
  }

  override def run(): Unit = {
    super.run()
    log.info("Start successful")
    println(questions)
  }

  private def withUserProgress(sender: Int, args: Seq[String])(fun: (Int, Seq[String], UserProgress) => Unit): Unit = {
    gameProgress.get(sender) match {
      case Some(progress) if progress.level == questions.size =>
        replyTo(sender) {
          "You have already won the game. \n If you want to try again please type /refresh"
        }
      case Some(progress) => fun(sender, args, progress)
      case None =>
        replyTo(sender) {
          "You need to start game. \n Type /start"
        }
    }
  }

}
