package models

import akka.actor.{Props, ActorRef, Actor, ActorLogging}
import argonaut.Argonaut._
import argonaut.Json

/**
 * Created by ruben on 10-11-2014.
 *
 */

case class User (id: Int, username: String, first_name: String, last_name: String, password: String, lat: String, lon: String, timestamp: Long){
  def toJson = this.asJson.toString()
} //groups: List[String/url]

object UserActor {
  def props(user: ActorRef) = Props(classOf[UserActor], user)
}

class UserActor(user: ActorRef) extends Actor with ActorLogging{

  def receive = {
    case json: String => createUser(json)
  }

  def createUser(json: String): Json = {
    Json()
  }

}
