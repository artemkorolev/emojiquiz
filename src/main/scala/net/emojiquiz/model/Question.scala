package net.emojiquiz.model

import com.mongodb.{BasicDBList, DBObject}
import com.mongodb.casbah.MongoCollection
import com.mongodb.casbah.commons.MongoDBObject
import net.emojiquiz.utils.MongoUtils

import scala.collection.JavaConverters._


// TODO difficulty field

case class Question(_id: String,
                    topic: String,
                    questionEmojis: String,
                    answers: List[String],
                    hintEmojis: List[String] = List.empty[String]) {


  lazy val hintsAvailable = hintEmojis.size


  def hintsString(hintsToPrint: Int): Option[String] = {
    if (hintEmojis.isEmpty) {
      None
    } else {
      Some(hintEmojis.take(hintsToPrint).mkString(""))
    }
  }

  def toDBObject: DBObject = {
    MongoDBObject("_id" -> _id, "topic" -> topic, "questionEmojis" -> questionEmojis, "answers" -> answers, "hintEmojis" -> hintEmojis)
  }
}

object Question {

  def apply(dbObject: DBObject): Question = {
    val id = dbObject.get("_id").toString
    val topic = dbObject.get("topic").toString
    val questionEmojis = dbObject.get("questionEmojis").toString
    val answers = dbObject.get("answers").asInstanceOf[BasicDBList].iterator().asScala.toList.map(_.toString)
    val hintEmojis = Option(dbObject.get("hintEmojis").asInstanceOf[BasicDBList].iterator().asScala.toList.map(_.toString)).getOrElse(List.empty)

    Question(id, topic, questionEmojis, answers, hintEmojis)
  }
}



object QuestionDAO {

  val Collection = "questions"

  def apply(mongoHosts: String,
            dbName: String = CommonDAO.Db,
            collectionName: String = Collection,
            user: Option[String] = None,
            password: Option[String] = None) = {
    val mongoCollection = MongoUtils.getCollection(mongoHosts, dbName, collectionName, user, password)
    new QuestionDAO(mongoCollection)
  }

}

class QuestionDAO(mongoCollection: MongoCollection) {

  def getList: List[Question] = {
    mongoCollection.find().map(Question.apply).toList
  }

}

