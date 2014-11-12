package models

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import argonaut.Argonaut._
import argonaut.{CodecJson, Json}
import reactivemongo.bson.{BSONDocumentWriter, BSONDocument, BSONDocumentReader}

/**
 * Created by ruben on 10-11-2014.
 *
 */

case class User (id: Int, username: String, first_name: String, last_name: String, password: String, lat: String, lon: String, timestamp: Long) //groups: List[String/url]

object User {

  implicit def UserCodecJson: CodecJson[User] = casecodec8(User.apply, User.unapply)("id", "username", "first_name", "last_name", "password", "lat", "lon", "timestamp")

  def toJson(user: User) = user.asJson

  def parse(user: String): Either[String, User] = user.decodeEither[User].toEither

  implicit object UserReader extends BSONDocumentReader[User] {
    def read(doc: BSONDocument): User = {
      val id = doc.getAs[Int]("id").get
      val username = doc.getAs[String]("username").get
      val first_name = doc.getAs[String]("first_name").get
      val last_name = doc.getAs[String]("last_name").get
      val password = doc.getAs[String]("password").get
      val lat = doc.getAs[String]("lat").get
      val lon = doc.getAs[String]("lon").get
      val timestamp = doc.getAs[Long]("timestamp").get
      User(id, username, first_name, last_name, password, lat, lon, timestamp)
    }
  }

  implicit object UserWriter extends BSONDocumentWriter[User]{
    def read(doc: BSONDocument): User = {
      val id = doc.getAs[Int]("id").get
      val username = doc.getAs[String]("username").get
      val first_name = doc.getAs[String]("first_name").get
      val last_name = doc.getAs[String]("last_name").get
      val password = doc.getAs[String]("password").get
      val lat = doc.getAs[String]("lat").get
      val lon = doc.getAs[String]("lon").get
      val timestamp = doc.getAs[Long]("timestamp").get
      User(id, username, first_name, last_name, password, lat, lon, timestamp)
    }

    override def write(u: User) = {
      BSONDocument(
        "id" -> u.id,
        "username" -> u.username,
        "first_name" -> u.first_name,
        "last_name" -> u.last_name,
        "password" -> u.password,
        "lat" -> u.lat,
        "lon" -> u.lon,
        "timestamp" -> u.timestamp
      )
    }
  }
}

object UserActor {
  def props(user: ActorRef) = Props(classOf[UserActor], user)
}

class UserActor(user: ActorRef) extends Actor with ActorLogging{

  def receive = {
    case json: String => createUser(json)
  }

  def createUser(json: String): Json = {
    User.parse(json) match {
      case Right(new_user) => println(new_user)
      case Left(error) => println("Error decoding")
    }
    Json()
  }

}
