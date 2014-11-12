package controllers

import akka.actor.ActorSystem
import models.{GroupActor, UserActor}
import persistence.{GroupMongoPersistence, UserMongoPersistence}
import play.api.mvc._

object Application extends Controller {

  implicit var system: ActorSystem = null
  system = ActorSystem("Fracker")

  val user = system.actorOf(UserMongoPersistence.props(), name="UserActor")
  val group = system.actorOf(GroupMongoPersistence.props(), name="GroupActor")

  val mUser = system.actorOf(UserActor.props(user))
  val mGroup = system.actorOf(GroupActor.props(group))

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def getUsers = Action {
    Ok("{'name': 'ruben', password: '30vinte'}").as("application/json")
  }

  def createUser = Action { request =>
    request.body.asText match {
      case Some(json) => mUser ! json
      case None => println("No data to parse")
    }
    Ok("{ 'id': 37 }").as("application/json")
  }

  def createGroup = Action { request =>
    request.body.asText match {
      case Some(json) => mGroup ! json
      case None => println("No data to parse")
    }
    Ok("cenas").as("application/json")
  }

}