package controllers

import akka.actor.ActorSystem
import models.UserActor
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

}