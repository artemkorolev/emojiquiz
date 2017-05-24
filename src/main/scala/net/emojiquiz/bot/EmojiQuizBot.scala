package net.emojiquiz.bot

import java.util.concurrent.{Executors, TimeUnit}

import com.mongodb.casbah.WriteConcern
import com.mongodb.casbah.commons.MongoDBObject
import info.mukel.telegrambot4s.api.{Commands, Polling, TelegramBot}
import info.mukel.telegrambot4s.models.Message
import net.emojiquiz.model.{Question, QuestionDAO, UserProgress, UserProgressDAO}
import net.emojiquiz.utils.StringUtils
import org.slf4j.LoggerFactory
import com.mongodb.casbah.Imports._

import scala.concurrent.Future


object EmojiQuizBot {

  //TODO: language specific
  val MessageOnGameComplete =
    """
      |You have already won the game.
      |If you want to try again please type /refresh""".stripMargin


  val MessageOnGameNotStarted =
    """
      |You need to start game.
      |Type /start""".stripMargin

}


// todo: remove polling
class EmojiQuizBot(val token: String, questionDao: QuestionDAO, userProgressDao: UserProgressDAO) extends TelegramBot with Polling with Commands /*with CommandsWithDefault*/ {

  @volatile var questions: List[Question] = _
  @volatile var topics: List[String] = _


  def firstQuestion = questions.head


  val log = LoggerFactory.getLogger(classOf[EmojiQuizBot])

  val questionsRefresher = Executors.newSingleThreadScheduledExecutor()

  questionsRefresher.scheduleAtFixedRate(new Runnable {
    override def run(): Unit = initQuestions()
  }, 0, 600, TimeUnit.SECONDS)


  def initQuestions(): Unit = {
    log.info("Refreshing questions")

    val tmpQuestions = questionDao.getList

    questions = tmpQuestions
    topics = questions.map(_.topic).distinct

    log.debug("Loaded questions {}", questions)

    log.info("Questions successfully refreshed")
  }


  //TODO some precaching of user progress
  //val gameProgress = new ConcurrentHashMap[Int, UserProgress]().asScala


  def topicsString: String =
    s"""
       |Available game topics:
       |${topics.mkString(",\n")}
     """.stripMargin


  on("/start") { implicit msg => args =>
    val response = userProgressDao.findOne(MongoDBObject("_id" -> msg.source)) match {
      case Some(userProgress) if userProgress.level == questions.size =>
        EmojiQuizBot.MessageOnGameComplete

      case Some(userProgress) =>
        userProgress.toPrettyString

      case None =>
        val newProgress = UserProgress(msg.source, Some(firstQuestion))
        userProgressDao.save(newProgress)
        s"""
           |New game started!
           |Question:
           |${firstQuestion.questionEmojis}
             """.stripMargin
    }
    reply(response)
  }


  on("/refresh") { implicit msg => args => {
      reply {
        userProgressDao.removeById(msg.source)
        """
          |Your progress successfully removed
          |Use /start to start new game""".stripMargin
      }
    }
  }

//  on("help") { implicit msg => args => {
//      replyTo(sender) {
//        s"""
//           |/start - start game
//           |/topics - see list of available topics
//           |/stat - see stat
//           |/refresh - clean stats
//           |/hint - ask for hint for question
//           |/help - see help
//         """.stripMargin
//      }
//    }
//  }

  on("/topics") { implicit msg => args => {
      reply {
        topics.mkString(",")
      }
    }
  }

  on("/stat") { implicit msg => args => {
      reply {
        userProgressDao.findOne(MongoDBObject("_id" -> msg.source)) match {
          case Some(progress) =>
            progress.toPrettyString
          case None =>
            EmojiQuizBot.MessageOnGameNotStarted
        }
      }
    }
  }

