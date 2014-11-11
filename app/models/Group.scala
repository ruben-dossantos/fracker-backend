package models

import akka.actor.{Actor, ActorLogging, Props, ActorRef}
import argonaut.Json

/**
 * Created by ruben on 10-11-2014.
 *
 */
case class Group(name: String, password: String, users: List[String])

object GroupActor {
  def props(group: ActorRef) = Props(classOf[GroupActor], group)
}

class GroupActor(group: ActorRef) extends Actor with ActorLogging {

  def receive = {
    case json: String => createGroup(json)
  }

  def createGroup(json: String): Json = {
    Json()
  }

}
