package persistence

import akka.actor.{ActorLogging, Props}
import models.{Groups, Group}
import reactivemongo.api.MongoDriver
import reactivemongo.api.collections.default.BSONCollection
import reactivemongo.api.indexes.Index
import reactivemongo.api.indexes.IndexType.Ascending
import reactivemongo.bson.{BSONDocument, BSONObjectID}

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}

/**
 * Created by ruben on 11-11-2014.
 *
 */
object GroupMongoPersistence {
  def props(db_name: String = "fracker", collection_name: String = "groups"): Props = {
    Props(classOf[GroupMongoPersistence], db_name, collection_name)
  }
}

class GroupMongoPersistence (db_name: String, collection_name: String) extends GroupPersistence with ActorLogging{

  var connection = MongoDriver().connection(Seq("localhost"))
  var db = connection.db(db_name)
  var groups = db.collection[BSONCollection](collection_name)


  def withMongoConnection[T](body: => T): Try[T] = {
    Try{
      if(groups == null){
        connection = MongoDriver().connection(Seq("localhost"))
        db = connection.db(db_name)
        groups.indexesManager.ensure(Index(List(("_id", Ascending)), unique = true))
        groups.indexesManager.ensure(Index(List(("name", Ascending)), unique = true))
      }

      body
    }
  }

  override def createGroup(group: Group): Option[BSONObjectID] = {
    val new_group = Group(Some(BSONObjectID.generate), group.name, group.password, group.users)
    withMongoConnection {
      Await.result(
        groups.insert(new_group).map {
          lastError =>
            lastError.ok match {
              case true => new_group._id
              case false => throw new Exception(lastError)
            }
        }, 5.seconds
      )
    } match {
      case Success(id) => id //TODO whats new_user
      case Failure(_) => None
    }
  }

  override def deleteGroup(id: String): Boolean = {
    withMongoConnection {
      groups.remove(BSONDocument("_id" -> BSONObjectID(id)))
    } match {
      case Success(_) => true
      case Failure(_) => false
    }
  }

  override def updateGroup(id: String, group: Group): Boolean = ???

  override def readGroup(id: String): Option[Group] = {
    withMongoConnection {
      val query = BSONDocument("_id" -> BSONObjectID(id))
      Await.result(groups.find(query).one[Group], 5.seconds)
    } match {
      case Success(group) => group
      case Failure(_) => None
    }
  }

  override def readGroups(username: Option[String]): Groups = {
    withMongoConnection {
      val query = BSONDocument()
      Await.result(groups.find(query).cursor[Group].collect[List](), 5.seconds)
    } match {
      case Success(group) => Groups(group)
      case Failure(_) => Groups(List())
    }
  }

  override def findGroup(name: String): Boolean = {
    withMongoConnection {
      val query = BSONDocument("name" -> name)
      Await.result(groups.find(query).one[Group], 5.seconds)
    } match {
      case Success(group) =>
        group match {
          case Some(_) => true
          case None => false
        }
      case Failure(_) => false
    }
  }
}
