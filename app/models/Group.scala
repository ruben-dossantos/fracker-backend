package models

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import argonaut.Argonaut._
import argonaut.{CodecJson, DecodeJson, EncodeJson, Json}
import persistence.GroupPersistence._
import reactivemongo.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter, BSONObjectID}
import utils.Helpers._
import utils.ActorUtils._

import scala.util.{Failure, Success, Try}

/**
 * Created by ruben on 10-11-2014.
 *
 */
case class Group(_id: Option[BSONObjectID], name: String, password: String, users: List[User])      // TODO: this needs to have more info without recursively take one another

case class Groups(groups: List[Group]) {
  implicit def GroupsCodecJson: CodecJson[Groups] = casecodec1(Groups.apply, Groups.unapply)("groups")

  def toJson = this.groups.asJson
}

case class GroupMinified(_id: Option[BSONObjectID], name: String){

  //implicit def GroupMinifiedCodecJson: CodecJson[GroupMinified] = casecodec2(GroupMinified.apply, GroupMinified.unapply)("_id", "name")

  def toJson = this.asJson
}

object GroupMinified {
  implicit def GroupMinifiedEncodeJson: EncodeJson[GroupMinified] = EncodeJson( (g: GroupMinified) => ("_id" := g._id.get.toString()) ->: ("name" := g.name) ->: jEmptyObject)

  implicit def GroupMinifiedDecodeJson: DecodeJson[GroupMinified] = DecodeJson( g => for {
    _id <- (g --\ "_id").as[Option[String]]
    name <- (g --\ "name").as[String]
  } yield GroupMinified(verify_id(_id), name))
}

case class GroupsMinified(groups: List[GroupMinified]) {
  implicit def GroupsMinifiedCodecJson: CodecJson[GroupsMinified] = casecodec1(GroupsMinified.apply, GroupsMinified.unapply)("groups")

  def toJson = this.groups.asJson
}

object Group {

  def minify(groups: List[Group]): Json = {
    GroupsMinified(groups map { group =>
      GroupMinified(group._id, group.name)
    }).toJson
  }

  implicit def GroupEncodeJson: EncodeJson[Group] = EncodeJson( (g: Group) => ("_id" := g._id.get.toString()) ->: ("name" := g.name) ->: ("password" := g.password)
    ->: ("users" := User.minify(g.users)) ->: jEmptyObject)

  implicit def GroupDecodeJson: DecodeJson[Group] = DecodeJson( g => for {
    _id <- (g --\ "_id").as[Option[String]]
    name <- (g --\ "name").as[String]
    password <- (g --\ "password").as[String]
    users <- (g --\ "users").as[List[User]]
  } yield Group(verify_id(_id), name, password, users))

  def toJson(group: Group) = group.asJson

  def parse(group: String): Either[String, Group] = group.decodeEither[Group].toEither

  implicit object GroupReader extends BSONDocumentReader[Group] {
    def read(doc: BSONDocument): Group = {
      val _id = doc.getAs[BSONObjectID]("_id").get
      val name = doc.getAs[String]("name").get
      val password = doc.getAs[String]("password").get
      val users = doc.getAs[List[User]]("users").get
      Group(Some(_id), name, password, users)
    }
  }

  implicit object GroupWriter extends BSONDocumentWriter[Group]{
    def read(doc: BSONDocument): Group = {
      val _id = doc.getAs[BSONObjectID]("_id").get
      val name = doc.getAs[String]("name").get
      val password = doc.getAs[String]("password").get
      val users = doc.getAs[List[User]]("users").get
      Group(Some(_id), name, password, users)
    }

    override def write(g: Group) = {
      BSONDocument(
        "_id" -> g._id,
        "name" -> g.name,
        "password" -> g.password,
        "users" -> g.users
      )
    }
  }
}

object GroupActor {
  def props(group: ActorRef) = Props(classOf[GroupActor], group)
}

class GroupActor(group: ActorRef) extends Actor with ActorLogging {

  def receive = {
    case post: POST => sender ! createGroup(post.json)
    case g: GET => sender ! getGroup(g.id)
    case g: GETS => sender ! getGroups(g.username)
    case p: PUT => sender ! updateGroup(p.id, p.json)
    case d: DELETE => sender ! deleteGroup(d.id)
  }

  def createGroup(json: String): Json = {
    Group.parse(json) match {
      case Right(new_group) =>
        await[Boolean](group, FindGroup(new_group.name)) match {
          case false =>
            await[Option[BSONObjectID]](group, CreateGroup(new_group)) match {
              case Some(id) => Json("_id" -> jString(id.stringify))
              case None => Json("error" -> jString("Error creating group in the database"))
            }
          case true => Json("error" -> jString("group with that name already exists"))
        }

      case Left(error) => Json("error" -> jString("Error decoding json group"))
    }
  }

  def getGroup(id: String): Json = {
    await[Option[Group]](group, ReadGroup(id)) match {
      case Some(this_group) => Group.toJson(this_group)
      case None => Json("error" -> jString("Group not found"))
    }
  }

  def getGroups(username: Option[String]): Json = await[Groups](group, ReadGroups(username)).toJson

  def deleteGroup(id: String): Boolean = await[Boolean](group, DeleteGroup(id))

  def updateGroup(id: String, json: String): Try[Json] = {
    Group.parse(json) match {
      case Right(update_group) =>
        await[Boolean](group, UpdateGroup(id, update_group)) match {
          case true => Success(Group.toJson(update_group))
          case false => Failure(jsonThrowable("[PUT] - Error updating group in the database"))
        }
      case Left(error) => Failure(jsonThrowable("[PUT] - Error decoding json group"))
    }
  }
}
