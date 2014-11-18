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

case class User (_id: Option[BSONObjectID], username: String, first_name: String, last_name: String, password: Option[String], lat: Option[String], lon: Option[String], timestamp: Option[Long], groups: List[Group]) //groups: List[String/url]

case class Users(users: List[User]) {
  implicit def UsersCodecJson: CodecJson[Users] = casecodec1(Users.apply, Users.unapply)("users")

  def toJson = this.users.asJson
}

case class UserMinified(_id: Option[BSONObjectID], username: String, first_name: String, last_name: String){
  //implicit def UserMinifiedCodecJson: CodecJson[UserMinified] = casecodec4(UserMinified.apply, UserMinified.unapply)("_id", "username", "first_name", "last_name")

  def toJson = this.asJson
}

object UserMinified {
  implicit def UserMinifiedEncodeJson: EncodeJson[UserMinified] = EncodeJson( (u: UserMinified) => ("_id" := u._id.get.toString()) ->: ("username" := u.username) ->: ("first_name" := u.first_name) ->: ("last_name" := u.last_name) ->: jEmptyObject)

  implicit def UserMinifiedDecodeJson: DecodeJson[UserMinified] = DecodeJson( u => for {
    _id <- (u --\ "_id").as[Option[String]]
    username <- (u --\ "username").as[String]
    first_name <- (u --\ "first_name").as[String]
    last_name <- (u --\ "last_name").as[String]
  } yield UserMinified(verify_id(_id), username, first_name, last_name))
}

case class UsersMinified(users: List[UserMinified]){
  implicit def UsersMinifiedCodecJson: CodecJson[UsersMinified] = casecodec1(UsersMinified.apply, UsersMinified.unapply)("users")

  def toJson = this.users.asJson
}

object User {

  def minify(users: List[User]) : Json = {
    UsersMinified(users map { user =>
      UserMinified(user._id, user.username, user.first_name, user.last_name)
    }).toJson
  }

  //TODO: map { group => search in database to encode
  implicit def UserEncodeJson: EncodeJson[User] = EncodeJson( (u: User) => ("_id" := u._id.get.stringify) ->: ("username" := u.username) ->: ("first_name" := u.first_name) ->: ("last_name" := u.last_name) ->: ("lat" := u.lat) ->: ("lon" := u.lon) ->: ("timestamp" := u.timestamp)
    ->: ("groups" := Group.minify(u.groups)) ->: jEmptyObject)

  implicit def UserDecodeJson: DecodeJson[User] = DecodeJson( u => for {
    _id <- (u --\ "_id").as[Option[String]]
    username <- (u --\ "username").as[String]
    first_name <- (u --\ "first_name").as[String]
    last_name <- (u --\ "last_name").as[String]
    password <- (u --\ "password").as[Option[String]]
    lat <- (u --\ "lat").as[Option[String]]
    lon <- (u --\ "lon").as[Option[String]]
    timestamp <- (u --\ "timestamp").as[Option[Long]]
    groups <- (u --\ "groups").as[List[Group]]
  } yield User(verify_id(_id), username, first_name, last_name, password, lat, lon, timestamp, groups))

  def toJson(user: User) = user.asJson

  def parse(user: String): Either[String, User] = user.decodeEither[User].toEither

  implicit object UserReader extends BSONDocumentReader[User] {
    def read(doc: BSONDocument): User = {
      val _id = doc.getAs[BSONObjectID]("_id").get
      val username = doc.getAs[String]("username").get
      val first_name = doc.getAs[String]("first_name").get
      val last_name = doc.getAs[String]("last_name").get
      val password = doc.getAs[String]("password")
      val lat = doc.getAs[String]("lat")
      val lon = doc.getAs[String]("lon")
      val timestamp = doc.getAs[Long]("timestamp")
      val groups = doc.getAs[List[Group]]("groups").get
      User(Some(_id), username, first_name, last_name, password, lat, lon, timestamp, groups)
    }
  }

  implicit object UserWriter extends BSONDocumentWriter[User]{
    def read(doc: BSONDocument): User = {
      val _id = doc.getAs[BSONObjectID]("_id").get
      val username = doc.getAs[String]("username").get
      val first_name = doc.getAs[String]("first_name").get
      val last_name = doc.getAs[String]("last_name").get
      val password = doc.getAs[String]("password")
      val lat = doc.getAs[String]("lat")
      val lon = doc.getAs[String]("lon")
      val timestamp = doc.getAs[Long]("timestamp")
      val groups = doc.getAs[List[Group]]("groups").get
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
    case p: PUT => sender ! updateUser(p.id, p.json)
    case d: DELETE => sender ! deleteUser(d.id)

  }

  def createUser(json: String): Try[Json] = {
    User.parse(json) match {
      case Right(new_user) =>
        await[Boolean](user, FindUser(new_user.username)) match {
          case false =>
            await[Option[BSONObjectID]](user, CreateUser(new_user)) match {
              case Some(id) => Success(Json("_id" -> jString(id.stringify)))
              case None => Failure(jsonThrowable("Error creating user in the database"))
            }
          case true => Failure(jsonThrowable("user with that username already exists"))
        }
      case Left(error) => Failure(jsonThrowable("Error decoding json user"))
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

  def updateUser(id: String, json: String): Try[Json] = {
    User.parse(json) match {
      case Right(update_user) =>
        await[Boolean](user, UpdateUser(id, update_user)) match {
          case true => Success(User.toJson(update_user))
          case false => Failure(jsonThrowable("[PUT] - Error updating user in the database"))
        }
      case Left(error) => Failure(jsonThrowable("[PUT] - Error decoding json user"))
    }
  }


}
