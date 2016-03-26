package net.avedernikov.emojiquiz.utils

import scala.collection.JavaConverters._
/**
  * @author Artem Vedernikov
  */
class Survey(val question: String, val answers: List[String]) {

  private val stat = new java.util.concurrent.ConcurrentHashMap[String,  Int]()

  init()

  def vote(answer: String): Unit = {
    if (stat.containsKey(answer)) {
      stat.put(answer, stat.get(answer) + 1)
    }
  }

  def getStats = stat.asScala.toMap

  private def init() = {
    answers.foreach { answer =>
      stat.put(answer, 0)
    }
  }


  def prettyResults: String = {
    val buffer = new StringBuffer("Опрос \n").append(question).append("\n\n")
    stat.asScala.foreach { case (q, n) =>
      buffer.append(q).append(" -> ").append(n).append("\n")
    }
    buffer.toString
  }
}
