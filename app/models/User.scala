package models

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import argonaut.Argonaut._
import argonaut.{CodecJson, DecodeJson, EncodeJson, Json}
import persistence.UserPersistence._
import reactivemongo.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter, BSONObjectID}
import utils.ActorUtils._
import utils.Helpers._
import scala.util.{Failure, Success, Try}

/**
 * Created by ruben on 10-11-2014.
 *
 */

case class User (_id: Option[BSONObjectID], username: String, first_name: String, last_name: String, password: String, lat: String, lon: String, timestamp: Long, groups: List[String]) //groups: List[String/url]

case class Users(users: List[User]) {
  implicit def UsersCodecJson: CodecJson[Users] = casecodec1(Users.apply, Users.unapply)("users")

  def toJson = this.users.asJson
}

object User {

  //TODO: map { group => search in database to encode
  implicit def UserEncodeJson: EncodeJson[User] = EncodeJson( (u: User) => ("_id" := u._id.get.stringify) ->: ("username" := u.username) ->: ("first_name" := u.first_name) ->: ("last_name" := u.last_name) ->: ("lat" := u.lat) ->: ("lon" := u.lon) ->: ("timestamp" := u.timestamp) ->: ("groups" := u.groups) ->: jEmptyObject)

  implicit def UserDecodeJson: DecodeJson[User] = DecodeJson( u => for {
    _id <- (u --\ "_id").as[Option[String]]
    username <- (u --\ "username").as[String]
    first_name <- (u --\ "first_name").as[String]
    last_name <- (u --\ "last_name").as[String]
    password <- (u --\ "password").as[String]
    lat <- (u --\ "lat").as[String]
    lon <- (u --\ "lon").as[String]
    timestamp <- (u --\ "timestamp").as[Long]
    groups <- (u --\ "groups").as[List[String]]
  } yield User(verify_id(_id), username, first_name, last_name, password, lat, lon, timestamp, groups))

  def toJson(user: User) = user.asJson

  def parse(user: String): Either[String, User] = user.decodeEither[User].toEither

  implicit object UserReader extends BSONDocumentReader[User] {
    def read(doc: BSONDocument): User = {
      val _id = doc.getAs[BSONObjectID]("_id").get
      val username = doc.getAs[String]("username").get
      val first_name = doc.getAs[String]("first_name").get
      val last_name = doc.getAs[String]("last_name").get
      val password = doc.getAs[String]("password").get
      val lat = doc.getAs[String]("lat").get
      val lon = doc.getAs[String]("lon").get
      val timestamp = doc.getAs[Long]("timestamp").get
      val groups = doc.getAs[List[String]]("groups").get
      User(Some(_id), username, first_name, last_name, password, lat, lon, timestamp, groups)
    }
  }

  implicit object UserWriter extends BSONDocumentWriter[User]{
    def read(doc: BSONDocument): User = {
      val _id = doc.getAs[BSONObjectID]("_id").get
      val username = doc.getAs[String]("username").get
      val first_name = doc.getAs[String]("first_name").get
      val last_name = doc.getAs[String]("last_name").get
      val password = doc.getAs[String]("password").get
      val lat = doc.getAs[String]("lat").get
      val lon = doc.getAs[String]("lon").get
      val timestamp = doc.getAs[Long]("timestamp").get
      val groups = doc.getAs[List[String]]("groups").get
      User(Some(_id), username, first_name, last_name, password, lat, lon, timestamp, groups)
    }

    override def write(u: User) = {
      BSONDocument(
        "_id" -> u._id,
        "username" -> u.username,
        "first_name" -> u.first_name,
        "last_name" -> u.last_name,
        "password" -> u.password,
        "lat" -> u.lat,
        "lon" -> u.lon,
        "timestamp" -> u.timestamp,
        "groups" -> u.groups
      )
    }
  }
}

object UserActor {
  def props(user: ActorRef) = Props(classOf[UserActor], user)
}

class UserActor(user: ActorRef) extends Actor with ActorLogging{

  def receive = {
    case post: POST => sender ! createUser(post.json)
    case g: GET => sender ! getUser(g.id)
    case g: GETS => sender ! getUsers(g.username)
    case d: DELETE => sender ! deleteUser(d.id)
  }

  def createUser(json: String): Try[Json] = {
    User.parse(json) match {
      case Right(new_user) =>
        await[Boolean](user, FindUser(new_user.username)) match {
          case false =>
            await[Option[BSONObjectID]](user, CreateUser(new_user)) match {
              case Some(id) => Success(Json("_id" -> jString(id.stringify)))
              case None => Failure(new Throwable(Json("error" -> jString("Error creating user in the database")).toString()))
            }
          case true => Failure(new Throwable(Json("error" -> jString("user with that username already exists")).toString()))
        }
      case Left(error) => Failure(new Throwable(Json("error" -> jString("Error decoding json user")).toString()))
    }
  }

  def getUser(id: String): Json = {
    await[Option[User]](user, ReadUser(id)) match {
      case Some(this_user) => User.toJson(this_user)
      case None => Json("error" -> jString("User not found"))
    }
  }

  def getUsers(username: Option[String]): Json = await[Users](user, ReadUsers(username)).toJson

  def deleteUser(id: String): Boolean = await[Boolean](user, DeleteUser(id))


}