  on("/hint") { implicit msg => args => {
    withUserProgress(args) { (sender, args, userProgress) =>
      val currentQuestion = userProgress.currentQuestion.get
      if (currentQuestion.hintEmojis.isEmpty) {
        reply {
          s"""
             |No hints for current level are available.
             |Question:
             |${currentQuestion.questionEmojis}""".stripMargin
        }
      } else if (userProgress.canUseHints) {
        val updatedHintsUsed = userProgress.hintsUsed + 1
        val updatedTotalHintsUsed = userProgress.totalHintsUsed + 1

        val updateRequest = $set("hintsUsed" -> updatedHintsUsed, "totalHintsUsed" -> updatedTotalHintsUsed)
        userProgressDao.update(MongoDBObject("_id" -> sender), updateRequest, false, false, WriteConcern.Normal)
        reply {
          s"""
             |Using $updatedHintsUsed of ${currentQuestion.hintsAvailable}
             |Total hints used: $updatedTotalHintsUsed
             |Question:
             |${currentQuestion.questionEmojis}
             |Hints:
             |${currentQuestion.hintsString(updatedHintsUsed)}""".stripMargin
        }
      } else {
        reply {
          s"""
             |You can not use more hintss on this level.
             |Question:
             |${currentQuestion.questionEmojis}
             |Hintss:
             |${currentQuestion.hintsString(userProgress.hintsUsed)}""".stripMargin
        }
      }
    }
  }

  on("/answer") { implicit msg => args =>
    log.debug(s"Processing answer input $msg")
    userProgressDao.findOne(MongoDBObject("_id" -> msg.source)) match {
      case Some(userProgress) if userProgress.level == questions.size =>
        reply {
          log.debug("Finished")
          EmojiQuizBot.MessageOnGameComplete
        }

      case Some(userProgress) =>
        log.debug("Not Finished")

        if (StringUtils.answerCorrect(args.mkString(" "), userProgress.currentQuestion.get.answers)) {
          log.debug("Correct")
          val newLevel = userProgress.level + 1

          if (newLevel == questions.size) {
            log.debug("Finished")
            //TODO: FAILS ON CORRECT ANSWER HERE

            val updateQuery = $and($unset("currentQuestion"), $set("level" -> newLevel ))

            userProgressDao.update(MongoDBObject("_id" -> msg.source), updateQuery, false, false, WriteConcern.Normal)
            reply {
              """
                |Correct!
                |Congratulations! You won the game!
                |If you want to refresh your progress please send /refresh """.stripMargin
            }
          } else {
            log.debug("next")
            val nextQuestion = questions(newLevel)

            val updateQuery = $set("currentQuestion" -> nextQuestion.toDBObject, "level" -> newLevel)

            userProgressDao.update(MongoDBObject("_id" -> msg.source), updateQuery, false, false, WriteConcern.Normal)
            reply {
              s"""
                 |Correct!
                 |Your current level is now $newLevel
                 |Next question:
                 |${nextQuestion.questionEmojis}""".stripMargin
            }
          }
        } else {
          log.debug("Incorrect")
          reply {
            "Incorrect answer"
          }
        }
      case None =>
        log.debug("Not started")
        reply {
          EmojiQuizBot.MessageOnGameNotStarted
        }
      }
    }
  }


//  override def onNonCommand(msg: String, sender: Int): Unit = {
//    Future {
//      log.debug(s"Processing non command input $msg")
//      userProgressDao.findOne(MongoDBObject("_id" -> sender)) match {
//        case Some(userProgress) if userProgress.level == questions.size =>
//          reply {
//            log.debug("Finished")
//            QuizBot.MessageOnGameComplete
//          }
//
//        case Some(userProgress) =>
//          log.debug("Not Finished")
//
//          if (StringUtils.answerCorrect(msg, userProgress.currentQuestion.get.answers)) {
//            log.debug("Correct")
//            val newLevel = userProgress.level + 1
//
//            if (newLevel == questions.size) {
//              log.debug("Finished")
//              //TODO: FAILS ON CORRECT ANSWER HERE
//
//              val updatedProgress = userProgress.copy(currentQuestion = None, level = newLevel)
//              userProgressDao.update(MongoDBObject("_id" -> sender), updatedProgress, false, false, WriteConcern.Normal)
//              reply {
//                """
//                  |Correct!
//                  |Congratulations! You won the game!
//                  |If you want to refresh your progress please send /refresh """.stripMargin
//              }
//            } else {
//              log.debug("next")
//              val nextQuestion = questions(newLevel)
//              val updatedProgress = userProgress.copy(currentQuestion = Some(nextQuestion), level = newLevel)
//              userProgressDao.update(MongoDBObject("_id" -> sender), updatedProgress, false, false, WriteConcern.Normal)
//              reply {
//                s"""
//                   |Correct!
//                   |Your current level is now $newLevel
//                   |Next question:
//                   |${nextQuestion.questionEmojis}""".stripMargin
//              }
//            }
//          } else {
//            log.debug("Incorrect")
//            reply {
//              "Incorrect answer"
//            }
//          }
//        case None =>
//          log.debug("Not started")
//          reply {
//            QuizBot.MessageOnGameNotStarted
//          }
//      }
//    }
//  }

  override def run(): Unit = {
    initQuestions()

    super.run()

    log.info("Start successful")
    log.debug(questions.toString)
  }

  override def shutdown(): Future[Unit] = {
    super.shutdown().map { _ =>
      // TODO persist inf


      log.info("Stop successful")
    }

  }

  private def withUserProgress(args: Seq[String])(fun: (Long, Seq[String], UserProgress) => Unit)(implicit msg: Message): Unit = {
    userProgressDao.findOne(MongoDBObject("_id" -> msg.source)) match {
      case Some(progress) if progress.level == questions.size =>
        reply {
          EmojiQuizBot.MessageOnGameComplete
        }

      case Some(progress) => fun(msg.source, args, progress)

      case None =>
        reply {
          EmojiQuizBot.MessageOnGameNotStarted
        }
    }
  }

}
