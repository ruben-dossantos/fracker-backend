package utils

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

object ActorUtils {

  val time = 5

  def awaitf[T](action: Future[T], wait: Int = time): T = {
    implicit val timeout = Timeout(wait.seconds)
    Await.result(action, timeout.duration)
  }

  def await[T](actor: ActorRef, msg: Any, wait: Int = time): T = {
    implicit val timeout = Timeout(wait.seconds)
    awaitf(actor ? msg, wait).asInstanceOf[T]
  }

}
