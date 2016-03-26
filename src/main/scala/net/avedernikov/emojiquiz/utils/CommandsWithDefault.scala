package net.avedernikov.emojiquiz.utils

import info.mukel.telegram.bots.{AuthorizationManager, TelegramBot}
import info.mukel.telegram.bots.api.{Message, Update}

import scala.collection.mutable
import scala.concurrent.Future

/**
  * @author Artem Vedernikov
  */
trait CommandsWithDefault {
  this : TelegramBot =>

  // All commands must be preceeded by 'cmdPrefix' eg. /hello
  val cmdPrefix = "/"

  // Allows targeting specific bots eg. /hello@FlunkeyBot
  val cmdAt = "@"

  private val commands = mutable.HashMap[String, (Int, Seq[String]) => Unit]()

  /**
    * handleUpdate
    *
    * @param update  the update to be processed.
    *
    * Parses messages and spawns bot commands accordingly, supports targeting specific bots.
    * Commands and bot names are case INSENSITIVE, additional parameters are NOT.
    *
    * General syntax:
    *     /command[@BotUsername]* args*
    *
    * Assuming cmdPrefix = '/' and cmdAt = '@' here are some usage examples:
    *
    * To broadcast 'command' to ALL bots:
    *     /command
    *
    * To send 'command' (parameterless) to FooBot ONLY:
    *     /command@FooBot
    *
    * To send 'command' with args = ("hello", "world") to FooBot and BarBot:
    *     /command@FooBot@BarBot hello world
    */
  override def handleUpdate(update: Update): Unit = {
    for {
      msg <- update.message if AuthorizationManager.isAuthorized(msg.chat.id)
      text <- msg.text
    } /* do */ {

      println("Message: " + text)

      // TODO: Allow parameters with spaces e.g. /echo "Hello World"
      val tokens = text.trim split " "
      tokens match {
        case Array(rawCmd, args @ _*) if rawCmd startsWith cmdPrefix =>

          val parts = rawCmd stripPrefix cmdPrefix split cmdAt
          val cmd = parts.head.toLowerCase
          val addressees = parts.tail map (_.toLowerCase)

          if (addressees.isEmpty || addressees.contains(botName.toLowerCase)) {
            for (action <- commands.get(cmd))
              action(msg.chat.id, args)
          }
        case _ => onNonCommand(text.trim, msg.chat.id)
      }
    }
  }

  /**
    * replyTo
    *
    * Handy wrapper to send text replies
    */
  def replyTo(chatId: Int,
              parseMode : Option[String] = None,
              disableWebPagePreview : Option[Boolean] = None,
              replyToMessageId: Option[Int] = None)
             (text: String): Future[Message] = {
    sendMessage(chatId, text, parseMode, disableWebPagePreview, replyToMessageId)
  }

  /**
    * on
    *
    * Makes the bot able react to 'command' with the specified handler.
    * 'action' will receive the sender (chatId) and the arguments as parameters.
    */
  def on(command: String)(action: (Int, Seq[String]) => Unit): Unit = {
    commands += (command -> action)
  }

  def onNonCommand(text: String, chatId: Int): Unit = {}
}
