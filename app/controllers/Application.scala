package controllers

import akka.actor.ActorSystem
import argonaut.Argonaut._
import argonaut.Json
import models.{GroupActor, UserActor}
import persistence.{GroupMongoPersistence, UserMongoPersistence}
import play.api.mvc._
import utils.ActorUtils._
import utils.Helpers._

import scala.util.{Failure, Success, Try}

object Application extends Controller {

  implicit var system: ActorSystem = null
  system = ActorSystem("Fracker")

  val user = system.actorOf(UserMongoPersistence.props(), name="UserActor")
  val group = system.actorOf(GroupMongoPersistence.props(), name="GroupActor")

  val mUser = system.actorOf(UserActor.props(user), name = "UserModelActor")
  val mGroup = system.actorOf(GroupActor.props(group), name = "GroupModelActor")

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def createUser = Action { request =>
    request.body.asText match {
      case Some(json) =>
        await[Try[Json]](mUser, POST(json)) match {
          case Success(idJson) => Status(201)(idJson.toString())
          case Failure(e) => Status(500)(e.getMessage)
        }

      case None =>
        val answer = Json("error" -> jString("No data to parse"))
        Status(406)(answer.toString())
    }
    //Ok(answer.toString()).as("application/json")
  }

  def getUser(id: String) = Action {
    val answer = await[Json](mUser, GET(id))
    Ok(answer.toString()).as("application/json")
  }

  def getUsers = Action {
    val answer = await[Json](mUser, GETS(None, None))
    Ok(answer.toString()).as("application/json")
  }

  def updateUser(id: String) = Action { request =>
    request.body.asText match {
      case Some(json) => await[Try[Json]](mUser, PUT (id, json)) match {
        case Success(updated_user) => Status(200)(updated_user.toString())
        case Failure(e) => Status(500)(e.getMessage)
      }
      case None =>
        val answer = Json("error" -> jString("No data to parse"))
        Status(406)(answer.toString())
    }
  }

  def deleteUser(id: String) = Action { request =>
    await[Boolean](mUser, DELETE(id)) match {
      case true => Status(200)("User deleted successfully")
      case false => Status(503)("Failed to delete user")    // 500?
    }
  }









  def createGroup = Action { request =>
    val answer = request.body.asText match {
      case Some(json) => await[Json](mGroup, POST(json))
      case None => Json("error" -> jString("No data to parse"))
    }
    Ok(answer.toString()).as("application/json")
  }

  def getGroup(id_user: String, id_group: String) = Action {
    val answer = await[Json](mGroup, GET(id_group))
    Ok(answer.toString()).as("application/json")
  }

  def getGroups = Action {  //TODO: POST must come with user !!_id!! or token
  val answer = await[Json](mGroup, GETS(None, None))
    Ok(answer.toString()).as("application/json")
  }

  def getUserGroups(id: String) = Action { request =>
    Ok(Json("id" -> jString(id)).toString()).as("application/json")
  }

  def updateGroup(id: String) = Action { request =>
    request.body.asText match {
      case Some(json) => await[Try[Json]](mGroup, PUT (id, json)) match {
        case Success(updated_group) => Status(200)(updated_group.toString())
        case Failure(e) => Status(500)(e.getMessage)
      }
      case None =>
        val answer = Json("error" -> jString("No data to parse"))
        Status(406)(answer.toString())
    }
  }

  def deleteGroup(id: String) = Action { request =>
    await[Boolean](mGroup, DELETE(id)) match {
      case true => Status(200)("Group deleted successfully")
      case false => Status(503)("Failed to delete group")    // 500?
    }
  }

  def login = TODO

  def joinGroup(id: String) = TODO

  def abandonGroup(id_user: String, id_group: String) = TODO

}