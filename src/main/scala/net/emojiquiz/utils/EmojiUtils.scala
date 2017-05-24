package net.emojiquiz.utils

import com.lightbend.emoji.{Emoji, ShortCodes}

import scala.util.Random

object EmojiUtils {

  val Emojis = ShortCodes.Defaults.defaultImplicit.emojis
  val EmojisList = Emojis.toList

  def randomEmoji: Emoji = EmojisList(Random.nextInt(EmojisList.size))

}
