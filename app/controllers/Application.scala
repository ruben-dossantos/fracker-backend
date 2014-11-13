package controllers

import akka.actor.ActorSystem
import argonaut.Argonaut._
import argonaut.Json
import models.{GroupActor, UserActor}
import persistence.{GroupMongoPersistence, UserMongoPersistence}
import play.api.mvc._
import utils.ActorUtils._
import utils.Helpers.{GETS, POST}

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

  def getUsers = Action {
    val answer = await[Json](mUser, GETS(None, None))
    Ok(answer.toString()).as("application/json")
  }

  def createUser = Action { request =>
    val answer = request.body.asText match {
      case Some(json) => await[Json](mUser, POST(json))
      case None => Json("error" -> jString("No data to parse"))
    }
    Ok(answer.toString()).as("application/json")
  }

  def createGroup = Action { request =>
    val answer = request.body.asText match {
      case Some(json) => await[Json](mGroup, POST(json))
      case None => Json("error" -> jString("No data to parse"))
    }
    Ok(answer.toString()).as("application/json")
  }

}